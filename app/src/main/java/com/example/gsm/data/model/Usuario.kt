package com.example.gsm.data.model

data class Usuario(
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    val password: String,
    val rol: RolUsuario,
    val empresa: String? = null,
    val telefono: String? = null,
    val medioContacto: String? = "WhatsApp",
    val serviciosAsociados: String? = null,
    val plan: String = "PRO",
    val ticketsDisponibles: Int = 25,
    val estadoSuscripcion: String = "ACTIVA",
    val activo: Boolean = true,
    val debeCambiarPassword: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
)
