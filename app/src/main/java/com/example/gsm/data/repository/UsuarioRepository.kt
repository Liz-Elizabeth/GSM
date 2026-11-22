package com.example.gsm.data.repository

import com.example.gsm.data.local.dao.UsuarioDao
import com.example.gsm.data.local.entity.UsuarioEntity
import com.example.gsm.data.mapper.toDomain
import com.example.gsm.data.mapper.toEntity
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.data.remote.FirebaseClient
import com.example.gsm.security.GsmCryptoManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

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

            var entity = usuarioDao.obtenerUsuarioPorCorreo(correoLimpio)
            if (entity == null) {
                // Si el colaborador se creó en otro teléfono, intenta descargarlo de Firestore
                entity = buscarYGuardarUsuarioDesdeNube(correoLimpio)
            }
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
                val actualizado = usuarioEntity.copy(
                    password = passwordLimpia,
                    debeCambiarPassword = false,
                    fechaActualizacion = System.currentTimeMillis()
                )
                usuarioDao.actualizarUsuario(actualizado)
                respaldarUsuarioEnNube(actualizado)
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
            val entidad = usuario.toEntity().copy(fechaActualizacion = System.currentTimeMillis())
            usuarioDao.actualizarUsuario(entidad)
            respaldarUsuarioEnNube(entidad)
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

            val nueva = UsuarioEntity(
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
            val id = usuarioDao.insertarUsuario(nueva).toInt()
            respaldarUsuarioEnNube(nueva.copy(id = id))
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
            usuarioDao.obtenerUsuarioPorId(usuarioId)?.let { respaldarUsuarioEnNube(it) }
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
            usuarioDao.obtenerUsuarioPorId(usuarioId)?.let { respaldarUsuarioEnNube(it) }
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
            val objetivo = usuarioDao.obtenerUsuarioPorId(usuarioId)
            usuarioDao.eliminarUsuario(usuarioId)
            objetivo?.correo?.trim()?.lowercase()?.takeIf { it.isNotBlank() }?.let { correo ->
                try {
                    FirebaseClient.firestore.collection("usuarios").document(correo).delete()
                } catch (_: Throwable) {
                    // Seguro en offline o tests unitarios JVM
                }
            }
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

            respaldarUsuarioEnNube(actualizado)
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
                estadoSuscripcion = "INACTIVA",
                activo = true,
                debeCambiarPassword = false,
                fechaCreacion = System.currentTimeMillis(),
                fechaActualizacion = System.currentTimeMillis()
            )

            val nuevoId = usuarioDao.insertarUsuario(nuevaEntity)
            val entityConId = nuevaEntity.copy(id = nuevoId.toInt())
            val usuarioCreado = entityConId.toDomain()

            // Respaldo completo en la nube con Firestore para login multi-dispositivo
            respaldarUsuarioEnNube(entityConId)

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
            val usuarioActual = usuarioDao.obtenerUsuarioPorId(usuarioId)
            val empresaUsuario = usuarioActual?.empresa.orEmpty()
            if (usuarioActual != null && empresaUsuario.isNotBlank()) {
                usuarioDao.actualizarPlanEmpresa(
                    empresa = empresaUsuario,
                    plan = nuevoPlan,
                    tickets = cuotaTickets,
                    estado = "ACTIVA"
                )
            } else {
                usuarioDao.actualizarPlanSuscripcion(
                    id = usuarioId,
                    plan = nuevoPlan,
                    tickets = cuotaTickets,
                    estado = "ACTIVA"
                )
            }

            usuarioDao.obtenerUsuarioPorId(usuarioId)?.let { respaldarUsuarioEnNube(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observa todos los colaboradores (rol CLIENTE) pertenecientes a la misma empresa.
     */
    fun obtenerEquipoEmpresa(empresa: String): Flow<List<Usuario>> =
        usuarioDao.obtenerEquipoPorEmpresa(empresa).map { lista -> lista.map { it.toDomain() } }

    /**
     * Agrega un colaborador a la misma empresa heredando el plan y bolsa de tickets compartida.
     */
    suspend fun agregarColaboradorEmpresa(
        nombre: String,
        correo: String,
        telefono: String,
        passwordTemporal: String,
        empleador: Usuario
    ): Result<Usuario> {
        return try {
            require(empleador.rol == RolUsuario.CLIENTE) { "Solo cuentas de empresa pueden gestionar colaboradores." }
            val empresaEmpleador = empleador.empresa.orEmpty().trim()
            require(empresaEmpleador.isNotBlank()) { "Tu cuenta debe tener una empresa asociada." }

            val nombreLimpio = nombre.trim()
            val correoLimpio = correo.trim().lowercase()
            val telefonoLimpio = telefono.trim()
            val passLimpia = passwordTemporal.trim()

            require(nombreLimpio.length >= 3) { "El nombre del colaborador debe tener al menos 3 caracteres." }
            require(correoLimpio.contains("@") && correoLimpio.contains(".")) { "Ingresa un correo corporativo válido." }
            require(passLimpia.length >= 6) { "La contraseña temporal debe tener al menos 6 caracteres." }
            require(usuarioDao.obtenerUsuarioPorCorreo(correoLimpio) == null) {
                "Ya existe un usuario registrado con ese correo."
            }

            val actualEmpleador = usuarioDao.obtenerUsuarioPorId(empleador.id)?.toDomain() ?: empleador
            val ahora = System.currentTimeMillis()
            val entity = UsuarioEntity(
                nombre = nombreLimpio,
                correo = correoLimpio,
                password = passLimpia,
                rol = RolUsuario.CLIENTE.name,
                empresa = actualEmpleador.empresa.orEmpty().trim(),
                telefono = telefonoLimpio,
                plan = actualEmpleador.plan,
                ticketsDisponibles = actualEmpleador.ticketsDisponibles,
                estadoSuscripcion = actualEmpleador.estadoSuscripcion,
                activo = true,
                debeCambiarPassword = true,
                fechaCreacion = ahora,
                fechaActualizacion = ahora
            )

            val nuevoId = usuarioDao.insertarUsuario(entity).toInt()
            val conId = entity.copy(id = nuevoId)
            respaldarUsuarioEnNube(conId)
            Result.success(conId.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Activa o desactiva un colaborador dentro de la misma empresa.
     */
    suspend fun alternarEstadoColaborador(colaboradorId: Int, activo: Boolean, empleador: Usuario): Result<Unit> {
        return try {
            require(empleador.rol == RolUsuario.CLIENTE) { "Acceso denegado." }
            require(colaboradorId != empleador.id) { "No puedes desactivar tu propia cuenta." }

            val objetivo = usuarioDao.obtenerUsuarioPorId(colaboradorId)
                ?: throw IllegalArgumentException("Colaborador no encontrado.")
            require(objetivo.empresa.orEmpty().trim().equals(empleador.empresa.orEmpty().trim(), ignoreCase = true)) {
                "No tienes permiso para modificar usuarios de otra empresa."
            }

            usuarioDao.cambiarEstadoActivo(colaboradorId, activo)
            respaldarUsuarioEnNube(objetivo.copy(activo = activo, fechaActualizacion = System.currentTimeMillis()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Descarga de Firestore los colaboradores de la misma empresa que faltan en Room local.
     */
    suspend fun sincronizarEquipoDesdeNube(empresa: String) {
        val empresaLimpia = empresa.trim()
        if (empresaLimpia.isBlank()) return
        try {
            val snap = FirebaseClient.firestore
                .collection("usuarios")
                .whereEqualTo("empresa", empresaLimpia)
                .get()
                .await()
            snap.documents.forEach { doc ->
                val entidad = mapearUsuarioDesdeNube(doc) ?: return@forEach
                val existente = usuarioDao.obtenerUsuarioPorCorreo(entidad.correo)
                if (existente == null) {
                    usuarioDao.insertarUsuario(entidad)
                } else if (entidad.fechaActualizacion > existente.fechaActualizacion) {
                    usuarioDao.actualizarUsuario(entidad.copy(id = existente.id))
                }
            }
        } catch (_: Throwable) {
            // Silencioso en offline o tests JVM donde FirebaseApp no está inicializado
        }
    }

    /**
     * Descarga y sincroniza todo el directorio de usuarios desde Firestore (uso del Administrador).
     */
    suspend fun sincronizarTodosUsuariosDesdeNube() {
        try {
            val snap = FirebaseClient.firestore.collection("usuarios").get().await()
            snap.documents.forEach { doc ->
                val entidad = mapearUsuarioDesdeNube(doc) ?: return@forEach
                val existente = usuarioDao.obtenerUsuarioPorCorreo(entidad.correo)
                if (existente == null) {
                    usuarioDao.insertarUsuario(entidad)
                } else if (entidad.fechaActualizacion > existente.fechaActualizacion) {
                    usuarioDao.actualizarUsuario(entidad.copy(id = existente.id))
                }
            }
        } catch (_: Throwable) {
            // Silencioso en offline o tests JVM
        }
    }

    private fun respaldarUsuarioEnNube(entity: UsuarioEntity) {
        try {
            val correo = entity.correo.trim().lowercase()
            if (correo.isBlank()) return
            val datos = hashMapOf(
                "id" to entity.id,
                "nombre" to entity.nombre,
                "correo" to correo,
                "password" to GsmCryptoManager.encrypt(entity.password),
                "rol" to entity.rol,
                "empresa" to entity.empresa,
                "telefono" to entity.telefono,
                "plan" to entity.plan,
                "ticketsDisponibles" to entity.ticketsDisponibles,
                "estadoSuscripcion" to entity.estadoSuscripcion,
                "activo" to entity.activo,
                "debeCambiarPassword" to entity.debeCambiarPassword,
                "fechaCreacion" to entity.fechaCreacion,
                "fechaActualizacion" to entity.fechaActualizacion
            )
            FirebaseClient.firestore
                .collection("usuarios")
                .document(correo)
                .set(datos, SetOptions.merge())
        } catch (_: Throwable) {
            // Seguro en offline o tests unitarios JVM
        }
    }

    private suspend fun buscarYGuardarUsuarioDesdeNube(correoLimpio: String): UsuarioEntity? {
        return try {
            val doc = FirebaseClient.firestore
                .collection("usuarios")
                .document(correoLimpio)
                .get()
                .await()
            val entidad = mapearUsuarioDesdeNube(doc) ?: return null
            val id = usuarioDao.insertarUsuario(entidad).toInt()
            entidad.copy(id = id)
        } catch (_: Throwable) {
            null
        }
    }

    private fun mapearUsuarioDesdeNube(doc: DocumentSnapshot): UsuarioEntity? {
        val data = doc.data ?: return null
        val correo = (data["correo"] as? String)?.trim()?.lowercase() ?: doc.id.trim().lowercase()
        val rawPass = data["password"] as? String ?: return null
        val pass = if (rawPass.startsWith("ENC:")) GsmCryptoManager.decrypt(rawPass) else rawPass
        return UsuarioEntity(
            nombre = (data["nombre"] as? String).orEmpty().ifBlank { correo },
            correo = correo,
            password = pass,
            rol = (data["rol"] as? String) ?: RolUsuario.CLIENTE.name,
            empresa = (data["empresa"] as? String)?.trim()?.ifBlank { null },
            telefono = (data["telefono"] as? String)?.trim()?.ifBlank { null },
            plan = (data["plan"] as? String) ?: "SIN_PLAN",
            ticketsDisponibles = (data["ticketsDisponibles"] as? Number)?.toInt() ?: 0,
            estadoSuscripcion = (data["estadoSuscripcion"] as? String) ?: "INACTIVA",
            activo = (data["activo"] as? Boolean) ?: true,
            debeCambiarPassword = (data["debeCambiarPassword"] as? Boolean) ?: false,
            fechaCreacion = (data["fechaCreacion"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            fechaActualizacion = (data["fechaActualizacion"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
