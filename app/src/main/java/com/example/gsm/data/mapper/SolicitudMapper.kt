package com.example.gsm.data.mapper

import com.example.gsm.data.local.entity.SolicitudEntity
import com.example.gsm.data.model.CategoriaSolicitud
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.Solicitud

/**
 * Mapper exclusivo para el ciclo de vida de Solicitudes.
 */
fun SolicitudEntity.toDomain() = Solicitud(
    id = id,
    codigoSeguimiento = codigoSeguimiento,
    titulo = titulo,
    descripcion = descripcion,
    categoria = runCatching { CategoriaSolicitud.valueOf(categoria) }.getOrDefault(CategoriaSolicitud.OTRO),
    clienteId = clienteId,
    tecnicoId = tecnicoId,
    estado = runCatching { EstadoSolicitud.valueOf(estado) }.getOrDefault(EstadoSolicitud.RECIBIDA),
    prioridad = runCatching { PrioridadSolicitud.valueOf(prioridad) }.getOrDefault(PrioridadSolicitud.MEDIA),
    servicioRelacionado = servicioRelacionado,
    activoRelacionado = activoRelacionado,
    resolucion = resolucion,
    motivoCancelacion = motivoCancelacion,
    motivoReapertura = motivoReapertura,
    conformidadCliente = conformidadCliente,
    fechaLimite = fechaLimite,
    fechaCreacion = fechaCreacion,
    fechaActualizacion = fechaActualizacion,
    fechaResolucion = fechaResolucion,
    fechaCierre = fechaCierre
)

fun Solicitud.toEntity(esBorrador: Boolean = false) = SolicitudEntity(
    id = id,
    codigoSeguimiento = codigoSeguimiento,
    titulo = titulo,
    descripcion = descripcion,
    categoria = categoria.name,
    clienteId = clienteId,
    tecnicoId = tecnicoId,
    estado = estado.name,
    prioridad = prioridad.name,
    servicioRelacionado = servicioRelacionado,
    activoRelacionado = activoRelacionado,
    resolucion = resolucion,
    motivoCancelacion = motivoCancelacion,
    motivoReapertura = motivoReapertura,
    conformidadCliente = conformidadCliente,
    fechaLimite = fechaLimite,
    esBorrador = esBorrador,
    fechaCreacion = fechaCreacion,
    fechaActualizacion = fechaActualizacion,
    fechaResolucion = fechaResolucion,
    fechaCierre = fechaCierre
)


