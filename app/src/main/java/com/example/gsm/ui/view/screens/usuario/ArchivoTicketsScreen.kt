package com.example.gsm.ui.view.screens.usuario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.Solicitud
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.view.components.formatearFechaHora
import com.example.gsm.ui.view.screens.usuario.componentes.AppleBackground
import com.example.gsm.ui.view.screens.usuario.componentes.EstadoVacioCard
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import com.example.gsm.ui.viewmodel.SolicitudViewModel

/**
 * Pantalla de Archivo de Tickets (KISS).
 * 
 * Centraliza las solicitudes resueltas conforme, cerradas y canceladas.
 * Mantiene la bandeja principal libre de ruido visual mientras preserva el 100% de la trazabilidad y respaldo.
 */
@Composable
fun ArchivoTicketsScreen(
    usuario: Usuario,
    solicitudVm: SolicitudViewModel,
    alVolver: () -> Unit,
    alIrADetalle: (Int) -> Unit
) {
    var textoBusqueda by remember { mutableStateOf("") }
    var filtroEstado by remember { mutableStateOf<EstadoSolicitud?>(null) }

    LaunchedEffect(Unit) {
        solicitudVm.sincronizarArchivoCompleto()
    }

    // Se recalcula solo cuando cambia la lista fuente, el chip de estado o la búsqueda local
    val archivadas = remember(solicitudVm.listaSolicitudes, textoBusqueda, filtroEstado) {
        val base = solicitudVm.solicitudesArchivadas
        val porEstado = if (filtroEstado == null) base else base.filter { it.estado == filtroEstado }
        val query = textoBusqueda.trim().lowercase()
        if (query.isEmpty()) porEstado
        else porEstado.filter {
            it.codigoSeguimiento.lowercase().contains(query) ||
                it.titulo.lowercase().contains(query)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 1. Cabecera con botón de retorno al Perfil
        EncabezadoPantalla(
            titulo = "Archivo de Tickets",
            textoBotonDerecho = "Volver",
            alHacerClicDerecho = alVolver
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Banner informativo de política de archivo
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE5E5EA)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = TeamworkBlue.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Archive,
                            contentDescription = null,
                            tint = TeamworkBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Almacén y Respaldo Permanente",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Text(
                        text = "Aquí encuentras tickets cerrados con tu conformidad o cancelados. Puedes auditarlos o solicitar reaperturas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8E8E93)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (solicitudVm.solicitudesArchivadas.isNotEmpty()) {
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = { Text("Buscar por código o título...") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = TeamworkBlue)
                },
                trailingIcon = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { textoBusqueda = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = TeamworkBlue,
                    unfocusedBorderColor = Color(0xFFE5E5EA)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val opcionesFiltro = listOf(
                    null to "Todas",
                    EstadoSolicitud.CERRADA to "Cerradas",
                    EstadoSolicitud.CANCELADA to "Canceladas"
                )
                opcionesFiltro.forEach { (estado, etiqueta) ->
                    FilterChip(
                        selected = filtroEstado == estado,
                        onClick = { filtroEstado = estado },
                        label = { Text(etiqueta) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 3. Lista de tickets archivados o estado vacío
        if (archivadas.isEmpty()) {
            EstadoVacioCard(
                icono = Icons.Filled.Inventory2,
                titulo = if (textoBusqueda.isNotBlank() || filtroEstado != null) "Sin coincidencias" else "Sin tickets archivados",
                subtitulo = "Los tickets que cierres o que sean cancelados aparecerán en este archivo de respaldo.",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(items = archivadas, key = { it.id }) { solicitud ->
                    TarjetaTicketArchivado(
                        solicitud = solicitud,
                        alVerDetalle = { alIrADetalle(solicitud.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaTicketArchivado(
    solicitud: Solicitud,
    alVerDetalle: () -> Unit
) {
    val esCancelada = solicitud.estado == EstadoSolicitud.CANCELADA
    val colorEstado = if (esCancelada) Color(0xFFD92D20) else Color(0xFF027A48)
    val fondoEstado = if (esCancelada) Color(0xFFFEE4E2) else Color(0xFFECFDF3)
    val etiquetaEstado = if (esCancelada) "Cancelada" else "Cerrada / Conforme"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E5EA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Fila Superior: Código y Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${solicitud.codigoSeguimiento}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TeamworkBlue
                )
                Surface(
                    color = fondoEstado,
                    shape = CircleShape
                ) {
                    Text(
                        text = etiquetaEstado,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorEstado,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Título
            Text(
                text = solicitud.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Resumen de resolución o cancelación
            if (esCancelada && !solicitud.motivoCancelacion.isNullOrBlank()) {
                Surface(
                    color = Color(0xFFFFF1F2),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Motivo: ${solicitud.motivoCancelacion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9F1239),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else if (!solicitud.resolucion.isNullOrBlank()) {
                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Solución: ${solicitud.resolucion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF166534),
                        modifier = Modifier.padding(8.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Fila Inferior: Fecha y Botón de Acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registrado: ${formatearFechaHora(solicitud.fechaCreacion)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8E8E93)
                )

                TextButton(
                    onClick = alVerDetalle,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (esCancelada) "Revisar / Reabrir ›" else "Ver Detalle ›",
                        fontWeight = FontWeight.Bold,
                        color = TeamworkBlue,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
