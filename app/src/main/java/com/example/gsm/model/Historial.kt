package com.example.gsm.model

data class Historial(

    val id: Int,
    val solicitudId: Int,
    val usuarioId: Int,
    val estadoAnterior: EstadoSolicitud?,
    val estadoNuevo: EstadoSolicitud,
    val comentario: String,
    val fechaCambio: Long


)
