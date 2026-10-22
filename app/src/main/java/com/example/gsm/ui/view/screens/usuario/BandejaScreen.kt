package com.example.gsm.ui.view.screens.usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.QrScannerModal
import com.example.gsm.ui.view.components.QrNoEncontradoDialog
import com.example.gsm.ui.view.components.TarjetaSolicitudItem
import com.example.gsm.ui.viewmodel.SolicitudViewModel

// Paleta Apple + Teamwork (Fondo agrupado y acento corporativo)
private val AppleBackground = Color(0xFFF2F2F7)
private val AppleInputFill = Color(0xFFF2F2F7)
private val TeamworkBlue = Color(0xFF0066FF)

@Composable
fun BandejaScreen(
    usuario: Usuario,
    solicitudVm: SolicitudViewModel,
    alIrACrear: () -> Unit,
    alIrADetalle: (Int) -> Unit,
    alIrADashboard: () -> Unit,
    alCerrarSesion: () -> Unit
) {
    var mostrarQrScanner by remember { mutableStateOf(false) }
    var qrNoEncontradoCodigo by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(usuario.id) {
        solicitudVm.cargarSolicitudes(usuario)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 1. HEADER LIMPIO (Título + Badge RBAC en la primera línea, Nombre completo abajo sin cortarse)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Bandeja GSM",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = TeamworkBlue.copy(alpha = 0.12f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = usuario.rol.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = TeamworkBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = usuario.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. BARRA DE HERRAMIENTAS UNIFICADA (Buscador + Filtros en una sola tarjeta limpia)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Buscador Spotlight con botón '✕' + Lector QR con CameraX
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = solicitudVm.textoBusqueda,
                        onValueChange = { solicitudVm.cambiarTextoBusqueda(it, usuario) },
                        placeholder = {
                            Text(
                                text = "🔍 Buscar código o título...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        trailingIcon = {
                            if (solicitudVm.textoBusqueda.isNotEmpty()) {
                                TextButton(onClick = { solicitudVm.cambiarTextoBusqueda("", usuario) }) {
                                    Text("✕", fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = AppleInputFill,
                            unfocusedContainerColor = AppleInputFill,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilledTonalButton(
                        onClick = { mostrarQrScanner = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = TeamworkBlue.copy(alpha = 0.12f),
                            contentColor = TeamworkBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = "Escanear QR",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "QR",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Filtros tipo píldora por Estado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = solicitudVm.estadoFiltro == null && !solicitudVm.soloAtrasadas,
                        onClick = { solicitudVm.cambiarFiltroEstado(null, usuario) },
                        label = { Text("Todas") },
                        shape = CircleShape,
                        border = null
                    )
                    FilterChip(
                        selected = solicitudVm.soloAtrasadas,
                        onClick = {
                            solicitudVm.cambiarFiltroAtrasadas(!solicitudVm.soloAtrasadas, usuario)
                        },
                        label = { Text("⚠️ Atrasadas") },
                        shape = CircleShape,
                        border = null
                    )
                    EstadoSolicitud.entries.forEach { estado ->
                        FilterChip(
                            selected = solicitudVm.estadoFiltro == estado,
                            onClick = { solicitudVm.cambiarFiltroEstado(estado, usuario) },
                            label = {
                                Text(
                                    estado.name.replace("_", " ")
                                        .lowercase()
                                        .replaceFirstChar { it.uppercase() }
                                )
                            },
                            shape = CircleShape,
                            border = null
                        )
                    }
                }

                // Filtros adicionales de Prioridad, Cliente y Técnico (Criterios 25, 26 y 27)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PrioridadSolicitud.entries.forEach { prio ->
                        FilterChip(
                            selected = solicitudVm.prioridadFiltro == prio,
                            onClick = {
                                val nueva = if (solicitudVm.prioridadFiltro == prio) null else prio
                                solicitudVm.cambiarFiltroPrioridad(nueva, usuario)
                            },
                            label = { Text("⚡ ${prio.name}") },
                            shape = CircleShape,
                            border = null
                        )
                    }

                    if (usuario.rol == RolUsuario.ADMINISTRADOR) {
                        FilterChip(
                            selected = solicitudVm.clienteFiltroId == 1,
                            onClick = {
                                val nuevo = if (solicitudVm.clienteFiltroId == 1) null else 1
                                solicitudVm.cambiarFiltroCliente(nuevo, usuario)
                            },
                            label = { Text("👤 Cliente: Camila") },
                            shape = CircleShape,
                            border = null
                        )
                        FilterChip(
                            selected = solicitudVm.tecnicoFiltroId == 3,
                            onClick = {
                                val nuevo = if (solicitudVm.tecnicoFiltroId == 3) null else 3
                                solicitudVm.cambiarFiltroTecnico(nuevo, usuario)
                            },
                            label = { Text("🔧 Téc: Diego") },
                            shape = CircleShape,
                            border = null
                        )
                        FilterChip(
                            selected = solicitudVm.tecnicoFiltroId == 4,
                            onClick = {
                                val nuevo = if (solicitudVm.tecnicoFiltroId == 4) null else 4
                                solicitudVm.cambiarFiltroTecnico(nuevo, usuario)
                            },
                            label = { Text("🔧 Téc: Valentina") },
                            shape = CircleShape,
                            border = null
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. CABECERA DE COLA DE TICKETS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SOLICITUDES",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${solicitudVm.listaSolicitudes.size} resultados",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 4. LISTA DE SOLICITUDES O ESTADO VACÍO AMIGABLE
        if (solicitudVm.listaSolicitudes.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📭", style = MaterialTheme.typography.displaySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bandeja al día",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "No hay solicitudes que coincidan con este filtro.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(solicitudVm.listaSolicitudes) { solicitud ->
                    TarjetaSolicitudItem(
                        solicitud = solicitud,
                        alHacerClic = { alIrADetalle(solicitud.id) }
                    )
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
