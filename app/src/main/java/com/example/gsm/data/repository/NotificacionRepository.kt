package com.example.gsm.data.repository

import com.example.gsm.data.local.dao.NotificacionDao
import com.example.gsm.data.local.entity.NotificacionEntity
import kotlinx.coroutines.flow.Flow

class NotificacionRepository(
    private val notificacionDao: NotificacionDao
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
        return notificacionDao.insertar(entidad)
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