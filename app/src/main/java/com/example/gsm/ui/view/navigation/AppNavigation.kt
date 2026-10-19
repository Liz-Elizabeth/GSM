package com.example.gsm.ui.view.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.gsm.ui.view.screens.admin.AdminDashboardScreen
import com.example.gsm.ui.view.screens.auth.LoginScreen
import com.example.gsm.ui.view.screens.usuario.BandejaScreen
import com.example.gsm.ui.view.screens.usuario.CrearSolicitudScreen
import com.example.gsm.ui.view.screens.usuario.DetalleSolicitudScreen
import com.example.gsm.ui.viewmodel.AdminDashboardViewModel
import com.example.gsm.ui.viewmodel.AuthViewModel
import com.example.gsm.ui.viewmodel.DetalleSolicitudViewModel
import com.example.gsm.ui.viewmodel.SolicitudViewModel

/**
 * Controlador de navegación KISS entre las pantallas de Auth, Usuario y Admin.
 */
@Composable
fun AppNavigation(
    authVm: AuthViewModel,
    solicitudVm: SolicitudViewModel,
    detalleVm: DetalleSolicitudViewModel,
    dashboardVm: AdminDashboardViewModel,
    modifier: Modifier = Modifier
) {
    val usuarioActual = authVm.usuarioActual
    var pantallaActual by remember { mutableStateOf("bandeja") }
    var solicitudSeleccionadaId by remember { mutableIntStateOf(0) }

    Box(modifier = modifier.fillMaxSize()) {
        if (usuarioActual == null) {
            LoginScreen(authVm = authVm)
        } else {
            when (pantallaActual) {
                "crear" -> CrearSolicitudScreen(
                    cliente = usuarioActual,
                    solicitudVm = solicitudVm,
                    alVolver = { pantallaActual = "bandeja" }
                )

                "detalle" -> DetalleSolicitudScreen(
                    solicitudId = solicitudSeleccionadaId,
                    usuario = usuarioActual,
                    detalleVm = detalleVm,
                    alVolver = { pantallaActual = "bandeja" }
                )

                "dashboard" -> AdminDashboardScreen(
                    dashboardVm = dashboardVm,
                    alVolver = { pantallaActual = "bandeja" }
                )

                else -> BandejaScreen(
                    usuario = usuarioActual,
                    solicitudVm = solicitudVm,
                    alIrACrear = {
                        solicitudVm.limpiarMensaje()
                        pantallaActual = "crear"
                    },
                    alIrADetalle = { id ->
                        solicitudSeleccionadaId = id
                        pantallaActual = "detalle"
                    },
                    alIrADashboard = {
                        pantallaActual = "dashboard"
                    },
                    alCerrarSesion = {
                        pantallaActual = "bandeja"
                        authVm.cerrarSesion()
                    }
                )
            }
        }
    }
}
