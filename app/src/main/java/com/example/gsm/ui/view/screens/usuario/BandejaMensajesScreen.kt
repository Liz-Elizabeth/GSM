package com.example.gsm.ui.view.screens.usuario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.Solicitud
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.formatearFechaHora
import com.example.gsm.ui.viewmodel.SolicitudViewModel

private val AppleBackground = Color(0xFFF2F2F7)
private val TeamworkBlue = Color(0xFF0066FF)

/**
 * Centro de Mensajes y Chats Directos de Tickets (KISS).
 * 
 * Reemplaza la pestaña estática de alertas con un buzón activo estilo Teamwork Inbox / WhatsApp:
 * - Cada ticket con soporte aparece como una conversación interactiva.
 * - Acceso en 1 toque al chat en vivo sin pasar por pantallas intermedias.
 * - Sincronizado en tiempo real con Firestore y SQLite local.
 */
@Composable
fun BandejaMensajesScreen(
    usuario: Usuario,
    solicitudVm: SolicitudViewModel,
    alIrAChat: (Int) -> Unit,
    alIrACrear: () -> Unit
) {
    var textoFiltro by remember { mutableStateOf("") }

    val ticketsConversacion = remember(solicitudVm.listaSolicitudes, textoFiltro) {
        solicitudVm.solicitudesActivas.filter { sol ->
            textoFiltro.isBlank() ||
            sol.codigoSeguimiento.contains(textoFiltro, ignoreCase = true) ||
            sol.titulo.contains(textoFiltro, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 1. Cabecera limpia
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Mensajes",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
                Text(
                    text = "Conversaciones en vivo con soporte técnico",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8E8E93)
                )
            }

            Surface(
                color = TeamworkBlue.copy(alpha = 0.12f),
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChatBubble,
                        contentDescription = null,
                        tint = TeamworkBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${ticketsConversacion.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TeamworkBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Buscador de conversaciones
        OutlinedTextField(
            value = textoFiltro,
            onValueChange = { textoFiltro = it },
            placeholder = { Text("Buscar conversación o #código...", fontSize = 14.sp) },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF8E8E93), modifier = Modifier.size(20.dp))
            },
            trailingIcon = {
                if (textoFiltro.isNotEmpty()) {
                    IconButton(onClick = { textoFiltro = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "Limpiar", tint = Color(0xFF8E8E93), modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = TeamworkBlue,
                unfocusedBorderColor = Color(0xFFE5E5EA)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Lista de Chats o Estado Vacío
        if (ticketsConversacion.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AppleBackground,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Forum,
                                contentDescription = null,
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sin conversaciones activas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (textoFiltro.isNotEmpty()) {
                            "No se encontraron chats que coincidan con la búsqueda."
                        } else {
                            "Cuando abras un ticket o el equipo de soporte te responda, la conversación aparecerá aquí."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8E8E93),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = alIrACrear,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Crear nuevo ticket", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(items = ticketsConversacion, key = { it.id }) { ticket ->
                    TarjetaConversacionItem(
                        ticket = ticket,
                        alHacerClic = { alIrAChat(ticket.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaConversacionItem(
    ticket: Solicitud,
    alHacerClic: () -> Unit
) {
    val (estadoTexto, colorEstado, fondoEstado) = when (ticket.estado) {
        EstadoSolicitud.RECIBIDA -> Triple("Recibida", TeamworkBlue, TeamworkBlue.copy(alpha = 0.10f))
        EstadoSolicitud.EN_REVISION -> Triple("En revisión", Color(0xFFB54708), Color(0xFFFEF0C7))
        EstadoSolicitud.ASIGNADA -> Triple("Asignada", Color(0xFF7A5AF8), Color(0xFFF4F3FF))
        EstadoSolicitud.EN_PROCESO -> Triple("En proceso", Color(0xFF027A48), Color(0xFFECFDF3))
        EstadoSolicitud.PENDIENTE_INFORMACION -> Triple("Pendiente info", Color(0xFFB54708), Color(0xFFFEF0C7))
        EstadoSolicitud.RESUELTA -> Triple("Resuelta", Color(0xFF027A48), Color(0xFFECFDF3))
        EstadoSolicitud.CERRADA -> Triple("Cerrada", Color(0xFF475569), Color(0xFFF1F5F9))
        EstadoSolicitud.CANCELADA -> Triple("Cancelada", Color(0xFFD92D20), Color(0xFFFEE4E2))
    }

    val fechaFormateada = remember(ticket.fechaActualizacion, ticket.fechaCreacion) {
        val ts = if (ticket.fechaActualizacion > 0) ticket.fechaActualizacion else ticket.fechaCreacion
        formatearFechaHora(ts)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E5EA)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alHacerClic)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar con icono de mensaje e indicador de estado
            Box {
                Surface(
                    shape = CircleShape,
                    color = TeamworkBlue.copy(alpha = 0.12f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Forum,
                            contentDescription = null,
                            tint = TeamworkBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                // Punto de estado activo
                Surface(
                    shape = CircleShape,
                    color = colorEstado,
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.BottomEnd)
                ) {}
            }

            // Información central
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${ticket.codigoSeguimiento}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TeamworkBlue
                    )
                    Text(
                        text = fechaFormateada,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8E8E93),
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = ticket.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ticket.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8E8E93),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = fondoEstado,
                        shape = CircleShape
                    ) {
                        Text(
                            text = estadoTexto,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorEstado,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Flecha indicadora
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Abrir chat",
                tint = Color(0xFFC7C7CC),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
