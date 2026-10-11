package com.example.gsm.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey






@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    val rol: String






)
