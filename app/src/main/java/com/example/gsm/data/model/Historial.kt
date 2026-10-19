package com.example.gsm.data.model

data class Historial(
    val id: Int = 0,
    val solicitudId: Int,
    val usuarioId: Int,
    val estadoAnterior: EstadoSolicitud?,
    val estadoNuevo: EstadoSolicitud,
    val accion: String, // Ej: "Asignación", "Avance", "Reapertura"
    val comentario: String,
    val perfilUsuario: RolUsuario,
    val esComentarioInterno: Boolean = false, // Oculto para perfil CLIENTE
    val evidenciaAdjunta: String? = null,
    val fechaCambio: Long = System.currentTimeMillis()

)
