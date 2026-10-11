package com.example.gsm.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "historial",
    foreignKeys = [
        ForeignKey(
            entity = SolicitudEntity::class,
            parentColumns = ["id"],
            childColumns = ["solicitudId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["solicitudId"]),
        Index(value = ["usuarioId"]),
        Index(value = ["fechaCambio"])
    ]
)
data class HistorialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val solicitudId: Int,
    val usuarioId: Int,
    val estadoAnterior: String?,
    val estadoNuevo: String,
    val accion: String, // "CREACION", "ASIGNACION", "AVANCE", "RESOLUCION", "CIERRE", "REAPERTURA"
    val comentario: String,
    val perfilUsuario: String, // "CLIENTE", "ADMINISTRADOR", "TECNICO"
    val esComentarioInterno: Boolean = false, // True si es solo visible para Admin/Técnico
    val evidenciaAdjunta: String? = null,
    val fechaCambio: Long = System.currentTimeMillis()
)