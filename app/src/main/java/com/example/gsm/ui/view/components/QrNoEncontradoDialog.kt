package com.example.gsm.ui.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue

/**
 * Diálogo modal animado cuando un código QR no coincide con ningún ticket en la base de datos.
 * Principio KISS: Feedback visual claro con animación fluida de rebote/pulso.
 */
@Composable
fun QrNoEncontradoDialog(
    codigoEscaneado: String,
    onReintentar: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Icono de alerta con sacudida única al aparecer
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shakeOnError(1)
                        .background(Color(0xFFFFEBEE), CircleShape)
                        .border(2.dp, Color(0xFFEF5350).copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SearchOff,
                        contentDescription = "No Encontrado",
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(40.dp)
                    )
                }

                // 2. Títulos y explicación
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Ticket No Encontrado",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "El código QR leído no coincide con ninguna solicitud registrada en el sistema o fue eliminada.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // 3. Chip que resalta el código leído
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Código leído:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = codigoEscaneado.ifBlank { "Vacío / No legible" },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onReintentar,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reintentar Escaneo", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCerrar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
