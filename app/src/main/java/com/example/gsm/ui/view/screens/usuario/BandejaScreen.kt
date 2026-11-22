package com.example.gsm.ui.view.screens.usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.QrScannerModal
import com.example.gsm.ui.view.components.QrNoEncontradoDialog
import com.example.gsm.ui.view.components.SkeletonTarjetaTicket
import com.example.gsm.ui.view.components.TarjetaSolicitudItem
import com.example.gsm.ui.view.screens.usuario.componentes.AppleBackground
import com.example.gsm.ui.view.screens.usuario.componentes.BarraFiltrosBandeja
import com.example.gsm.ui.view.screens.usuario.componentes.EstadoVacioCard
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import com.example.gsm.ui.viewmodel.SolicitudViewModel

/**
 * Pantalla Principal de la Bandeja de Tickets GSM (Patrón KISS).
 * Rediseñada con filtros mediante menús desplegables (DropdownMenu),
 * barra de búsqueda moderna y diseño visual limpio estilo Apple HIG.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandejaScreen(
    usuario: Usuario,
    solicitudVm: SolicitudViewModel,
    usuariosFiltro: List<Usuario> = emptyList(),
    alIrACrear: () -> Unit,
    alIrADetalle: (Int) -> Unit,
    alIrADashboard: () -> Unit,
    alIrAArchivo: () -> Unit = {},
    alCerrarSesion: () -> Unit
) {
    var mostrarQrScanner by remember { mutableStateOf(false) }
    var qrNoEncontradoCodigo by remember { mutableStateOf<String?>(null) }
    var ordenarPorSla by remember { mutableStateOf(false) }

    // Recarga los tickets del rol/organización solo si cambia la sesión activa
    LaunchedEffect(usuario.id) {
        solicitudVm.cargarSolicitudes(usuario)
    }

    // Comprobación reactiva si hay algún filtro activo
    val hayFiltroActivo = solicitudVm.estadoFiltro != null ||
            solicitudVm.prioridadFiltro != null ||
            solicitudVm.soloAtrasadas ||
            solicitudVm.clienteFiltroId != null ||
            solicitudVm.tecnicoFiltroId != null ||
            solicitudVm.textoBusqueda.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 1. HEADER LIMPIO (Título + Badge de Rol + Botón directo a Nuevo Ticket)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Bandeja GSM",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
                Text(
                    text = "Hola, ${usuario.nombre}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Botones de acción en la cabecera
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (usuario.rol == RolUsuario.ADMINISTRADOR) {
                    IconButton(
                        onClick = alIrADashboard,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BarChart,
                            contentDescription = "Panel de Métricas",
                            tint = TeamworkBlue
                        )
                    }
                }

                // Acceso directo en 1 toque al Archivo sin recargar la NavigationBar
                val archivadosCount = solicitudVm.solicitudesArchivadas.size
                IconButton(
                    onClick = alIrAArchivo,
                    modifier = Modifier.size(38.dp)
                ) {
                    if (archivadosCount > 0) {
                        BadgedBox(badge = { Badge { Text("$archivadosCount") } }) {
                            Icon(
                                imageVector = Icons.Filled.Inventory2,
                                contentDescription = "Archivo de tickets",
                                tint = Color(0xFF475569)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Inventory2,
                            contentDescription = "Archivo de tickets",
                            tint = Color(0xFF475569)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. BARRA DE HERRAMIENTAS COMPACTA: BÚSQUEDA + QR + FILTROS DESPLEGABLES
        BarraFiltrosBandeja(
            usuario = usuario,
            solicitudVm = solicitudVm,
            usuariosFiltro = usuariosFiltro,
            ordenarPorSla = ordenarPorSla,
            onCambiarOrdenSla = { ordenarPorSla = it },
            hayFiltroActivo = hayFiltroActivo,
            onAbrirQr = { mostrarQrScanner = true }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filtro inteligente de bandeja: por defecto solo tickets activos (no cerrados ni archivados)
        val solicitudesBase = if (hayFiltroActivo || solicitudVm.estadoFiltro != null) {
            solicitudVm.listaSolicitudes
        } else {
            solicitudVm.solicitudesActivas
        }
        val solicitudesVisibles = remember(solicitudesBase, ordenarPorSla) {
            if (ordenarPorSla) {
                solicitudesBase.sortedBy { it.fechaLimite ?: Long.MAX_VALUE }
            } else {
                solicitudesBase
            }
        }

        // 3. CABECERA DE RESULTADOS (conteo exacto de tarjetas visibles)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (hayFiltroActivo) "RESULTADOS" else "SOLICITUDES ACTIVAS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Surface(
                shape = CircleShape,
                color = if (solicitudesVisibles.isNotEmpty()) TeamworkBlue.copy(alpha = 0.12f) else Color(0xFFE5E5EA)
            ) {
                Text(
                    text = if (hayFiltroActivo) "${solicitudesVisibles.size} resultados" else "${solicitudesVisibles.size} activos",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (solicitudesVisibles.isNotEmpty()) TeamworkBlue else Color(0xFF636366),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. LISTA DE TICKETS, SKELETONS DE CARGA O ESTADO VACÍO
        if (solicitudVm.cargando && solicitudesVisibles.isEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(4) {
                    SkeletonTarjetaTicket()
                }
            }
        } else if (solicitudesVisibles.isEmpty()) {
            EstadoVacioCard(
                icono = Icons.Filled.Inbox,
                titulo = "Bandeja al día",
                subtitulo = if (hayFiltroActivo || solicitudVm.textoBusqueda.isNotEmpty()) {
                    "No se encontraron tickets con los filtros actuales."
                } else {
                    "No tienes solicitudes pendientes en este momento."
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (hayFiltroActivo) {
                    OutlinedButton(
                        onClick = {
                            solicitudVm.cambiarTextoBusqueda("", usuario)
                            solicitudVm.cambiarFiltroAtrasadas(false, usuario)
                            solicitudVm.cambiarFiltroEstado(null, usuario)
                            solicitudVm.cambiarFiltroPrioridad(null, usuario)
                            solicitudVm.cambiarFiltroTecnico(null, usuario)
                            solicitudVm.cambiarFiltroCliente(null, usuario)
                            ordenarPorSla = false
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Quitar todos los filtros", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = alIrACrear,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Crear primera solicitud", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            val ahoraLista = remember(solicitudesVisibles) { System.currentTimeMillis() }
            PullToRefreshBox(
                isRefreshing = solicitudVm.cargando,
                onRefresh = { solicitudVm.cargarSolicitudes(usuario) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = solicitudesVisibles,
                        key = { it.id }
                    ) { solicitud ->
                        TarjetaSolicitudItem(
                            solicitud = solicitud,
                            ahora = ahoraLista,
                            modifier = Modifier.animateItem(),
                            alHacerClic = { alIrADetalle(solicitud.id) }
                        )
                    }
                }
            }
        }
    }

    // Modal de Escaneo con CameraX
    if (mostrarQrScanner) {
        QrScannerModal(
            onDismiss = { mostrarQrScanner = false },
            onQrDetectado = { codigoLeido ->
                mostrarQrScanner = false
                solicitudVm.buscarPorQr(
                    codigoQr = codigoLeido,
                    alEncontrar = { idTicket ->
                        alIrADetalle(idTicket)
                    },
                    alFallar = {
                        qrNoEncontradoCodigo = codigoLeido
                    }
                )
            }
        )
    }

    // Modal animado si el QR no coincide con ningún ticket existente
    if (qrNoEncontradoCodigo != null) {
        QrNoEncontradoDialog(
            codigoEscaneado = qrNoEncontradoCodigo!!,
            onReintentar = {
                qrNoEncontradoCodigo = null
                mostrarQrScanner = true
            },
            onCerrar = {
                qrNoEncontradoCodigo = null
            }
        )
    }
}
