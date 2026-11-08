package com.example.gsm.ui.view.screens.usuario.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.Historial
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.ui.view.components.DatosEvidenciaVisualizar
import com.example.gsm.ui.view.components.formatearFechaHora
import com.example.gsm.util.FileUtil

// Colores fieles a la experiencia social (WhatsApp / Telegram)
private val ChatGreenSent = Color(0xFFE7FFDB)
private val ChatBlueCheck = Color(0xFF34B7F1)
private val ChatReceivedBg = Color.White
private val ChatSenderTech = Color(0xFF0F766E)
private val ChatSenderClient = Color(0xFF0284C7)

/**
 * Píldora de Fecha Flotante ("Hoy", "Ayer", etc.) como en WhatsApp o Telegram.
 */
@Composable
fun ChipFechaConversacion(fechaTexto: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFE2E8F0).copy(alpha = 0.85f),
            shadowElevation = 0.5.dp
        ) {
            Text(
                text = fechaTexto,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF475569),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
            )
        }
    }
}

/**
 * Píldora de Eventos de Sistema ("⚙️ Ticket asignado a Ing. Carlos", etc.)
 */
@Composable
fun ChipEventoTransicion(texto: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF1F5F9).copy(alpha = 0.9f),
            border = BorderStroke(0.5.dp, Color(0xFFCBD5E1))
        ) {
            Text(
                text = texto,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF475569),
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * Burbuja de chat con experiencia social auténtica:
 * - Mensajes propios a la derecha con verde WhatsApp (#E7FFDB) y doble check azul (#34B7F1).
 * - Mensajes recibidos a la izquierda en blanco puro con nombre del remitente en color de acento.
 * - Soporte para miniaturas de fotos inline clicables para visor a pantalla completa.
 */
@Composable
fun BurbujaChatWhatsApp(
    evento: Historial,
    usuarioActualId: Int,
    usuarioActualRol: RolUsuario,
    alVerEvidencia: (DatosEvidenciaVisualizar) -> Unit = {}
) {
    val context = LocalContext.current

    // Caso 1: Nota Interna confidencial de soporte
    if (evento.esComentarioInterno) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = InternalNoteBg,
            border = BorderStroke(1.dp, Color(0xFFFEDF89)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = InternalNoteText,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "NOTA INTERNA",
                            style = MaterialTheme.typography.labelSmall,
                            color = InternalNoteText,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            Text(
                                text = evento.perfilUsuario.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = InternalNoteText,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                fontSize = 9.sp
                            )
                        }
                    }
                    Text(
                        text = formatearFechaHora(evento.fechaCambio),
                        style = MaterialTheme.typography.labelSmall,
                        color = InternalNoteText.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = evento.comentario,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5A2A00)
                )
            }
        }
        return
    }

    // Caso 2: Notificación del sistema centrada
    val esEventoDeSistema = evento.comentario.isBlank()
    if (esEventoDeSistema) {
        val accionLimpia = when (evento.accion) {
            "CREACION" -> "Solicitud ingresada formalmente"
            "ASIGNACION" -> "Técnico asignado al ticket"
            "CAMBIO_ESTADO" -> "Estado actualizado a ${evento.estadoNuevo.name.replace("_", " ")}"
            "RESOLUCION" -> "Solución técnica registrada"
            "CIERRE" -> "Ticket cerrado y conforme"
            "CANCELACION" -> "Ticket cancelado"
            "REAPERTURA" -> "Solicitud reabierta"
            else -> "${evento.accion.replace("_", " ")}: ${evento.estadoNuevo.name.replace("_", " ")}"
        }
        ChipEventoTransicion("⚙️ $accionLimpia")
        return
    }

    // Caso 3: Mensaje de Chat conversacional
    val esMiMensaje = (evento.usuarioId == usuarioActualId) || (evento.perfilUsuario == usuarioActualRol)
    val alineacion = if (esMiMensaje) Alignment.End else Alignment.Start
    val colorBurbuja = if (esMiMensaje) ChatGreenSent else ChatReceivedBg

    // Forma de burbuja estilo WhatsApp con "colita"
    val formaBurbuja = if (esMiMensaje) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 3.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 3.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalAlignment = alineacion
    ) {
        Surface(
            shape = formaBurbuja,
            color = colorBurbuja,
            shadowElevation = 0.8.dp,
            border = if (!esMiMensaje) BorderStroke(0.5.dp, Color(0xFFE2E8F0)) else null,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Nombre del remitente en mensajes recibidos (estilo grupos de chat)
                if (!esMiMensaje) {
                    val colorAutor = if (evento.perfilUsuario == RolUsuario.CLIENTE) ChatSenderClient else ChatSenderTech
                    val rolTexto = when (evento.perfilUsuario) {
                        RolUsuario.CLIENTE -> "Cliente"
                        RolUsuario.TECNICO -> "Soporte Técnico"
                        RolUsuario.ADMINISTRADOR -> "Admin GSM"
                    }
                    Text(
                        text = rolTexto,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorAutor,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }

                // Miniatura de evidencia si incluye foto adjunta
                if (!evento.evidenciaAdjunta.isNullOrBlank()) {
                    val ruta = evento.evidenciaAdjunta
                    val bitmap = remember(ruta) {
                        try {
                            FileUtil.cargarBitmapDesdeUri(context, ruta)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (bitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    alVerEvidencia(
                                        DatosEvidenciaVisualizar(
                                            nombreArchivo = ruta,
                                            tipoArchivo = "image/jpeg",
                                            tamanoBytes = 0L,
                                            uriString = ruta,
                                            fecha = formatearFechaHora(evento.fechaCambio)
                                        )
                                    )
                                }
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Foto adjunta",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        // Tarjeta de archivo o documento
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.05f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    alVerEvidencia(
                                        DatosEvidenciaVisualizar(
                                            nombreArchivo = ruta,
                                            tipoArchivo = "application/octet-stream",
                                            tamanoBytes = 0L,
                                            uriString = ruta
                                        )
                                    )
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AttachFile,
                                    contentDescription = null,
                                    tint = TeamworkBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = ruta.substringAfterLast("/"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TeamworkBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Texto del comentario
                if (evento.comentario.isNotBlank()) {
                    Text(
                        text = evento.comentario,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF111B21),
                        fontSize = 14.sp
                    )
                }

                // Hora y doble check a la derecha
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val horaTexto = remember(evento.fechaCambio) {
                        try {
                            val partes = formatearFechaHora(evento.fechaCambio).split(" ")
                            if (partes.size > 1) partes[1] else partes[0]
                        } catch (e: Exception) {
                            ""
                        }
                    }
                    Text(
                        text = horaTexto,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = Color(0xFF667781)
                    )
                    if (esMiMensaje) {
                        Text(
                            text = "✓✓",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChatBlueCheck
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sección de conversación para incrustar dentro de la pestaña de detalle del ticket.
 */
@Composable
fun ChatConversacionTicket(
    historial: List<Historial>,
    usuarioActualId: Int,
    usuarioActualRol: RolUsuario,
    estaCerrado: Boolean,
    textoMensaje: String,
    onTextoChange: (String) -> Unit,
    esNotaInterna: Boolean = false,
    onNotaInternaChange: ((Boolean) -> Unit)? = null,
    alEnviar: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (historial.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💬 Sin mensajes aún. Escribe abajo para iniciar conversación.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                } else {
                    historial.forEach { evento ->
                        BurbujaChatWhatsApp(
                            evento = evento,
                            usuarioActualId = usuarioActualId,
                            usuarioActualRol = usuarioActualRol
                        )
                    }
                }

                if (!estaCerrado) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = textoMensaje,
                            onValueChange = onTextoChange,
                            placeholder = { Text("Escribe un mensaje...", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = TeamworkBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        )

                        val puedeEnviar = textoMensaje.isNotBlank()
                        Surface(
                            onClick = { if (puedeEnviar) alEnviar() },
                            enabled = puedeEnviar,
                            shape = CircleShape,
                            color = if (puedeEnviar) TeamworkBlue else Color(0xFFD1D1D6),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Enviar",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
