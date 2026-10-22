package com.example.gsm.ui.view.screens.usuario.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.Solicitud
import com.example.gsm.ui.view.components.formatearFechaHora

/**
 * Fila de metadato clave-valor con tipografía limpia estilo iOS Settings.
 */
@Composable
private fun FilaMetadato(icono: String, etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$icono $etiqueta",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Ficha del ticket estilo Inset Grouped de Apple + Teamwork Desk.
 * Muestra el estado, prioridad, título, descripción, metadatos y la solución técnica.
 */
@Composable
fun FichaResumenTicket(solicitud: Solicitud) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SeccionTitulo("Resumen del Ticket")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Fila de Badges: Estado + Prioridad + SLA
                val ahora = System.currentTimeMillis()
                val estaAtrasada = solicitud.fechaLimite != null &&
                        solicitud.fechaLimite < ahora &&
                        solicitud.estado != EstadoSolicitud.RESUELTA &&
                        solicitud.estado != EstadoSolicitud.CERRADA &&
                        solicitud.estado != EstadoSolicitud.CANCELADA

                val (fondoEstado, textoEstado) = when (solicitud.estado) {
                    EstadoSolicitud.CERRADA, EstadoSolicitud.RESUELTA -> Pair(SuccessGreenBg, SuccessGreenText)
                    EstadoSolicitud.CANCELADA -> Pair(AlertRedBg, AlertRedText)
                    EstadoSolicitud.PENDIENTE_INFORMACION -> Pair(InternalNoteBg, InternalNoteText)
                    else -> Pair(TeamworkBlue.copy(alpha = 0.12f), TeamworkBlue)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        BadgePill(
                            texto = "📌 ${solicitud.estado.name}",
                            colorFondo = fondoEstado,
                            colorTexto = textoEstado
                        )
                        BadgePill(
                            texto = "⚡ ${solicitud.prioridad.name}",
                            colorFondo = AppleBackground,
                            colorTexto = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (estaAtrasada) {
                        BadgePill(
                            texto = "⚠️ SLA Vencido",
                            colorFondo = AlertRedBg,
                            colorTexto = AlertRedText
                        )
                    }
                }

                // 2. Título y Descripción del Ticket
                Text(
                    text = solicitud.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = solicitud.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = AppleBackground, modifier = Modifier.padding(vertical = 2.dp))

                // 3. Metadatos del Ticket
                FilaMetadato(icono = "🏷️", etiqueta = "Categoría", valor = solicitud.categoria.descripcion)
                FilaMetadato(icono = "📅", etiqueta = "Fecha Creación", valor = formatearFechaHora(solicitud.fechaCreacion))
                FilaMetadato(icono = "🕒", etiqueta = "Última Actualización", valor = formatearFechaHora(solicitud.fechaActualizacion))

                if (!solicitud.servicioRelacionado.isNullOrBlank()) {
                    FilaMetadato(icono = "🛠️", etiqueta = "Servicio", valor = solicitud.servicioRelacionado)
                }
                if (!solicitud.activoRelacionado.isNullOrBlank()) {
                    FilaMetadato(icono = "🌐", etiqueta = "Activo Relacionado", valor = solicitud.activoRelacionado)
                }

                // 4. Solución Técnica (si existe)
                if (!solicitud.resolucion.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SuccessGreenBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "✅ Solución Técnica Confirmada",
                                style = MaterialTheme.typography.labelMedium,
                                color = SuccessGreenText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = solicitud.resolucion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SuccessGreenText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
