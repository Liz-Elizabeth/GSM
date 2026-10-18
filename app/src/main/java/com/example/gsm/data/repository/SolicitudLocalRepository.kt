package com.example.gsm.data.repository

import com.example.gsm.data.local.dao.SolicitudDao
import com.example.gsm.data.local.model.ConteoPorTecnicoDto
import com.example.gsm.data.mapper.toDomain
import com.example.gsm.data.mapper.toEntity
import com.example.gsm.model.CategoriaSolicitud
import com.example.gsm.model.EstadoSolicitud
import com.example.gsm.model.PrioridadSolicitud
import com.example.gsm.model.Solicitud
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * Sub-repositorio encargado exclusivamente de la persistencia y lectura
 * de solicitudes en la base de datos local (Room).
 */


class SolicitudLocalRepository(
    private val solicitudDao: SolicitudDao
) {
    suspend fun insertar(solicitud: Solicitud, esBorrador: Boolean = false): Long{
        return solicitudDao.insertarSolicitud(solicitud.toEntity(esBorrador))
}
    suspend fun obtenerPorId(id:Int): Solicitud?{
        return solicitudDao.obtenerSolicitudPorId(id)?.toDomain()
    }
    suspend fun obtenerPorCodigo(codigo:String): Solicitud?{
        return solicitudDao.obtenerPorCodigoSeguimiento(codigo.trim().uppercase())?.toDomain()
    }

    fun filtrar(
        estado: EstadoSolicitud? = null,
        prioridad: PrioridadSolicitud? = null,
        categoria: CategoriaSolicitud? = null,
        clienteId: Int? = null,
        tecnicoId: Int? = null
    ): Flow<List<Solicitud>>{
        return solicitudDao.filtrarSolicitudes(
            estado = estado?.name,
            prioridad = prioridad?.name,
            categoria = categoria?.name,
            clienteId = clienteId,
            tecnicoId = tecnicoId
        ).map { lista -> lista.map { it.toDomain() } }
    }

    fun obtenerAtrasadas(): Flow<List<Solicitud>> {
        val ahora = System.currentTimeMillis()
        return solicitudDao.obtenerSolicitudesAtrasadas(ahora).map { lista ->
            lista.map { it.toDomain() }
        }
    }

    fun obtenerBorradores(clienteId: Int): Flow<List<Solicitud>> {
        return solicitudDao.obtenerBorradoresPorCliente(clienteId).map { lista ->
            lista.map { it.toDomain() }
        }
    }

    // --- Métodos de actualización de estado atómico ---
    suspend fun actualizarEstado(id: Int, nuevoEstado: EstadoSolicitud, ahora: Long) {
        solicitudDao.actualizarEstado(id, nuevoEstado.name, ahora)
    }

    suspend fun clasificarYPriorizar(
        id: Int,
        categoria: CategoriaSolicitud,
        prioridad: PrioridadSolicitud,
        ahora: Long
    ) {
        val nuevaFechaLimite = calcularSlaFechaLimite(prioridad, ahora)
        solicitudDao.clasificarYPriorizar(
            solicitudId = id,
            categoria = categoria.name,
            prioridad = prioridad.name,
            nuevaFechaLimite = nuevaFechaLimite,
            ts = ahora
        )
    }

    suspend fun asignarTecnico(id: Int, tecnicoId: Int, ahora: Long) {
        solicitudDao.asignarTecnico(id, tecnicoId, ahora)
    }

    suspend fun registrarResolucion(id: Int, resolucion: String, ahora: Long) {
        solicitudDao.registrarResolucion(id, resolucion, ahora)
    }

    suspend fun confirmarCierre(id: Int, conforme: Boolean, ahora: Long) {
        solicitudDao.confirmarCierre(id, conforme, ahora)
    }

    suspend fun solicitarReapertura(id: Int, motivo: String, ahora: Long) {
        solicitudDao.solicitarReapertura(id, motivo, ahora)
    }

    suspend fun cancelar(id: Int, motivo: String, ahora: Long) {
        solicitudDao.cancelarSolicitud(id, motivo, ahora)
    }

    // --- Métricas para Panel Administrador ---
    fun contarPorEstado(estado: EstadoSolicitud): Flow<Int> =
        solicitudDao.contarPorEstado(estado.name)

    fun contarAtrasadas(): Flow<Int> =
        solicitudDao.contarAtrasadas(System.currentTimeMillis())

    fun contarPorPrioridad(prioridad: PrioridadSolicitud): Flow<Int> =
        solicitudDao.contarPorPrioridad(prioridad.name)

    fun contarPorTecnico(): Flow<List<ConteoPorTecnicoDto>> =
        solicitudDao.contarSolicitudesPorTecnico()

    // --- Funciones de utilidad interna ---
    fun generarCodigoUnico(): String {
        val anio = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val correlativo = Random.nextInt(1000, 9999)
        return "SOL-$anio-$correlativo"
    }

    fun calcularSlaFechaLimite(prioridad: PrioridadSolicitud, desdeMs: Long): Long {
        val horaMs = 3600 * 1000L
        val horas = when (prioridad) {
            PrioridadSolicitud.URGENTE -> 6L
            PrioridadSolicitud.ALTA -> 24L
            PrioridadSolicitud.MEDIA -> 72L
            PrioridadSolicitud.BAJA -> 168L
        }
        return desdeMs + (horas * horaMs)
    }


}