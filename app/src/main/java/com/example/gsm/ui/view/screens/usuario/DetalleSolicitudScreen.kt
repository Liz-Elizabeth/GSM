package com.example.gsm.ui.view.screens.usuario

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.model.EstadoSolicitud
import com.example.gsm.model.PrioridadSolicitud
import com.example.gsm.model.RolUsuario
import com.example.gsm.model.Usuario
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.viewmodel.DetalleSolicitudViewModel

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
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        EncabezadoPantalla(
            titulo = sol?.codigoSeguimiento ?: "Cargando...",
            alHacerClicDerecho = alVolver
        )

        if (sol == null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Cargando información de la solicitud...")
            return@Column
        }

        if (detalleVm.mensaje != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ℹ️ ${detalleVm.mensaje}",
                    modifier = Modifier.padding(12.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 1. FICHA DE DATOS DE LA SOLICITUD
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(sol.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(sol.descripcion)
                Spacer(modifier = Modifier.height(8.dp))
                Text("📌 Estado: ${sol.estado.name}", fontWeight = FontWeight.SemiBold)
                Text("⚡ Prioridad: ${sol.prioridad.name}")
                Text("🏷️ Categoría: ${sol.categoria.descripcion}")
                if (!sol.servicioRelacionado.isNullOrBlank()) {
                    Text("🛠️ Servicio: ${sol.servicioRelacionado}")
                }
                if (!sol.resolucion.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("✅ Solución Técnica: ${sol.resolucion}", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. TARJETA DE DERIVACIÓN A WHATSAPP
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("📲 Mensaje Estructurado para WhatsApp", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(detalleVm.mensajeWhatsApp, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { detalleVm.simularEnvioWhatsApp() },
                    enabled = !detalleVm.enviandoWhatsApp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (detalleVm.enviandoWhatsApp) "Enviando a WhatsApp..." else "Simular Envío por WhatsApp")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. PANEL DE ACCIONES SEGÚN EL ROL DEL USUARIO CONECTADO
        Text(
            text = "Acciones disponibles (${usuario.rol.name})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = textoAccion,
            onValueChange = { textoAccion = it },
            label = { Text("Comentario / Motivo / Solución") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (usuario.rol) {
            RolUsuario.ADMINISTRADOR -> {
                Text("1. Elegir Prioridad y Clasificar:", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrioridadSolicitud.entries.forEach { prio ->
                        FilterChip(
                            selected = prioridadElegida == prio,
                            onClick = { prioridadElegida = prio },
                            label = { Text(prio.name) }
                        )
                    }
                }
                Button(
                    onClick = {
                        detalleVm.clasificarYPriorizar(sol.id, sol.categoria, prioridadElegida, usuario)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clasificar como ${prioridadElegida.name} (Pasa a EN_REVISION)")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("2. Asignar Responsable Técnico:", style = MaterialTheme.typography.labelLarge)
                detalleVm.tecnicosDisponibles.forEach { tecnico ->
                    OutlinedButton(
                        onClick = { detalleVm.asignarTecnico(sol.id, tecnico.id, usuario) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Asignar a: ${tecnico.nombre}")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        detalleVm.cancelar(sol.id, usuario, textoAccion)
                        textoAccion = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🚫 Cancelar Solicitud (Usa el motivo escrito arriba)")
                }
            }

            RolUsuario.TECNICO -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = esNotaInterna,
                        onCheckedChange = { esNotaInterna = it }
                    )
                    Text("Es comentario interno (Oculto para el Cliente)")
                }

                Button(
                    onClick = {
                        detalleVm.registrarAvance(sol.id, usuario, textoAccion, esNotaInterna)
                        textoAccion = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔧 Registrar Avance (Pasa a EN_PROCESO)")
                }

                OutlinedButton(
                    onClick = {
                        detalleVm.solicitarInformacion(sol.id, usuario, textoAccion)
                        textoAccion = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("❓ Solicitar Más Información al Cliente")
                }

                Button(
                    onClick = {
                        detalleVm.registrarResolucion(sol.id, usuario, textoAccion)
                        textoAccion = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("✅ Registrar Solución Final (Pasa a RESUELTA)")
                }
            }

            RolUsuario.CLIENTE -> {
                if (sol.estado == EstadoSolicitud.PENDIENTE_INFORMACION) {
                    Button(
                        onClick = {
                            detalleVm.responderInformacion(sol.id, usuario, textoAccion)
                            textoAccion = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📤 Enviar Información Solicitada")
                    }
                }

                if (sol.estado == EstadoSolicitud.RESUELTA) {
                    Button(
                        onClick = { detalleVm.confirmarCierre(sol.id, usuario, true) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("👍 Confirmar Conformidad y Cerrar Solicitud")
                    }
                }

                if (sol.estado == EstadoSolicitud.RESUELTA || sol.estado == EstadoSolicitud.CERRADA) {
                    OutlinedButton(
                        onClick = {
                            detalleVm.solicitarReapertura(sol.id, usuario, textoAccion)
                            textoAccion = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔄 Solicitar Reapertura (Usa el motivo escrito arriba)")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))

        // 4. EVIDENCIAS ADJUNTAS
        Text("📎 Evidencias Adjuntas (${detalleVm.evidencias.size})", fontWeight = FontWeight.Bold)
        if (detalleVm.evidencias.isEmpty()) {
            Text("Sin archivos adjuntos.", style = MaterialTheme.typography.bodySmall)
        } else {
            detalleVm.evidencias.forEach { ev ->
                Text("• ${ev.nombreArchivo} (${ev.tipoArchivo}) - Estado: ${ev.estadoRevision}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. HISTORIAL DE AUDITORÍA
        Text("🕒 Historial de Seguimiento (${detalleVm.historial.size})", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        detalleVm.historial.forEach { evento ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "${evento.accion} → ${evento.estadoNuevo.name} (${evento.perfilUsuario.name})",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = evento.comentario)
                    if (evento.esComentarioInterno) {
                        Text(
                            text = "🔒 Nota interna (Oculta al cliente)",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
