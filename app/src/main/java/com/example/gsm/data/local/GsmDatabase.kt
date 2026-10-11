package com.example.gsm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gsm.data.local.dao.EvidenciaDao
import com.example.gsm.data.local.dao.HistorialDao
import com.example.gsm.data.local.dao.SolicitudDao
import com.example.gsm.data.local.dao.UsuarioDao
import com.example.gsm.data.local.entity.EvidenciaEntity
import com.example.gsm.data.local.entity.HistorialEntity
import com.example.gsm.data.local.entity.SolicitudEntity
import com.example.gsm.data.local.entity.UsuarioEntity

@Database(
    entities = [
        UsuarioEntity::class,
        SolicitudEntity::class,
        HistorialEntity::class,
        EvidenciaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GsmDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun solicitudDao(): SolicitudDao
    abstract fun historialDao(): HistorialDao
    abstract fun evidenciaDao(): EvidenciaDao
}
