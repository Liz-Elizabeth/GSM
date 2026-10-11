package com.example.gsm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gsm.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarUsuario(usuario: UsuarioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuarios(usuarios: List<UsuarioEntity>)

    @Update
    suspend fun actualizarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    fun obtenerUsuarios(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtenerUsuarioPorId(id: Int): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity?

    // Autenticación ficticia requerida por el caso de uso
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND password = :password AND activo = 1 LIMIT 1")
    suspend fun autenticar(correo: String, password: String): UsuarioEntity?

    // Para el panel administrativo al asignar técnico responsable
    @Query("SELECT * FROM usuarios WHERE rol = 'TECNICO' AND activo = 1 ORDER BY nombre ASC")
    fun obtenerTecnicosDisponibles(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE rol = :rol AND activo = 1 ORDER BY nombre ASC")
    fun obtenerUsuariosPorRol(rol: String): Flow<List<UsuarioEntity>>
}
