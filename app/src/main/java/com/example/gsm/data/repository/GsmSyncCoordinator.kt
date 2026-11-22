package com.example.gsm.data.repository

import android.util.Log
import com.example.gsm.data.local.dao.UsuarioDao
import com.example.gsm.data.model.RolUsuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * Coordinador de sincronización remota (Firestore <-> Room y subida diferida a Appwrite).
 * Mantiene GsmRepository acotado a lógica de dominio sin cambiar su API pública.
 */
internal class GsmSyncCoordinator(
    private val localRepo: SolicitudLocalRepository,
    private val historialRepo: HistorialRepository,
    private val notificacionRepo: NotificacionRepository,
    private val firebaseRepo: FirebaseSolicitudRepository? = null,
    private val appwriteStorageRepo: AppwriteStorageRepository? = null,
    private val usuarioDao: UsuarioDao? = null
) {
    private val _ultimoErrorSync = MutableStateFlow<String?>(null)
    val ultimoErrorSync: StateFlow<String?> = _ultimoErrorSync.asStateFlow()

    @Volatile
    private var sincronizandoEvidencias = false

    private val timestampsPropios = ConcurrentHashMap<String, Long>()

    fun registrarCambioLocal(codigo: String, ts: Long = System.currentTimeMillis()) {
        if (codigo.isNotBlank()) {
            timestampsPropios[codigo.trim().uppercase()] = ts
        }
    }

    fun limpiarErrorSync() {
        _ultimoErrorSync.value = null
    }

    fun reportarErrorSync(mensaje: String) {
        _ultimoErrorSync.value = mensaje
    }

    fun iniciarSincronizacionBidireccional(scope: CoroutineScope) {
        var cargaInicial = true
        val ultimoTsPorCodigo = mutableMapOf<String, Long>()

        firebaseRepo?.escucharSolicitudes()
            ?.catch { e ->
                Log.e("FirebaseSync", "Error en flujo de solicitudes: ${e.message}", e)
                _ultimoErrorSync.value = "Error al sincronizar tickets con la nube."
            }
            ?.onEach { solicitudesRemotas ->
                _ultimoErrorSync.value = null
                solicitudesRemotas.forEach { solicitud ->
                    val previoLocal = localRepo.obtenerPorCodigo(solicitud.codigoSeguimiento)
                    val prevTs = ultimoTsPorCodigo[solicitud.codigoSeguimiento]
                        ?: previoLocal?.fechaActualizacion
                        ?: 0L
                    localRepo.guardarOActualizar(solicitud)
                    ultimoTsPorCodigo[solicitud.codigoSeguimiento] = solicitud.fechaActualizacion

                    val codigoKey = solicitud.codigoSeguimiento.trim().uppercase()
                    val tsPropio = timestampsPropios[codigoKey] ?: 0L
                    val esCambioPropio = tsPropio > 0L && abs(solicitud.fechaActualizacion - tsPropio) < 5_000L

                    // Notifica cambios remotos reales cuando el usuario no está dentro del ticket
                    if (!cargaInicial && !esCambioPropio && prevTs > 0L && solicitud.fechaActualizacion > prevTs) {
                        val destinoId = previoLocal?.clienteId ?: solicitud.clienteId
                        val localId = previoLocal?.id ?: solicitud.id
                        if (localId > 0) {
                            notificacionRepo.crearNotificacion(
                                usuarioDestinoId = destinoId,
                                solicitudId = localId,
                                titulo = "Actualización en #${solicitud.codigoSeguimiento}",
                                mensaje = solicitud.titulo,
                                tipoEvento = "ACTUALIZACION_TICKET",
                                ahora = solicitud.fechaActualizacion
                            )
                        }
                    }
                }
                cargaInicial = false
                // Al recibir snapshots de Firestore hay red: reintenta subir evidencias guardadas offline
                scope.launch(Dispatchers.IO) { sincronizarEvidenciasPendientes() }
            }
            // Ejecuta escrituras Room y descifrado fuera del hilo principal para evitar ANR al iniciar
            ?.flowOn(Dispatchers.IO)
            ?.launchIn(scope)
    }

    fun iniciarSincronizacionHistorial(codigoSeguimiento: String, solicitudIdLocal: Int, scope: CoroutineScope): Job? {
        val codigoLimpio = codigoSeguimiento.trim()
        if (codigoLimpio.isBlank()) return null

        var cargaInicial = true
        return firebaseRepo?.escucharHistorialChat(codigoLimpio)
            ?.catch { e ->
                Log.e("FirebaseSync", "Error en flujo historial chat: ${e.message}", e)
                _ultimoErrorSync.value = "No se pudo sincronizar el chat en vivo."
            }
            ?.onEach { mensajesRemotos ->
                val ticketLocal = localRepo.obtenerPorCodigo(codigoLimpio)
                val targetSolId = ticketLocal?.id ?: solicitudIdLocal
                if (targetSolId <= 0) return@onEach

                mensajesRemotos.forEach { msg ->
                    val usuariosMock = listOf(1, 2, 3, 4, 5, 99)
                    val usuarioIdSeguro = when {
                        msg.usuarioId in usuariosMock -> msg.usuarioId
                        msg.usuarioId > 0 -> msg.usuarioId
                        else -> when (msg.perfilUsuario) {
                            RolUsuario.ADMINISTRADOR -> 2
                            RolUsuario.TECNICO -> 3
                            RolUsuario.CLIENTE -> 1
                        }
                    }

                    val esNuevo = historialRepo.registrarSiNoExiste(
                        solicitudId = targetSolId,
                        usuarioId = usuarioIdSeguro,
                        perfil = msg.perfilUsuario,
                        estadoAnterior = msg.estadoAnterior?.name,
                        estadoNuevo = msg.estadoNuevo.name,
                        accion = msg.accion,
                        comentario = msg.comentario,
                        esComentarioInterno = msg.esComentarioInterno,
                        evidenciaAdjunta = msg.evidenciaAdjunta,
                        fechaCambio = msg.fechaCambio
                    )

                    if (!cargaInicial && esNuevo && !msg.esComentarioInterno) {
                        val destinoId = if (msg.perfilUsuario == RolUsuario.CLIENTE) {
                            ticketLocal?.tecnicoId ?: 2
                        } else {
                            ticketLocal?.clienteId ?: 1
                        }
                        notificacionRepo.crearNotificacion(
                            usuarioDestinoId = destinoId,
                            solicitudId = targetSolId,
                            titulo = "Nuevo mensaje en #$codigoLimpio",
                            mensaje = msg.comentario.ifBlank { "Tienes una nueva actualización." }.take(120),
                            tipoEvento = "CHAT_MENSAJE",
                            ahora = msg.fechaCambio
                        )
                    }
                }
                cargaInicial = false
            }
            ?.flowOn(Dispatchers.IO)
            ?.launchIn(scope)
    }

    suspend fun sincronizarArchivoRemoto(limite: Long = 500) {
        val lista = firebaseRepo?.sincronizarArchivoRemoto(limite) ?: return
        lista.forEach { localRepo.guardarOActualizar(it) }
    }

    suspend fun contarTotalTicketsRemotos(): Long? =
        firebaseRepo?.contarSolicitudesRemotas()

    suspend fun subirEvidenciaSiLocal(nombreArchivo: String, uriLocal: String): String? {
        if (uriLocal.startsWith("http")) return uriLocal
        val repo = appwriteStorageRepo ?: return null
        return repo.subirEvidencia(nombreArchivo, uriLocal)
            .onFailure { e ->
                Log.e("AppwriteStorage", "Fallo al subir evidencia '$nombreArchivo': ${e.message}", e)
                _ultimoErrorSync.value = "Evidencia '$nombreArchivo' guardada local; se reintentará al sincronizar."
            }
            .getOrNull()
    }

    private suspend fun sincronizarEvidenciasPendientes() {
        if (appwriteStorageRepo == null || sincronizandoEvidencias) return
        sincronizandoEvidencias = true
        try {
            val pendientes = historialRepo.obtenerPendientesSubida()
            for (evento in pendientes) {
                val rutaLocal = evento.evidenciaAdjunta ?: continue
                val nombre = rutaLocal.substringAfterLast('/').ifBlank { "evidencia_${evento.id}.jpg" }
                val urlNube = subirEvidenciaSiLocal(nombre, rutaLocal) ?: continue

                historialRepo.actualizarEvidencia(evento.id, urlNube)
                val sol = localRepo.obtenerPorId(evento.solicitudId) ?: continue
                val autor = usuarioDao?.obtenerUsuarioPorId(evento.usuarioId)
                runCatching {
                    firebaseRepo?.enviarMensajeChat(
                        codigoSeguimiento = sol.codigoSeguimiento,
                        solicitudId = sol.id,
                        usuarioId = evento.usuarioId,
                        autorNombre = autor?.nombre ?: evento.perfilUsuario.name,
                        autorRol = evento.perfilUsuario,
                        mensaje = evento.comentario,
                        esInterno = evento.esComentarioInterno,
                        evidenciaAdjunta = urlNube,
                        accion = evento.accion,
                        fecha = evento.fechaCambio
                    )
                }.onFailure { e ->
                    Log.w("FirebaseSync", "Error replicando evidencia subida: ${e.message}")
                }
            }
        } finally {
            sincronizandoEvidencias = false
        }
    }
}
