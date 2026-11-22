package com.example.gsm.data.local.model

/**
 * DTO para la métrica administrativa "Solicitudes por responsable" (PDF Pág. 11).
 */
data class ConteoPorTecnicoDto(
    val tecnicoId: Int?,
    val nombreTecnico: String?,
    val totalSolicitudes: Int
)
