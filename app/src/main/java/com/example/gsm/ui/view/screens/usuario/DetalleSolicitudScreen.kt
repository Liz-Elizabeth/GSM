package com.example.gsm.ui.view.screens.usuario

import com.example.gsm.data.local.entity.EvidenciaEntity
import com.example.gsm.ui.view.components.VisorEvidenciaModal
import com.example.gsm.ui.view.components.DatosEvidenciaVisualizar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.gsm.util.FileUtil
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.view.screens.usuario.componentes.*
import com.example.gsm.ui.viewmodel.DetalleSolicitudViewModel
import com.example.gsm.util.QrCodeUtil

/**
 * Pantalla orquestadora del Detalle de Solicitud.
 * Sigue la estética híbrida Apple (iOS HIG) + Teamwork Desk bajo una arquitectura modular limpia (KISS).
 */
@Composable
fun DetalleSolicitudScreen(
    solicitudId: Int,
    usuario: Usuario,
    detalleVm: DetalleSolicitudViewModel,
    alVolver: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(solicitudId, usuario.rol) {
        detalleVm.limpiarMensaje()
        detalleVm.cargarDetalle(solicitudId, usuario.rol)
    }

    val sol = detalleVm.solicitud

    var textoMensaje by remember { mutableStateOf("") }
    var esNotaInterna by remember { mutableStateOf(false) }
    var prioridadElegida by remember { mutableStateOf(PrioridadSolicitud.ALTA) }
    var mostrarQrTicket by remember { mutableStateOf(false) }
    var evidenciaParaVer by remember { mutableStateOf<EvidenciaEntity?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && sol != null) {
            val adjunto = FileUtil.obtenerDatosDeUri(context, uri)
            detalleVm.adjuntarEvidencia(
                solicitudId = sol.id,
                usuario = usuario,
                nombre = adjunto.nombre,
                tipo = adjunto.tipoMime,
                tamano = adjunto.tamanoBytes,
                uri = adjunto.uriString
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Encabezado Superior con Navigation Bar estilo Apple
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

        // Banner de notificación o feedback del ViewModel
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

        // 2. Ficha Resumen del Ticket (Zona rediseñada)
        FichaResumenTicket(solicitud = sol)

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Consola de Gestión o Tarjeta de Cierre
        when (sol.estado) {
            EstadoSolicitud.CERRADA -> {
                TicketCerradoAnimadoCard(
                    solicitud = sol,
                    esCliente = usuario.rol == RolUsuario.CLIENTE,
                    onSolicitarReapertura = { motivo ->
                        detalleVm.solicitarReapertura(sol.id, usuario, motivo)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            EstadoSolicitud.CANCELADA -> {
                TicketCanceladoCard(solicitud = sol)
                Spacer(modifier = Modifier.height(16.dp))
            }

            else -> {
                // Panel administrativo para cambiar prioridad, asignar técnico o cancelar
                if (usuario.rol == RolUsuario.ADMINISTRADOR) {
                    SeccionTitulo("Consola de Gestión • Administrador")
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
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
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Guardar Prioridad ${prioridadElegida.name}", fontWeight = FontWeight.SemiBold)
                            }

                            HorizontalDivider(color = AppleBackground)

                            Text(
                                text = "2. Asignar Responsable",
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
                                    detalleVm.cancelar(sol.id, usuario, textoMensaje)
                                    textoMensaje = ""
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🚫 Cancelar Solicitud",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Panel rápido de Soporte Técnico
                if (usuario.rol == RolUsuario.TECNICO) {
                    SeccionTitulo("Acciones de Soporte Técnico")
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilledTonalButton(
                                    onClick = {
                                        val motivo = if (textoMensaje.isNotBlank()) textoMensaje else "Se requiere información adicional"
                                        detalleVm.solicitarInformacion(sol.id, usuario, motivo)
                                        textoMensaje = ""
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("❓ Pedir Info")
                                }

                                Button(
                                    onClick = {
                                        val resol = if (textoMensaje.isNotBlank()) textoMensaje else "Problema resuelto satisfactoriamente"
                                        detalleVm.registrarResolucion(sol.id, usuario, resol)
                                        textoMensaje = ""
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreenText),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("✅ Resolver")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Panel de confirmación para el Cliente (Ticket Resuelto o Pendiente)
                if (usuario.rol == RolUsuario.CLIENTE) {
                    if (sol.estado == EstadoSolicitud.PENDIENTE_INFORMACION) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = InternalNoteBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⏳", fontSize = 24.sp)
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Información Solicitada",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = InternalNoteText,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "El equipo de soporte necesita antecedentes. Escribe tu respuesta abajo y pulsa Enviar.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF5A2A00)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (sol.estado == EstadoSolicitud.RESUELTA) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "✨ Tu ticket ha sido marcado como Resuelto",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreenText
                                )
                                Text(
                                    text = "Por favor confirma si estás conforme con la solución brindada para dar por finalizado el caso:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = { detalleVm.confirmarCierre(sol.id, usuario, true) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreenText),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("👍 Confirmar Conformidad y Cerrar Ticket", fontWeight = FontWeight.SemiBold)
                                }
                                FilledTonalButton(
                                    onClick = {
                                        detalleVm.solicitarReapertura(sol.id, usuario, textoMensaje.ifBlank { "Reapertura solicitada por el cliente" })
                                        textoMensaje = ""
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("🔄 El problema continúa (Reabrir)")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // 4. Conversación y Chat estilo WhatsApp (Burbujas + Barra de Envío)
        ChatConversacionTicket(
            historial = detalleVm.historial,
            usuarioActualId = usuario.id,
            usuarioActualRol = usuario.rol,
            estaCerrado = sol.estado == EstadoSolicitud.CERRADA || sol.estado == EstadoSolicitud.CANCELADA,
            textoMensaje = textoMensaje,
            onTextoChange = { textoMensaje = it },
            esNotaInterna = esNotaInterna,
            onNotaInternaChange = { esNotaInterna = it },
            alEnviar = {
                when (usuario.rol) {
                    RolUsuario.CLIENTE -> {
                        detalleVm.responderInformacion(sol.id, usuario, textoMensaje)
                        textoMensaje = ""
                    }
                    RolUsuario.TECNICO, RolUsuario.ADMINISTRADOR -> {
                        detalleVm.registrarAvance(sol.id, usuario, textoMensaje, esNotaInterna)
                        textoMensaje = ""
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Integración WhatsApp y Evidencias
        SeccionTitulo("Integración y Archivos")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = { detalleVm.simularEnvioWhatsApp() },
                            enabled = !detalleVm.enviandoWhatsApp,
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (detalleVm.enviandoWhatsApp) "Enviando..." else "Simular envío",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = {
                                QrCodeUtil.compartirQrConTexto(
                                    context = context,
                                    codigo = sol.codigoSeguimiento,
                                    texto = detalleVm.mensajeWhatsApp,
                                    tituloChooser = "Compartir ticket y QR por WhatsApp"
                                )
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreenText),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Compartir 📲",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = detalleVm.mensajeWhatsApp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = AppleBackground)

                // Rótulo QR generado en la app
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🔲 Rótulo QR del Ticket",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Para escaneo en terreno con CameraX",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { mostrarQrTicket = true },
                        shape = CircleShape,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = TeamworkBlue.copy(alpha = 0.12f),
                            contentColor = TeamworkBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text("Ver QR", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = AppleBackground)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📎 Evidencias (${detalleVm.evidencias.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = {
                                detalleVm.adjuntarEvidencia(
                                    solicitudId = sol.id,
                                    usuario = usuario,
                                    nombre = "captura_pantalla_error.png",
                                    tipo = "image/png",
                                    tamano = 420_000L,
                                    uri = "file:///demo/captura_pantalla_error.png"
                                )
                            },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Filled.Smartphone, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Demo", style = MaterialTheme.typography.labelSmall)
                        }

                        FilledTonalButton(
                            onClick = { filePickerLauncher.launch("*/*") },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Adjuntar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (detalleVm.evidencias.isEmpty()) {
                    Text(
                        text = "Sin archivos adjuntos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    detalleVm.evidencias.forEach { ev ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AppleBackground,
                            onClick = { evidenciaParaVer = ev },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = FileUtil.obtenerIconoVectorSegunMime(ev.tipoArchivo),
                                        contentDescription = null,
                                        tint = TeamworkBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = ev.nombreArchivo,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${FileUtil.formatearTamano(ev.tamanoBytes)} • Toca para ver",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    BadgePill(
                                        texto = ev.estadoRevision,
                                        colorFondo = Color.White,
                                        colorTexto = TeamworkBlue
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.Visibility,
                                        contentDescription = "Ver",
                                        tint = TeamworkBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Modal de Visualización de Rótulo QR del Ticket
    if (mostrarQrTicket && sol != null) {
        val codigo = sol.codigoSeguimiento
        val qrBitmap = remember(codigo) {
            QrCodeUtil.generarQrBitmap(codigo, 512, 512)
        }

        AlertDialog(
            onDismissRequest = { mostrarQrTicket = false },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "🔲 Rótulo QR de Solicitud",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (qrBitmap != null) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = AppleBackground,
                            modifier = Modifier.padding(6.dp)
                        ) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Código QR del Ticket",
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(10.dp)
                            )
                        }
                    }

                    Text(
                        text = codigo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TeamworkBlue
                    )

                    Text(
                        text = "Escanea este código con CameraX desde la Bandeja GSM para abrir la ficha del ticket al instante.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { mostrarQrTicket = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue)
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Modal para Visualizar Evidencias In-App (Imágenes, Texto/Logs y Documentos)
    if (evidenciaParaVer != null) {
        val ev = evidenciaParaVer!!
        val fechaFormateada = remember(ev.fechaIncorporacion) {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ev.fechaIncorporacion))
        }
        VisorEvidenciaModal(
            evidencia = DatosEvidenciaVisualizar(
                nombreArchivo = ev.nombreArchivo,
                tipoArchivo = ev.tipoArchivo,
                tamanoBytes = ev.tamanoBytes,
                uriString = ev.uriLocal,
                fecha = fechaFormateada,
                estadoRevision = ev.estadoRevision
            ),
            onDismiss = { evidenciaParaVer = null }
        )
    }
}
