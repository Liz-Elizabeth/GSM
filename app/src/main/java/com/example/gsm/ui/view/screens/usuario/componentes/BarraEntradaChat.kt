package com.example.gsm.ui.view.screens.usuario.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.util.ArchivoAdjunto
import com.example.gsm.util.FileUtil

/**
 * Barra inferior de composición del chat (soporta nota interna, adjuntos y plantillas).
 */
@Composable
fun BarraEntradaChat(
    rolUsuario: RolUsuario,
    textoMensaje: String,
    onTextoChange: (String) -> Unit,
    esNotaInterna: Boolean,
    onToggleNotaInterna: () -> Unit,
    evidenciaSeleccionada: ArchivoAdjunto?,
    onQuitarEvidencia: () -> Unit,
    enviando: Boolean,
    onTomarFoto: () -> Unit,
    onAdjuntarImagen: () -> Unit,
    onEnviar: () -> Unit
) {
    val context = LocalContext.current
    var mostrarPlantillasRapidas by remember { mutableStateOf(false) }
    var menuAccionesAbierto by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (rolUsuario == RolUsuario.ADMINISTRADOR || rolUsuario == RolUsuario.TECNICO) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onToggleNotaInterna,
                        shape = RoundedCornerShape(12.dp),
                        color = if (esNotaInterna) Color(0xFFFEF0C7) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (esNotaInterna) Color(0xFFF79009) else Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (esNotaInterna) Icons.Filled.Lock else Icons.Filled.Public,
                                contentDescription = null,
                                tint = if (esNotaInterna) Color(0xFFB54708) else Color(0xFF64748B),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (esNotaInterna) "Modo: Nota Interna 🔒 (Oculto al cliente)" else "Modo: Respuesta Pública 🌐",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (esNotaInterna) Color(0xFFB54708) else Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            if (evidenciaSeleccionada != null) {
                val bitmap = produceState<android.graphics.Bitmap?>(initialValue = null, evidenciaSeleccionada.uriString) {
                    value = FileUtil.cargarBitmapAsync(context, evidenciaSeleccionada.uriString)
                }.value
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Miniatura",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            Icon(Icons.Filled.Image, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(24.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = evidenciaSeleccionada.nombre,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = FileUtil.formatearTamano(evidenciaSeleccionada.tamanoBytes),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8E8E93)
                            )
                        }

                        IconButton(onClick = onQuitarEvidencia, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Quitar foto", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            if (mostrarPlantillasRapidas) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val plantillas = listOf(
                        "Revisando requerimiento 🔍",
                        "Favor enviar captura de pantalla 📸",
                        "El servicio ha sido restablecido ✅",
                        "Todo probado y funcionando ok 👍",
                        "¿Podrías confirmar funcionamiento? 💬"
                    )
                    plantillas.forEach { plantilla ->
                        Surface(
                            onClick = {
                                onTextoChange(plantilla)
                                mostrarPlantillasRapidas = false
                            },
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = plantilla,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.padding(bottom = 4.dp)) {
                    Surface(
                        onClick = { menuAccionesAbierto = true },
                        shape = CircleShape,
                        color = if (mostrarPlantillasRapidas) TeamworkBlue.copy(alpha = 0.14f) else Color(0xFFF1F5F9),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Más acciones",
                                tint = if (mostrarPlantillasRapidas) TeamworkBlue else Color(0xFF475569),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    DropdownMenu(expanded = menuAccionesAbierto, onDismissRequest = { menuAccionesAbierto = false }) {
                        DropdownMenuItem(
                            text = { Text("Respuestas rápidas") },
                            leadingIcon = { Icon(Icons.Filled.Bolt, contentDescription = null, tint = TeamworkBlue) },
                            onClick = {
                                menuAccionesAbierto = false
                                mostrarPlantillasRapidas = !mostrarPlantillasRapidas
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Tomar foto") },
                            leadingIcon = { Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color(0xFF475569)) },
                            onClick = {
                                menuAccionesAbierto = false
                                onTomarFoto()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Adjuntar imagen") },
                            leadingIcon = { Icon(Icons.Filled.AttachFile, contentDescription = null, tint = Color(0xFF475569)) },
                            onClick = {
                                menuAccionesAbierto = false
                                onAdjuntarImagen()
                            }
                        )
                    }
                }

                TextField(
                    value = textoMensaje,
                    onValueChange = onTextoChange,
                    placeholder = {
                        Text(
                            text = if (esNotaInterna) "Nota privada..." else "Mensaje...",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    minLines = 1,
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if (esNotaInterna) Color(0xFFFFFBEB) else Color(0xFFF1F5F9),
                        unfocusedContainerColor = if (esNotaInterna) Color(0xFFFFFBEB) else Color(0xFFF1F5F9),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                )

                val puedeEnviar = (textoMensaje.isNotBlank() || evidenciaSeleccionada != null) && !enviando
                Surface(
                    onClick = { if (puedeEnviar) onEnviar() },
                    enabled = puedeEnviar,
                    shape = CircleShape,
                    color = if (puedeEnviar) (if (esNotaInterna) Color(0xFFD97706) else TeamworkBlue) else Color(0xFFCBD5E1),
                    shadowElevation = if (puedeEnviar) 2.dp else 0.dp,
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (enviando) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar mensaje", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}
