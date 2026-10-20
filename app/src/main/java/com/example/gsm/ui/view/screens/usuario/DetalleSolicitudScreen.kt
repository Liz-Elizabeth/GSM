package com.example.gsm.ui.view.screens.usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.viewmodel.DetalleSolicitudViewModel

// Paleta Apple + Teamwork
private val AppleBackground = Color(0xFFF2F2F7)
private val TeamworkBlue = Color(0xFF0066FF)
private val InternalNoteBg = Color(0xFFFFF8E6)
private val InternalNoteText = Color(0xFFB25000)
private val SuccessGreenBg = Color(0xFFE8F8EE)
private val SuccessGreenText = Color(0xFF1B7F37)

@Composable
private fun SeccionTitulo(texto: String) {
    Text(
        text = texto.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
private fun BadgePill(texto: String, colorFondo: Color, colorTexto: Color) {
    Surface(color = colorFondo, shape = CircleShape) {
        Text(
            text = texto.replace("_", " "),
            style = MaterialTheme.typography.labelSmall,
            color = colorTexto,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DetalleSolicitudScreen(
    solicitudId: Int,
    usuario: Usuario,
    detalleVm: DetalleSolicitudViewModel,
    alVolver: () -> Unit
) {
    LaunchedEffect(solicitudId, usuario.rol) {
        detalleVm.limpiarMensaje()
        detalleVm.cargarDetalle(solicitudId, usuario.rol)
    }

    val sol = detalleVm.solicitud

    var textoAccion by remember { mutableStateOf("") }
    var esNotaInterna by remember { mutableStateOf(false) }
    var prioridadElegida by remember { mutableStateOf(PrioridadSolicitud.ALTA) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        EncabezadoPantalla(
            titulo = sol?.codigoSeguimiento ?: "Cargando...",
            alHacerClicDerecho = alVolver
        )

        if (sol == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TeamworkBlue)
            }
            return@Column
        }

        // Banner de confirmación / error
        if (detalleVm.mensaje != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = TeamworkBlue.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ℹ️ ${detalleVm.mensaje}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TeamworkBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. FICHA DEL TICKET ESTILO TEAMWORK DESK
        SeccionTitulo("Resumen del Ticket")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Píldoras de Estado y Prioridad a primera vista
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BadgePill(
                        texto = "📌 ${sol.estado.name}",
                        colorFondo = TeamworkBlue.copy(alpha = 0.12f),
                        colorTexto = TeamworkBlue
                    )
                    BadgePill(
                        texto = "⚡ ${sol.prioridad.name}",
                        colorFondo = AppleBackground,
                        colorTexto = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = sol.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = sol.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = AppleBackground)

                // Metadatos limpios
                Text(
                    text = "🏷️ Categoría: ${sol.categoria.descripcion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!sol.servicioRelacionado.isNullOrBlank()) {
                    Text(
                        text = "🛠️ Servicio: ${sol.servicioRelacionado}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Caja destacada de Solución Técnica (Verde suave)
                if (!sol.resolucion.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SuccessGreenBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = "✅ Solución: ${sol.resolucion}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SuccessGreenText,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. CONSOLA DE GESTIÓN UNIFICADA (Caja de Respuesta + Botones de Rol)
        SeccionTitulo("Consola de Gestión • ${usuario.rol.name}")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Input de comentario estilo Spotlight dentro de la tarjeta
                TextField(
                    value = textoAccion,
                    onValueChange = { textoAccion = it },
                    placeholder = {
                        Text(
                            text = "✍️ Escribe un comentario, motivo o solución...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if (esNotaInterna) InternalNoteBg else AppleBackground,
                        unfocusedContainerColor = if (esNotaInterna) InternalNoteBg else AppleBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                when (usuario.rol) {
                    RolUsuario.ADMINISTRADOR -> {
                        Text(
                            text = "1. Clasificar Prioridad",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PrioridadSolicitud.entries.forEach { prio ->
                                FilterChip(
                                    selected = prioridadElegida == prio,
                                    onClick = { prioridadElegida = prio },
                                    label = { Text(prio.name) },
                                    shape = CircleShape,
                                    border = null
                                )
                            }
                        }
                        Button(
                            onClick = {
                                detalleVm.clasificarYPriorizar(sol.id, sol.categoria, prioridadElegida, usuario)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Guardar Prioridad ${prioridadElegida.name}", fontWeight = FontWeight.SemiBold)
                        }

                        HorizontalDivider(color = AppleBackground)

                        Text(
                            text = "2. Asignar Responsable (1 Clic)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            detalleVm.tecnicosDisponibles.forEach { tecnico ->
                                FilledTonalButton(
                                    onClick = { detalleVm.asignarTecnico(sol.id, tecnico.id, usuario) },
                                    shape = CircleShape
                                ) {
                                    Text("👤 ${tecnico.nombre}", fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        TextButton(
                            onClick = {
                                detalleVm.cancelar(sol.id, usuario, textoAccion)
                                textoAccion = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🚫 Cancelar Solicitud",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    RolUsuario.TECNICO -> {
                        // Switch de Nota Interna con alerta visual ámbar estilo Teamwork
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (esNotaInterna) InternalNoteBg else AppleBackground,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔒 Nota interna (Oculta al cliente)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (esNotaInterna) InternalNoteText else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Switch(
                                    checked = esNotaInterna,
                                    onCheckedChange = { esNotaInterna = it }
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    detalleVm.solicitarInformacion(sol.id, usuario, textoAccion)
                                    textoAccion = ""
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("❓ Pedir Info")
                            }

                            Button(
                                onClick = {
                                    detalleVm.registrarAvance(sol.id, usuario, textoAccion, esNotaInterna)
                                    textoAccion = ""
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("🔧 Avance")
                            }
                        }

                        Button(
                            onClick = {
                                detalleVm.registrarResolucion(sol.id, usuario, textoAccion)
                                textoAccion = ""
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreenText),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("✅ Marcar como Resuelta", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    RolUsuario.CLIENTE -> {
                        if (sol.estado == EstadoSolicitud.PENDIENTE_INFORMACION) {
                            Button(
                                onClick = {
                                    detalleVm.responderInformacion(sol.id, usuario, textoAccion)
                                    textoAccion = ""
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("📤 Enviar Información Solicitada")
                            }
                        }

                        if (sol.estado == EstadoSolicitud.RESUELTA) {
                            Button(
                                onClick = { detalleVm.confirmarCierre(sol.id, usuario, true) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreenText),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("👍 Confirmar y Cerrar Ticket")
                            }
                        }

                        if (sol.estado == EstadoSolicitud.RESUELTA || sol.estado == EstadoSolicitud.CERRADA) {
                            FilledTonalButton(
                                onClick = {
                                    detalleVm.solicitarReapertura(sol.id, usuario, textoAccion)
                                    textoAccion = ""
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("🔄 Solicitar Reapertura")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. NOTIFICACIÓN WHATSAPP Y EVIDENCIAS
        SeccionTitulo("Integración y Archivos")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📲 Resumen WhatsApp",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    FilledTonalButton(
                        onClick = { detalleVm.simularEnvioWhatsApp() },
                        enabled = !detalleVm.enviandoWhatsApp,
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (detalleVm.enviandoWhatsApp) "Enviando..." else "Enviar ahora",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = detalleVm.mensajeWhatsApp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = AppleBackground)

                Text(
                    text = "📎 Evidencias (${detalleVm.evidencias.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (detalleVm.evidencias.isEmpty()) {
                    Text(
                        text = "Sin archivos adjuntos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    detalleVm.evidencias.forEach { ev ->
                        Text(
                            text = "• ${ev.nombreArchivo} (${ev.tipoArchivo}) • ${ev.estadoRevision}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. HISTORIAL DE ACTIVIDAD (Timeline estilo Teamwork)
        SeccionTitulo("Actividad del Ticket (${detalleVm.historial.size})")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            detalleVm.historial.forEach { evento ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (evento.esComentarioInterno) InternalNoteBg else Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${evento.accion} → ${evento.estadoNuevo.name.replace("_", " ")}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = evento.perfilUsuario.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = evento.comentario,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (evento.esComentarioInterno) {
                            Text(
                                text = "🔒 Nota interna del equipo",
                                color = InternalNoteText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}