package com.example.gsm.data.repository

import android.util.Log
import com.example.gsm.data.model.CategoriaSolicitud
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.Historial
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Solicitud
import com.example.gsm.data.remote.FirebaseClient
import com.example.gsm.security.GsmCryptoManager
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de Firestore con soporte Offline-First y sincronización bidireccional.
 * Gestiona la colección principal "solicitudes" y la subcolección "historial" (chat en vivo).
 */
class FirebaseSolicitudRepository(
    private val solicitudesRef: CollectionReference = FirebaseClient.firestore.collection("solicitudes")
) {

    /**
     * Guarda o actualiza una solicitud en Firebase.
     */
    suspend fun guardarSolicitud(solicitud: Solicitud): Result<String> {
        return try {
            val codigo = solicitud.codigoSeguimiento.trim()
            val docId = codigo.ifBlank {
                solicitudesRef.document().id
            }
            val docRef = solicitudesRef.document(docId)
            val datos = hashMapOf(
                "id" to solicitud.id,
                "idLocal" to solicitud.id,
                "codigoSeguimiento" to if (codigo.isNotBlank()) codigo else docId,
                "titulo" to solicitud.titulo.trim(),
                "descripcion" to GsmCryptoManager.encrypt(solicitud.descripcion.trim()),
                "categoria" to solicitud.categoria.name,
                "clienteId" to solicitud.clienteId,
                "tecnicoId" to solicitud.tecnicoId,
                "estado" to solicitud.estado.name,
                "prioridad" to solicitud.prioridad.name,
                "servicioRelacionado" to solicitud.servicioRelacionado,
                "activoRelacionado" to solicitud.activoRelacionado,
                "resolucion" to solicitud.resolucion?.let { GsmCryptoManager.encrypt(it) },
                "motivoCancelacion" to solicitud.motivoCancelacion?.let { GsmCryptoManager.encrypt(it) },
                "motivoReapertura" to solicitud.motivoReapertura?.let { GsmCryptoManager.encrypt(it) },
                "conformidadCliente" to solicitud.conformidadCliente,
                "fechaLimite" to solicitud.fechaLimite,
                "fechaCreacion" to solicitud.fechaCreacion,
                "fechaActualizacion" to System.currentTimeMillis()
            )
            docRef.set(datos, SetOptions.merge()).await()
            Log.d("FirebaseSync", "Solicitud guardada en Firestore: $docId")
            Result.success(docId)
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error al guardar solicitud en Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Escucha en tiempo real todas las solicitudes.
     * Mantiene el flujo vivo ante avisos de red o reconexiones.
     */
    fun escucharSolicitudes(): Flow<List<Solicitud>> = callbackFlow {
        val listener = solicitudesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("FirebaseSync", "Aviso en listener de solicitudes: ${error.message}")
                return@addSnapshotListener
            }
            val solicitudes = snapshot?.documents?.mapNotNull { doc ->
                mapearDocumentoASolicitud(doc)
            } ?: emptyList()
            Log.d("FirebaseSync", "Descargadas ${solicitudes.size} solicitudes desde Firestore")
            trySend(solicitudes)
        }
        awaitClose { listener.remove() }
    }

    /**
     * Envía un mensaje o avance al chat en vivo de la solicitud en Firestore.
     * Ruta: /solicitudes/{codigoSeguimiento}/historial/{autoId}
     * Compatible con la versión Web y con otros dispositivos móviles.
     */
    suspend fun enviarMensajeChat(
        codigoSeguimiento: String,
        solicitudId: Int,
        usuarioId: Int,
        autorNombre: String,
        autorRol: RolUsuario,
        mensaje: String,
        esInterno: Boolean = false,
        evidenciaAdjunta: String? = null,
        accion: String = "AVANCE",
        fecha: Long = System.currentTimeMillis()
    ): Result<String> {
        return try {
            val codigo = codigoSeguimiento.trim()
            require(codigo.isNotBlank()) { "El código de seguimiento no puede estar vacío" }
            val historialRef = solicitudesRef.document(codigo).collection("historial")

            val datos = hashMapOf(
                "solicitudId" to solicitudId,
                "autorId" to usuarioId,
                "usuarioId" to usuarioId,
                "autorNombre" to autorNombre,
                "autorRol" to autorRol.name,
                "perfilUsuario" to autorRol.name,
                "mensaje" to GsmCryptoManager.encrypt(mensaje.trim()),
                "comentario" to GsmCryptoManager.encrypt(mensaje.trim()),
                "fecha" to fecha,
                "fechaCambio" to fecha,
                "esInterno" to esInterno,
                "esComentarioInterno" to esInterno,
                "evidenciaAdjunta" to evidenciaAdjunta,
                "accion" to accion
            )

            val doc = historialRef.add(datos).await()

            // Actualizar timestamp en la cabecera del ticket
            solicitudesRef.document(codigo).update("fechaActualizacion", fecha).await()

            Log.d("FirebaseSync", "Mensaje enviado a Firestore ($codigo): ${doc.id}")
            Result.success(doc.id)
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error enviando mensaje chat a Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Escucha en tiempo real los mensajes del chat de un ticket específico en Firestore.
     */
    fun escucharHistorialChat(codigoSeguimiento: String): Flow<List<Historial>> = callbackFlow {
        val codigo = codigoSeguimiento.trim()
        if (codigo.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val historialRef = solicitudesRef.document(codigo).collection("historial")
        val listener = historialRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("FirebaseSync", "Aviso en escucha de chat ($codigo): ${error.message}")
                return@addSnapshotListener
            }

            val mensajes = snapshot?.documents?.mapNotNull { doc ->
                kotlin.runCatching {
                    val data = doc.data ?: return@runCatching null
                    val rawTexto = (data["comentario"] as? String)
                        ?: (data["mensaje"] as? String)
                        ?: ""
                    val comentario = GsmCryptoManager.decrypt(rawTexto)
                    if (comentario.isBlank()) return@runCatching null

                    val rawFecha = data["fechaCambio"] ?: data["fecha"]
                    val fecha = when (rawFecha) {
                        is Number -> rawFecha.toLong()
                        is com.google.firebase.Timestamp -> rawFecha.toDate().time
                        is String -> rawFecha.toLongOrNull() ?: System.currentTimeMillis()
                        else -> System.currentTimeMillis()
                    }
                    val rolStr = (data["perfilUsuario"] as? String)
                        ?: (data["autorRol"] as? String)
                        ?: "CLIENTE"
                    val rol = runCatching { RolUsuario.valueOf(rolStr) }.getOrDefault(RolUsuario.CLIENTE)
                    val usuarioId = (data["usuarioId"] as? Number)?.toInt()
                        ?: (data["autorId"] as? Number)?.toInt()
                        ?: 1
                    val esInterno = (data["esComentarioInterno"] as? Boolean)
                        ?: (data["esInterno"] as? Boolean)
                        ?: false
                    val accion = (data["accion"] as? String) ?: "AVANCE"
                    val evidencia = data["evidenciaAdjunta"] as? String
                    val solId = (data["solicitudId"] as? Number)?.toInt() ?: 0

                    Historial(
                        id = 0,
                        solicitudId = solId,
                        usuarioId = usuarioId,
                        estadoAnterior = null,
                        estadoNuevo = EstadoSolicitud.EN_PROCESO,
                        accion = accion,
                        comentario = comentario,
                        perfilUsuario = rol,
                        esComentarioInterno = esInterno,
                        evidenciaAdjunta = evidencia,
                        fechaCambio = fecha
                    )
                }.getOrNull()
            }?.sortedBy { it.fechaCambio } ?: emptyList()

            Log.d("FirebaseSync", "Recibidos ${mensajes.size} mensajes de chat para $codigo")
            trySend(mensajes)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Actualiza únicamente el estado de una solicitud.
     */
    suspend fun actualizarEstado(codigoSeguimiento: String, nuevoEstado: EstadoSolicitud): Result<Unit> {
        return try {
            val codigo = codigoSeguimiento.trim()
            solicitudesRef.document(codigo)
                .update(
                    mapOf(
                        "estado" to nuevoEstado.name,
                        "fechaActualizacion" to System.currentTimeMillis()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mapea de forma segura los datos de Firebase al modelo Solicitud de la app.
     */
    private fun mapearDocumentoASolicitud(doc: DocumentSnapshot): Solicitud? {
        return kotlin.runCatching {
            val data = doc.data ?: return null
            val categoriaStr = data["categoria"] as? String ?: CategoriaSolicitud.OTRO.name
            val estadoStr = data["estado"] as? String ?: EstadoSolicitud.RECIBIDA.name
            val prioridadStr = data["prioridad"] as? String ?: PrioridadSolicitud.MEDIA.name
            val codigo = (data["codigoSeguimiento"] as? String)?.trim() ?: doc.id.trim()
            val idExtraido = (data["idLocal"] as? Number)?.toInt()
                ?: (data["id"] as? Number)?.toInt()
                ?: 0

            fun extraerEpoch(valor: Any?): Long {
                return when (valor) {
                    is Number -> valor.toLong()
                    is com.google.firebase.Timestamp -> valor.toDate().time
                    is String -> valor.toLongOrNull() ?: System.currentTimeMillis()
                    else -> System.currentTimeMillis()
                }
            }

            fun extraerEpochOpcional(valor: Any?): Long? {
                return when (valor) {
                    null -> null
                    is Number -> valor.toLong()
                    is com.google.firebase.Timestamp -> valor.toDate().time
                    is String -> valor.toLongOrNull()
                    else -> null
                }
            }

            val rawDesc = data["descripcion"] as? String ?: ""

            Solicitud(
                id = idExtraido,
                codigoSeguimiento = codigo,
                titulo = data["titulo"] as? String ?: "",
                descripcion = GsmCryptoManager.decrypt(rawDesc),
                categoria = runCatching { CategoriaSolicitud.valueOf(categoriaStr) }.getOrDefault(CategoriaSolicitud.OTRO),
                clienteId = (data["clienteId"] as? Number)?.toInt() ?: 1,
                tecnicoId = (data["tecnicoId"] as? Number)?.toInt(),
                estado = runCatching { EstadoSolicitud.valueOf(estadoStr) }.getOrDefault(EstadoSolicitud.RECIBIDA),
                prioridad = runCatching { PrioridadSolicitud.valueOf(prioridadStr) }.getOrDefault(PrioridadSolicitud.MEDIA),
                servicioRelacionado = data["servicioRelacionado"] as? String,
                activoRelacionado = data["activoRelacionado"] as? String,
                resolucion = (data["resolucion"] as? String)?.let { GsmCryptoManager.decrypt(it) },
                motivoCancelacion = (data["motivoCancelacion"] as? String)?.let { GsmCryptoManager.decrypt(it) },
                motivoReapertura = (data["motivoReapertura"] as? String)?.let { GsmCryptoManager.decrypt(it) },
                conformidadCliente = data["conformidadCliente"] as? Boolean,
                fechaLimite = extraerEpochOpcional(data["fechaLimite"]),
                fechaCreacion = extraerEpoch(data["fechaCreacion"]),
                fechaActualizacion = extraerEpoch(data["fechaActualizacion"]),
                fechaResolucion = extraerEpochOpcional(data["fechaResolucion"]),
                fechaCierre = extraerEpochOpcional(data["fechaCierre"])
            )
        }.getOrNull()
    }
}
