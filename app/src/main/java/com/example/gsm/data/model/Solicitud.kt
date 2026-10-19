package com.example.gsm.data.model

data class Solicitud(
    val id: Int = 0,
    val codigoSeguimiento: String, // Ej: "SOL-2026-0001" para WhatsApp
    val titulo: String,
    val descripcion: String,
    val categoria: CategoriaSolicitud,
    val clienteId: Int,
    val tecnicoId: Int? = null,
    val estado: EstadoSolicitud = EstadoSolicitud.RECIBIDA,
    val prioridad: PrioridadSolicitud = PrioridadSolicitud.MEDIA,
    val servicioRelacionado: String? = null,
    val activoRelacionado: String? = null, // URL de sitio o servidor
    val resolucion: String? = null,
    val motivoCancelacion: String? = null,
    val motivoReapertura: String? = null,
    val conformidadCliente: Boolean? = null,
    val fechaLimite: Long? = null, // Para cálculo de solicitudes atrasadas
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis(),
    val fechaResolucion: Long? = null,
    val fechaCierre: Long? = null

)




