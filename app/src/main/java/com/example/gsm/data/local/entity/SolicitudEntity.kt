
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
            childColumns = ["clienteId"]
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["tecnicoId"]
        )
    ],
    indices = [
        Index(value = ["clienteId"]),
        Index(value = ["tecnicoId"])
    ]
)
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val clienteId: Int,
    val tecnicoId: Int? = null,
    val estado: String,
    val prioridad: String,
    val fechaCreacion: Long,
    val fechaActualizacion: Long,
    val fechaResolucion: Long? = null,
    val fechaCierre: Long? = null
)
