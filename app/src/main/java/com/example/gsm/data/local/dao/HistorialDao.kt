
package com.example.gsm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gsm.data.local.entity.HistorialEntity

@Dao
interface HistorialDao {

    @Insert
    suspend fun insertarHistorial(historial: HistorialEntity)

    @Query("SELECT * FROM historial ORDER BY fechaCambio DESC")
    suspend fun obtenerTodoHistorial(): List<HistorialEntity>

    @Query(
        "SELECT * FROM historial WHERE solicitudId = :solicitudId ORDER BY fechaCambio DESC"
    )
    suspend fun obtenerHistorialPorSolicitud(
        solicitudId: Int
    ): List<HistorialEntity>
}
