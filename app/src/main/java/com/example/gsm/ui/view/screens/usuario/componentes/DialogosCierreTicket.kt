package com.example.gsm.ui.view.screens.usuario.componentes

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Tarjeta de conformidad y calificación CSAT (1-5 estrellas) cuando el ticket está RESUELTA.
 */
@Composable
fun TarjetaCierreCalificacionCliente(
    onConfirmarCierre: (calificacion: Int) -> Unit
) {
    var calificacionSeleccionada by remember { mutableIntStateOf(5) }
    val haptic = LocalHapticFeedback.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SuccessGreenBg,
        border = BorderStroke(1.dp, SuccessGreenText.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreenText)
                Text(
                    text = "Ticket Resuelto por Soporte",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreenText
                )
            }
            Text(
                text = "El equipo técnico ha finalizado la atención. Califica el servicio y confirma tu conformidad para cerrar el caso.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2C2C2E)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (estrella in 1..5) {
                    val activa = estrella <= calificacionSeleccionada
                    val escalaEstrella by animateFloatAsState(
                        targetValue = if (activa) 1.15f else 1f,
                        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
                        label = "escalaEstrella$estrella"
                    )
                    IconButton(
                        onClick = {
                            calificacionSeleccionada = estrella
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "$estrella estrellas",
                            tint = if (activa) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                            modifier = Modifier
                                .size(26.dp)
                                .graphicsLayer {
                                    scaleX = escalaEstrella
                                    scaleY = escalaEstrella
                                }
                        )
                    }
                }
            }
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onConfirmarCierre(calificacionSeleccionada)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreenText),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Confirmar Conformidad y Cerrar Ticket", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Diálogo reutilizable para ingresar motivo de cancelación o solicitud de anulación.
 */
@Composable
fun DialogoMotivoTicket(
    titulo: String,
    descripcion: String,
    placeholder: String,
    textoConfirmar: String,
    colorConfirmar: Color,
    onConfirmar: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var motivo by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(descripcion)
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    placeholder = { Text(placeholder) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(motivo) },
                colors = ButtonDefaults.buttonColors(containerColor = colorConfirmar)
            ) {
                Text(textoConfirmar, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Volver") }
        }
    )
}

@Composable
fun DialogoDesasignarTecnico(
    nombreTecnico: String,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(18.dp),
        containerColor = Color.White,
        icon = {
            Icon(
                Icons.Filled.PersonRemove,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text("¿Desasignar Técnico?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                text = "¿Estás seguro de que deseas retirar a $nombreTecnico de este ticket? La solicitud volverá al estado 'En revisión' para que pueda ser reasignada a otro especialista.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Confirmar Desasignación") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
