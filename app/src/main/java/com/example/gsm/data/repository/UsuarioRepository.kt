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

    companion object {
        const val DEFAULT_PASSWORD = "GSMPass12345"
    }

    /**
     * Autentica un usuario validando credenciales y estado de cuenta (OWASP Input Validation).
     * Si la cuenta fue desactivada por el administrador, entrega un mensaje explicativo claro.
     */
    suspend fun autenticar(correo: String, password: String): Result<Usuario> {
        return try {
            val correoLimpio = correo.trim().lowercase()
            val passwordLimpia = password.trim()

            require(correoLimpio.isNotBlank() && passwordLimpia.isNotBlank()) {
                "Debes ingresar correo y contraseña."
            }

            val entity = usuarioDao.obtenerUsuarioPorCorreo(correoLimpio)
            if (entity == null) {
                return Result.failure(IllegalArgumentException("No existe ninguna cuenta registrada con este correo."))
            }

            if (entity.password != passwordLimpia) {
                return Result.failure(IllegalArgumentException("Contraseña incorrecta."))
            }

            if (!entity.activo) {
                return Result.failure(IllegalArgumentException("Tu cuenta ha sido desactivada por el administrador. Contacta a soporte."))
            }

            Result.success(entity.toDomain())
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
                        debeCambiarPassword = false,
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
     * Por defecto asigna la contraseña GSMPass12345 y marca debeCambiarPassword = true.
     */
    suspend fun crearUsuario(
        nombre: String,
        correo: String,
        password: String = DEFAULT_PASSWORD,
        rol: RolUsuario,
        empresa: String?,
        telefono: String?
    ): Result<Unit> {
        return try {
            val nombreLimpio = nombre.trim()
            val correoLimpio = correo.trim().lowercase()
            val passwordLimpia = password.trim().ifBlank { DEFAULT_PASSWORD }

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
                    debeCambiarPassword = true,
                    fechaCreacion = System.currentTimeMillis(),
                    fechaActualizacion = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * El Administrador restablece la contraseña de un usuario a la contraseña por defecto GSMPass12345.
     * Marca debeCambiarPassword = true para que el usuario sea obligado a renovarla al ingresar.
     */
    suspend fun resetearPasswordPorDefecto(usuarioId: Int, admin: Usuario): Result<Unit> {
        return try {
            require(admin.rol == RolUsuario.ADMINISTRADOR) {
                "Acceso denegado: Solo el administrador puede resetear contraseñas."
            }
            usuarioDao.actualizarCredenciales(
                id = usuarioId,
                password = DEFAULT_PASSWORD,
                debeCambiar = true
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * El Administrador activa o desactiva (soft delete) una cuenta de usuario.
     * No permite que el administrador desactive su propia cuenta en sesión.
     */
    suspend fun cambiarEstadoActivo(usuarioId: Int, activo: Boolean, admin: Usuario): Result<Unit> {
        return try {
            require(admin.rol == RolUsuario.ADMINISTRADOR) {
                "Acceso denegado: Solo el administrador puede modificar el estado de las cuentas."
            }
            require(admin.id != usuarioId) {
                "No puedes desactivar tu propia cuenta de administrador."
            }
            usuarioDao.cambiarEstadoActivo(usuarioId, activo)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * El Administrador elimina permanentemente una cuenta de usuario.
     * No permite que el administrador elimine su propia cuenta en sesión.
     */
    suspend fun eliminarUsuario(usuarioId: Int, admin: Usuario): Result<Unit> {
        return try {
            require(admin.rol == RolUsuario.ADMINISTRADOR) {
                "Acceso denegado: Solo el administrador puede eliminar cuentas."
            }
            require(admin.id != usuarioId) {
                "No puedes eliminar tu propia cuenta de administrador."
            }
            usuarioDao.eliminarUsuario(usuarioId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza la contraseña obligatoria por una nueva elegida por el usuario
     * y remueve el flag debeCambiarPassword.
     */
    suspend fun completarCambioPasswordObligatorio(usuarioId: Int, nuevaPassword: String): Result<Usuario> {
        return try {
            val passLimpia = nuevaPassword.trim()
            require(passLimpia.length >= 4) { "La nueva contraseña debe tener al menos 4 caracteres." }
            require(passLimpia != DEFAULT_PASSWORD) { "Por seguridad, la nueva contraseña no puede ser la clave por defecto ($DEFAULT_PASSWORD)." }

            usuarioDao.actualizarCredenciales(
                id = usuarioId,
                password = passLimpia,
                debeCambiar = false
            )

            val actualizado = usuarioDao.obtenerUsuarioPorId(usuarioId)
                ?: throw IllegalStateException("Usuario no encontrado.")

            Result.success(actualizado.toDomain())
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

    /**
     * Auto-registro exclusivo para nuevos Clientes / Empresas desde la pantalla de bienvenida.
     */
    suspend fun registrarCliente(
        nombre: String,
        correo: String,
        empresa: String,
        telefono: String,
        password: String
    ): Result<Usuario> {
        return try {
            val nombreLimpio = nombre.trim()
            val correoLimpio = correo.trim().lowercase()
            val empresaLimpia = empresa.trim()
            val telefonoLimpio = telefono.trim()
            val passwordLimpia = password.trim()

            require(nombreLimpio.isNotBlank()) { "Ingresa tu nombre completo." }
            require(correoLimpio.isNotBlank() && correoLimpio.contains("@")) { "Ingresa un correo electrónico corporativo válido." }
            require(empresaLimpia.isNotBlank()) { "Ingresa la razón social de tu empresa." }
            require(telefonoLimpio.isNotBlank()) { "Ingresa tu teléfono de contacto." }
            require(passwordLimpia.length >= 6) { "La contraseña debe tener al menos 6 caracteres." }

            val existente = usuarioDao.obtenerUsuarioPorCorreo(correoLimpio)
            if (existente != null) {
                return Result.failure(IllegalArgumentException("Ya existe una cuenta registrada con este correo electrónico."))
            }

            val nuevaEntity = UsuarioEntity(
                nombre = nombreLimpio,
                correo = correoLimpio,
                password = passwordLimpia,
                rol = RolUsuario.CLIENTE.name,
                empresa = empresaLimpia,
                telefono = telefonoLimpio,
                plan = "SIN_PLAN",
                ticketsDisponibles = 0,
                estadoSuscripcion = "PENDIENTE_PAGO",
                activo = true,
                debeCambiarPassword = false,
                fechaCreacion = System.currentTimeMillis(),
                fechaActualizacion = System.currentTimeMillis()
            )

            val nuevoId = usuarioDao.insertarUsuario(nuevaEntity)
            val usuarioCreado = nuevaEntity.copy(id = nuevoId.toInt()).toDomain()

            // Respaldo en la nube con Firestore
            try {
                com.example.gsm.data.remote.FirebaseClient.firestore
                    .collection("usuarios")
                    .document(correoLimpio)
                    .set(
                        mapOf(
                            "id" to nuevoId,
                            "nombre" to nombreLimpio,
                            "correo" to correoLimpio,
                            "empresa" to empresaLimpia,
                            "telefono" to telefonoLimpio,
                            "rol" to RolUsuario.CLIENTE.name,
                            "plan" to "SIN_PLAN",
                            "ticketsDisponibles" to 0,
                            "estadoSuscripcion" to "PENDIENTE_PAGO",
                            "fechaCreacion" to System.currentTimeMillis()
                        )
                    )
            } catch (e: Exception) {
                android.util.Log.w("UsuarioRepository", "No se pudo respaldar cliente en Firestore", e)
            }

            Result.success(usuarioCreado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza el plan activo y abona la cuota de tickets tras el pago aprobado en Flow.
     */
    suspend fun actualizarPlanSuscripcion(usuarioId: Int, nuevoPlan: String, cuotaTickets: Int): Result<Unit> {
        return try {
            usuarioDao.actualizarPlanSuscripcion(
                id = usuarioId,
                plan = nuevoPlan,
                tickets = cuotaTickets,
                estado = "ACTIVA"
            )

            val usuarioActual = usuarioDao.obtenerUsuarioPorId(usuarioId)
            if (usuarioActual != null) {
                try {
                    com.example.gsm.data.remote.FirebaseClient.firestore
                        .collection("usuarios")
                        .document(usuarioActual.correo)
                        .update(
                            mapOf(
                                "plan" to nuevoPlan,
                                "ticketsDisponibles" to cuotaTickets,
                                "estadoSuscripcion" to "ACTIVA",
                                "fechaActualizacion" to System.currentTimeMillis()
                            )
                        )
                } catch (e: Exception) {
                    android.util.Log.w("UsuarioRepository", "No se pudo actualizar plan en Firestore", e)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
