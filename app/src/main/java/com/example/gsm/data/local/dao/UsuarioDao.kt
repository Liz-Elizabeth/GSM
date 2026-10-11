
package com.example.gsm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gsm.data.local.entity.UsuarioEntity

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios")
    suspend fun obtenerUsuarios(): List<UsuarioEntity>

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtenerUsuarioPorId(id: Int): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE correo = :correo")
    suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity?
}
