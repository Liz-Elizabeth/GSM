package com.example.gsm.ui.view.screens.usuario.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.Historial
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.ui.view.components.formatearFechaHora

/**
 * Burbuja de chat individual inspirada en WhatsApp y Apple iMessage.
 * Ubica los mensajes propios a la derecha con verde suave (#E8F8EE) y checks (✓✓),
 * y los mensajes recibidos a la izquierda en blanco puro con badge de autor.
 */
@Composable
private fun BurbujaChatWhatsApp(
    evento: Historial,
    usuarioActualId: Int,
    usuarioActualRol: RolUsuario
) {
    // Caso 1: Nota Interna privada del equipo técnico
    if (evento.esComentarioInterno) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = InternalNoteBg,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
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
                        Text(
                            text = "🔒 NOTA INTERNA",
                            style = MaterialTheme.typography.labelSmall,
                            color = InternalNoteText,
                            fontWeight = FontWeight.Bold
                        )
                        BadgePill(
                            texto = evento.perfilUsuario.name,
                            colorFondo = Color.White,
                            colorTexto = InternalNoteText
                        )
                    }
                    Text(
                        text = formatearFechaHora(evento.fechaCambio),
                        style = MaterialTheme.typography.labelSmall,
                        color = InternalNoteText.copy(alpha = 0.8f)
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

    // Caso 2: Notificación de cambio de estado o evento de sistema centrado
    val esEventoDeSistema = evento.comentario.isBlank() ||
            evento.accion.contains("Estado", ignoreCase = true) ||
            evento.accion.contains("Asignación", ignoreCase = true)

    if (esEventoDeSistema) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = AppleBackground
            ) {
                Text(
                    text = "⚙️ ${evento.accion.replace("_", " ")}: ${evento.estadoNuevo.name.replace("_", " ")} • ${formatearFechaHora(evento.fechaCambio)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
        return
    }

    // Caso 3: Mensaje conversacional
    val esMiMensaje = (evento.usuarioId == usuarioActualId) || (evento.perfilUsuario == usuarioActualRol)
    val alineacion = if (esMiMensaje) Alignment.End else Alignment.Start
    val colorBurbuja = if (esMiMensaje) SuccessGreenBg else Color.White
    val formaBurbuja = if (esMiMensaje) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 3.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 3.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = alineacion
    ) {
        Surface(
            shape = formaBurbuja,
            color = colorBurbuja,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Cabecera con autor para mensajes recibidos
                if (!esMiMensaje) {
                    val icono = when (evento.perfilUsuario) {
                        RolUsuario.CLIENTE -> "👤"
                        RolUsuario.TECNICO -> "🔧"
                        RolUsuario.ADMINISTRADOR -> "👑"
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$icono ${evento.perfilUsuario.name.replace("_", " ")}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TeamworkBlue
                        )
                        BadgePill(
                            texto = evento.accion,
                            colorFondo = AppleBackground,
                            colorTexto = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Texto del comentario
                Text(
                    text = evento.comentario,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!evento.evidenciaAdjunta.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AppleBackground,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "📎 Archivo: ${evento.evidenciaAdjunta}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TeamworkBlue,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Hora del mensaje y checks dobles
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val horaTexto = try {
                        val partes = formatearFechaHora(evento.fechaCambio).split(" ")
                        if (partes.size > 1) partes[1] else partes[0]
                    } catch (e: Exception) {
                        ""
                    }
                    Text(
                        text = horaTexto,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (esMiMensaje) {
                        Text(
                            text = "✓✓",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreenText
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sección completa de conversación del ticket con estética WhatsApp:
 * Despliega la lista de mensajes en burbujas y la barra de envío inferior.
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
        SeccionTitulo("Conversación del Ticket (${historial.size})")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.85f),
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
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay mensajes en la conversación aún. ¡Escribe el primero!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

                // Barra de envío estilo WhatsApp
                if (!estaCerrado) {
                    HorizontalDivider(color = AppleBackground, modifier = Modifier.padding(vertical = 4.dp))

                    // Selector de Nota Interna (Disponible para Administrador y Técnicos)
                    if (onNotaInternaChange != null && (usuarioActualRol == RolUsuario.ADMINISTRADOR || usuarioActualRol == RolUsuario.TECNICO)) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (esNotaInterna) InternalNoteBg else AppleBackground.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (esNotaInterna) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                        contentDescription = null,
                                        tint = if (esNotaInterna) InternalNoteText else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (esNotaInterna) "Nota interna (Oculta al cliente)" else "Mensaje visible para el cliente",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (esNotaInterna) InternalNoteText else Color.Gray
                                    )
                                }
                                Switch(
                                    checked = esNotaInterna,
                                    onCheckedChange = onNotaInternaChange
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = textoMensaje,
                            onValueChange = onTextoChange,
                            placeholder = {
                                Text(
                                    text = if (esNotaInterna) "Escribe nota interna..." else "Escribe un mensaje...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.weight(1f),
                            minLines = 1,
                            maxLines = 4,
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = if (esNotaInterna) InternalNoteBg else AppleBackground,
                                unfocusedContainerColor = if (esNotaInterna) InternalNoteBg else AppleBackground,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )

                        val puedeEnviar = textoMensaje.isNotBlank()
                        Surface(
                            onClick = {
                                if (puedeEnviar) {
                                    alEnviar()
                                }
                            },
                            enabled = puedeEnviar,
                            shape = CircleShape,
                            color = if (puedeEnviar) TeamworkBlue else Color(0xFFD1D1D6),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "➤",
                                    fontSize = 18.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(shape = CircleShape, color = AppleBackground) {
                            Text(
                                text = "🔒 Este ticket está cerrado. La conversación está finalizada.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
