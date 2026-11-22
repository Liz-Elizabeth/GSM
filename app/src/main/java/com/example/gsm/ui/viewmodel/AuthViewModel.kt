package com.example.gsm.ui.viewmodel

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gsm.data.repository.UsuarioRepository
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AuthViewModel(
    private val usuarioRepo: UsuarioRepository,
    private val sessionPrefs: SharedPreferences? = null
) : ViewModel() {

    companion object {
        private const val KEY_ULTIMO_USUARIO_ID = "ultimo_usuario_id"
    }

    // La pantalla puede leerlos, pero solo AuthViewModel puede modificarlos (private set)
    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    init {
        // Restaura la sesión local si existe un usuario activo guardado
        val idGuardado = sessionPrefs?.getInt(KEY_ULTIMO_USUARIO_ID, -1) ?: -1
        if (idGuardado > 0) {
            viewModelScope.launch {
                val usuario = usuarioRepo.obtenerPorId(idGuardado)
                if (usuario != null && usuario.activo) {
                    usuarioActual = usuario
                } else {
                    sessionPrefs?.edit()?.remove(KEY_ULTIMO_USUARIO_ID)?.apply()
                }
            }
        }
    }

    private fun guardarSesion(usuario: Usuario) {
        usuarioActual = usuario
        sessionPrefs?.edit()?.putInt(KEY_ULTIMO_USUARIO_ID, usuario.id)?.apply()
    }

    var cargando by mutableStateOf(false)
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

    // Estados para recuperación y cambio de contraseña (KISS)
    var recuperandoPassword by mutableStateOf(false)
        private set

    var mensajeRecuperacion by mutableStateOf<String?>(null)
        private set

    var errorRecuperacion by mutableStateOf<String?>(null)
        private set

    // Estados para cambio obligatorio de contraseña (primer login tras reset o creación)
    var cambiandoPasswordObligatorio by mutableStateOf(false)
        private set

    var errorPasswordObligatoria by mutableStateOf<String?>(null)
        private set

    // Estados para edición de perfil de usuario (KISS)
    var guardandoPerfil by mutableStateOf(false)
        private set

    var mensajePerfil by mutableStateOf<String?>(null)
        private set

    var errorPerfil by mutableStateOf<String?>(null)
        private set

    // Estados para auto-registro exclusivo de Clientes / Empresas
    var registrando by mutableStateOf(false)
        private set

    var errorRegistro by mutableStateOf<String?>(null)
        private set

    fun limpiarErrorRegistro() {
        errorRegistro = null
    }

    fun registrarUsuario(
        nombre: String,
        correo: String,
        empresa: String,
        telefono: String,
        password: String,
        confirmarPassword: String,
        onExito: (Usuario) -> Unit
    ) {
        val pass = password.trim()
        val conf = confirmarPassword.trim()

        if (pass.length < 6) {
            errorRegistro = "La contraseña debe tener al menos 6 caracteres."
            return
        }
        if (pass != conf) {
            errorRegistro = "Las contraseñas no coinciden."
            return
        }

        viewModelScope.launch {
            registrando = true
            errorRegistro = null

            val resultado = usuarioRepo.registrarCliente(
                nombre = nombre,
                correo = correo,
                empresa = empresa,
                telefono = telefono,
                password = pass
            )

            if (resultado.isSuccess) {
                val user = resultado.getOrThrow()
                guardarSesion(user)
                onExito(user)
            } else {
                errorRegistro = resultado.exceptionOrNull()?.message ?: "Error al registrar la cuenta."
            }
            registrando = false
        }
    }

    fun actualizarPlanUsuario(nuevoPlan: String, cuotaTickets: Int) {
        val actual = usuarioActual ?: return
        viewModelScope.launch {
            usuarioRepo.actualizarPlanSuscripcion(actual.id, nuevoPlan, cuotaTickets)
            guardarSesion(
                actual.copy(
                    plan = nuevoPlan,
                    ticketsDisponibles = cuotaTickets,
                    estadoSuscripcion = "ACTIVA"
                )
            )
        }
    }

    fun iniciarSesion(correo: String, password: String) {
        viewModelScope.launch {
            cargando = true
            mensajeError = null

            val resultado = usuarioRepo.autenticar(correo, password)

            if (resultado.isSuccess) {
                resultado.getOrNull()?.let { guardarSesion(it) }
            } else {
                mensajeError = resultado.exceptionOrNull()?.message ?: "Credenciales incorrectas"
            }
            cargando = false
        }
    }

    fun iniciarSesionDemo(rol: RolUsuario) {
        if (rol == RolUsuario.CLIENTE) {
            iniciarSesion("cliente@pyme.cl", "1234")
        } else if (rol == RolUsuario.ADMINISTRADOR) {
            iniciarSesion("admin@gsm.cl", "1234")
        } else {
            iniciarSesion("tecnico1@gsm.cl", "1234")
        }
    }

    fun recuperarPassword(correo: String, nuevaPassword: String, confirmarPassword: String) {
        val passLimpia = nuevaPassword.trim()
        val confLimpia = confirmarPassword.trim()

        if (correo.isBlank()) {
            errorRecuperacion = "Ingresa tu correo electrónico."
            return
        }
        if (passLimpia.length < 4) {
            errorRecuperacion = "La nueva contraseña debe tener al menos 4 caracteres."
            return
        }
        if (passLimpia != confLimpia) {
            errorRecuperacion = "Las contraseñas no coinciden."
            return
        }

        viewModelScope.launch {
            recuperandoPassword = true
            errorRecuperacion = null
            mensajeRecuperacion = null

            val resultado = usuarioRepo.recuperarPassword(correo, passLimpia)
            if (resultado.isSuccess) {
                mensajeRecuperacion = resultado.getOrNull()
            } else {
                errorRecuperacion = resultado.exceptionOrNull()?.message ?: "Error al actualizar contraseña."
            }
            recuperandoPassword = false
        }
    }

    fun limpiarEstadoRecuperacion() {
        mensajeRecuperacion = null
        errorRecuperacion = null
        recuperandoPassword = false
    }

    fun cambiarPasswordObligatorio(
        nuevaPassword: String,
        confirmacion: String,
        onExito: () -> Unit = {}
    ) {
        val actual = usuarioActual ?: return
        val passLimpia = nuevaPassword.trim()
        val confLimpia = confirmacion.trim()

        if (passLimpia.length < 4) {
            errorPasswordObligatoria = "La nueva contraseña debe tener al menos 4 caracteres."
            return
        }
        if (passLimpia == UsuarioRepository.DEFAULT_PASSWORD) {
            errorPasswordObligatoria = "Por seguridad, la contraseña no puede ser la clave por defecto (${UsuarioRepository.DEFAULT_PASSWORD})."
            return
        }
        if (passLimpia != confLimpia) {
            errorPasswordObligatoria = "Las contraseñas no coinciden."
            return
        }

        viewModelScope.launch {
            cambiandoPasswordObligatorio = true
            errorPasswordObligatoria = null

            val resultado = usuarioRepo.completarCambioPasswordObligatorio(actual.id, passLimpia)
            if (resultado.isSuccess) {
                resultado.getOrNull()?.let { guardarSesion(it) }
                onExito()
            } else {
                errorPasswordObligatoria = resultado.exceptionOrNull()?.message ?: "Error al actualizar contraseña."
            }
            cambiandoPasswordObligatorio = false
        }
    }

    fun limpiarEstadoPasswordObligatoria() {
        errorPasswordObligatoria = null
        cambiandoPasswordObligatorio = false
    }

    fun actualizarPerfil(
        nombre: String,
        telefono: String?,
        empresa: String?,
        nuevaPassword: String? = null,
        onExito: () -> Unit = {}
    ) {
        val actual = usuarioActual ?: return
        val nombreLimpio = nombre.trim()
        val passLimpia = nuevaPassword?.trim().orEmpty()

        if (nombreLimpio.isBlank()) {
            errorPerfil = "El nombre no puede estar vacío."
            return
        }
        if (passLimpia.isNotEmpty() && passLimpia.length < 6) {
            errorPerfil = "La nueva contraseña debe tener al menos 6 caracteres."
            return
        }

        viewModelScope.launch {
            guardandoPerfil = true
            errorPerfil = null
            mensajePerfil = null

            val usuarioActualizado = actual.copy(
                nombre = nombreLimpio,
                telefono = telefono?.trim()?.ifBlank { null },
                empresa = empresa?.trim()?.ifBlank { null },
                fechaActualizacion = System.currentTimeMillis()
            )

            val resultado = usuarioRepo.actualizarPerfil(usuarioActualizado)
            if (resultado.isSuccess) {
                if (passLimpia.isNotEmpty()) {
                    val passRes = usuarioRepo.completarCambioPasswordObligatorio(actual.id, passLimpia)
                    if (passRes.isFailure) {
                        errorPerfil = passRes.exceptionOrNull()?.message ?: "Error al actualizar contraseña."
                        guardandoPerfil = false
                        return@launch
                    }
                    guardarSesion(passRes.getOrNull() ?: usuarioActualizado)
                } else {
                    guardarSesion(usuarioActualizado)
                }
                mensajePerfil = "Datos actualizados exitosamente."
                onExito()
            } else {
                errorPerfil = resultado.exceptionOrNull()?.message ?: "Error al actualizar perfil."
            }
            guardandoPerfil = false
        }
    }

    fun limpiarMensajesPerfil() {
        mensajePerfil = null
        errorPerfil = null
    }

    // --- MI EMPRESA / EQUIPO (Multiusuario por Organización) ---
    var equipoEmpresa by mutableStateOf<List<Usuario>>(emptyList())
        private set

    var mensajeEquipo by mutableStateOf<String?>(null)
        private set

    var errorEquipo by mutableStateOf<String?>(null)
        private set

    private var escuchaEquipoJob: Job? = null

    fun cargarEquipoEmpresa() {
        val empresa = usuarioActual?.empresa?.trim().orEmpty()
        if (empresa.isBlank()) return
        escuchaEquipoJob?.cancel()
        escuchaEquipoJob = viewModelScope.launch {
            launch { usuarioRepo.sincronizarEquipoDesdeNube(empresa) }
            usuarioRepo.obtenerEquipoEmpresa(empresa).collect { lista ->
                equipoEmpresa = lista
            }
        }
    }

    fun agregarColaborador(
        nombre: String,
        correo: String,
        telefono: String,
        passwordTemporal: String,
        onExito: () -> Unit = {}
    ) {
        val actual = usuarioActual ?: return
        viewModelScope.launch {
            limpiarMensajesEquipo()
            val res = usuarioRepo.agregarColaboradorEmpresa(nombre, correo, telefono, passwordTemporal, actual)
            if (res.isSuccess) {
                mensajeEquipo = "Colaborador agregado a ${actual.empresa}."
                onExito()
            } else {
                errorEquipo = res.exceptionOrNull()?.message ?: "No se pudo agregar al colaborador."
            }
        }
    }

    fun alternarEstadoColaborador(colaboradorId: Int, activo: Boolean) {
        val actual = usuarioActual ?: return
        viewModelScope.launch {
            limpiarMensajesEquipo()
            val res = usuarioRepo.alternarEstadoColaborador(colaboradorId, activo, actual)
            if (res.isSuccess) {
                mensajeEquipo = if (activo) "Colaborador reactivado." else "Colaborador desactivado."
            } else {
                errorEquipo = res.exceptionOrNull()?.message ?: "Error al actualizar estado."
            }
        }
    }

    fun limpiarMensajesEquipo() {
        mensajeEquipo = null
        errorEquipo = null
    }

    fun actualizarUsuarioEnSesion(usuario: Usuario) {
        guardarSesion(usuario)
    }

    fun cerrarSesion() {
        escuchaEquipoJob?.cancel()
        equipoEmpresa = emptyList()
        limpiarMensajesEquipo()
        usuarioActual = null
        sessionPrefs?.edit()?.remove(KEY_ULTIMO_USUARIO_ID)?.apply()
        mensajeError = null
        limpiarEstadoRecuperacion()
        limpiarEstadoPasswordObligatoria()
        limpiarMensajesPerfil()
    }
}
