package com.example.gsm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gsm.data.local.entity.SolicitudEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SolicitudDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarSolicitud(solicitud: SolicitudEntity): Long

    @Update
    suspend fun actualizarSolicitud(solicitud: SolicitudEntity)

    // ==========================================
    // CONSULTAS GENERALES Y POR ID/CÓDIGO
    // ==========================================

    @Query("SELECT * FROM solicitudes ORDER BY fechaActualizacion DESC")
    fun obtenerSolicitudes(): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    suspend fun obtenerSolicitudPorId(id: Int): SolicitudEntity?

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    fun observarSolicitudPorId(id: Int): Flow<SolicitudEntity?>

    // Requerido para derivación y seguimiento mediante WhatsApp
    @Query("SELECT * FROM solicitudes WHERE codigoSeguimiento = :codigo LIMIT 1")
    suspend fun obtenerPorCodigoSeguimiento(codigo: String): SolicitudEntity?

    // ==========================================
    // CONSULTAS POR PERFIL
    // ==========================================

    @Query("SELECT * FROM solicitudes WHERE clienteId = :clienteId ORDER BY fechaActualizacion DESC")
    fun obtenerSolicitudesPorCliente(clienteId: Int): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitudes WHERE tecnicoId = :tecnicoId ORDER BY fechaActualizacion DESC")
    fun obtenerSolicitudesPorTecnico(tecnicoId: Int): Flow<List<SolicitudEntity>>

    // ==========================================
    // BANDEJAS ADMINISTRATIVAS Y TÉCNICAS (CRITERIOS 12, 22, 23)
    // ==========================================

    // Solicitudes nuevas recibidas
    @Query("SELECT * FROM solicitudes WHERE estado = 'RECIBIDA' ORDER BY fechaCreacion DESC")
    fun obtenerSolicitudesNuevas(): Flow<List<SolicitudEntity>>

    // Solicitudes pendientes de información adicional del cliente
    @Query("SELECT * FROM solicitudes WHERE estado = 'PENDIENTE_INFORMACION' ORDER BY fechaActualizacion DESC")
    fun obtenerPendientesInformacion(): Flow<List<SolicitudEntity>>

    // Solicitudes atrasadas (fecha límite vencida y no resuelta/cerrada/cancelada)
    @Query("""
            SELECT * FROM solicitudes
            WHERE fechaLimite IS NOT NULL
              AND fechaLimite < :timestampActual
              AND estado NOT IN ('RESUELTA', 'CERRADA', 'CANCELADA')
            ORDER BY fechaLimite ASC
        """)
    fun obtenerSolicitudesAtrasadas(timestampActual: Long): Flow<List<SolicitudEntity>>

    // ==========================================
    // FILTROS AVANZADOS (CRITERIOS 24, 25, 26, 27)
    // ==========================================

    @Query("""
            SELECT * FROM solicitudes
            WHERE (:estado IS NULL OR estado = :estado)
              AND (:prioridad IS NULL OR prioridad = :prioridad)
              AND (:clienteId IS NULL OR clienteId = :clienteId)
              AND (:tecnicoId IS NULL OR tecnicoId = :tecnicoId)
              AND (:categoria IS NULL OR categoria = :categoria)
            ORDER BY fechaActualizacion DESC
        """)
    fun filtrarSolicitudes(
        estado: String? = null,
        prioridad: String? = null,
        clienteId: Int? = null,
        tecnicoId: Int? = null,
        categoria: String? = null
    ): Flow<List<SolicitudEntity>>

    // ==========================================
    // MÉTRICAS Y RESUMEN ADMINISTRATIVO (CRITERIOS 29 Y 30)
    // ==========================================

    @Query("SELECT COUNT(*) FROM solicitudes WHERE estado = :estado")
    fun contarPorEstado(estado: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM solicitudes WHERE prioridad = :prioridad")
    fun contarPorPrioridad(prioridad: String): Flow<Int>

    @Query("""
            SELECT COUNT(*) FROM solicitudes
            WHERE fechaLimite IS NOT NULL
              AND fechaLimite < :timestampActual
              AND estado NOT IN ('RESUELTA', 'CERRADA', 'CANCELADA')
        """)
    fun contarAtrasadas(timestampActual: Long): Flow<Int>
}
