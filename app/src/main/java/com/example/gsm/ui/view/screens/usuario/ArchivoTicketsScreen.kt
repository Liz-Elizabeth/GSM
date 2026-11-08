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
import com.example.gsm.ui.viewmodel.SolicitudViewModel

private val AppleBackground = Color(0xFFF2F2F7)
private val TeamworkBlue = Color(0xFF0066FF)

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
    val archivadas = remember(solicitudVm.listaSolicitudes) {
        solicitudVm.solicitudesArchivadas
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

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Lista de tickets archivados o estado vacío
        if (archivadas.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AppleBackground,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Inventory2,
                                contentDescription = null,
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sin tickets archivados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Los tickets que cierres o que sean cancelados aparecerán en este archivo de respaldo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8E8E93),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
