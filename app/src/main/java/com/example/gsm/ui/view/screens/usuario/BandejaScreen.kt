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

// Paleta minimalista estilo iOS (System Grouped Background)
private val AppleBackground = Color(0xFFF2F2F7)
private val AppleSearchFill = Color(0xFFE5E5EA)

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
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // 1. Encabezado limpio estilo iOS Large Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bandeja GSM",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${usuario.nombre} • ${usuario.rol.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = alCerrarSesion) {
                Text(
                    text = "Salir",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Acción principal con bordes suaves (12.dp)
        if (usuario.rol == RolUsuario.CLIENTE || usuario.rol == RolUsuario.ADMINISTRADOR) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (usuario.rol == RolUsuario.CLIENTE) {
                    Button(
                        onClick = alIrACrear,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+ Nueva Solicitud", fontWeight = FontWeight.SemiBold)
                    }
                }
                if (usuario.rol == RolUsuario.ADMINISTRADOR) {
                    Button(
                        onClick = alIrADashboard,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("📊 Panel de Métricas", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 3. Buscador estilo Spotlight (relleno suave, sin bordes duros)
        TextField(
            value = solicitudVm.textoBusqueda,
            onValueChange = { solicitudVm.cambiarTextoBusqueda(it, usuario) },
            placeholder = {
                Text(
                    text = "🔍 Buscar por código (SOL-2026...) o título",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = AppleSearchFill,
                unfocusedContainerColor = AppleSearchFill,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Filtros rápidos tipo píldora (CircleShape y sin borde)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    label = { Text(estado.name) },
                    shape = CircleShape,
                    border = null
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Lista de solicitudes con espaciado respirable
        if (solicitudVm.listaSolicitudes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay solicitudes para mostrar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
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