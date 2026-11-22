package com.example.gsm.fakes

import com.example.gsm.data.local.dao.SolicitudDao
import com.example.gsm.data.local.entity.SolicitudEntity
import com.example.gsm.data.local.model.ConteoPorTecnicoDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake en memoria para SolicitudDao, simulando operaciones de tickets de soporte.
 */
class FakeSolicitudDao : SolicitudDao {

    val solicitudes = mutableListOf<SolicitudEntity>()
    private val _solicitudesFlow = MutableStateFlow<List<SolicitudEntity>>(emptyList())
    private var nextId = 1

    private fun notificarCambio() {
        _solicitudesFlow.value = solicitudes.toList()
    }

    override suspend fun insertarSolicitud(solicitud: SolicitudEntity): Long {
        val idAsignado = if (solicitud.id > 0) solicitud.id else nextId++
        val nueva = solicitud.copy(id = idAsignado)
        solicitudes.add(nueva)
        notificarCambio()
        return idAsignado.toLong()
    }

    override suspend fun actualizarSolicitud(solicitud: SolicitudEntity) {
        val index = solicitudes.indexOfFirst { it.id == solicitud.id }
        if (index != -1) {
            solicitudes[index] = solicitud
            notificarCambio()
        }
    }

    override suspend fun obtenerSolicitudPorId(id: Int): SolicitudEntity? =
        solicitudes.firstOrNull { it.id == id }

    override fun observarSolicitudPorId(id: Int): Flow<SolicitudEntity?> =
        _solicitudesFlow.asStateFlow().map { lista -> lista.firstOrNull { it.id == id } }

    override suspend fun obtenerPorCodigoSeguimiento(codigo: String): SolicitudEntity? =
        solicitudes.firstOrNull { it.codigoSeguimiento.trim().equals(codigo.trim(), ignoreCase = true) }

    override suspend fun actualizarInformacion(
        solicitudId: Int,
        titulo: String,
        descripcion: String,
        categoria: String,
        servicio: String?,
        activo: String?,
        ts: Long
    ) {
        val index = solicitudes.indexOfFirst { it.id == solicitudId }
        if (index != -1) {
            solicitudes[index] = solicitudes[index].copy(
                titulo = titulo,
                descripcion = descripcion,
                categoria = categoria,
                servicioRelacionado = servicio,
                activoRelacionado = activo,
                fechaActualizacion = ts
            )
            notificarCambio()
        }
    }

    override fun obtenerBorradoresPorCliente(clienteId: Int): Flow<List<SolicitudEntity>> =
        _solicitudesFlow.asStateFlow().map { lista ->
            lista.filter { it.clienteId == clienteId && it.esBorrador }
                .sortedByDescending { it.fechaActualizacion }
        }

    override fun filtrarSolicitudes(
        estado: String?,
        prioridad: String?,
        clienteId: Int?,
        tecnicoId: Int?,
        categoria: String?
    ): Flow<List<SolicitudEntity>> =
        _solicitudesFlow.asStateFlow().map { lista ->
            lista.filter { s ->
                !s.esBorrador &&
                (estado == null || s.estado == estado) &&
                (prioridad == null || s.prioridad == prioridad) &&
                (clienteId == null || s.clienteId == clienteId) &&
                (tecnicoId == null || s.tecnicoId == tecnicoId) &&
                (categoria == null || s.categoria == categoria)
            }.sortedByDescending { it.fechaActualizacion }
        }

    override fun obtenerSolicitudesAtrasadas(ahora: Long): Flow<List<SolicitudEntity>> =
        _solicitudesFlow.asStateFlow().map { lista ->
            lista.filter { s ->
                !s.esBorrador &&
                s.fechaLimite != null &&
                s.fechaLimite!! < ahora &&
                s.estado !in listOf("RESUELTA", "CERRADA", "CANCELADA")
            }.sortedBy { it.fechaLimite }
        }

    override fun contarPorEstado(estado: String): Flow<Int> =
        _solicitudesFlow.asStateFlow().map { lista ->
            lista.count { it.estado == estado && !it.esBorrador }
        }

    override fun contarPorPrioridad(prioridad: String): Flow<Int> =
        _solicitudesFlow.asStateFlow().map { lista ->
            lista.count { it.prioridad == prioridad && !it.esBorrador }
        }

    override fun contarAtrasadas(ahora: Long): Flow<Int> =
        _solicitudesFlow.asStateFlow().map { lista ->
            lista.count { s ->
                !s.esBorrador &&
                s.fechaLimite != null &&
                s.fechaLimite!! < ahora &&
                s.estado !in listOf("RESUELTA", "CERRADA", "CANCELADA")
            }
        }

    override fun contarSolicitudesPorTecnico(): Flow<List<ConteoPorTecnicoDto>> =
        _solicitudesFlow.asStateFlow().map { emptyList() }

    override suspend fun clasificarYPriorizar(
        solicitudId: Int,
        categoria: String,
        prioridad: String,
        nuevaFechaLimite: Long,
        ts: Long
    ) {
        val index = solicitudes.indexOfFirst { it.id == solicitudId }
        if (index != -1) {
            solicitudes[index] = solicitudes[index].copy(
                categoria = categoria,
                prioridad = prioridad,
                fechaLimite = nuevaFechaLimite,
                estado = "EN_REVISION",
                fechaActualizacion = ts
            )
            notificarCambio()
        }
    }

    override suspend fun asignarTecnico(solicitudId: Int, tecnicoId: Int, ts: Long) {
        val index = solicitudes.indexOfFirst { it.id == solicitudId }
        if (index != -1) {
            solicitudes[index] = solicitudes[index].copy(
                tecnicoId = tecnicoId,
                estado = "ASIGNADA",
                fechaActualizacion = ts
            )
            notificarCambio()
        }
    }

    override suspend fun desasignarTecnico(solicitudId: Int, ts: Long) {
        val index = solicitudes.indexOfFirst { it.id == solicitudId }
        if (index != -1) {
            solicitudes[index] = solicitudes[index].copy(
                tecnicoId = null,
                estado = "EN_REVISION",
                fechaActualizacion = ts
            )
            notificarCambio()
        }
    }

    override suspend fun actualizarEstado(solicitudId: Int, nuevoEstado: String, ts: Long) {
        val index = solicitudes.indexOfFirst { it.id == solicitudId }
        if (index != -1) {
            solicitudes[index] = solicitudes[index].copy(
                estado = nuevoEstado,
                fechaActualizacion = ts
            )
            notificarCambio()
        }
    }

    override suspend fun registrarResolucion(solicitudId: Int, resolucion: String, ts: Long) {
        val index = solicitudes.indexOfFirst { it.id == solicitudId }
        if (index != -1) {
            solicitudes[index] = solicitudes[index].copy(
                resolucion = resolucion,
                estado = "RESUELTA",
                fechaResolucion = ts,
                fechaActualizacion = ts
            )
            notificarCambio()
        }
    }

    override suspend fun confirmarCierre(solicitudId: Int, conforme: Boolean, calificacion: Int?, ts: Long) {
        val index = solicitudes.indexOfFirst { it.id == solicitudId }
        if (index != -1) {
            solicitudes[index] = solicitudes[index].copy(
                conformidadCliente = conforme,
                calificacionCliente = calificacion,
                estado = "CERRADA",
                fechaCierre = ts,
                fechaActualizacion = ts
            )
            notificarCambio()
        }
    }

    override fun promedioCalificacion(): Flow<Double?> =
        _solicitudesFlow.asStateFlow().map { lista ->
            val calificaciones = lista.mapNotNull { it.calificacionCliente }
            if (calificaciones.isEmpty()) null else calificaciones.average()
        }

    override suspend fun solicitarReapertura(solicitudId: Int, motivo: String, ts: Long) {
        val index = solicitudes.indexOfFirst { it.id == solicitudId }
        if (index != -1) {
            solicitudes[index] = solicitudes[index].copy(
                motivoReapertura = motivo,
                estado = "EN_REVISION",
                fechaActualizacion = ts
            )
            notificarCambio()
        }
    }

    override suspend fun cancelarSolicitud(solicitudId: Int, motivo: String, ts: Long) {
        val index = solicitudes.indexOfFirst { it.id == solicitudId }
        if (index != -1) {
            solicitudes[index] = solicitudes[index].copy(
                motivoCancelacion = motivo,
                estado = "CANCELADA",
                fechaActualizacion = ts
            )
            notificarCambio()
        }
    }
}
