package com.example.gsm.data.repository

import com.example.gsm.data.local.dao.UsuarioDao
import com.example.gsm.data.local.entity.EvidenciaEntity
import com.example.gsm.data.local.model.ConteoPorTecnicoDto
import com.example.gsm.data.model.CategoriaSolicitud
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.Historial
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Solicitud
import com.example.gsm.data.model.Usuario
import com.example.gsm.util.ArchivoAdjunto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import android.util.Log

/**
 * REPOSITORIO PRINCIPAL (FACHADA / COORDINADOR)
 * Centraliza la comunicación entre Solicitudes, Historial, Evidencias, Notificaciones y WhatsApp.
 * Aplica control de acceso basado en roles (RBAC - OWASP M1/A01) exigido en Págs. 6, 7, 8 y 15 del PDF.
 */
class GsmRepository(
    private val localRepo: SolicitudLocalRepository,
    private val historialRepo: HistorialRepository,
    private val notificacionRepo: NotificacionRepository,
    private val evidenciaRepo: EvidenciaRepository? = null,
    private val firebaseRepo: FirebaseSolicitudRepository? = null,
    private val appwriteStorageRepo: AppwriteStorageRepository? = null,
    private val usuarioDao: UsuarioDao? = null
) {

    private val syncCoordinator = GsmSyncCoordinator(
        localRepo = localRepo,
        historialRepo = historialRepo,
        notificacionRepo = notificacionRepo,
        firebaseRepo = firebaseRepo,
        appwriteStorageRepo = appwriteStorageRepo,
        usuarioDao = usuarioDao
    )

    val ultimoErrorSync: StateFlow<String?> = syncCoordinator.ultimoErrorSync

    fun limpiarErrorSync() = syncCoordinator.limpiarErrorSync()

    fun iniciarSincronizacionBidireccional(scope: CoroutineScope) =
        syncCoordinator.iniciarSincronizacionBidireccional(scope)

    fun iniciarSincronizacionHistorial(codigoSeguimiento: String, solicitudIdLocal: Int, scope: CoroutineScope): Job? =
        syncCoordinator.iniciarSincronizacionHistorial(codigoSeguimiento, solicitudIdLocal, scope)

    suspend fun sincronizarArchivoRemoto(limite: Long = 500) =
        syncCoordinator.sincronizarArchivoRemoto(limite)

    suspend fun contarTotalTicketsRemotos(): Long? =
        syncCoordinator.contarTotalTicketsRemotos()

    private fun reportarSiFalla(res: Result<*>?, mensaje: String) {
        if (res != null && res.isFailure) {
            syncCoordinator.reportarErrorSync(mensaje)
        }
    }

    private suspend fun syncFirestore(solicitudId: Int) {
        val s = localRepo.obtenerPorId(solicitudId) ?: return
        syncCoordinator.registrarCambioLocal(s.codigoSeguimiento)
        val res = runCatching { firebaseRepo?.guardarSolicitud(s) }
            .fold(onSuccess = { it }, onFailure = { Result.failure<String>(it) })
        reportarSiFalla(res, "Cambio guardado local; pendiente de sincronizar con Firestore.")
    }

    /**
     * Envía un mensaje conversacional limpio al ticket (KISS).
     * Envía exactamente lo que el usuario escribió sin preformateos ni alterar estados.
     */
    suspend fun enviarMensajeChat(
        solicitudId: Int,
        usuario: Usuario,
        texto: String,
        esInterno: Boolean = false,
        evidenciaAdjunta: String? = null
    ): Result<Unit> = runCatching {
        val mensajeLimpio = texto.trim()
        val tieneEvidencia = !evidenciaAdjunta.isNullOrBlank()
        require(mensajeLimpio.isNotBlank() || tieneEvidencia) { "El mensaje no puede estar vacío" }

        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada")

        // 1. Guardar en SQLite local (Room) con el texto exacto del usuario
        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = usuario.id,
            perfil = usuario.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = solicitud.estado.name,
            accion = "MENSAJE",
            comentario = mensajeLimpio,
            esComentarioInterno = esInterno,
            evidenciaAdjunta = evidenciaAdjunta,
            ahora = ahora
        )

        // 2. Si hay imagen/archivo local, intenta subir a Appwrite Storage para que otros dispositivos la vean por URL
        val evidenciaParaNube = if (tieneEvidencia && !evidenciaAdjunta!!.startsWith("http")) {
            val nombreAdjunto = evidenciaAdjunta.substringAfterLast('/').ifBlank { "chat_$ahora.jpg" }
            subirEvidenciaSiLocal(nombreAdjunto, evidenciaAdjunta) ?: evidenciaAdjunta
        } else {
            evidenciaAdjunta
        }

        // 3. Sincronizar en tiempo real con Firestore con el texto exacto y URL en la nube
        syncCoordinator.registrarCambioLocal(solicitud.codigoSeguimiento, ahora)
        val resEnvio = runCatching {
            firebaseRepo?.enviarMensajeChat(
                codigoSeguimiento = solicitud.codigoSeguimiento,
                solicitudId = solicitudId,
                usuarioId = usuario.id,
                autorNombre = usuario.nombre,
                autorRol = usuario.rol,
                mensaje = mensajeLimpio,
                esInterno = esInterno,
                evidenciaAdjunta = evidenciaParaNube,
                accion = "MENSAJE",
                fecha = ahora
            )
        }.fold(onSuccess = { it }, onFailure = { Result.failure<String>(it) })
        reportarSiFalla(resEnvio, "Mensaje guardado local; pendiente de enviar a la nube.")
    }

    suspend fun actualizarInformacionSolicitud(
        solicitudId: Int,
        nuevoTitulo: String,
        nuevaDescripcion: String,
        usuario: Usuario,
        nuevaCategoria: CategoriaSolicitud? = null,
        nuevoServicio: String? = null,
        nuevoActivo: String? = null
    ): Result<Unit> = runCatching {
        require(nuevoTitulo.isNotBlank()) { "El título no puede estar vacío" }
        require(nuevaDescripcion.isNotBlank()) { "La descripción no puede estar vacía" }
        val actual = localRepo.obtenerPorId(solicitudId) ?: throw IllegalArgumentException("Solicitud no encontrada")
        val ahora = System.currentTimeMillis()
        localRepo.actualizarInformacion(
            id = solicitudId,
            nuevoTitulo = nuevoTitulo.trim(),
            nuevaDescripcion = nuevaDescripcion.trim(),
            categoria = nuevaCategoria ?: actual.categoria,
            servicioRelacionado = nuevoServicio?.trim()?.ifBlank { null },
            activoRelacionado = nuevoActivo?.trim()?.ifBlank { null },
            ahora = ahora
        )
        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = usuario.id,
            perfil = usuario.rol,
            estadoAnterior = actual.estado.name,
            estadoNuevo = actual.estado.name,
            accion = "EDICION",
            comentario = "Información del ticket actualizada.",
            esComentarioInterno = false,
            ahora = ahora
        )
        syncFirestore(solicitudId)
    }

    // 1. FLUJOS DE CREACIÓN (Con trazabilidad, validación y código único)
    // -------------------------------------------------------------

    suspend fun crearSolicitud(
        titulo: String,
        descripcion: String,
        categoria: CategoriaSolicitud,
        prioridad: PrioridadSolicitud = PrioridadSolicitud.MEDIA,
        cliente: Usuario,
        servicioRelacionado: String? = null,
        activoRelacionado: String? = null,
        nombreArchivoEvidencia: String? = null,
        tipoArchivoEvidencia: String? = null,
        tamanoArchivoEvidencia: Long? = null,
        uriEvidencia: String? = null,
        evidencias: List<ArchivoAdjunto> = emptyList(),
        esBorrador: Boolean = false
    ): Result<Solicitud> {
        return try {
            require(titulo.isNotBlank() && descripcion.isNotBlank()) {
                "El título y la descripción son obligatorios."
            }

            if (!esBorrador && cliente.rol == RolUsuario.CLIENTE) {
                val disponibles = usuarioDao?.obtenerUsuarioPorId(cliente.id)?.ticketsDisponibles ?: cliente.ticketsDisponibles
                require(disponibles > 0) {
                    "No tienes tickets disponibles en tu plan actual. Activa o renueva tu suscripción para crear solicitudes."
                }
                val empresaCliente = cliente.empresa.orEmpty()
                if (empresaCliente.isNotBlank()) {
                    usuarioDao?.descontarTicketEmpresa(empresaCliente)
                } else {
                    usuarioDao?.descontarTicketDisponible(cliente.id)
                }
            }

            val ahora = System.currentTimeMillis()
            val codigo = localRepo.generarCodigoUnico() // Generación del código único oficial del ticket  Ej: SOL-2026-100X
            // Cálculo automático del plazo según nivel SLA
            val fechaLimite = localRepo.calcularSlaFechaLimite(prioridad, ahora)


            val nuevaSolicitud = Solicitud(
                id = 0,
                codigoSeguimiento = codigo,
                titulo = titulo.trim(),
                descripcion = descripcion.trim(),
                categoria = categoria,
                clienteId = cliente.id,
                tecnicoId = null,
                estado = EstadoSolicitud.RECIBIDA,
                prioridad = prioridad,
                servicioRelacionado = servicioRelacionado?.trim(),
                activoRelacionado = activoRelacionado?.trim(),
                fechaLimite = fechaLimite,
                fechaCreacion = ahora,
                fechaActualizacion = ahora
            )

            val idGenerado = localRepo.insertar(nuevaSolicitud, esBorrador).toInt()
            val solicitudGuardada = nuevaSolicitud.copy(id = idGenerado)

            // Unifica adjuntos múltiples (o el parámetro único heredado por compatibilidad con tests).
            val listaEvidencias = evidencias.ifEmpty {
                if (!nombreArchivoEvidencia.isNullOrBlank()) {
                    listOf(
                        ArchivoAdjunto(
                            nombre = nombreArchivoEvidencia,
                            tipoMime = tipoArchivoEvidencia ?: "image/png",
                            tamanoBytes = tamanoArchivoEvidencia ?: 250_000L,
                            uriString = uriEvidencia.orEmpty()
                        )
                    )
                } else emptyList()
            }

            // Sube cada adjunto a Appwrite antes de persistir; si está offline conserva file:// para reintento.
            val adjuntosProcesados = listaEvidencias.mapIndexed { idx, adjunto ->
                val urlNube = if (!esBorrador && adjunto.uriString.isNotBlank()) {
                    subirEvidenciaSiLocal(adjunto.nombre, adjunto.uriString)
                } else {
                    null
                }
                val uriFinal = urlNube ?: adjunto.uriString.ifBlank { null }

                evidenciaRepo?.adjuntarEvidencia(
                    solicitudId = idGenerado,
                    usuarioId = cliente.id,
                    nombreArchivo = adjunto.nombre,
                    tipoArchivo = adjunto.tipoMime,
                    tamanoBytes = adjunto.tamanoBytes,
                    uriLocal = uriFinal,
                    descripcion = "Evidencia adjuntada al crear la solicitud (${idx + 1})",
                    ahora = ahora + idx
                )
                Triple(adjunto, uriFinal, urlNube)
            }

            // Registra hitos de auditoría y replica en Firestore cada adjunto que ya obtuvo URL http.
            if (!esBorrador) {
                if (adjuntosProcesados.isEmpty()) {
                    historialRepo.registrarEvento(
                        solicitudId = idGenerado,
                        usuarioId = cliente.id,
                        perfil = cliente.rol,
                        estadoAnterior = null,
                        estadoNuevo = EstadoSolicitud.RECIBIDA.name,
                        accion = "CREACION",
                        comentario = "Solicitud ingresada formalmente por el cliente.",
                        esComentarioInterno = false,
                        evidenciaAdjunta = null,
                        ahora = ahora
                    )
                } else {
                    adjuntosProcesados.forEachIndexed { idx, (adjunto, uriFinal, urlNube) ->
                        val accion = if (idx == 0) "CREACION" else "EVIDENCIA"
                        val comentario = if (idx == 0) {
                            "Solicitud ingresada formalmente por el cliente."
                        } else {
                            "Evidencia adjunta: ${adjunto.nombre}"
                        }
                        historialRepo.registrarEvento(
                            solicitudId = idGenerado,
                            usuarioId = cliente.id,
                            perfil = cliente.rol,
                            estadoAnterior = if (idx == 0) null else EstadoSolicitud.RECIBIDA.name,
                            estadoNuevo = EstadoSolicitud.RECIBIDA.name,
                            accion = accion,
                            comentario = comentario,
                            esComentarioInterno = false,
                            evidenciaAdjunta = uriFinal ?: adjunto.nombre,
                            ahora = ahora + idx
                        )
                        // Solo replica en Firestore cuando hay URL http pública; si es file:// se sube al reconectar.
                        if (!urlNube.isNullOrBlank()) {
                            runCatching {
                                firebaseRepo?.enviarMensajeChat(
                                    codigoSeguimiento = codigo,
                                    solicitudId = idGenerado,
                                    usuarioId = cliente.id,
                                    autorNombre = cliente.nombre,
                                    autorRol = cliente.rol,
                                    mensaje = comentario,
                                    esInterno = false,
                                    evidenciaAdjunta = urlNube,
                                    accion = accion,
                                    fecha = ahora + idx
                                )
                            }
                        }
                    }
                }
                syncCoordinator.registrarCambioLocal(solicitudGuardada.codigoSeguimiento)
                val resGuardar = runCatching { firebaseRepo?.guardarSolicitud(solicitudGuardada) }
                    .fold(onSuccess = { it }, onFailure = { Result.failure<String>(it) })
                reportarSiFalla(resGuardar, "Ticket guardado local; pendiente de sincronizar con Firestore.")
            }

            Result.success(solicitudGuardada)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // 2. TRANSICIONES DE ESTADO (Coordinación Atómica + Seguridad RBAC)
    // -------------------------------------------------------------

    /**
     * El Administrador clasifica la solicitud y define su prioridad (Criterios 13 y 14 del PDF).
     */
    suspend fun clasificarYPriorizar(
        solicitudId: Int,
        nuevaCategoria: CategoriaSolicitud,
        nuevaPrioridad: PrioridadSolicitud,
        admin: Usuario,
        comentario: String = "Solicitud clasificada y priorizada por coordinación."
    ): Result<Unit> = runCatching {
        require(admin.rol == RolUsuario.ADMINISTRADOR) {
            "Acceso denegado: Solo el administrador puede clasificar y definir prioridades."
        }
        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        localRepo.clasificarYPriorizar(solicitudId, nuevaCategoria, nuevaPrioridad, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = admin.id,
            perfil = admin.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.EN_REVISION.name,
            accion = "CLASIFICACION",
            comentario = "$comentario (Categoría: ${nuevaCategoria.descripcion}, Prioridad: ${nuevaPrioridad.name})",
            ahora = ahora
        )

        notificacionRepo.crearNotificacion(
            usuarioDestinoId = solicitud.clienteId,
            solicitudId = solicitudId,
            titulo = "Solicitud en Revisión",
            mensaje = "Tu solicitud #${solicitud.codigoSeguimiento} fue clasificada con prioridad ${nuevaPrioridad.name}.",
            tipoEvento = "CAMBIO_PRIORIDAD",
            ahora = ahora
        )
        syncFirestore(solicitudId)
    }

    /**
     * El Administrador asigna o reasigna un técnico responsable (Criterios 15 y 16 del PDF).
     */
    suspend fun asignarTecnico(
        solicitudId: Int,
        tecnicoId: Int,
        admin: Usuario,
        comentario: String = "Técnico asignado para ejecución."
    ): Result<Unit> = runCatching {
        require(admin.rol == RolUsuario.ADMINISTRADOR) {
            "Acceso denegado: Solo el administrador puede asignar responsables técnicos."
        }
        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        localRepo.asignarTecnico(solicitudId, tecnicoId, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = admin.id,
            perfil = admin.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.ASIGNADA.name,
            accion = "ASIGNACION",
            comentario = comentario,
            ahora = ahora
        )

        notificacionRepo.crearNotificacion(
            usuarioDestinoId = tecnicoId,
            solicitudId = solicitudId,
            titulo = "Nueva Solicitud Asignada",
            mensaje = "Se te ha asignado la solicitud #${solicitud.codigoSeguimiento}",
            tipoEvento = "ASIGNACION",
            ahora = ahora
        )
        syncFirestore(solicitudId)
    }

    /**
     * El Administrador remueve o desasigna al técnico responsable del ticket.
     * El ticket revierte su estado a EN_REVISION para permitir una nueva asignación.
     */
    suspend fun desasignarTecnico(
        solicitudId: Int,
        admin: Usuario,
        motivo: String = "Técnico desasignado por el administrador."
    ): Result<Unit> = runCatching {
        require(admin.rol == RolUsuario.ADMINISTRADOR) {
            "Acceso denegado: Solo el administrador puede desasignar responsables técnicos."
        }
        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        require(solicitud.tecnicoId != null) {
            "La solicitud no tiene ningún técnico asignado para remover."
        }

        val tecnicoAnteriorId = solicitud.tecnicoId

        localRepo.desasignarTecnico(solicitudId, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = admin.id,
            perfil = admin.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.EN_REVISION.name,
            accion = "DESASIGNACION",
            comentario = motivo,
            ahora = ahora
        )

        notificacionRepo.crearNotificacion(
            usuarioDestinoId = tecnicoAnteriorId,
            solicitudId = solicitudId,
            titulo = "Solicitud Desasignada",
            mensaje = "Has sido desasignado de la solicitud #${solicitud.codigoSeguimiento}",
            tipoEvento = "DESASIGNACION",
            ahora = ahora
        )

        syncFirestore(solicitudId)
    }

    /**
     * El Técnico o Administrador registra un avance o comentario de seguimiento (Criterios 18, 19, 20 y 21).
     */
    suspend fun registrarAvance(
        solicitudId: Int,
        usuario: Usuario,
        comentario: String,
        esInterno: Boolean = false
    ): Result<Unit> = runCatching {
        require(usuario.rol == RolUsuario.TECNICO || usuario.rol == RolUsuario.ADMINISTRADOR) {
            "Acceso denegado: Solo técnicos o administradores pueden registrar avances técnicos."
        }
        require(comentario.isNotBlank()) { "El comentario de avance no puede estar vacío." }

        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        localRepo.actualizarEstado(solicitudId, EstadoSolicitud.EN_PROCESO, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = usuario.id,
            perfil = usuario.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.EN_PROCESO.name,
            accion = "AVANCE",
            comentario = comentario.trim(),
            esComentarioInterno = esInterno,
            ahora = ahora
        )

        if (!esInterno) {
            notificacionRepo.crearNotificacion(
                usuarioDestinoId = solicitud.clienteId,
                solicitudId = solicitudId,
                titulo = "Nuevo Avance en tu Solicitud",
                mensaje = "Se registró un avance en la solicitud #${solicitud.codigoSeguimiento}.",
                tipoEvento = "EN_PROCESO",
                ahora = ahora
            )
        }

        // Sincronización en tiempo real del chat con Firestore (Web y otros celulares)
        runCatching {
            firebaseRepo?.enviarMensajeChat(
                codigoSeguimiento = solicitud.codigoSeguimiento,
                solicitudId = solicitudId,
                usuarioId = usuario.id,
                autorNombre = usuario.nombre,
                autorRol = usuario.rol,
                mensaje = comentario.trim(),
                esInterno = esInterno,
                accion = "AVANCE",
                fecha = ahora
            )
        }
        syncFirestore(solicitudId)
    }

    /**
     * Técnico o Administrador solicita antecedentes adicionales al cliente (Criterio 22).
     */
    suspend fun solicitarInformacion(
        solicitudId: Int,
        usuario: Usuario,
        motivo: String
    ): Result<Unit> = runCatching {
        require(usuario.rol == RolUsuario.TECNICO || usuario.rol == RolUsuario.ADMINISTRADOR) {
            "Acceso denegado: Solo personal de la empresa puede solicitar información adicional."
        }
        require(motivo.isNotBlank()) { "Debes indicar qué información se requiere." }

        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        localRepo.actualizarEstado(solicitudId, EstadoSolicitud.PENDIENTE_INFORMACION, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = usuario.id,
            perfil = usuario.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.PENDIENTE_INFORMACION.name,
            accion = "SOLICITUD_INFO",
            comentario = motivo.trim(),
            ahora = ahora
        )

        notificacionRepo.crearNotificacion(
            usuarioDestinoId = solicitud.clienteId,
            solicitudId = solicitudId,
            titulo = "Información Requerida",
            mensaje = "Se requieren antecedentes adicionales para tu solicitud #${solicitud.codigoSeguimiento}.",
            tipoEvento = "PENDIENTE_INFORMACION",
            ahora = ahora
        )
        syncFirestore(solicitudId)
    }

    /**
     * El Cliente responde a una solicitud de información aportando antecedentes (PDF Pág. 3 y 6).
     */
    suspend fun responderInformacion(
        solicitudId: Int,
        cliente: Usuario,
        respuesta: String,
        nombreArchivoEvidencia: String? = null,
        tipoArchivoEvidencia: String? = null,
        tamanoArchivoEvidencia: Long? = null,
        uriEvidencia: String? = null
    ): Result<Unit> = runCatching {
        require(cliente.rol == RolUsuario.CLIENTE) {
            "Acceso denegado: Solo el cliente titular puede aportar la información requerida."
        }
        require(respuesta.isNotBlank()) { "Debes ingresar una respuesta explicativa." }

        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        val nuevoEstado = if (solicitud.tecnicoId != null) EstadoSolicitud.EN_PROCESO else EstadoSolicitud.ASIGNADA

        if (!nombreArchivoEvidencia.isNullOrBlank()) {
            evidenciaRepo?.adjuntarEvidencia(
                solicitudId = solicitudId,
                usuarioId = cliente.id,
                nombreArchivo = nombreArchivoEvidencia,
                descripcion = "Antecedente adicional enviado por el cliente",
                ahora = ahora
            )
        }

        localRepo.actualizarEstado(solicitudId, nuevoEstado, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = cliente.id,
            perfil = cliente.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = nuevoEstado.name,
            accion = "RESPUESTA_INFO",
            comentario = respuesta.trim(),
            evidenciaAdjunta = nombreArchivoEvidencia,
            ahora = ahora
        )

        // Sincronización en tiempo real del chat con Firestore (Web y otros celulares)
        runCatching {
            firebaseRepo?.enviarMensajeChat(
                codigoSeguimiento = solicitud.codigoSeguimiento,
                solicitudId = solicitudId,
                usuarioId = cliente.id,
                autorNombre = cliente.nombre,
                autorRol = cliente.rol,
                mensaje = respuesta.trim(),
                esInterno = false,
                evidenciaAdjunta = nombreArchivoEvidencia,
                accion = "RESPUESTA_INFO",
                fecha = ahora
            )
        }
        syncFirestore(solicitudId)
    }

    /**
     * El Técnico ejecutor marca la solicitud como RESUELTA indicando la solución técnica (Criterio 25).
     */
    suspend fun registrarResolucion(
        solicitudId: Int,
        tecnico: Usuario,
        solucion: String
    ): Result<Unit> = runCatching {
        require(tecnico.rol == RolUsuario.TECNICO || tecnico.rol == RolUsuario.ADMINISTRADOR) {
            "Acceso denegado: Solo el ejecutor técnico puede resolver el requerimiento."
        }
        require(solucion.isNotBlank()) { "Debes documentar la solución técnica aplicada." }

        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        localRepo.registrarResolucion(solicitudId, solucion.trim(), ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = tecnico.id,
            perfil = tecnico.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.RESUELTA.name,
            accion = "RESOLUCION",
            comentario = solucion.trim(),
            ahora = ahora
        )

        notificacionRepo.crearNotificacion(
            usuarioDestinoId = solicitud.clienteId,
            solicitudId = solicitudId,
            titulo = "Solicitud Resuelta",
            mensaje = "Tu requerimiento #${solicitud.codigoSeguimiento} fue resuelto. Por favor confirma conformidad.",
            tipoEvento = "RESUELTA",
            ahora = ahora
        )
        syncFirestore(solicitudId)
    }

    /**
     * El Cliente confirma conformidad cerrando formalmente el ciclo de vida (Criterio 26 y 27).
     */
    suspend fun confirmarCierre(
        solicitudId: Int,
        usuario: Usuario,
        conforme: Boolean = true,
        calificacion: Int? = null
    ): Result<Unit> = runCatching {
        require(usuario.rol == RolUsuario.CLIENTE || usuario.rol == RolUsuario.ADMINISTRADOR) {
            "Acceso denegado: Solo el cliente o el administrador pueden dar cierre de conformidad."
        }
        val calificacionValida = calificacion?.coerceIn(1, 5)
        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        localRepo.confirmarCierre(solicitudId, conforme, ahora, calificacionValida)

        val sufijoNota = calificacionValida?.let { " (Calificación: $it/5 ★)" }.orEmpty()
        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = usuario.id,
            perfil = usuario.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.CERRADA.name,
            accion = "CIERRE",
            comentario = (if (conforme) "Cliente confirma conformidad con la solución." else "Cierre registrado por administración.") + sufijoNota,
            ahora = ahora
        )
        syncFirestore(solicitudId)
    }

    /**
     * El Cliente rechaza la resolución o solicita reapertura por disconformidad (Pág. 7).
     */
    suspend fun solicitarReapertura(
        solicitudId: Int,
        cliente: Usuario,
        motivo: String
    ): Result<Unit> = runCatching {
        require(cliente.rol == RolUsuario.CLIENTE) {
            "Acceso denegado: Solo el cliente solicitante puede pedir reapertura."
        }
        require(motivo.isNotBlank()) { "Debes indicar el motivo por el cual solicitas reabrir el caso." }

        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        require(solicitud.estado == EstadoSolicitud.RESUELTA || solicitud.estado == EstadoSolicitud.CERRADA) {
            "Solo se puede reabrir una solicitud que haya sido marcada como Resuelta o Cerrada."
        }

        localRepo.solicitarReapertura(solicitudId, motivo.trim(), ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = cliente.id,
            perfil = cliente.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.EN_REVISION.name,
            accion = "REAPERTURA",
            comentario = motivo.trim(),
            ahora = ahora
        )
        syncFirestore(solicitudId)
    }

    /**
     * El Cliente solicita la anulación/cancelación formal del ticket por equivocación o duplicidad.
     * Respeta la regla de negocio RBAC: el cliente emite la solicitud y el Administrador la autoriza.
     */
    suspend fun solicitarCancelacion(
        solicitudId: Int,
        cliente: Usuario,
        motivo: String
    ): Result<Unit> = runCatching {
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        require(cliente.rol == RolUsuario.CLIENTE && solicitud.clienteId == cliente.id) {
            "Acceso denegado: Solo el autor de la solicitud puede pedir su anulación."
        }
        val motivoLimpio = motivo.trim().ifBlank { "Solicitud de anulación por equivocación del cliente." }
        val ahora = System.currentTimeMillis()

        // 1. Bitácora de historial
        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = cliente.id,
            perfil = cliente.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = solicitud.estado.name,
            accion = "SOLICITUD_CANCELACION",
            comentario = "⚠️ El cliente ha solicitado anular el ticket: $motivoLimpio",
            ahora = ahora
        )

        // 2. Chat en tiempo real para alertar de inmediato a soporte y al administrador
        syncCoordinator.registrarCambioLocal(solicitud.codigoSeguimiento, ahora)
        val resCancelChat = runCatching {
            firebaseRepo?.enviarMensajeChat(
                codigoSeguimiento = solicitud.codigoSeguimiento,
                solicitudId = solicitudId,
                usuarioId = cliente.id,
                autorNombre = cliente.nombre,
                autorRol = cliente.rol,
                mensaje = "⚠️ He solicitado anular este ticket: $motivoLimpio",
                esInterno = false,
                accion = "SOLICITUD_CANCELACION",
                fecha = ahora
            )
        }.fold(onSuccess = { it }, onFailure = { Result.failure<String>(it) })
        reportarSiFalla(resCancelChat, "Solicitud de anulación guardada local; pendiente de sincronizar con la nube.")

        syncFirestore(solicitudId)
    }

    /**
     * Cancelar solicitud: Función exclusiva del ADMINISTRADOR (Regla RBAC estricta según rúbrica).
     * Muta el estado a CANCELADA, guarda el motivo y lo archiva en tiempo real.
     */
    suspend fun cancelar(
        solicitudId: Int,
        admin: Usuario,
        motivo: String
    ): Result<Unit> = runCatching {
        require(admin.rol == RolUsuario.ADMINISTRADOR) {
            "Acceso denegado: Solo el administrador puede cancelar una solicitud (Regla RBAC)."
        }
        require(motivo.isNotBlank()) { "Debes ingresar el motivo de cancelación." }

        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        localRepo.cancelar(solicitudId, motivo.trim(), ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = admin.id,
            perfil = admin.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.CANCELADA.name,
            accion = "CANCELACION",
            comentario = motivo.trim(),
            ahora = ahora
        )
        syncFirestore(solicitudId)
    }

    // -------------------------------------------------------------
    // 3. CONSULTAS, HISTORIAL, EVIDENCIAS Y NOTIFICACIONES
    // -------------------------------------------------------------

    fun listarSolicitudes(
        estado: EstadoSolicitud? = null,
        prioridad: PrioridadSolicitud? = null,
        clienteId: Int? = null,
        tecnicoId: Int? = null,
        categoria: CategoriaSolicitud? = null
    ): Flow<List<Solicitud>> = localRepo.filtrar(estado, prioridad, categoria, clienteId, tecnicoId)

    suspend fun obtenerPorId(id: Int): Solicitud? = localRepo.obtenerPorId(id)

    suspend fun buscarPorCodigo(codigo: String): Solicitud? = localRepo.obtenerPorCodigo(codigo)

    fun listarAtrasadas(): Flow<List<Solicitud>> = localRepo.obtenerAtrasadas()

    fun obtenerHistorial(solicitudId: Int, rol: RolUsuario): Flow<List<Historial>> =
        historialRepo.obtenerHistorial(solicitudId, rol)

    suspend fun adjuntarEvidencia(
        solicitudId: Int,
        usuario: Usuario,
        nombreArchivo: String,
        tipoArchivo: String = "image/png",
        tamanoBytes: Long = 250_000L,
        uriLocal: String? = null,
        descripcion: String? = null
    ): Result<Long> {
        val repo = evidenciaRepo
            ?: return Result.failure(IllegalStateException("EvidenciaRepository no inicializado."))
        val ahora = System.currentTimeMillis()
        // Sube primero a Appwrite; si está offline, guarda la ruta local para reintento automático
        val urlNube = if (!nombreArchivo.isBlank() && !uriLocal.isNullOrBlank()) {
            subirEvidenciaSiLocal(nombreArchivo, uriLocal)
        } else {
            null
        }
        val uriFinal = urlNube ?: uriLocal

        val resultado = repo.adjuntarEvidencia(
            solicitudId = solicitudId,
            usuarioId = usuario.id,
            nombreArchivo = nombreArchivo,
            tipoArchivo = tipoArchivo,
            tamanoBytes = tamanoBytes,
            uriLocal = uriFinal,
            descripcion = descripcion,
            ahora = ahora
        )

        if (resultado.isSuccess && !uriFinal.isNullOrBlank()) {
            val sol = localRepo.obtenerPorId(solicitudId)
            val estadoActual = sol?.estado?.name ?: EstadoSolicitud.EN_PROCESO.name
            val textoEvento = "Evidencia adjunta: $nombreArchivo"
            historialRepo.registrarEvento(
                solicitudId = solicitudId,
                usuarioId = usuario.id,
                perfil = usuario.rol,
                estadoAnterior = estadoActual,
                estadoNuevo = estadoActual,
                accion = "EVIDENCIA",
                comentario = textoEvento,
                esComentarioInterno = false,
                evidenciaAdjunta = uriFinal,
                ahora = ahora
            )
            // Solo replica en Firestore cuando existe URL http pública; si es local, se sube al reconectar
            if (sol != null && !urlNube.isNullOrBlank()) {
                syncCoordinator.registrarCambioLocal(sol.codigoSeguimiento, ahora)
                val resEvidencia = runCatching {
                    firebaseRepo?.enviarMensajeChat(
                        codigoSeguimiento = sol.codigoSeguimiento,
                        solicitudId = solicitudId,
                        usuarioId = usuario.id,
                        autorNombre = usuario.nombre,
                        autorRol = usuario.rol,
                        mensaje = textoEvento,
                        esInterno = false,
                        evidenciaAdjunta = urlNube,
                        accion = "EVIDENCIA",
                        fecha = ahora
                    )
                }.fold(onSuccess = { it }, onFailure = { Result.failure<String>(it) })
                reportarSiFalla(resEvidencia, "Evidencia guardada local; pendiente de replicar en la nube.")
            }
        }
        return resultado
    }

    private suspend fun subirEvidenciaSiLocal(nombreArchivo: String, uriLocal: String): String? =
        syncCoordinator.subirEvidenciaSiLocal(nombreArchivo, uriLocal)

    fun obtenerEvidencias(solicitudId: Int): Flow<List<EvidenciaEntity>>? =
        evidenciaRepo?.obtenerPorSolicitud(solicitudId)

    fun obtenerNotificaciones(usuarioId: Int) = notificacionRepo.obtenerPorUsuario(usuarioId)

    fun contarNotificacionesNoLeidas(usuarioId: Int) = notificacionRepo.contarNoLeidas(usuarioId)

    suspend fun marcarNotificacionLeida(id: Int) = notificacionRepo.marcarComoLeida(id)

    // -------------------------------------------------------------
    // 4. DASHBOARD Y MÉTRICAS (Criterios 29 y 30)
    // -------------------------------------------------------------

    fun metricasPorEstado(estado: EstadoSolicitud): Flow<Int> = localRepo.contarPorEstado(estado)
    fun metricasAtrasadas(): Flow<Int> = localRepo.contarAtrasadas()
    fun metricasPorPrioridad(prioridad: PrioridadSolicitud): Flow<Int> = localRepo.contarPorPrioridad(prioridad)
    fun metricasPorTecnico(): Flow<List<ConteoPorTecnicoDto>> = localRepo.contarPorTecnico()
    fun metricasPromedioCsat(): Flow<Double?> = localRepo.promedioCalificacion()

    suspend fun obtenerIdsClientesEmpresa(empresa: String): List<Int> =
        if (empresa.isBlank()) emptyList() else usuarioDao?.obtenerIdsPorEmpresa(empresa) ?: emptyList()
}
