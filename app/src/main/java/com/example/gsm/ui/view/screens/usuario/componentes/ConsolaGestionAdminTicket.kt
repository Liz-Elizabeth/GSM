package com.example.gsm.ui.view.screens.usuario.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.Solicitud
import com.example.gsm.data.model.Usuario

/**
 * Consola de gestión operativa para el Administrador dentro del detalle del ticket.
 * Permite ajustar criticidad SLA, asignar/desasignar técnicos, ir al chat o cancelar el ticket.
 */
@Composable
fun ConsolaGestionAdminTicket(
    sol: Solicitud,
    usuario: Usuario,
    prioridadElegida: PrioridadSolicitud,
    onPrioridadElegidaChange: (PrioridadSolicitud) -> Unit,
    tecnicosDisponibles: List<Usuario>,
    totalMensajesHistorial: Int,
    onGuardarPrioridad: (PrioridadSolicitud) -> Unit,
    onAsignarTecnico: (Int) -> Unit,
    onSolicitarDesasignar: (Usuario) -> Unit,
    onAbrirChat: () -> Unit,
    onSolicitarCancelar: () -> Unit
) {
    val tecnicoAsignado = remember(sol.tecnicoId, tecnicosDisponibles) {
        tecnicosDisponibles.find { it.id == sol.tecnicoId }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Panel de Control Operativo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Gestión de criticidad, asignación de especialistas y auditoría",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8E8E93)
                )
            }
            BadgePill(
                texto = sol.estado.name,
                colorFondo = TeamworkBlue.copy(alpha = 0.12f),
                colorTexto = TeamworkBlue
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 1. Nivel de Criticidad (SLA)
    SeccionTitulo("1. Nivel de Criticidad (SLA)")
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Severidad y Tiempo de Respuesta",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Actual: ${sol.prioridad.name}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TeamworkBlue
                )
            }

            // Alineado exactamente con SolicitudLocalRepository.calcularSlaFechaLimite
            val prioridadesConfig = listOf(
                Triple(PrioridadSolicitud.BAJA, "🟢 Baja", "SLA: 168 hrs máx"),
                Triple(PrioridadSolicitud.MEDIA, "🟡 Media", "SLA: 72 hrs máx"),
                Triple(PrioridadSolicitud.ALTA, "🟠 Alta", "SLA: 24 hrs máx"),
                Triple(PrioridadSolicitud.URGENTE, "🔴 Urgente", "SLA: 6 hrs máx")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                prioridadesConfig.chunked(2).forEach { fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fila.forEach { (prio, etiqueta, sla) ->
                            val seleccionada = prioridadElegida == prio
                            val colorTema = when (prio) {
                                PrioridadSolicitud.BAJA -> Color(0xFF2E7D32)
                                PrioridadSolicitud.MEDIA -> TeamworkBlue
                                PrioridadSolicitud.ALTA -> Color(0xFFE65100)
                                PrioridadSolicitud.URGENTE -> Color(0xFFD32F2F)
                            }
                            val colorFondo = if (seleccionada) colorTema.copy(alpha = 0.08f) else Color(0xFFF9F9FB)

                            Surface(
                                onClick = { onPrioridadElegidaChange(prio) },
                                shape = RoundedCornerShape(12.dp),
                                color = colorFondo,
                                border = BorderStroke(
                                    width = if (seleccionada) 2.dp else 1.dp,
                                    color = if (seleccionada) colorTema else Color(0xFFE5E5EA)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = etiqueta,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (seleccionada) colorTema else Color(0xFF1C1C1E)
                                        )
                                        if (seleccionada) {
                                            Icon(
                                                Icons.Filled.CheckCircle,
                                                contentDescription = null,
                                                tint = colorTema,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = sla,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (seleccionada) colorTema.copy(alpha = 0.85f) else Color(0xFF8E8E93),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (prioridadElegida != sol.prioridad) {
                Button(
                    onClick = { onGuardarPrioridad(prioridadElegida) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Nueva Prioridad (${prioridadElegida.name})", fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF2F2F7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ El ticket ya se encuentra en severidad ${sol.prioridad.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF636366),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Asignación de Técnico
    SeccionTitulo("2. Asignación de Técnico")
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Especialista a Cargo",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            if (tecnicoAsignado != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SuccessGreenBg,
                    border = BorderStroke(1.dp, SuccessGreenText.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SuccessGreenText,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = tecnicoAsignado.nombre.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = tecnicoAsignado.nombre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1C1E)
                                )
                                BadgePill(
                                    texto = "Asignado",
                                    colorFondo = SuccessGreenText.copy(alpha = 0.15f),
                                    colorTexto = SuccessGreenText
                                )
                            }
                            Text(
                                text = tecnicoAsignado.correo,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF3C3C43)
                            )
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = InternalNoteBg,
                    border = BorderStroke(1.dp, InternalNoteText.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.PersonOff, contentDescription = null, tint = InternalNoteText)
                        Column {
                            Text(
                                text = "Sin técnico asignado",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = InternalNoteText
                            )
                            Text(
                                text = "Elige un especialista disponible de la lista para atender el ticket.",
                                style = MaterialTheme.typography.labelSmall,
                                color = InternalNoteText.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            Text(
                text = "Especialistas Disponibles en Soporte:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF636366),
                modifier = Modifier.padding(top = 4.dp)
            )

            if (tecnicosDisponibles.isEmpty()) {
                Text(
                    text = "No hay técnicos disponibles.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tecnicosDisponibles.forEach { tecnico ->
                        val estaAsignado = sol.tecnicoId == tecnico.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (estaAsignado) SuccessGreenBg.copy(alpha = 0.5f) else Color(0xFFF9F9FB),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (estaAsignado) SuccessGreenText.copy(alpha = 0.35f) else Color(0xFFEBEBEF)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (estaAsignado) SuccessGreenText else TeamworkBlue.copy(alpha = 0.15f),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = tecnico.nombre.take(1).uppercase(),
                                                color = if (estaAsignado) Color.White else TeamworkBlue,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = tecnico.nombre,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1C1C1E)
                                        )
                                        Text(
                                            text = tecnico.correo,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF8E8E93),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (estaAsignado) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = SuccessGreenBg,
                                            border = BorderStroke(1.dp, SuccessGreenText.copy(alpha = 0.5f))
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = SuccessGreenText,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "A cargo",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = SuccessGreenText,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = { onSolicitarDesasignar(tecnico) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            ),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.PersonRemove,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Desasignar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    FilledTonalButton(
                                        onClick = { onAsignarTecnico(tecnico.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                                    ) {
                                        Text("Asignar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Acceso al Chat
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = TeamworkBlue.copy(alpha = 0.12f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = TeamworkBlue)
                    }
                }
                Column {
                    Text(
                        text = "Conversación del Ticket",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$totalMensajesHistorial mensaje(s) intercambiados",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8E8E93)
                    )
                }
            }
            Button(
                onClick = onAbrirChat,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue)
            ) {
                Text("Abrir Chat", fontWeight = FontWeight.Bold)
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 3. Zona de Control: Cancelación
    SeccionTitulo("3. Zona de Control")
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AlertRedBg,
        border = BorderStroke(1.dp, AlertRedText.copy(alpha = 0.25f)),
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
                Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = AlertRedText)
                Text(
                    text = "Cancelar Solicitud",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AlertRedText
                )
            }
            Text(
                text = "Si la solicitud no procede o fue duplicada, puedes anularla dejando constancia formal del motivo en la auditoría.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4A4A4A),
                lineHeight = 18.sp
            )
            OutlinedButton(
                onClick = onSolicitarCancelar,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRedText),
                border = BorderStroke(1.5.dp, AlertRedText),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cancelar este Ticket", fontWeight = FontWeight.Bold)
            }
        }
    }
}
