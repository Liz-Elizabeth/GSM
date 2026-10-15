package com.example.gsm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.gsm.data.local.entity.SolicitudEntity
import com.example.gsm.data.local.model.ConteoPorTecnicoDto
import com.example.gsm.data.local.model.SolicitudConParticipantesDto
import com.example.gsm.data.local.model.SolicitudDetalleCompletoDto
import kotlinx.coroutines.flow.Flow

@Dao
interface SolicitudDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarSolicitud(solicitud: SolicitudEntity): Long

    @Update
    suspend fun actualizarSolicitud(solicitud: SolicitudEntity)

    // ---------- Consultas por id / código ----------

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    suspend fun obtenerSolicitudPorId(id: Int): SolicitudEntity?

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    fun observarSolicitudPorId(id: Int): Flow<SolicitudEntity?>

    // Seguimiento vía WhatsApp (PDF Pág. 13)
    @Query("SELECT * FROM solicitudes WHERE codigoSeguimiento = :codigo LIMIT 1")
    suspend fun obtenerPorCodigoSeguimiento(codigo: String): SolicitudEntity?

    // Borradores del cliente (PDF Pág. 6)
    @Query("SELECT * FROM solicitudes WHERE clienteId = :clienteId AND esBorrador = 1 ORDER BY fechaActualizacion DESC")
    fun obtenerBorradoresPorCliente(clienteId: Int): Flow<List<SolicitudEntity>>

    // ---------- Listados y filtros (cubre: todas, por cliente, por técnico, nuevas, pendientes) ----------
    // Todos los parámetros son opcionales: null = no filtra.

    @Query(
        """
        SELECT * FROM solicitudes
        WHERE esBorrador = 0
          AND (:estado IS NULL OR estado = :estado)
          AND (:prioridad IS NULL OR prioridad = :prioridad)
          AND (:clienteId IS NULL OR clienteId = :clienteId)
          AND (:tecnicoId IS NULL OR tecnicoId = :tecnicoId)
          AND (:categoria IS NULL OR categoria = :categoria)
        ORDER BY fechaActualizacion DESC
        """
    )
    fun filtrarSolicitudes(
        estado: String? = null,
        prioridad: String? = null,
        clienteId: Int? = null,
        tecnicoId: Int? = null,
        categoria: String? = null
    ): Flow<List<SolicitudEntity>>

    // Atrasadas: fecha límite vencida y no finalizadas
    @Query(
        """
        SELECT * FROM solicitudes
        WHERE esBorrador = 0
          AND fechaLimite IS NOT NULL
          AND fechaLimite < :ahora
          AND estado NOT IN ('RESUELTA', 'CERRADA', 'CANCELADA')
        ORDER BY fechaLimite ASC
        """
    )
    fun obtenerSolicitudesAtrasadas(ahora: Long): Flow<List<SolicitudEntity>>

    // ---------- Métricas ----------

    @Query("SELECT COUNT(*) FROM solicitudes WHERE estado = :estado AND esBorrador = 0")
    fun contarPorEstado(estado: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM solicitudes WHERE prioridad = :prioridad AND esBorrador = 0")
    fun contarPorPrioridad(prioridad: String): Flow<Int>

    @Query(
        """
        SELECT COUNT(*) FROM solicitudes
        WHERE esBorrador = 0
          AND fechaLimite IS NOT NULL
          AND fechaLimite < :ahora
          AND estado NOT IN ('RESUELTA', 'CERRADA', 'CANCELADA')
        """
    )
    fun contarAtrasadas(ahora: Long): Flow<Int>

    // "Solicitudes por responsable" (PDF Pág. 11)
    @Query(
        """
        SELECT s.tecnicoId AS tecnicoId, u.nombre AS nombreTecnico, COUNT(s.id) AS totalSolicitudes
        FROM solicitudes s
        LEFT JOIN usuarios u ON s.tecnicoId = u.id
        WHERE s.tecnicoId IS NOT NULL AND s.esBorrador = 0
        GROUP BY s.tecnicoId
        """
    )
    fun contarSolicitudesPorTecnico(): Flow<List<ConteoPorTecnicoDto>>

    // ---------- Cambios de estado ----------

    @Query("UPDATE solicitudes SET tecnicoId = :tecnicoId, estado = 'ASIGNADA', fechaActualizacion = :ts WHERE id = :solicitudId")
    suspend fun asignarTecnico(solicitudId: Int, tecnicoId: Int, ts: Long = System.currentTimeMillis())

    @Query("UPDATE solicitudes SET estado = :nuevoEstado, fechaActualizacion = :ts WHERE id = :solicitudId")
    suspend fun actualizarEstado(solicitudId: Int, nuevoEstado: String, ts: Long = System.currentTimeMillis())

    @Query("UPDATE solicitudes SET resolucion = :resolucion, estado = 'RESUELTA', fechaResolucion = :ts, fechaActualizacion = :ts WHERE id = :solicitudId")
    suspend fun registrarResolucion(solicitudId: Int, resolucion: String, ts: Long = System.currentTimeMillis())

    @Query("UPDATE solicitudes SET conformidadCliente = :conforme, estado = 'CERRADA', fechaCierre = :ts, fechaActualizacion = :ts WHERE id = :solicitudId")
    suspend fun confirmarCierre(solicitudId: Int, conforme: Boolean, ts: Long = System.currentTimeMillis())

    @Query("UPDATE solicitudes SET motivoReapertura = :motivo, estado = 'EN_REVISION', fechaActualizacion = :ts WHERE id = :solicitudId")
    suspend fun solicitarReapertura(solicitudId: Int, motivo: String, ts: Long = System.currentTimeMillis())

    @Query("UPDATE solicitudes SET motivoCancelacion = :motivo, estado = 'CANCELADA', fechaActualizacion = :ts WHERE id = :solicitudId")
    suspend fun cancelarSolicitud(solicitudId: Int, motivo: String, ts: Long = System.currentTimeMillis())

    // ---------- Relaciones para Compose ----------

    @Transaction
    @Query("SELECT * FROM solicitudes WHERE esBorrador = 0 ORDER BY fechaActualizacion DESC")
    fun observarSolicitudesConParticipantes(): Flow<List<SolicitudConParticipantesDto>>

    @Transaction
    @Query("SELECT * FROM solicitudes WHERE id = :id")
    fun observarSolicitudDetalleCompleto(id: Int): Flow<SolicitudDetalleCompletoDto?>
}