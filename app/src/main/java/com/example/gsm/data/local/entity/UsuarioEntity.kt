package com.example.gsm.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["correo"], unique = true)
    ]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    val password: String, // Simulación de login académico por perfiles
    val rol: String, // CLIENTE, ADMINISTRADOR, TECNICO
    val empresa: String? = null,
    val telefono: String? = null,
    // --- CAMPOS NUEVOS EXIGIDOS POR PDF PÁG. 8 ---
    val medioContacto: String? = "WhatsApp", // Canal de comunicación preferente de prueba
    val serviciosAsociados: String? = null, // Ej: "Hosting, Mantenimiento Web, Soporte Técnico"
    // ----------------------------------------------
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
)