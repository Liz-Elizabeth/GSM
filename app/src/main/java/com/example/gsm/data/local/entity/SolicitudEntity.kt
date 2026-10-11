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
        Index(value = ["estado"]),
        Index(value = ["prioridad"]),
        Index(value = ["codigoSeguimiento"], unique = true) // Búsqueda rápida por WhatsApp
    ]
)
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val codigoSeguimiento: String, // Código alfanumérico visible para el cliente
    val titulo: String,
    val descripcion: String,
    val categoria: String, // DESARROLLO_WEB, HOSTING, etc.
    val clienteId: Int,
    val tecnicoId: Int? = null,
    val estado: String = "RECIBIDA",
    val prioridad: String = "MEDIA",
    val servicioRelacionado: String? = null,
    val activoRelacionado: String? = null,
    val resolucion: String? = null, // Descripción de solución técnica
    val motivoCancelacion: String? = null,
    val motivoReapertura: String? = null,
    val conformidadCliente: Boolean? = null,
    val fechaLimite: Long? = null, // Timestamp límite para detectar atrasos
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis(),
    val fechaResolucion: Long? = null,
    val fechaCierre: Long? = null
)
