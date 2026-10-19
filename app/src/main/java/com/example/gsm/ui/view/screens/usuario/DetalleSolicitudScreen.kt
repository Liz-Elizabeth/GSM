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

private val AppleBackground = Color(0xFFF2F2F7)

@Composable
private fun SeccionTitulo(texto: String) {
    Text(
        text = texto.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
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
            .padding(horizontal = 20.dp, vertical = 16.dp)
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
                CircularProgressIndicator()
            }
            return@Column
        }

        // Banner de notificación limpio
        if (detalleVm.mensaje != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ℹ️ ${detalleVm.mensaje}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. FICHA DE DATOS DE LA SOLICITUD
        SeccionTitulo("Detalle del Requerimiento")
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
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

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = AppleBackground)

                Text("📌 Estado: ${sol.estado.name}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("⚡ Prioridad: ${sol.prioridad.name}", style = MaterialTheme.typography.bodyMedium)
                Text("🏷️ Categoría: ${sol.categoria.descripcion}", style = MaterialTheme.typography.bodyMedium)

                if (!sol.servicioRelacionado.isNullOrBlank()) {
                    Text("🛠️ Servicio: ${sol.servicioRelacionado}", style = MaterialTheme.typography.bodyMedium)
                }
                if (!sol.resolucion.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AppleBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✅ Solución Técnica: ${sol.resolucion}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. TARJETA DE DERIVACIÓN A WHATSAPP
        SeccionTitulo("Notificación Externa")
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📲 Mensaje para WhatsApp",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = detalleVm.mensajeWhatsApp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = { detalleVm.simularEnvioWhatsApp() },
                    enabled = !detalleVm.enviandoWhatsApp,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (detalleVm.enviandoWhatsApp) "Enviando..." else "Simular Envío por WhatsApp",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. PANEL DE ACCIONES SEGÚN EL ROL
        SeccionTitulo("Gestión (${usuario.rol.name})")

        TextField(
            value = textoAccion,
            onValueChange = { textoAccion = it },
            label = { Text("Comentario / Motivo / Solución") },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (usuario.rol) {
                RolUsuario.ADMINISTRADOR -> {
                    Text("Prioridad:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clasificar como ${prioridadElegida.name} (EN_REVISION)")
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Asignar Técnico:", style = MaterialTheme.typography.labelMedium)
                    detalleVm.tecnicosDisponibles.forEach { tecnico ->
                        FilledTonalButton(
                            onClick = { detalleVm.asignarTecnico(sol.id, tecnico.id, usuario) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Asignar a ${tecnico.nombre}", fontWeight = FontWeight.Medium)
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
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Nota interna (Oculta al cliente)", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = esNotaInterna,
                                onCheckedChange = { esNotaInterna = it }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            detalleVm.registrarAvance(sol.id, usuario, textoAccion, esNotaInterna)
                            textoAccion = ""
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔧 Registrar Avance (EN_PROCESO)")
                    }

                    FilledTonalButton(
                        onClick = {
                            detalleVm.solicitarInformacion(sol.id, usuario, textoAccion)
                            textoAccion = ""
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("❓ Solicitar Más Información")
                    }

                    Button(
                        onClick = {
                            detalleVm.registrarResolucion(sol.id, usuario, textoAccion)
                            textoAccion = ""
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✅ Registrar Solución Final (RESUELTA)")
                    }
                }

                RolUsuario.CLIENTE -> {
                    if (sol.estado == EstadoSolicitud.PENDIENTE_INFORMACION) {
                        Button(
                            onClick = {
                                detalleVm.responderInformacion(sol.id, usuario, textoAccion)
                                textoAccion = ""
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📤 Enviar Información Solicitada")
                        }
                    }

                    if (sol.estado == EstadoSolicitud.RESUELTA) {
                        Button(
                            onClick = { detalleVm.confirmarCierre(sol.id, usuario, true) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("👍 Confirmar Conformidad y Cerrar")
                        }
                    }

                    if (sol.estado == EstadoSolicitud.RESUELTA || sol.estado == EstadoSolicitud.CERRADA) {
                        FilledTonalButton(
                            onClick = {
                                detalleVm.solicitarReapertura(sol.id, usuario, textoAccion)
                                textoAccion = ""
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🔄 Solicitar Reapertura")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. EVIDENCIAS ADJUNTAS
        SeccionTitulo("Evidencias Adjuntas (${detalleVm.evidencias.size})")
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (detalleVm.evidencias.isEmpty()) {
                    Text(
                        text = "Sin archivos adjuntos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    detalleVm.evidencias.forEach { ev ->
                        Text(
                            text = "📎 ${ev.nombreArchivo} (${ev.tipoArchivo}) • ${ev.estadoRevision}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. HISTORIAL DE AUDITORÍA
        SeccionTitulo("Historial de Seguimiento (${detalleVm.historial.size})")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            detalleVm.historial.forEach { evento ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${evento.accion} → ${evento.estadoNuevo.name} (${evento.perfilUsuario.name})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = evento.comentario,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (evento.esComentarioInterno) {
                            Text(
                                text = "🔒 Nota interna (Oculta al cliente)",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}