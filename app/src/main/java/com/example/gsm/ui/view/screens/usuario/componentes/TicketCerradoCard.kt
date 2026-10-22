package com.example.gsm.ui.view.screens.usuario.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.Solicitud
import com.example.gsm.ui.view.components.formatearFechaHora

/**
 * Tarjeta animada que se despliega cuando el ticket está cerrado.
 * Muestra el checkmark con escala animada, datos del cierre y el formulario de reapertura para el cliente.
 */
@Composable
fun TicketCerradoAnimadoCard(
    solicitud: Solicitud,
    esCliente: Boolean,
    onSolicitarReapertura: (String) -> Unit
) {
    var animadoIniciado by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animadoIniciado = true
    }

    val escalaIcono by animateFloatAsState(
        targetValue = if (animadoIniciado) 1f else 0.4f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "escalaCheckCierre"
    )

    var mostrarFormularioReapertura by remember { mutableStateOf(false) }
    var motivoReapertura by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        SeccionTitulo("Estado de Cierre")
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(500)) + expandVertically() + scaleIn(initialScale = 0.92f)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icono central con animación de escala
                    Surface(
                        shape = CircleShape,
                        color = SuccessGreenBg,
                        modifier = Modifier
                            .size(64.dp)
                            .scale(escalaIcono)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "✓",
                                fontSize = 32.sp,
                                color = SuccessGreenText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Ticket Cerrado y Finalizado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Este ticket ha concluido su ciclo de atención y se encuentra archivado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    // Resumen de la solución y fecha de cierre
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SuccessGreenBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔒 Estado:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreenText,
                                    fontWeight = FontWeight.Bold
                                )
                                BadgePill(
                                    texto = "CERRADO",
                                    colorFondo = Color.White,
                                    colorTexto = SuccessGreenText
                                )
                            }

                            if (!solicitud.resolucion.isNullOrBlank()) {
                                Text(
                                    text = "📋 Solución aplicada: ${solicitud.resolucion}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SuccessGreenText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            val fechaCierreTexto = if (solicitud.fechaCierre != null) {
                                formatearFechaHora(solicitud.fechaCierre)
                            } else {
                                formatearFechaHora(solicitud.fechaActualizacion)
                            }
                            Text(
                                text = "🕒 Finalizado el: $fechaCierreTexto",
                                style = MaterialTheme.typography.labelSmall,
                                color = SuccessGreenText
                            )
                        }
                    }

                    // Opción para el cliente de solicitar reapertura en caso de disconformidad
                    if (esCliente) {
                        HorizontalDivider(color = AppleBackground, modifier = Modifier.padding(vertical = 4.dp))

                        if (!mostrarFormularioReapertura) {
                            FilledTonalButton(
                                onClick = { mostrarFormularioReapertura = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("🔄 ¿El problema persiste? Solicitar Reapertura")
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Motivo de la reapertura:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                TextField(
                                    value = motivoReapertura,
                                    onValueChange = { motivoReapertura = it },
                                    placeholder = {
                                        Text(
                                            text = "Explica por qué solicitas reabrir este ticket...",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    minLines = 2,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = AppleBackground,
                                        unfocusedContainerColor = AppleBackground,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(
                                        onClick = {
                                            mostrarFormularioReapertura = false
                                            motivoReapertura = ""
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cancelar")
                                    }
                                    Button(
                                        onClick = {
                                            onSolicitarReapertura(motivoReapertura)
                                            mostrarFormularioReapertura = false
                                            motivoReapertura = ""
                                        },
                                        enabled = motivoReapertura.isNotBlank(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Confirmar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de cancelación cuando el ticket fue cancelado por el administrador.
 */
@Composable
fun TicketCanceladoCard(solicitud: Solicitud) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SeccionTitulo("Estado de Solicitud")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = AlertRedBg,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🚫", fontSize = 28.sp)
                    }
                }
                Text(
                    text = "Solicitud Cancelada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AlertRedText
                )
                Text(
                    text = solicitud.motivoCancelacion ?: "Esta solicitud fue cancelada por un administrador.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
