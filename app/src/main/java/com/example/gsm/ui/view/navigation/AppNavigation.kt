package com.example.gsm.ui.view.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.ui.view.screens.admin.AdminDashboardScreen
import com.example.gsm.ui.view.screens.admin.GestionUsuariosScreen
import com.example.gsm.ui.view.screens.auth.LoginScreen
import com.example.gsm.ui.view.screens.usuario.BandejaScreen
import com.example.gsm.ui.view.screens.usuario.CrearSolicitudScreen
import com.example.gsm.ui.view.screens.usuario.DetalleSolicitudScreen
import com.example.gsm.ui.view.screens.usuario.NotificacionesScreen
import com.example.gsm.ui.view.screens.usuario.PerfilScreen
import com.example.gsm.ui.view.screens.usuario.WalletTicketsScreen
import com.example.gsm.ui.viewmodel.AdminDashboardViewModel
import com.example.gsm.ui.viewmodel.AuthViewModel
import com.example.gsm.ui.viewmodel.DetalleSolicitudViewModel
import com.example.gsm.ui.viewmodel.SolicitudViewModel

/**
 * Controlador de navegación KISS con barra inferior estilo Instagram (Bottom Navigation Bar).
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

    if (usuarioActual == null) {
        Box(modifier = modifier.fillMaxSize()) {
            LoginScreen(authVm = authVm)
        }
    } else {
        val noLeidas = solicitudVm.notificaciones.count { !it.leida }

        Column(modifier = modifier.fillMaxSize()) {
            // 1. CONTENIDO DE LA PANTALLA ACTIVA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
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
                        alIrAGestionUsuarios = { pantallaActual = "gestion_usuarios" },
                        alVolver = { pantallaActual = "bandeja" }
                    )

                    "gestion_usuarios" -> GestionUsuariosScreen(
                        dashboardVm = dashboardVm,
                        alVolver = { pantallaActual = "perfil" }
                    )

                    "wallet" -> WalletTicketsScreen(
                        usuario = usuarioActual,
                        solicitudVm = solicitudVm,
                        alIrADetalle = { id ->
                            solicitudSeleccionadaId = id
                            pantallaActual = "detalle"
                        }
                    )

                    "notificaciones" -> NotificacionesScreen(
                        solicitudVm = solicitudVm,
                        alIrADetalle = { id ->
                            solicitudSeleccionadaId = id
                            pantallaActual = "detalle"
                        }
                    )

                    "perfil" -> PerfilScreen(
                        usuario = usuarioActual,
                        authVm = authVm,
                        alIrAGestionUsuarios = { pantallaActual = "gestion_usuarios" },
                        alCerrarSesion = {
                            pantallaActual = "bandeja"
                            authVm.cerrarSesion()
                        }
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

            // 2. BARRA DE NAVEGACIÓN INFERIOR ESTILO INSTAGRAM
            NavigationBar(
                containerColor = Color.White
            ) {
                // Pestaña 1: Bandeja / Inicio
                NavigationBarItem(
                    selected = pantallaActual == "bandeja" || pantallaActual == "detalle",
                    onClick = { pantallaActual = "bandeja" },
                    icon = { Icon(Icons.Filled.ConfirmationNumber, contentDescription = "Bandeja") },
                    label = { Text("Bandeja", fontWeight = FontWeight.Medium) }
                )

                // Pestaña 2: Wallet / Pases QR (Estilo Apple Wallet / Eventos)
                NavigationBarItem(
                    selected = pantallaActual == "wallet",
                    onClick = { pantallaActual = "wallet" },
                    icon = { Icon(Icons.Filled.QrCode2, contentDescription = "Pases QR") },
                    label = { Text("Pases QR", fontWeight = FontWeight.Medium) }
                )

                // Pestaña 2 (Según Rol): Crear Ticket (Cliente) o Métricas (Admin)
                if (usuarioActual.rol == RolUsuario.CLIENTE) {
                    NavigationBarItem(
                        selected = pantallaActual == "crear",
                        onClick = {
                            solicitudVm.limpiarMensaje()
                            pantallaActual = "crear"
                        },
                        icon = { Icon(Icons.Filled.AddCircle, contentDescription = "Nuevo") },
                        label = { Text("Nuevo", fontWeight = FontWeight.Medium) }
                    )
                } else if (usuarioActual.rol == RolUsuario.ADMINISTRADOR) {
                    NavigationBarItem(
                        selected = pantallaActual == "dashboard",
                        onClick = { pantallaActual = "dashboard" },
                        icon = { Icon(Icons.Filled.BarChart, contentDescription = "Métricas") },
                        label = { Text("Métricas", fontWeight = FontWeight.Medium) }
                    )
                }

                // Pestaña 3: Notificaciones / Alertas
                NavigationBarItem(
                    selected = pantallaActual == "notificaciones",
                    onClick = { pantallaActual = "notificaciones" },
                    icon = {
                        if (noLeidas > 0) {
                            BadgedBox(badge = { Badge { Text("$noLeidas") } }) {
                                Icon(Icons.Filled.Notifications, contentDescription = "Alertas")
                            }
                        } else {
                            Icon(Icons.Filled.NotificationsNone, contentDescription = "Alertas")
                        }
                    },
                    label = { Text("Alertas", fontWeight = FontWeight.Medium) }
                )

                // Pestaña 4: Perfil RBAC + Cerrar Sesión
                NavigationBarItem(
                    selected = pantallaActual == "perfil",
                    onClick = { pantallaActual = "perfil" },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil", fontWeight = FontWeight.Medium) }
                )
            }
        }
    }
}
