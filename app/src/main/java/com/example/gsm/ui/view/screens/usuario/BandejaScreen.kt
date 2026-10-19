package com.example.gsm.ui.view.screens.usuario

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.model.EstadoSolicitud
import com.example.gsm.model.RolUsuario
import com.example.gsm.model.Usuario
import com.example.gsm.ui.view.components.TarjetaSolicitudItem
import com.example.gsm.ui.viewmodel.SolicitudViewModel

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
            .padding(16.dp)
    ) {
        // 1. Encabezado con usuario actual y botón Salir
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bandeja GSM",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${usuario.nombre} • Rol: ${usuario.rol.name}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            OutlinedButton(onClick = alCerrarSesion) {
                Text("Salir")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Botones principales según el Rol
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (usuario.rol == RolUsuario.CLIENTE) {
                Button(
                    onClick = alIrACrear,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+ Nueva Solicitud")
                }
            }
            if (usuario.rol == RolUsuario.ADMINISTRADOR) {
                Button(
                    onClick = alIrADashboard,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📊 Panel de Métricas")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Buscador por código SOL-YYYY-XXXX o título
        OutlinedTextField(
            value = solicitudVm.textoBusqueda,
            onValueChange = { solicitudVm.cambiarTextoBusqueda(it, usuario) },
            label = { Text("🔍 Buscar por código (SOL-2026...) o título") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Filtros rápidos horizontales
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = solicitudVm.estadoFiltro == null && !solicitudVm.soloAtrasadas,
                onClick = { solicitudVm.cambiarFiltroEstado(null, usuario) },
                label = { Text("Todas") }
            )
            FilterChip(
                selected = solicitudVm.soloAtrasadas,
                onClick = {
                    solicitudVm.cambiarFiltroAtrasadas(!solicitudVm.soloAtrasadas, usuario)
                },
                label = { Text("⚠️ Atrasadas") }
            )
            EstadoSolicitud.entries.forEach { estado ->
                FilterChip(
                    selected = solicitudVm.estadoFiltro == estado,
                    onClick = { solicitudVm.cambiarFiltroEstado(estado, usuario) },
                    label = { Text(estado.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Lista de solicitudes usando el componente TarjetaSolicitudItem
        if (solicitudVm.listaSolicitudes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay solicitudes para mostrar con este filtro.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
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
