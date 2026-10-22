package com.example.gsm.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gsm.data.repository.UsuarioRepository
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import kotlinx.coroutines.launch

class AuthViewModel(
    private val usuarioRepo: UsuarioRepository
) : ViewModel() {

    // La pantalla puede leerlos, pero solo AuthViewModel puede modificarlos (private set)
    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

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

    // Estados para edición de perfil de usuario (KISS)
    var guardandoPerfil by mutableStateOf(false)
        private set

    var mensajePerfil by mutableStateOf<String?>(null)
        private set

    var errorPerfil by mutableStateOf<String?>(null)
        private set

    fun iniciarSesion(correo: String, password: String) {
        viewModelScope.launch {
            cargando = true
            mensajeError = null

            val resultado = usuarioRepo.autenticar(correo, password)

            if (resultado.isSuccess) {
                usuarioActual = resultado.getOrNull()
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

    fun actualizarPerfil(nombre: String, telefono: String?, empresa: String?, onExito: () -> Unit = {}) {
        val actual = usuarioActual ?: return
        val nombreLimpio = nombre.trim()

        if (nombreLimpio.isBlank()) {
            errorPerfil = "El nombre no puede estar vacío."
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
                usuarioActual = usuarioActualizado
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

    fun actualizarUsuarioEnSesion(usuario: Usuario) {
        usuarioActual = usuario
    }

    fun cerrarSesion() {
        usuarioActual = null
        mensajeError = null
        limpiarEstadoRecuperacion()
        limpiarMensajesPerfil()
    }
}
