package com.example.gsm.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "solicitudes",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["tecnicoId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["clienteId"]),
        Index(value = ["tecnicoId"]),
        Index(value = ["estado", "esBorrador"]),
        Index(value = ["prioridad"]),
        Index(value = ["codigoSeguimiento"], unique = true) // Búsqueda indexada vía WhatsApp
    ]
)
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val codigoSeguimiento: String, // Identificador visible para cliente y WhatsApp (ej: SOL-2026-001)
    val titulo: String,
    val descripcion: String,
    val categoria: String, // DESARROLLO_WEB, HOSTING, INCIDENTE_TECNICO, etc.
    val clienteId: Int,
    val tecnicoId: Int? = null,
    val estado: String = "RECIBIDA",
    val prioridad: String = "MEDIA",
    val servicioRelacionado: String? = null,
    val activoRelacionado: String? = null,
    val resolucion: String? = null, // Solución técnica ingresada por el ejecutor
    val motivoCancelacion: String? = null,
    val motivoReapertura: String? = null,
    val conformidadCliente: Boolean? = null,
    val fechaLimite: Long? = null, // SLA / fecha máxima para cálculo de atrasos
    val esBorrador: Boolean = false, // True si aún no se envía al administrador
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis(),
    val fechaResolucion: Long? = null,
    val fechaCierre: Long? = null
)