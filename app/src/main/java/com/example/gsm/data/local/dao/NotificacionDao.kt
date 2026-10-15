package com.example.gsm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gsm.data.local.entity.NotificacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificacionDao {

    @Insert
    suspend fun insertar(notificacion: NotificacionEntity): Long

    @Query("SELECT * FROM notificaciones WHERE usuarioDestinoId = :usuarioId ORDER BY fechaCreacion DESC")
    fun obtenerPorUsuario(usuarioId: Int): Flow<List<NotificacionEntity>>

    @Query("SELECT COUNT(*) FROM notificaciones WHERE usuarioDestinoId = :usuarioId AND leida = 0")
    fun contarNoLeidas(usuarioId: Int): Flow<Int>

    // Si se pasa id marca una; para marcar todas de un usuario usa marcarTodasComoLeidas
    @Query("UPDATE notificaciones SET leida = 1 WHERE id = :id")
    suspend fun marcarComoLeida(id: Int)

    @Query("UPDATE notificaciones SET leida = 1 WHERE usuarioDestinoId = :usuarioId")
    suspend fun marcarTodasComoLeidas(usuarioId: Int)
}