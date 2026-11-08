package com.example.gsm.data.repository

import android.content.Context
import com.example.gsm.data.local.dao.NotificacionDao
import com.example.gsm.data.local.entity.NotificacionEntity
import com.example.gsm.util.GsmNotificationHelper
import kotlinx.coroutines.flow.Flow

class NotificacionRepository(
    private val notificacionDao: NotificacionDao,
    private val context: Context? = null
) {

    suspend fun crearNotificacion(
        usuarioDestinoId: Int,
        solicitudId: Int,
        titulo: String,
        mensaje: String,
        tipoEvento: String,
        ahora: Long = System.currentTimeMillis()
    ): Long {
        val entidad = NotificacionEntity(
            usuarioDestinoId = usuarioDestinoId,
            solicitudId = solicitudId,
            titulo = titulo,
            mensaje = mensaje,
            tipoEvento = tipoEvento,
            leida = false,
            fechaCreacion = ahora
        )
        val id = notificacionDao.insertar(entidad)

        // Disparar notificación nativa Heads-up en el sistema
        context?.let { ctx ->
            val esChat = tipoEvento.contains("CHAT", ignoreCase = true) ||
                    tipoEvento.contains("MENSAJE", ignoreCase = true)
            GsmNotificationHelper.mostrarNotificacionNativa(
                context = ctx,
                titulo = titulo,
                mensaje = mensaje,
                solicitudId = solicitudId,
                esChat = esChat
            )
        }

        return id
    }

    fun obtenerPorUsuario(usuarioId: Int): Flow<List<NotificacionEntity>> =
        notificacionDao.obtenerPorUsuario(usuarioId)

    fun contarNoLeidas(usuarioId: Int): Flow<Int> =
        notificacionDao.contarNoLeidas(usuarioId)

    suspend fun marcarComoLeida(notificacionId: Int) =
        notificacionDao.marcarComoLeida(notificacionId)

    suspend fun marcarTodasComoLeidas(usuarioId: Int) =
        notificacionDao.marcarTodasComoLeidas(usuarioId)
}
