package com.example.gsm.data.mapper

import com.example.gsm.data.local.entity.HistorialEntity
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.Historial
import com.example.gsm.data.model.RolUsuario

/**
 * Mapper exclusivo para la Bitácora de Auditoría y Trazabilidad.
 */
fun HistorialEntity.toDomain() = Historial(
    id = id,
    solicitudId = solicitudId,
    usuarioId = usuarioId,
    estadoAnterior = estadoAnterior?.let { raw ->
        runCatching { EstadoSolicitud.valueOf(raw) }.getOrNull()
    },
    estadoNuevo = runCatching { EstadoSolicitud.valueOf(estadoNuevo) }.getOrDefault(EstadoSolicitud.RECIBIDA),
    accion = accion,
    comentario = comentario,
    perfilUsuario = runCatching { RolUsuario.valueOf(perfilUsuario) }.getOrDefault(RolUsuario.CLIENTE),
    esComentarioInterno = esComentarioInterno,
    evidenciaAdjunta = evidenciaAdjunta,
    fechaCambio = fechaCambio
)

fun Historial.toEntity() = HistorialEntity(
    id = id,
    solicitudId = solicitudId,
    usuarioId = usuarioId,
    estadoAnterior = estadoAnterior?.name,
    estadoNuevo = estadoNuevo.name,
    accion = accion,
    comentario = comentario,
    perfilUsuario = perfilUsuario.name,
    esComentarioInterno = esComentarioInterno,
    evidenciaAdjunta = evidenciaAdjunta,
    fechaCambio = fechaCambio
)
