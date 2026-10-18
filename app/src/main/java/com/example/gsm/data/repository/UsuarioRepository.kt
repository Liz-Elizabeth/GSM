package com.example.gsm.data.repository

import com.example.gsm.data.local.dao.UsuarioDao
import com.example.gsm.data.mapper.toDomain
import com.example.gsm.model.RolUsuario
import com.example.gsm.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Sub-repositorio encargado de la autenticación ficticia y consulta de perfiles
 * (Cliente, Administrador, Técnico).
 */
class UsuarioRepository(
    private val usuarioDao: UsuarioDao
) {

    /**
     * Autentica un usuario validando que los campos no estén vacíos (OWASP Input Validation).
     */
    suspend fun autenticar(correo: String, password: String): Result<Usuario> {
        return try {
            val correoLimpio = correo.trim().lowercase()
            val passwordLimpia = password.trim()

            require(correoLimpio.isNotBlank() && passwordLimpia.isNotBlank()) {
                "Debes ingresar correo y contraseña."
            }

            val entity = usuarioDao.autenticar(correoLimpio, passwordLimpia)
            if (entity != null) {
                Result.success(entity.toDomain())
            } else {
                Result.failure(IllegalArgumentException("Credenciales incorrectas o cuenta inactiva."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerPorId(id: Int): Usuario? =
        usuarioDao.obtenerUsuarioPorId(id)?.toDomain()

    // Para que el Administrador elija a qué técnico asignar (Criterio 15)
    fun obtenerTecnicosDisponibles(): Flow<List<Usuario>> =
        usuarioDao.obtenerTecnicosDisponibles().map { lista -> lista.map { it.toDomain() } }

    // Para que el Administrador filtre solicitudes por cliente (Criterio 26)
    fun obtenerClientes(): Flow<List<Usuario>> =
        usuarioDao.obtenerClientes().map { lista -> lista.map { it.toDomain() } }

    fun obtenerPorRol(rol: RolUsuario): Flow<List<Usuario>> =
        usuarioDao.obtenerUsuariosPorRol(rol.name).map { lista -> lista.map { it.toDomain() } }

    fun obtenerTodos(): Flow<List<Usuario>> =
        usuarioDao.obtenerUsuarios().map { lista -> lista.map { it.toDomain() } }
}
