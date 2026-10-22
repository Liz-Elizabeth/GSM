package com.example.gsm.data.repository

import com.example.gsm.data.local.dao.UsuarioDao
import com.example.gsm.data.local.entity.UsuarioEntity
import com.example.gsm.data.mapper.toDomain
import com.example.gsm.data.mapper.toEntity
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
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

    /**
     * Restablece la contraseña de una cuenta por la que el usuario elija (KISS).
     */
    suspend fun recuperarPassword(correo: String, nuevaPassword: String): Result<String> {
        return try {
            val correoLimpio = correo.trim().lowercase()
            val passwordLimpia = nuevaPassword.trim()

            require(correoLimpio.isNotBlank()) { "Debes ingresar tu correo electrónico." }
            require(passwordLimpia.length >= 4) { "La nueva contraseña debe tener al menos 4 caracteres." }

            val usuarioEntity = usuarioDao.obtenerUsuarioPorCorreo(correoLimpio)
            if (usuarioEntity != null) {
                usuarioDao.actualizarUsuario(
                    usuarioEntity.copy(
                        password = passwordLimpia,
                        fechaActualizacion = System.currentTimeMillis()
                    )
                )
                Result.success("¡Contraseña actualizada con éxito! Ya puedes iniciar sesión.")
            } else {
                Result.failure(IllegalArgumentException("No existe ninguna cuenta registrada con este correo."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza los datos personales del usuario (nombre, teléfono, empresa).
     */
    suspend fun actualizarPerfil(usuario: Usuario): Result<Unit> {
        return try {
            require(usuario.nombre.isNotBlank()) { "El nombre no puede estar vacío." }
            usuarioDao.actualizarUsuario(
                usuario.toEntity().copy(fechaActualizacion = System.currentTimeMillis())
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Da de alta un nuevo usuario y su rol RBAC (uso por Administrador).
     */
    suspend fun crearUsuario(
        nombre: String,
        correo: String,
        password: String,
        rol: RolUsuario,
        empresa: String?,
        telefono: String?
    ): Result<Unit> {
        return try {
            val nombreLimpio = nombre.trim()
            val correoLimpio = correo.trim().lowercase()
            val passwordLimpia = password.trim()

            require(nombreLimpio.isNotBlank()) { "El nombre es obligatorio." }
            require(correoLimpio.isNotBlank() && correoLimpio.contains("@")) { "Ingresa un correo electrónico válido." }
            require(passwordLimpia.length >= 4) { "La contraseña debe tener al menos 4 caracteres." }

            val existente = usuarioDao.obtenerUsuarioPorCorreo(correoLimpio)
            if (existente != null) {
                return Result.failure(IllegalArgumentException("Ya existe un usuario con este correo."))
            }

            usuarioDao.insertarUsuario(
                UsuarioEntity(
                    nombre = nombreLimpio,
                    correo = correoLimpio,
                    password = passwordLimpia,
                    rol = rol.name,
                    empresa = empresa?.trim()?.ifBlank { null },
                    telefono = telefono?.trim()?.ifBlank { null },
                    activo = true,
                    fechaCreacion = System.currentTimeMillis(),
                    fechaActualizacion = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
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
