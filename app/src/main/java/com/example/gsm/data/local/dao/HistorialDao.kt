package com.example.gsm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gsm.data.local.entity.HistorialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialDao {

    @Insert
    suspend fun insertarHistorial(historial: HistorialEntity): Long

    // Auditoría completa para Administrador y Técnico (cronológico)
    @Query("SELECT * FROM historial WHERE solicitudId = :solicitudId ORDER BY fechaCambio ASC")
    fun obtenerHistorialCompleto(solicitudId: Int): Flow<List<HistorialEntity>>

    // Vista de cliente: filtra comentarios internos (regla de negocio Sección 3.1)
    @Query(
        """
        SELECT * FROM historial
        WHERE solicitudId = :solicitudId
          AND esComentarioInterno = 0
        ORDER BY fechaCambio ASC
        """
    )
    fun obtenerHistorialVisibleCliente(solicitudId: Int): Flow<List<HistorialEntity>>

    // Historial de acciones del técnico (PDF Pág. 7)
    @Query("SELECT * FROM historial WHERE usuarioId = :usuarioId ORDER BY fechaCambio DESC")
    fun obtenerHistorialPorUsuario(usuarioId: Int): Flow<List<HistorialEntity>>

    // Última actualización para la vista resumen del cliente (Criterio 10)
    @Query("SELECT * FROM historial WHERE solicitudId = :solicitudId ORDER BY fechaCambio DESC LIMIT 1")
    suspend fun obtenerUltimaActualizacion(solicitudId: Int): HistorialEntity?

    @Query("SELECT * FROM historial ORDER BY fechaCambio DESC")
    fun obtenerTodoHistorial(): Flow<List<HistorialEntity>>
}