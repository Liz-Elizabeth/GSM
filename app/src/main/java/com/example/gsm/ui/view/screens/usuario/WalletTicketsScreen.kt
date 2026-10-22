package com.example.gsm.ui.view.screens.usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.QrScannerModal
import com.example.gsm.ui.view.components.QrNoEncontradoDialog
import com.example.gsm.ui.view.screens.usuario.componentes.AppleBackground
import com.example.gsm.ui.view.screens.usuario.componentes.TarjetaWalletTicket
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import com.example.gsm.ui.viewmodel.SolicitudViewModel

/**
 * Pantalla Billetera de Pases Digitales (Estilo Apple Wallet / TicketMaster).
 * Muestra las solicitudes en formato de entrada/pase troquelado con su QR en alta definición.
 * Principio KISS: Modular, conectada a SolicitudViewModel existente sin código duplicado.
 */
@Composable
fun WalletTicketsScreen(
    usuario: Usuario,
    solicitudVm: SolicitudViewModel,
    alIrADetalle: (Int) -> Unit
) {
    var filtroSoloActivas by remember { mutableStateOf(true) }
    var mostrarQrScanner by remember { mutableStateOf(false) }
    var qrNoEncontradoCodigo by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(usuario.id) {
        solicitudVm.cargarSolicitudes(usuario)
    }

    val listaFiltrada = remember(solicitudVm.listaSolicitudes, filtroSoloActivas) {
        if (filtroSoloActivas) {
            solicitudVm.listaSolicitudes.filter {
                it.estado != EstadoSolicitud.CERRADA && it.estado != EstadoSolicitud.CANCELADA
            }
        } else {
            solicitudVm.listaSolicitudes
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 1. ENCABEZADO ESTILO APPLE WALLET
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🎟️ Pases Digitales",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = TeamworkBlue.copy(alpha = 0.12f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${listaFiltrada.size} activos",
                            style = MaterialTheme.typography.labelSmall,
                            color = TeamworkBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Billetera de tickets y credenciales técnicas GSM",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Botón de Escáner rápido CameraX
            IconButton(
                onClick = { mostrarQrScanner = true },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
            ) {
                Text("📷", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. SELECTOR DE FILTRO RÁPIDO (Activas / Todos los Pases)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filtroSoloActivas,
                onClick = { filtroSoloActivas = true },
                label = { Text("⚡ Pases Activos") },
                shape = CircleShape
            )
            FilterChip(
                selected = !filtroSoloActivas,
                onClick = { filtroSoloActivas = false },
                label = { Text("📚 Historial Completo") },
                shape = CircleShape
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. LISTADO DE PASES DIGITALES
        if (listaFiltrada.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🎟️", style = MaterialTheme.typography.displayMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Sin Pases en tu Billetera",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (filtroSoloActivas)
                            "No tienes solicitudes activas en este momento."
                        else
                            "No se registran solicitudes históricas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(listaFiltrada) { sol ->
                    TarjetaWalletTicket(
                        solicitud = sol,
                        alVerDetalle = { alIrADetalle(sol.id) }
                    )
                }
            }
        }
    }

    // Modal de Escáner CameraX invocado desde la Wallet
    if (mostrarQrScanner) {
        QrScannerModal(
            onDismiss = { mostrarQrScanner = false },
            onQrDetectado = { codigoDetectado ->
                mostrarQrScanner = false
                solicitudVm.buscarPorQr(
                    codigoQr = codigoDetectado,
                    alEncontrar = { idTicket ->
                        alIrADetalle(idTicket)
                    },
                    alFallar = {
                        qrNoEncontradoCodigo = codigoDetectado
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
