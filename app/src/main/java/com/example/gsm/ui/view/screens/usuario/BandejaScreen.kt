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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
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
    LaunchedEffect(usuario.id) {
        solicitudVm.cargarSolicitudes(usuario)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 1. HEADER ESTILO TEAMWORK + APPLE (Usuario + Badge de Rol + Salir)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Bandeja GSM",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = usuario.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Badge de Rol estilo Teamwork
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
            }

            FilledTonalButton(
                onClick = alCerrarSesion,
                shape = CircleShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.error
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Salir", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. BARRA DE HERRAMIENTAS UNIFICADA (Acción + Buscador + Filtros en una sola tarjeta limpia)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Botón de acción rápida según rol (Estilo Teamwork CTA)
                if (usuario.rol == RolUsuario.CLIENTE) {
                    Button(
                        onClick = alIrACrear,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue)
                    ) {
                        Text("＋ Nueva Solicitud", fontWeight = FontWeight.SemiBold)
                    }
                } else if (usuario.rol == RolUsuario.ADMINISTRADOR) {
                    Button(
                        onClick = alIrADashboard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue)
                    ) {
                        Text("📊 Panel de Métricas", fontWeight = FontWeight.SemiBold)
                    }
                }

                // Buscador Spotlight con botón '✕' para limpiar rápido
                TextField(
                    value = solicitudVm.textoBusqueda,
                    onValueChange = { solicitudVm.cambiarTextoBusqueda(it, usuario) },
                    placeholder = {
                        Text(
                            text = "🔍 Buscar código (SOL-2026...) o título",
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
                    modifier = Modifier.fillMaxWidth()
                )

                // Filtros tipo píldora (con nombres limpios sin guiones bajos)
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
                            // Convierte "EN_PROCESO" -> "En proceso"
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
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. CABECERA DE COLA DE TICKETS (Contador en vivo estilo Teamwork)
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
}