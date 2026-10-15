package com.example.gsm.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notificaciones",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioDestinoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SolicitudEntity::class,
            parentColumns = ["id"],
            childColumns = ["solicitudId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuarioDestinoId"]),
        Index(value = ["solicitudId"]),
        Index(value = ["leida"])
    ]
)
data class NotificacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioDestinoId: Int, // Destinatario (Cliente, Técnico o Admin)
    val solicitudId: Int,
    val titulo: String,
    val mensaje: String,
    val tipoEvento: String, // "NUEVA_SOLICITUD", "ASIGNACION", "ESTADO_CAMBIADO", "ATRASADA", "COMENTARIO"
    val leida: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis()
)