package com.example.gsm.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "evidencias",
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
        Index(value = ["estadoRevision"])
    ]
)
data class EvidenciaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val solicitudId: Int,
    val usuarioId: Int,
    val nombreArchivo: String,
    val tipoArchivo: String, // "image/jpeg", "application/pdf", etc.
    val tamanoBytes: Long,
    val uriLocal: String,
    val descripcion: String? = null,
    // --- CAMPO NUEVO OBLIGATORIO SEGÚN PDF PÁG. 11 ---
    val estadoRevision: String = "PENDIENTE", // "PENDIENTE", "APROBADA", "RECHAZADA"
    // -------------------------------------------------
    val fechaIncorporacion: Long = System.currentTimeMillis()
)
