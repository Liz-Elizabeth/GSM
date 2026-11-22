package com.example.gsm.ui.view.screens.usuario.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.local.entity.EvidenciaEntity
import com.example.gsm.util.FileUtil

/**
 * Sección de Evidencias Adjuntas extraída de DetalleSolicitudScreen para evitar recomposiciones innecesarias.
 */
@Composable
fun SeccionEvidenciasTicket(
    evidencias: List<EvidenciaEntity>,
    onTomarFoto: () -> Unit,
    onSubirArchivo: () -> Unit,
    onVerEvidencia: (EvidenciaEntity) -> Unit
) {
    SeccionTitulo("Evidencias Adjuntas (${evidencias.size})")
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onTomarFoto,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = TeamworkBlue.copy(alpha = 0.10f),
                        contentColor = TeamworkBlue
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Cámara", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tomar Foto", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = onSubirArchivo,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Filled.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Subir Archivo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (evidencias.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AppleBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AttachFile,
                            contentDescription = null,
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Sin evidencias adjuntas todavía",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1E)
                        )
                        Text(
                            text = "Toma una foto o sube documentos de respaldo para el equipo técnico.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E8E93),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    evidencias.forEach { ev ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF9F9FB),
                            border = BorderStroke(1.dp, Color(0xFFEBEBEF)),
                            onClick = { onVerEvidencia(ev) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = TeamworkBlue.copy(alpha = 0.12f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = FileUtil.obtenerIconoVectorSegunMime(ev.tipoArchivo),
                                                contentDescription = null,
                                                tint = TeamworkBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = ev.nombreArchivo,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1C1C1E),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${FileUtil.formatearTamano(ev.tamanoBytes)} • Toca para previsualizar",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF8E8E93)
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Filled.Visibility,
                                    contentDescription = "Ver archivo",
                                    tint = TeamworkBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
