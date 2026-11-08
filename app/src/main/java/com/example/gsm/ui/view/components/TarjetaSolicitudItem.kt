package com.example.gsm.ui.view.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.Solicitud
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Paleta Apple + Teamwork (Baseline UI Tokens)
private val TeamworkBlue = Color(0xFF0066FF)
private val NeutralPillBg = Color(0xFFF2F2F7)
private val NeutralBorder = Color(0xFFE5E5EA)
private val AlertRedBg = Color(0xFFFFECEB)
private val AlertRedText = Color(0xFFD92D20)
private val SuccessGreenBg = Color(0xFFE8F8EE)
private val SuccessGreenText = Color(0xFF1B7F37)
private val WarningAmberBg = Color(0xFFFEF3EB)
private val WarningAmberText = Color(0xFFB54708)

fun formatearFechaHora(milisegundos: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(milisegundos))
}

private fun formatearEstado(estado: EstadoSolicitud): String {
    return when (estado) {
        EstadoSolicitud.RECIBIDA -> "Recibida"
        EstadoSolicitud.EN_REVISION -> "En revisión"
        EstadoSolicitud.ASIGNADA -> "Asignada"
        EstadoSolicitud.EN_PROCESO -> "En proceso"
        EstadoSolicitud.PENDIENTE_INFORMACION -> "Pendiente info"
        EstadoSolicitud.RESUELTA -> "Resuelta"
        EstadoSolicitud.CERRADA -> "Cerrada"
        EstadoSolicitud.CANCELADA -> "Cancelada"
    }
}

private fun formatearPrioridad(prioridad: PrioridadSolicitud): String {
    return when (prioridad) {
        PrioridadSolicitud.BAJA -> "Baja"
        PrioridadSolicitud.MEDIA -> "Media"
        PrioridadSolicitud.ALTA -> "Alta"
        PrioridadSolicitud.URGENTE -> "Urgente"
    }
}

/**
 * Componente reutilizable para mostrar una tarjeta de solicitud en la bandeja.
 * Estilo refinado Baseline UI: jerarquía tipográfica limpia, sin sobrecarga visual,
 * código monospace y contraste accesible.
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

    val (fondoEstado, textoEstado) = when (solicitud.estado) {
        EstadoSolicitud.RESUELTA, EstadoSolicitud.CERRADA -> Pair(SuccessGreenBg, SuccessGreenText)
        EstadoSolicitud.CANCELADA -> Pair(AlertRedBg, AlertRedText)
        EstadoSolicitud.PENDIENTE_INFORMACION -> Pair(WarningAmberBg, WarningAmberText)
        else -> Pair(TeamworkBlue.copy(alpha = 0.10f), TeamworkBlue)
    }

    Surface(
        onClick = alHacerClic,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, NeutralBorder.copy(alpha = 0.8f)),
        shadowElevation = 0.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. FILA SUPERIOR: Código Monospace + Badges de Estado / SLA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NeutralPillBg
                ) {
                    Text(
                        text = solicitud.codigoSeguimiento,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (estaAtrasada) {
                        Surface(color = AlertRedBg, shape = CircleShape) {
                            Text(
                                text = "Atrasada",
                                style = MaterialTheme.typography.labelSmall,
                                color = AlertRedText,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Surface(
                        color = fondoEstado,
                        shape = CircleShape
                    ) {
                        Text(
                            text = formatearEstado(solicitud.estado),
                            style = MaterialTheme.typography.labelSmall,
                            color = textoEstado,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // 2. TÍTULO DEL TICKET (Text-balance, maxLines 2, ellipsis)
            Text(
                text = solicitud.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // 3. PIE DE TARJETA: Categoría + Prioridad + Chevron iOS (›)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = solicitud.categoria.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8E8E93),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(color = NeutralPillBg, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = formatearPrioridad(solicitud.prioridad),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF636366),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "›",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFC7C7CC),
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}
