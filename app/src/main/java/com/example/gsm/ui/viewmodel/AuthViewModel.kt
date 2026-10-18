package com.example.gsm.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gsm.data.repository.UsuarioRepository
import com.example.gsm.model.RolUsuario
import com.example.gsm.model.Usuario
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

    fun cerrarSesion() {
        usuarioActual = null
        mensajeError = null
    }
}
