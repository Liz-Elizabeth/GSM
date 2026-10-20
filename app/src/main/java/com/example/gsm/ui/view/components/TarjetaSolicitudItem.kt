package com.example.gsm.ui.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.Solicitud
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Paleta Apple + Teamwork
private val TeamworkBlue = Color(0xFF0066FF)
private val NeutralPillBg = Color(0xFFF2F2F7)
private val AlertRedBg = Color(0xFFFFECEB)
private val AlertRedText = Color(0xFFD92D20)
private val SuccessGreenBg = Color(0xFFE8F8EE)
private val SuccessGreenText = Color(0xFF1B7F37)

fun formatearFechaHora(milisegundos: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(milisegundos))
}

/**
 * Componente reutilizable para mostrar una tarjeta de solicitud en la bandeja.
 * Estilo híbrido Teamwork Desk Ticket Card + Apple Inset Grouped Row.
 */
@Composable
fun TarjetaSolicitudItem(
    solicitud: Solicitud,
    alHacerClic: () -> Unit
) {
    val ahora = System.currentTimeMillis()
    val estaAtrasada = solicitud.fechaLimite != null &&
            solicitud.fechaLimite < ahora &&
            solicitud.estado != EstadoSolicitud.RESUELTA &&
            solicitud.estado != EstadoSolicitud.CERRADA &&
            solicitud.estado != EstadoSolicitud.CANCELADA

    val esResueltaOCerrada = solicitud.estado == EstadoSolicitud.RESUELTA ||
            solicitud.estado == EstadoSolicitud.CERRADA

    Surface(
        onClick = alHacerClic,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. FILA SUPERIOR: Código de ticket + Badges de Estado / SLA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = solicitud.codigoSeguimiento,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TeamworkBlue
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (estaAtrasada) {
                        Surface(color = AlertRedBg, shape = CircleShape) {
                            Text(
                                text = "⚠️ Atrasada",
                                style = MaterialTheme.typography.labelSmall,
                                color = AlertRedText,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Píldora de Estado (Verde si está resuelta, Azul suave si está activa)
                    Surface(
                        color = if (esResueltaOCerrada) SuccessGreenBg else TeamworkBlue.copy(alpha = 0.1f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = solicitud.estado.name.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (esResueltaOCerrada) SuccessGreenText else TeamworkBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 2. TÍTULO DEL TICKET
            Text(
                text = solicitud.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // 3. PIE DE TARJETA: Categoría + Prioridad + Chevron iOS (›)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏷️ ${solicitud.categoria.descripcion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(color = NeutralPillBg, shape = CircleShape) {
                    Text(
                        text = "⚡ ${solicitud.prioridad.name}  ›",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // 4. FECHA DE ÚLTIMA ACTUALIZACIÓN (Criterio 10)
            Text(
                text = "🕒 Última actualización: ${formatearFechaHora(solicitud.fechaActualizacion)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
