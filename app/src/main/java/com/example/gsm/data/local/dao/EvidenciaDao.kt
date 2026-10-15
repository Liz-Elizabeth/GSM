package com.example.gsm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.gsm.data.local.entity.EvidenciaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenciaDao {

    @Insert
    suspend fun insertarEvidencia(evidencia: EvidenciaEntity): Long

    @Query("SELECT * FROM evidencias WHERE solicitudId = :solicitudId ORDER BY fechaIncorporacion DESC")
    fun obtenerEvidenciasPorSolicitud(solicitudId: Int): Flow<List<EvidenciaEntity>>

    @Query("SELECT * FROM evidencias WHERE id = :id")
    suspend fun obtenerEvidenciaPorId(id: Int): EvidenciaEntity?

    // Actualiza el estado de revisión de la evidencia (PDF Pág. 11)
    @Query("UPDATE evidencias SET estadoRevision = :nuevoEstado WHERE id = :id")
    suspend fun actualizarEstadoRevision(id: Int, nuevoEstado: String)

    @Delete
    suspend fun eliminarEvidencia(evidencia: EvidenciaEntity)
}