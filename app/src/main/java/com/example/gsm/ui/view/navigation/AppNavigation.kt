package com.example.gsm.ui.view.navigation

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.R
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
import com.example.gsm.ui.view.screens.usuario.MiEmpresaScreen
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
    solicitudIdInicial: Int = 0,
    modifier: Modifier = Modifier
) {
    val usuarioActual = authVm.usuarioActual
    var pantallaActual by remember { mutableStateOf(if (solicitudIdInicial > 0) "detalle" else "bandeja") }
    var solicitudSeleccionadaId by remember { mutableIntStateOf(solicitudIdInicial) }
    var origenDetalle by remember { mutableStateOf("bandeja") }
    var origenChat by remember { mutableStateOf("detalle") }
    var origenDashboard by remember { mutableStateOf("perfil") }
    var origenArchivo by remember { mutableStateOf("perfil") }
    var origenPlanes by remember { mutableStateOf("wallet") }
    var mostrandoSplash by remember { mutableStateOf(true) }

    LaunchedEffect(solicitudIdInicial) {
        if (solicitudIdInicial > 0) {
            solicitudSeleccionadaId = solicitudIdInicial
            pantallaActual = "detalle"
        }
    }

    if (mostrandoSplash) {
        SplashScreen(onFinalizado = { mostrandoSplash = false })
    } else if (usuarioActual == null) {
        Box(modifier = modifier.fillMaxSize()) {
            LoginScreen(
                authVm = authVm,
                onRegistroCompletado = {
                    pantallaActual = "planes_onboarding"
                }
            )
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
        val estaConectado = rememberEstaConectado()
        val errorSync by solicitudVm.errorSync.collectAsState()

        // Botón Atrás nativo de Android para evitar salir de la app al volver desde sub-pantallas
        BackHandler(enabled = pantallaActual != "bandeja") {
            pantallaActual = when (pantallaActual) {
                "chat" -> origenChat
                "detalle" -> origenDetalle
                "archivo" -> origenArchivo
                "planes" -> origenPlanes
                "dashboard" -> origenDashboard
                "mi_empresa", "gestion_usuarios" -> "perfil"
                else -> "bandeja"
            }
        }

        Column(modifier = modifier.fillMaxSize()) {
            // Franja informativa no intrusiva: avisa cuando se trabaja en modo local Room (offline)
            AnimatedVisibility(visible = !estaConectado) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF3E0))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudOff,
                        contentDescription = null,
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.aviso_sin_conexion),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF92400E),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Aviso descartable si ocurre un error de red/Firestore/Appwrite estando en línea
            AnimatedVisibility(visible = estaConectado && errorSync != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFECEB))
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = errorSync ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFD92D20),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { solicitudVm.limpiarErrorSync() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar aviso",
                            tint = Color(0xFFD92D20),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 1. CONTENIDO DE LA PANTALLA ACTIVA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Crossfade(
                    targetState = pantallaActual,
                    animationSpec = tween(160),
                    label = "navCrossfade"
                ) { rutaActiva ->
                when (rutaActiva) {
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
                        alVolver = { pantallaActual = origenChat },
                        alIrADetalle = { pantallaActual = "detalle" }
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
                        alVolver = { pantallaActual = origenArchivo },
                        alIrADetalle = { id ->
                            solicitudSeleccionadaId = id
                            origenDetalle = "archivo"
                            pantallaActual = "detalle"
                        }
                    )

                    "dashboard" -> AdminDashboardScreen(
                        dashboardVm = dashboardVm,
                        alIrAGestionUsuarios = { pantallaActual = "gestion_usuarios" },
                        alVerAtrasadas = {
                            solicitudVm.cambiarFiltroEstado(null, usuarioActual)
                            solicitudVm.cambiarFiltroPrioridad(null, usuarioActual)
                            solicitudVm.cambiarFiltroTecnico(null, usuarioActual)
                            solicitudVm.cambiarFiltroAtrasadas(true, usuarioActual)
                            pantallaActual = "bandeja"
                        },
                        alFiltrarPrioridad = { prio ->
                            solicitudVm.cambiarFiltroAtrasadas(false, usuarioActual)
                            solicitudVm.cambiarFiltroEstado(null, usuarioActual)
                            solicitudVm.cambiarFiltroTecnico(null, usuarioActual)
                            solicitudVm.cambiarFiltroPrioridad(prio, usuarioActual)
                            pantallaActual = "bandeja"
                        },
                        alFiltrarTecnico = { tecId ->
                            solicitudVm.cambiarFiltroAtrasadas(false, usuarioActual)
                            solicitudVm.cambiarFiltroEstado(null, usuarioActual)
                            solicitudVm.cambiarFiltroPrioridad(null, usuarioActual)
                            solicitudVm.cambiarFiltroTecnico(tecId, usuarioActual)
                            pantallaActual = "bandeja"
                        },
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
                            origenPlanes = "wallet"
                            pantallaActual = "planes"
                        }
                    )

                    "planes" -> PlanesSoporteScreen(
                        usuario = usuarioActual,
                        authVm = authVm,
                        alVolver = {
                            pantallaActual = origenPlanes
                        }
                    )

                    "planes_onboarding" -> PlanesSoporteScreen(
                        usuario = usuarioActual,
                        authVm = authVm,
                        esModoOnboarding = true,
                        alCompletarOnboarding = {
                            pantallaActual = "bandeja"
                        },
                        alVolver = {
                            pantallaActual = "bandeja"
                        }
                    )

                    "mi_empresa" -> MiEmpresaScreen(
                        usuario = usuarioActual,
                        authVm = authVm,
                        alVolver = { pantallaActual = "perfil" }
                    )

                    "perfil" -> PerfilScreen(
                        usuario = usuarioActual,
                        authVm = authVm,
                        archivadosCount = solicitudVm.solicitudesArchivadas.size,
                        alIrAGestionUsuarios = { pantallaActual = "gestion_usuarios" },
                        alIrADashboard = {
                            origenDashboard = "perfil"
                            pantallaActual = "dashboard"
                        },
                        alIrAPlanes = {
                            origenPlanes = "perfil"
                            pantallaActual = "planes"
                        },
                        alIrAMiEmpresa = { pantallaActual = "mi_empresa" },
                        alIrAArchivo = {
                            origenArchivo = "perfil"
                            pantallaActual = "archivo"
                        },
                        alCerrarSesion = {
                            pantallaActual = "bandeja"
                            solicitudVm.limpiarSesion()
                            authVm.cerrarSesion()
                        }
                    )

                    else -> BandejaScreen(
                        usuario = usuarioActual,
                        solicitudVm = solicitudVm,
                        usuariosFiltro = if (usuarioActual.rol == RolUsuario.ADMINISTRADOR) dashboardVm.listaUsuarios else emptyList(),
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
                        alIrAArchivo = {
                            origenArchivo = "bandeja"
                            pantallaActual = "archivo"
                        },
                        alCerrarSesion = {
                            pantallaActual = "bandeja"
                            solicitudVm.limpiarSesion()
                            authVm.cerrarSesion()
                        }
                    )
                }
                }
            }

            // 2. BARRA DE NAVEGACIÓN INFERIOR (Oculta suavemente en chat, nuevo ticket o planes)
            val mostrarBarra = pantallaActual != "chat" && pantallaActual != "planes" && pantallaActual != "planes_onboarding" && pantallaActual != "crear" && pantallaActual != "mi_empresa"
            val haptic = LocalHapticFeedback.current
            val navItemColors = NavigationBarItemDefaults.colors(
                indicatorColor = TeamworkBlue.copy(alpha = 0.10f),
                selectedIconColor = TeamworkBlue,
                selectedTextColor = TeamworkBlue,
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            )
            AnimatedVisibility(
                visible = mostrarBarra,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(180)) + fadeIn(tween(180)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(150)) + fadeOut(tween(150))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = Color(0xFFE5E5EA))
                    // WindowInsets(0) evita doble padding inferior con el Scaffold de MainActivity
                    NavigationBar(
                        containerColor = Color.White,
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        modifier = Modifier.height(64.dp)
                    ) {
                        val esBandejaSeleccionada = pantallaActual == "bandeja" || (pantallaActual == "detalle" && origenDetalle == "bandeja")
                        NavigationBarItem(
                            selected = esBandejaSeleccionada,
                            onClick = { pantallaActual = "bandeja" },
                            colors = navItemColors,
                            icon = {
                                NavIconScaled(esBandejaSeleccionada) {
                                    Icon(Icons.Filled.ConfirmationNumber, contentDescription = stringResource(R.string.nav_bandeja))
                                }
                            },
                            label = {
                                Text(
                                    text = stringResource(R.string.nav_bandeja),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )

                        val esMensajesSeleccionados = pantallaActual == "mensajes" || (pantallaActual == "detalle" && origenDetalle == "mensajes")
                        NavigationBarItem(
                            selected = esMensajesSeleccionados,
                            onClick = { pantallaActual = "mensajes" },
                            colors = navItemColors,
                            icon = {
                                NavIconScaled(esMensajesSeleccionados) {
                                    if (noLeidas > 0) {
                                        BadgedBox(badge = { Badge { Text("$noLeidas") } }) {
                                            Icon(Icons.Filled.Forum, contentDescription = stringResource(R.string.nav_mensajes))
                                        }
                                    } else {
                                        Icon(Icons.Filled.Forum, contentDescription = stringResource(R.string.nav_mensajes))
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = stringResource(R.string.nav_mensajes),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )

                        NavigationBarItem(
                            selected = pantallaActual == "crear",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                solicitudVm.limpiarMensaje()
                                pantallaActual = "crear"
                            },
                            colors = navItemColors.copy(selectedIndicatorColor = Color.Transparent),
                            icon = {
                                Surface(
                                    shape = CircleShape,
                                    color = TeamworkBlue,
                                    shadowElevation = 3.dp,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "Crear nuevo ticket",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = stringResource(R.string.nav_nuevo),
                                    fontWeight = FontWeight.Bold,
                                    color = TeamworkBlue,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )

                        val esWalletSeleccionada = pantallaActual == "wallet" || (pantallaActual == "detalle" && origenDetalle == "wallet")
                        NavigationBarItem(
                            selected = esWalletSeleccionada,
                            onClick = { pantallaActual = "wallet" },
                            colors = navItemColors,
                            icon = {
                                NavIconScaled(esWalletSeleccionada) {
                                    Icon(Icons.Filled.QrCode2, contentDescription = stringResource(R.string.nav_pases_qr))
                                }
                            },
                            label = {
                                Text(
                                    text = stringResource(R.string.nav_pases_qr),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )

                        val esPerfilSeleccionado = pantallaActual == "perfil" || pantallaActual == "archivo" || (pantallaActual == "detalle" && origenDetalle == "archivo")
                        NavigationBarItem(
                            selected = esPerfilSeleccionado,
                            onClick = { pantallaActual = "perfil" },
                            colors = navItemColors,
                            icon = {
                                NavIconScaled(esPerfilSeleccionado) {
                                    Icon(Icons.Filled.Person, contentDescription = stringResource(R.string.nav_perfil))
                                }
                            },
                            label = {
                                Text(
                                    text = stringResource(R.string.nav_perfil),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavIconScaled(
    seleccionado: Boolean,
    content: @Composable () -> Unit
) {
    val escala by animateFloatAsState(
        targetValue = if (seleccionado) 1.14f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
        label = "navIconScale"
    )
    Box(modifier = Modifier.graphicsLayer { scaleX = escala; scaleY = escala }) {
        content()
    }
}

/**
 * Observa en tiempo real la conectividad del dispositivo mediante ConnectivityManager nativo.
 * Permite mostrar el aviso offline sin añadir librerías ni polling manual.
 */
@Composable
private fun rememberEstaConectado(): Boolean {
    val context = LocalContext.current
    val cm = remember(context) {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun verificarRedActual(): Boolean {
        val red = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(red) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    var conectado by remember { mutableStateOf(verificarRedActual()) }

    DisposableEffect(cm) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                conectado = true
            }
            override fun onLost(network: Network) {
                conectado = verificarRedActual()
            }
        }
        runCatching { cm.registerDefaultNetworkCallback(callback) }
        onDispose {
            runCatching { cm.unregisterNetworkCallback(callback) }
        }
    }
    return conectado
}
