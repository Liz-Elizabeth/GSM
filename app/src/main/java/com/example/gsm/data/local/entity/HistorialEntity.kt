
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
            childColumns = ["solicitudId"]
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"]
        )
    ],
    indices = [
        Index(value = ["solicitudId"]),
        Index(value = ["usuarioId"])
    ]
)
data class HistorialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val solicitudId: Int,
    val usuarioId: Int,
    val estadoAnterior: String?,
    val estadoNuevo: String,
    val comentario: String,
    val fechaCambio: Long
)
