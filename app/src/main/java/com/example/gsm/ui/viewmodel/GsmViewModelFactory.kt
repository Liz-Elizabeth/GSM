package com.example.gsm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gsm.data.repository.GsmRepository
import com.example.gsm.data.repository.UsuarioRepository

/**
 * Fábrica manual (KISS) que entrega los repositorios a cada ViewModel.
 * Evita usar librerías complejas como Hilt o Koin.
 */
class GsmViewModelFactory(
    private val gsmRepo: GsmRepository,
    private val usuarioRepo: UsuarioRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(usuarioRepo) as T
            }
            modelClass.isAssignableFrom(SolicitudViewModel::class.java) -> {
                SolicitudViewModel(gsmRepo) as T
            }
            modelClass.isAssignableFrom(DetalleSolicitudViewModel::class.java) -> {
                DetalleSolicitudViewModel(gsmRepo, usuarioRepo) as T
            }
            modelClass.isAssignableFrom(AdminDashboardViewModel::class.java) -> {
                AdminDashboardViewModel(gsmRepo, usuarioRepo) as T
            }
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
