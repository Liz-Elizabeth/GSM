package com.example.gsm.ui.view.screens.usuario

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.local.entity.EvidenciaEntity
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.DatosEvidenciaVisualizar
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.view.components.PantallaExitoCompleta
import com.example.gsm.ui.view.components.SkeletonDetalleTicket
import com.example.gsm.ui.view.components.VisorEvidenciaModal
import com.example.gsm.ui.view.screens.usuario.componentes.*
import com.example.gsm.ui.viewmodel.DetalleSolicitudViewModel
import com.example.gsm.util.FileUtil
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla Principal del Detalle de Solicitud (Patrón KISS).
 *
 * Incluye un menú de navegación interno por pestañas (TabRow):
 * - Pestaña 1: "Información y Evidencias" (Título, descripción editable, botón de chat y cámara).
 * - Pestaña 2: "Gestión Admin" (Consola de criticidad, asignación de técnicos y cancelación).
 */
@Composable
fun DetalleSolicitudScreen(
    solicitudId: Int,
    usuario: Usuario,
    detalleVm: DetalleSolicitudViewModel,
    alVolver: () -> Unit,
    alIrAChat: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(solicitudId, usuario.rol) {
        detalleVm.limpiarMensaje()
        detalleVm.cargarDetalle(solicitudId, usuario.rol)
    }

    val sol = detalleVm.solicitud
    var evidenciaParaVer by remember { mutableStateOf<EvidenciaEntity?>(null) }

    // Menú de navegación interno dentro del ticket (0 = Detalle, 1 = Gestión Admin)
    var pestanaInterna by remember { mutableIntStateOf(0) }

    // Estados para Gestión y Cancelación
    var prioridadElegida by remember(sol?.prioridad) { mutableStateOf(sol?.prioridad ?: PrioridadSolicitud.MEDIA) }
    var mostrarDialogoCancelar by remember { mutableStateOf(false) }
    var mostrarDialogoSolicitarCancelacion by remember { mutableStateOf(false) }
    var mostrarDialogoDesasignar by remember { mutableStateOf(false) }
    var tecnicoADesasignar by remember { mutableStateOf<Usuario?>(null) }
    var exitoCierreTexto by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(exitoCierreTexto) {
        if (exitoCierreTexto != null) {
            delay(1150)
            alVolver()
        }
    }

    // Ruta persistida con rememberSaveable para no perder la foto si Android recrea la Activity
    var rutaFotoCamara by rememberSaveable { mutableStateOf<String?>(null) }

    // Launcher de la Cámara para tomar fotos reales
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        val ruta = rutaFotoCamara
        if (exito && ruta != null && sol != null) {
            val file = File(ruta)
            if (file.exists() && file.length() > 0L) {
                detalleVm.adjuntarEvidencia(
                    solicitudId = sol.id,
                    usuario = usuario,
                    nombre = file.name,
                    tipo = "image/jpeg",
                    tamano = file.length(),
                    uri = Uri.fromFile(file).toString()
                )
            }
        }
        rutaFotoCamara = null
    }

    fun ejecutarCapturaCamara() {
        try {
            val (photoFile, uri) = FileUtil.crearArchivoFotoTemporal(context, "FOTO")
            rutaFotoCamara = photoFile.absolutePath
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir la cámara: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            ejecutarCapturaCamara()
        } else {
            Toast.makeText(context, "Se requiere permiso de cámara para capturar fotos", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para elegir archivos de la galería o documentos
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

    // Verifica permiso de cámara en runtime antes de lanzar TakePicture
    fun tomarFotoConCamara() {
        val permiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permiso == PackageManager.PERMISSION_GRANTED) {
            ejecutarCapturaCamara()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Encabezado Superior con Navigation Bar
        EncabezadoPantalla(
            titulo = sol?.codigoSeguimiento ?: "Cargando...",
            alHacerClicDerecho = alVolver
        )

        if (sol == null || detalleVm.cargando) {
            Spacer(modifier = Modifier.height(14.dp))
            SkeletonDetalleTicket()
            return@Column
        }

        // Feedback / Notificación de mensaje
        if (detalleVm.mensaje != null) {
            Spacer(modifier = Modifier.height(10.dp))
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

        Spacer(modifier = Modifier.height(10.dp))

        // -------------------------------------------------------------
        // MENÚ DE NAVEGACIÓN INTERNO DEL TICKET (Control Segmentado estilo iOS)
        // -------------------------------------------------------------
        val tabs = if (usuario.rol == RolUsuario.ADMINISTRADOR) {
            listOf(
                Pair("📄 Detalle", 0),
                Pair("📱 Pase QR", 1),
                Pair("👑 Gestión", 2)
            )
        } else {
            listOf(
                Pair("📄 Información y Evidencias", 0),
                Pair("📱 Pase QR y Compartir", 1)
            )
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFE5E5EA),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEach { (titulo, index) ->
                    val seleccionado = pestanaInterna == index
                    Surface(
                        onClick = { pestanaInterna = index },
                        shape = RoundedCornerShape(10.dp),
                        color = if (seleccionado) Color.White else Color.Transparent,
                        shadowElevation = if (seleccionado) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = titulo,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                                color = if (seleccionado) TeamworkBlue else Color(0xFF636366),
                                fontSize = if (tabs.size > 2) 12.sp else 13.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // -------------------------------------------------------------
        // PESTAÑA 0: INFORMACIÓN Y EVIDENCIAS
        // -------------------------------------------------------------
        if (pestanaInterna == 0) {
            // A. TARJETA DE INFORMACIÓN, EDICIÓN Y COMPARTIR DEL TICKET
            TarjetaInfoTicket(
                sol = sol,
                onActualizarInformacion = { nuevoTitulo, nuevaDesc, nuevaCat, nuevoServ, nuevoAct ->
                    detalleVm.actualizarInformacion(
                        solicitudId = sol.id,
                        nuevoTitulo = nuevoTitulo,
                        nuevaDescripcion = nuevaDesc,
                        usuario = usuario,
                        nuevaCategoria = nuevaCat,
                        nuevoServicio = nuevoServ,
                        nuevoActivo = nuevoAct
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // B. TARJETA ACCESO AL CHAT (UBICADA ENTRE INFORMACIÓN Y EVIDENCIAS)
            Surface(
                onClick = alIrAChat,
                shape = RoundedCornerShape(16.dp),
                color = TeamworkBlue,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Conversación del Ticket",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${detalleVm.historial.size} mensaje(s) • Toca para abrir chat",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Ir al chat",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // C. EVIDENCIAS ADJUNTAS (extraído para evitar recomposiciones innecesarias)
            SeccionEvidenciasTicket(
                evidencias = detalleVm.evidencias,
                onTomarFoto = { tomarFotoConCamara() },
                onSubirArchivo = { filePickerLauncher.launch("*/*") },
                onVerEvidencia = { evidenciaParaVer = it }
            )

            // D. Acciones de Cierre para Cliente o Técnico
            if (usuario.rol == RolUsuario.CLIENTE && sol.estado == EstadoSolicitud.RESUELTA) {
                Spacer(modifier = Modifier.height(16.dp))
                TarjetaCierreCalificacionCliente(
                    onConfirmarCierre = { calificacion ->
                        detalleVm.confirmarCierre(sol.id, usuario, true, calificacion)
                        exitoCierreTexto = "Calificación: $calificacion ★"
                    }
                )
            } else if (usuario.rol == RolUsuario.TECNICO && sol.estado != EstadoSolicitud.CERRADA && sol.estado != EstadoSolicitud.RESUELTA) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        detalleVm.registrarResolucion(sol.id, usuario, "Problema resuelto satisfactoriamente por soporte técnico.")
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreenText),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Marcar Ticket como Resuelto", fontWeight = FontWeight.Bold)
                }
            }

            // ZONA DE SOLICITUD DE ANULACIÓN (CLIENTE) O GESTIÓN FORMAL (ADMINISTRADOR)
            val yaSolicitoCancelacion = detalleVm.historial.any { it.accion == "SOLICITUD_CANCELACION" }

            if (sol.estado != EstadoSolicitud.CANCELADA && sol.estado != EstadoSolicitud.CERRADA) {
                Spacer(modifier = Modifier.height(16.dp))

                if (usuario.rol == RolUsuario.CLIENTE) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, if (yaSolicitoCancelacion) Color(0xFFFEDF89) else Color(0xFFFEE4E2)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (yaSolicitoCancelacion) Color(0xFFFEF0C7) else Color(0xFFFEE4E2),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (yaSolicitoCancelacion) Icons.Filled.HourglassTop else Icons.Filled.Cancel,
                                            contentDescription = null,
                                            tint = if (yaSolicitoCancelacion) Color(0xFFB54708) else AlertRedText,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (yaSolicitoCancelacion) "Anulación Solicitada al Administrador" else "¿Te equivocaste en este ticket?",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1C1E)
                                )
                            }

                            Text(
                                text = if (yaSolicitoCancelacion) {
                                    "Ya le solicitaste al administrador la anulación de esta solicitud. El equipo técnico revisará el caso y al confirmarse se archivará formalmente."
                                } else {
                                    "Si creaste esta solicitud por error o ya no necesitas soporte, puedes solicitar su anulación formal al administrador para que se archive en tu historial."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )

                            if (!yaSolicitoCancelacion) {
                                OutlinedButton(
                                    onClick = { mostrarDialogoSolicitarCancelacion = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRedText),
                                    border = BorderStroke(1.dp, Color(0xFFFDA29B)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                ) {
                                    Icon(Icons.Filled.ReportProblem, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Solicitar Anulación por Error", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else if (usuario.rol == RolUsuario.ADMINISTRADOR) {
                    // Vista del Administrador: si el cliente pidió anular, se lo resalta de inmediato
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (yaSolicitoCancelacion) Color(0xFFFFFBEB) else Color.White,
                        border = BorderStroke(1.dp, if (yaSolicitoCancelacion) Color(0xFFF59E0B) else Color(0xFFFEE4E2)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (yaSolicitoCancelacion) Icons.Filled.NotificationImportant else Icons.Filled.Cancel,
                                    contentDescription = null,
                                    tint = if (yaSolicitoCancelacion) Color(0xFFD97706) else AlertRedText
                                )
                                Text(
                                    text = if (yaSolicitoCancelacion) "El Cliente Solicitó Anular este Ticket" else "Zona de Cancelación (Administrador)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (yaSolicitoCancelacion) Color(0xFF92400E) else AlertRedText
                                )
                            }
                            Text(
                                text = if (yaSolicitoCancelacion) {
                                    "El cliente reportó una equivocación y pide la baja formal. Puedes aprobar la cancelación para archivar el ticket y liberar la bandeja."
                                } else {
                                    "Como Administrador puedes anular esta solicitud dejando constancia justificada en el registro de auditoría."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                            Button(
                                onClick = { mostrarDialogoCancelar = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                            ) {
                                Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (yaSolicitoCancelacion) "Aprobar y Confirmar Cancelación" else "Cancelar / Anular Ticket", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (sol.estado == EstadoSolicitud.CANCELADA) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFF1F2),
                    border = BorderStroke(1.dp, Color(0xFFFDA29B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = AlertRedText)
                            Text(
                                text = "Ticket Cancelado y Archivado",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AlertRedText
                            )
                        }
                        if (!sol.motivoCancelacion.isNullOrBlank()) {
                            Text(
                                text = "Motivo: ${sol.motivoCancelacion}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9F1239),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "Este ticket se encuentra preservado en tu Archivo de Tickets de respaldo. Puedes solicitar reapertura si necesitas reanudar el caso.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // PESTAÑA 1: PASE DIGITAL Y CÓDIGO QR CON BOTÓN COMPARTIR
        // -------------------------------------------------------------
        if (pestanaInterna == 1) {
            SeccionTitulo("Pase Digital y Código QR")
            TarjetaWalletTicket(
                solicitud = sol,
                alVerDetalle = { pestanaInterna = 0 },
                inicialAbierto = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = alIrAChat,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = TeamworkBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Abrir Chat del Ticket (${detalleVm.historial.size})",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // -------------------------------------------------------------
        // PESTAÑA 2: CONSOLA DE GESTIÓN • ADMINISTRADOR
        // -------------------------------------------------------------
        if (usuario.rol == RolUsuario.ADMINISTRADOR && pestanaInterna == 2) {
            ConsolaGestionAdminTicket(
                sol = sol,
                usuario = usuario,
                prioridadElegida = prioridadElegida,
                onPrioridadElegidaChange = { prioridadElegida = it },
                tecnicosDisponibles = detalleVm.tecnicosDisponibles,
                totalMensajesHistorial = detalleVm.historial.size,
                onGuardarPrioridad = { prio ->
                    detalleVm.clasificarYPriorizar(sol.id, sol.categoria, prio, usuario)
                },
                onAsignarTecnico = { tecnicoId ->
                    detalleVm.asignarTecnico(sol.id, tecnicoId, usuario)
                },
                onSolicitarDesasignar = { tecnico ->
                    tecnicoADesasignar = tecnico
                    mostrarDialogoDesasignar = true
                },
                onAbrirChat = alIrAChat,
                onSolicitarCancelar = { mostrarDialogoCancelar = true }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (mostrarDialogoSolicitarCancelacion && sol != null) {
        DialogoMotivoTicket(
            titulo = "Solicitar Anulación de Ticket",
            descripcion = "Indica el motivo por el cual solicitas anular este ticket (ej. equivocación en el título, ticket duplicado, etc.):",
            placeholder = "Motivo de la solicitud de anulación...",
            textoConfirmar = "Enviar al Administrador",
            colorConfirmar = TeamworkBlue,
            onConfirmar = { texto ->
                val motivo = texto.trim().ifBlank { "Solicitud de anulación por equivocación" }
                detalleVm.solicitarCancelacion(sol.id, usuario, motivo)
                mostrarDialogoSolicitarCancelacion = false
            },
            onDismiss = { mostrarDialogoSolicitarCancelacion = false }
        )
    }

    if (mostrarDialogoCancelar && sol != null) {
        DialogoMotivoTicket(
            titulo = "Cancelar Solicitud",
            descripcion = "Ingresa el motivo de cancelación para dejar registro en auditoría:",
            placeholder = "Motivo de cancelación...",
            textoConfirmar = "Confirmar Cancelación",
            colorConfirmar = MaterialTheme.colorScheme.error,
            onConfirmar = { texto ->
                val motivo = texto.trim().ifBlank { "Cancelada por el usuario." }
                detalleVm.cancelar(sol.id, usuario, motivo)
                mostrarDialogoCancelar = false
                alVolver()
            },
            onDismiss = { mostrarDialogoCancelar = false }
        )
    }

    if (mostrarDialogoDesasignar && tecnicoADesasignar != null && sol != null) {
        DialogoDesasignarTecnico(
            nombreTecnico = tecnicoADesasignar?.nombre.orEmpty(),
            onConfirmar = {
                detalleVm.desasignarTecnico(sol.id, usuario)
                mostrarDialogoDesasignar = false
                tecnicoADesasignar = null
            },
            onDismiss = {
                mostrarDialogoDesasignar = false
                tecnicoADesasignar = null
            }
        )
    }
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

    if (exitoCierreTexto != null) {
        PantallaExitoCompleta(
            titulo = "¡Caso cerrado con conformidad!",
            subtitulo = exitoCierreTexto
        )
    }
}
