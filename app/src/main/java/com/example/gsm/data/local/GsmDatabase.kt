
package com.example.gsm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

import com.example.gsm.data.local.dao.UsuarioDao
import com.example.gsm.data.local.dao.SolicitudDao
import com.example.gsm.data.local.dao.HistorialDao

import com.example.gsm.data.local.entity.UsuarioEntity
import com.example.gsm.data.local.entity.SolicitudEntity
import com.example.gsm.data.local.entity.HistorialEntity

@Database(
    entities = [
        UsuarioEntity::class,
        SolicitudEntity::class,
        HistorialEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GsmDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao

    abstract fun solicitudDao(): SolicitudDao

    abstract fun historialDao(): HistorialDao
}
