package com.example.gsm.ui.view.screens.usuario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.example.gsm.ui.view.screens.usuario.componentes.AlertRedBg
import com.example.gsm.ui.view.screens.usuario.componentes.AlertRedText
import com.example.gsm.ui.view.screens.usuario.componentes.AppleBackground
import com.example.gsm.ui.view.screens.usuario.componentes.EstadoVacioCard
import com.example.gsm.ui.view.screens.usuario.componentes.SuccessGreenBg
import com.example.gsm.ui.view.screens.usuario.componentes.SuccessGreenText
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import com.example.gsm.ui.view.screens.usuario.componentes.formatearEstado
import com.example.gsm.ui.viewmodel.SolicitudViewModel

private enum class FiltroMensajes { TODAS, ESPERANDO_RESPUESTA, SIN_ASIGNAR }

/**
 * Centro de Mensajes y Chats Directos de Tickets (KISS).
 * 
 * Reemplaza la pestaña estática de alertas con un buzón activo estilo Teamwork Inbox / WhatsApp:
 * - Cada ticket con soporte aparece como una conversación interactiva.
 * - Acceso en 1 toque al chat en vivo sin pasar por pantallas intermedias.
 * - Sincronizado en tiempo real con Firestore y SQLite local.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandejaMensajesScreen(
    usuario: Usuario,
    solicitudVm: SolicitudViewModel,
    alIrAChat: (Int) -> Unit,
    alIrACrear: () -> Unit
) {
    var textoFiltro by remember { mutableStateOf("") }
    var filtroRapido by remember { mutableStateOf(FiltroMensajes.TODAS) }

    // Se parte de solicitudesActivas para no mezclar tickets cerrados/cancelados en el buzón vivo,
    // y se ordena por actividad reciente para mostrar primero las conversaciones con movimiento.
    val ticketsConversacion = remember(solicitudVm.listaSolicitudes, textoFiltro, filtroRapido) {
        solicitudVm.solicitudesActivas
            .asSequence()
            .filter { sol ->
                when (filtroRapido) {
                    FiltroMensajes.TODAS -> true
                    FiltroMensajes.ESPERANDO_RESPUESTA -> sol.estado == EstadoSolicitud.PENDIENTE_INFORMACION
                    FiltroMensajes.SIN_ASIGNAR -> sol.tecnicoId == null
                }
            }
            .filter { sol ->
                textoFiltro.isBlank() ||
                    sol.codigoSeguimiento.contains(textoFiltro, ignoreCase = true) ||
                    sol.titulo.contains(textoFiltro, ignoreCase = true)
            }
            .sortedByDescending { maxOf(it.fechaActualizacion, it.fechaCreacion) }
            .toList()
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

        Spacer(modifier = Modifier.height(10.dp))

        // Chips rápidos de 1 toque para priorizar chats pendientes o sin técnico
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = filtroRapido == FiltroMensajes.TODAS,
                onClick = { filtroRapido = FiltroMensajes.TODAS },
                label = { Text("Todas") }
            )
            FilterChip(
                selected = filtroRapido == FiltroMensajes.ESPERANDO_RESPUESTA,
                onClick = {
                    filtroRapido = if (filtroRapido == FiltroMensajes.ESPERANDO_RESPUESTA) {
                        FiltroMensajes.TODAS
                    } else {
                        FiltroMensajes.ESPERANDO_RESPUESTA
                    }
                },
                label = { Text("Esperando respuesta") }
            )
            FilterChip(
                selected = filtroRapido == FiltroMensajes.SIN_ASIGNAR,
                onClick = {
                    filtroRapido = if (filtroRapido == FiltroMensajes.SIN_ASIGNAR) {
                        FiltroMensajes.TODAS
                    } else {
                        FiltroMensajes.SIN_ASIGNAR
                    }
                },
                label = { Text("Sin asignar") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Lista de Chats o Estado Vacío
        if (ticketsConversacion.isEmpty()) {
            EstadoVacioCard(
                icono = Icons.Filled.Forum,
                titulo = "Sin conversaciones activas",
                subtitulo = if (textoFiltro.isNotEmpty()) {
                    "No se encontraron chats que coincidan con la búsqueda."
                } else {
                    "Cuando abras un ticket o el equipo de soporte te responda, la conversación aparecerá aquí."
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
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
        } else {
            PullToRefreshBox(
                isRefreshing = solicitudVm.cargando,
                onRefresh = { solicitudVm.cargarSolicitudes(usuario) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
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
}

@Composable
private fun TarjetaConversacionItem(
    ticket: Solicitud,
    alHacerClic: () -> Unit
) {
    // Reutiliza formatearEstado y tokens semánticos compartidos con las tarjetas de bandeja
    val estadoTexto = formatearEstado(ticket.estado)
    val (colorEstado, fondoEstado) = when (ticket.estado) {
        EstadoSolicitud.RECIBIDA -> Pair(TeamworkBlue, TeamworkBlue.copy(alpha = 0.10f))
        EstadoSolicitud.EN_REVISION, EstadoSolicitud.PENDIENTE_INFORMACION -> Pair(Color(0xFFB54708), Color(0xFFFEF0C7))
        EstadoSolicitud.ASIGNADA -> Pair(Color(0xFF7A5AF8), Color(0xFFF4F3FF))
        EstadoSolicitud.EN_PROCESO, EstadoSolicitud.RESUELTA -> Pair(SuccessGreenText, SuccessGreenBg)
        EstadoSolicitud.CERRADA -> Pair(Color(0xFF475569), Color(0xFFF1F5F9))
        EstadoSolicitud.CANCELADA -> Pair(AlertRedText, AlertRedBg)
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
