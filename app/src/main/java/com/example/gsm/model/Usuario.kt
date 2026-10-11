package com.example.gsm.model

data class Usuario(
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    val password: String,
    val rol: RolUsuario,
    val empresa: String? = null,
    val telefono: String? = null,
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
)
