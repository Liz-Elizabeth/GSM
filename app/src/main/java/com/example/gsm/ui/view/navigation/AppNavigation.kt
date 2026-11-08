package com.example.gsm.ui.view.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.ui.view.screens.admin.AdminDashboardScreen
import com.example.gsm.ui.view.screens.admin.GestionUsuariosScreen
import com.example.gsm.ui.view.screens.auth.CambiarPasswordObligatorioScreen
import com.example.gsm.ui.view.screens.auth.LoginScreen
import com.example.gsm.ui.view.screens.splash.SplashScreen
import com.example.gsm.ui.view.screens.usuario.ArchivoTicketsScreen
import com.example.gsm.ui.view.screens.usuario.BandejaMensajesScreen
import com.example.gsm.ui.view.screens.usuario.BandejaScreen
import com.example.gsm.ui.view.screens.usuario.ChatTicketScreen
import com.example.gsm.ui.view.screens.usuario.CrearSolicitudScreen
import com.example.gsm.ui.view.screens.usuario.DetalleSolicitudScreen
import com.example.gsm.ui.view.screens.usuario.PerfilScreen
import com.example.gsm.ui.view.screens.usuario.PlanesSoporteScreen
import com.example.gsm.ui.view.screens.usuario.WalletTicketsScreen
import com.example.gsm.ui.viewmodel.AdminDashboardViewModel
import com.example.gsm.ui.viewmodel.AuthViewModel
import com.example.gsm.ui.viewmodel.DetalleSolicitudViewModel
import com.example.gsm.ui.viewmodel.SolicitudViewModel

/**
 * Controlador de navegación centralizado (KISS & Teamwork Desk UX).
 *
 * Arquitectura de 4 pestañas fijas y predecibles:
 * 1. Bandeja: Solicitudes activas en curso.
 * 2. Mensajes: Centro de chats directos de soporte en tiempo real.
 * 3. Pases QR: Billetera de credenciales de terreno y planes SLA.
 * 4. Perfil: Ajustes, roles RBAC y Archivo histórico de tickets.
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
    var origenDetalle by remember { mutableStateOf("bandeja") }
    var origenChat by remember { mutableStateOf("detalle") }
    var origenDashboard by remember { mutableStateOf("perfil") }
    var mostrandoSplash by remember { mutableStateOf(true) }

    if (mostrandoSplash) {
        SplashScreen(onFinalizado = { mostrandoSplash = false })
    } else if (usuarioActual == null) {
        Box(modifier = modifier.fillMaxSize()) {
            LoginScreen(authVm = authVm)
        }
    } else if (usuarioActual.debeCambiarPassword) {
        Box(modifier = modifier.fillMaxSize()) {
            CambiarPasswordObligatorioScreen(
                usuario = usuarioActual,
                authVm = authVm
            )
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
                        alVolver = { pantallaActual = origenDetalle },
                        alIrAChat = {
                            origenChat = "detalle"
                            pantallaActual = "chat"
                        }
                    )

                    "chat" -> ChatTicketScreen(
                        solicitudId = solicitudSeleccionadaId,
                        usuario = usuarioActual,
                        detalleVm = detalleVm,
                        alVolver = { pantallaActual = origenChat }
                    )

                    "mensajes" -> BandejaMensajesScreen(
                        usuario = usuarioActual,
                        solicitudVm = solicitudVm,
                        alIrAChat = { id ->
                            solicitudSeleccionadaId = id
                            origenChat = "mensajes"
                            pantallaActual = "chat"
                        },
                        alIrACrear = {
                            solicitudVm.limpiarMensaje()
                            pantallaActual = "crear"
                        }
                    )

                    "archivo" -> ArchivoTicketsScreen(
                        usuario = usuarioActual,
                        solicitudVm = solicitudVm,
                        alVolver = { pantallaActual = "perfil" },
                        alIrADetalle = { id ->
                            solicitudSeleccionadaId = id
                            origenDetalle = "archivo"
                            pantallaActual = "detalle"
                        }
                    )

                    "dashboard" -> AdminDashboardScreen(
                        dashboardVm = dashboardVm,
                        alIrAGestionUsuarios = { pantallaActual = "gestion_usuarios" },
                        alVolver = { pantallaActual = origenDashboard }
                    )

                    "gestion_usuarios" -> GestionUsuariosScreen(
                        admin = usuarioActual,
                        dashboardVm = dashboardVm,
                        alVolver = { pantallaActual = "perfil" }
                    )

                    "wallet" -> WalletTicketsScreen(
                        usuario = usuarioActual,
                        solicitudVm = solicitudVm,
                        alIrADetalle = { id ->
                            solicitudSeleccionadaId = id
                            origenDetalle = "wallet"
                            pantallaActual = "detalle"
                        },
                        alIrAPlanes = {
                            pantallaActual = "planes"
                        }
                    )

                    "planes" -> PlanesSoporteScreen(
                        usuario = usuarioActual,
                        alVolver = {
                            pantallaActual = "wallet"
                        }
                    )

                    "perfil" -> PerfilScreen(
                        usuario = usuarioActual,
                        authVm = authVm,
                        alIrAGestionUsuarios = { pantallaActual = "gestion_usuarios" },
                        alIrADashboard = {
                            origenDashboard = "perfil"
                            pantallaActual = "dashboard"
                        },
                        alIrAPlanes = { pantallaActual = "planes" },
                        alIrAArchivo = { pantallaActual = "archivo" },
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
                            origenDetalle = "bandeja"
                            pantallaActual = "detalle"
                        },
                        alIrADashboard = {
                            origenDashboard = "bandeja"
                            pantallaActual = "dashboard"
                        },
                        alCerrarSesion = {
                            pantallaActual = "bandeja"
                            authVm.cerrarSesion()
                        }
                    )
                }
            }

            // 2. BARRA DE NAVEGACIÓN INFERIOR (Oculta en chat, nuevo ticket o planes)
            if (pantallaActual != "chat" && pantallaActual != "planes" && pantallaActual != "crear") {
                NavigationBar(
                    containerColor = Color.White
                ) {
                    // Pestaña 1: Bandeja (Tickets Activos)
                    val esBandejaSeleccionada = pantallaActual == "bandeja" || (pantallaActual == "detalle" && origenDetalle == "bandeja")
                    NavigationBarItem(
                        selected = esBandejaSeleccionada,
                        onClick = { pantallaActual = "bandeja" },
                        icon = { Icon(Icons.Filled.ConfirmationNumber, contentDescription = "Bandeja") },
                        label = { Text("Bandeja", fontWeight = FontWeight.Medium) }
                    )

                    // Pestaña 2: Mensajes (Centro de Chats en vivo)
                    val esMensajesSeleccionados = pantallaActual == "mensajes" || (pantallaActual == "detalle" && origenDetalle == "mensajes")
                    NavigationBarItem(
                        selected = esMensajesSeleccionados,
                        onClick = { pantallaActual = "mensajes" },
                        icon = {
                            if (noLeidas > 0) {
                                BadgedBox(badge = { Badge { Text("$noLeidas") } }) {
                                    Icon(Icons.Filled.Forum, contentDescription = "Mensajes")
                                }
                            } else {
                                Icon(Icons.Filled.Forum, contentDescription = "Mensajes")
                            }
                        },
                        label = { Text("Mensajes", fontWeight = FontWeight.Medium) }
                    )

                    // Pestaña Central: Crear Ticket Nuevo (Estilo Instagram +)
                    NavigationBarItem(
                        selected = pantallaActual == "crear",
                        onClick = {
                            solicitudVm.limpiarMensaje()
                            pantallaActual = "crear"
                        },
                        icon = {
                            Surface(
                                shape = CircleShape,
                                color = TeamworkBlue,
                                shadowElevation = 3.dp,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Crear nuevo ticket",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        label = { Text("Nuevo", fontWeight = FontWeight.Bold, color = TeamworkBlue) }
                    )

                    // Pestaña 3: Pases QR (Billetera digital y credenciales)
                    val esWalletSeleccionada = pantallaActual == "wallet" || (pantallaActual == "detalle" && origenDetalle == "wallet")
                    NavigationBarItem(
                        selected = esWalletSeleccionada,
                        onClick = { pantallaActual = "wallet" },
                        icon = { Icon(Icons.Filled.QrCode2, contentDescription = "Pases QR") },
                        label = { Text("Pases QR", fontWeight = FontWeight.Medium) }
                    )

                    // Pestaña 4: Perfil (Cuenta, Roles y Archivo)
                    val esPerfilSeleccionado = pantallaActual == "perfil" || pantallaActual == "archivo" || (pantallaActual == "detalle" && origenDetalle == "archivo")
                    NavigationBarItem(
                        selected = esPerfilSeleccionado,
                        onClick = { pantallaActual = "perfil" },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
                        label = { Text("Perfil", fontWeight = FontWeight.Medium) }
                    )
                }
            }
        }
    }
}
