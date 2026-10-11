
package com.example.gsm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gsm.data.local.entity.SolicitudEntity

@Dao
interface SolicitudDao {

    @Insert
    suspend fun insertarSolicitud(solicitud: SolicitudEntity)

    @Update
    suspend fun actualizarSolicitud(solicitud: SolicitudEntity)

    @Query("SELECT * FROM solicitudes")
    suspend fun obtenerSolicitudes(): List<SolicitudEntity>

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    suspend fun obtenerSolicitudPorId(id: Int): SolicitudEntity?

    @Query("SELECT * FROM solicitudes WHERE clienteId = :clienteId")
    suspend fun obtenerSolicitudesPorCliente(
        clienteId: Int
    ): List<SolicitudEntity>

    @Query("SELECT * FROM solicitudes WHERE tecnicoId = :tecnicoId")
    suspend fun obtenerSolicitudesPorTecnico(
        tecnicoId: Int
    ): List<SolicitudEntity>
}
