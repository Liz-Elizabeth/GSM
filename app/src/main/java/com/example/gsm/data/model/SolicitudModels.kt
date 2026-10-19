package com.example.gsm.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gsm.data.local.entity.EvidenciaEntity
import com.example.gsm.data.local.entity.HistorialEntity
import com.example.gsm.data.local.entity.SolicitudEntity
import com.example.gsm.data.local.entity.UsuarioEntity

/**
 * DTO para la métrica administrativa "Solicitudes por responsable" (PDF Pág. 11).
 */
data class ConteoPorTecnicoDto(
    val tecnicoId: Int?,
    val nombreTecnico: String?,
    val totalSolicitudes: Int
)

/**
 * Estructura agregada que vincula una solicitud con su cliente y técnico asignado
 * para presentar en tarjetas de lista sin hacer múltiples queries en Compose.
 */
data class SolicitudConParticipantesDto(
    @Embedded val solicitud: SolicitudEntity,
    @Relation(parentColumn = "clienteId", entityColumn = "id")
    val cliente: UsuarioEntity,
    @Relation(parentColumn = "tecnicoId", entityColumn = "id")
    val tecnico: UsuarioEntity?
)

/**
 * Estructura completa de auditoría y detalle para la vista de ficha de solicitud.
 */
data class SolicitudDetalleCompletoDto(
    @Embedded val solicitud: SolicitudEntity,
    @Relation(parentColumn = "clienteId", entityColumn = "id")
    val cliente: UsuarioEntity,
    @Relation(parentColumn = "tecnicoId", entityColumn = "id")
    val tecnico: UsuarioEntity?,
    @Relation(parentColumn = "id", entityColumn = "solicitudId")
    val historial: List<HistorialEntity> = emptyList(),
    @Relation(parentColumn = "id", entityColumn = "solicitudId")
    val evidencias: List<EvidenciaEntity> = emptyList()
)
