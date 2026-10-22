package com.example.gsm.ui.view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.ui.view.screens.usuario.componentes.BadgePill
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import com.example.gsm.util.FileUtil

data class DatosEvidenciaVisualizar(
    val nombreArchivo: String,
    val tipoArchivo: String,
    val tamanoBytes: Long,
    val uriString: String,
    val autor: String? = null,
    val fecha: String? = null,
    val estadoRevision: String? = null
)

/**
 * Modal in-app para visualizar evidencias adjuntas (imágenes, textos/logs y documentos).
 * Principio KISS: Visualización nativa en Compose sin dependencias externas pesadas.
 */
@Composable
fun VisorEvidenciaModal(
    evidencia: DatosEvidenciaVisualizar,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val esImagen = evidencia.tipoArchivo.startsWith("image/") ||
            evidencia.nombreArchivo.endsWith(".png", ignoreCase = true) ||
            evidencia.nombreArchivo.endsWith(".jpg", ignoreCase = true) ||
            evidencia.nombreArchivo.endsWith(".jpeg", ignoreCase = true) ||
            evidencia.nombreArchivo.endsWith(".webp", ignoreCase = true)

    val esTexto = evidencia.tipoArchivo.startsWith("text/") ||
            evidencia.nombreArchivo.endsWith(".txt", ignoreCase = true) ||
            evidencia.nombreArchivo.endsWith(".log", ignoreCase = true) ||
            evidencia.nombreArchivo.endsWith(".csv", ignoreCase = true) ||
            evidencia.nombreArchivo.endsWith(".json", ignoreCase = true)

    val bitmap = remember(evidencia.uriString) {
        if (esImagen) FileUtil.cargarBitmapDesdeUri(context, evidencia.uriString) else null
    }

    val textoLeido = remember(evidencia.uriString) {
        if (esTexto) FileUtil.leerTextoDesdeUri(context, evidencia.uriString) else null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = FileUtil.obtenerIconoVectorSegunMime(evidencia.tipoArchivo),
                        contentDescription = null,
                        tint = TeamworkBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = evidencia.nombreArchivo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Barra de Metadatos
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Peso: " + FileUtil.formatearTamano(evidencia.tamanoBytes),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                            if (!evidencia.fecha.isNullOrBlank()) {
                                Text(
                                    text = "Fecha: " + evidencia.fecha,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        if (!evidencia.estadoRevision.isNullOrBlank()) {
                            BadgePill(
                                texto = evidencia.estadoRevision,
                                colorFondo = Color.White,
                                colorTexto = TeamworkBlue
                            )
                        }
                    }
                }

                // 2. Vista previa según tipo de archivo
                when {
                    esImagen -> {
                        if (bitmap != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = evidencia.nombreArchivo,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        } else {
                            // Demo o imagen cargada simulada
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E293B),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("🖼️", fontSize = 44.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Captura de Pantalla Adjunta",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = evidencia.nombreArchivo,
                                        color = Color(0xFF94A3B8),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    esTexto -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                val contenidoTexto = textoLeido ?: ("--- [LOG DE EVENTO ADJUNTO] ---\nTimestamp: " + (evidencia.fecha ?: "2026-10-22") + "\nArchivo: " + evidencia.nombreArchivo + "\nContenido de diagnóstico capturado en el dispositivo.")
                                Text(
                                    text = contenidoTexto,
                                    color = Color(0xFF38BDF8),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    else -> {
                        // Documento general (PDF, DOCX, ZIP, etc.)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = FileUtil.obtenerIconoVectorSegunMime(evidencia.tipoArchivo),
                                    contentDescription = null,
                                    tint = TeamworkBlue,
                                    modifier = Modifier.size(46.dp)
                                )
                                Text(
                                    text = evidencia.nombreArchivo,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "Formato: " + evidencia.tipoArchivo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (evidencia.uriString.isNotBlank() && !evidencia.uriString.startsWith("file:///demo")) {
                Button(
                    onClick = {
                        FileUtil.abrirArchivoExterno(context, evidencia.uriString, evidencia.tipoArchivo)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir con App Externa", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
