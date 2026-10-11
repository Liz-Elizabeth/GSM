package com.example.gsm.model

data class Solicitud(

    val id: Int,
    val titulo: String,
    val descripcion: String,
    val clienteId: Int,
    val tecnicoId: Int? = null,

    val estado: EstadoSolicitud,
    val prioridad: PrioridadSolicitud,

    val fechaCreacion: Long,
    val fechaActualizacion: Long? = null,
    val fechaResolucion: Long? = null,
    val fechaCierre: Long? = null
)




