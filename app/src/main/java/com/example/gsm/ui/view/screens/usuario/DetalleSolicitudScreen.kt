package com.example.gsm.ui.view.screens.usuario

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.core.content.FileProvider
import com.example.gsm.data.local.entity.EvidenciaEntity
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.DatosEvidenciaVisualizar
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.view.components.SkeletonDetalleTicket
import com.example.gsm.ui.view.components.VisorEvidenciaModal
import com.example.gsm.ui.view.screens.usuario.componentes.*
import com.example.gsm.ui.viewmodel.DetalleSolicitudViewModel
import com.example.gsm.util.FileUtil
import com.example.gsm.util.QrCodeUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatearEstado(estado: EstadoSolicitud): String = when (estado) {
    EstadoSolicitud.RECIBIDA -> "Recibida"
    EstadoSolicitud.EN_REVISION -> "En revisión"
    EstadoSolicitud.ASIGNADA -> "Asignada"
    EstadoSolicitud.EN_PROCESO -> "En proceso"
    EstadoSolicitud.PENDIENTE_INFORMACION -> "Pendiente info"
    EstadoSolicitud.RESUELTA -> "Resuelta"
    EstadoSolicitud.CERRADA -> "Cerrada"
    EstadoSolicitud.CANCELADA -> "Cancelada"
}

private fun formatearPrioridad(prioridad: PrioridadSolicitud): String = when (prioridad) {
    PrioridadSolicitud.BAJA -> "Baja"
    PrioridadSolicitud.MEDIA -> "Media"
    PrioridadSolicitud.ALTA -> "Alta"
    PrioridadSolicitud.URGENTE -> "Urgente"
}

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

    // Estados para edición de Información
    var modoEdicion by remember { mutableStateOf(false) }
    var tituloEditado by remember(sol?.titulo) { mutableStateOf(sol?.titulo ?: "") }
    var descripcionEditada by remember(sol?.descripcion) { mutableStateOf(sol?.descripcion ?: "") }

    // Estados para Gestión y Cancelación
    var prioridadElegida by remember(sol?.prioridad) { mutableStateOf(sol?.prioridad ?: PrioridadSolicitud.MEDIA) }
    var mostrarDialogoCancelar by remember { mutableStateOf(false) }
    var motivoCancelacion by remember { mutableStateOf("") }
    var mostrarDialogoSolicitarCancelacion by remember { mutableStateOf(false) }
    var motivoSolicitudCancelacion by remember { mutableStateOf("") }
    var mostrarDialogoDesasignar by remember { mutableStateOf(false) }
    var tecnicoADesasignar by remember { mutableStateOf<Usuario?>(null) }

    // Estado y archivos para la Cámara Nativa
    var uriFotoCamara by remember { mutableStateOf<Uri?>(null) }
    var archivoFotoCamara by remember { mutableStateOf<File?>(null) }

    // Launcher de la Cámara para tomar fotos reales
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito && archivoFotoCamara != null && sol != null) {
            val file = archivoFotoCamara!!
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

    // Función para activar la cámara de fotos
    fun tomarFotoConCamara() {
        val dir = File(context.filesDir, "evidencias").apply { if (!exists()) mkdirs() }
        val photoFile = File(dir, "FOTO_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        archivoFotoCamara = photoFile
        uriFotoCamara = uri
        cameraLauncher.launch(uri)
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
            val fechaCreacionFormateada = remember(sol.fechaCreacion) {
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(sol.fechaCreacion))
            }

            // A. TARJETA DE INFORMACIÓN DE LA SOLICITUD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SeccionTitulo("Información del Ticket")
                if (!modoEdicion && sol.estado != EstadoSolicitud.CERRADA && sol.estado != EstadoSolicitud.CANCELADA) {
                    FilledTonalButton(
                        onClick = {
                            tituloEditado = sol.titulo
                            descripcionEditada = sol.descripcion
                            modoEdicion = true
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = TeamworkBlue.copy(alpha = 0.10f),
                            contentColor = TeamworkBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Fila de Badges de Estado y Prioridad
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (fondoEstado, textoEstado) = when (sol.estado) {
                            EstadoSolicitud.CERRADA, EstadoSolicitud.RESUELTA -> Pair(SuccessGreenBg, SuccessGreenText)
                            EstadoSolicitud.CANCELADA -> Pair(AlertRedBg, AlertRedText)
                            EstadoSolicitud.PENDIENTE_INFORMACION -> Pair(InternalNoteBg, InternalNoteText)
                            else -> Pair(TeamworkBlue.copy(alpha = 0.12f), TeamworkBlue)
                        }

                        val (fondoPrio, textoPrio) = when (sol.prioridad) {
                            PrioridadSolicitud.URGENTE -> Pair(Color(0xFFFFEBEE), Color(0xFFD32F2F))
                            PrioridadSolicitud.ALTA -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100))
                            PrioridadSolicitud.MEDIA -> Pair(Color(0xFFEBF3FF), TeamworkBlue)
                            PrioridadSolicitud.BAJA -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
                        }

                        BadgePill(
                            texto = formatearEstado(sol.estado),
                            colorFondo = fondoEstado,
                            colorTexto = textoEstado
                        )

                        BadgePill(
                            texto = "Prioridad ${formatearPrioridad(sol.prioridad)}",
                            colorFondo = fondoPrio,
                            colorTexto = textoPrio
                        )

                        BadgePill(
                            texto = sol.categoria.descripcion,
                            colorFondo = AppleBackground,
                            colorTexto = Color(0xFF1C1C1E)
                        )
                    }

                    if (modoEdicion) {
                        // MODO EDICIÓN ACTIVO
                        OutlinedTextField(
                            value = tituloEditado,
                            onValueChange = { tituloEditado = it },
                            label = { Text("Título del Ticket") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = descripcionEditada,
                            onValueChange = { descripcionEditada = it },
                            label = { Text("Descripción Detallada") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            maxLines = 8,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { modoEdicion = false },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("Cancelar")
                            }

                            Button(
                                onClick = {
                                    if (tituloEditado.isNotBlank() && descripcionEditada.isNotBlank()) {
                                        detalleVm.actualizarInformacion(sol.id, tituloEditado, descripcionEditada, usuario)
                                        modoEdicion = false
                                    }
                                },
                                enabled = tituloEditado.isNotBlank() && descripcionEditada.isNotBlank(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Guardar", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // MODO VISUALIZACIÓN
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = sol.titulo,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1E)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFF8E8E93),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Registrado el $fechaCreacionFormateada",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF8E8E93)
                                )
                            }
                        }

                        HorizontalDivider(color = AppleBackground)

                        // Contenedor de la descripción estilo Apple Notes / Card
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "DESCRIPCIÓN DEL CASO",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8E8E93),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF9F9FB),
                                border = BorderStroke(1.dp, Color(0xFFEFEFF4)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = sol.descripcion,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF2C2C2E),
                                    lineHeight = 24.sp,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }

                        // Metadatos adicionales (Servicio y Activo)
                        if (!sol.servicioRelacionado.isNullOrBlank() || !sol.activoRelacionado.isNullOrBlank()) {
                            HorizontalDivider(color = AppleBackground)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (!sol.servicioRelacionado.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = AppleBackground,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Filled.Build, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp))
                                            Column {
                                                Text("Servicio", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                                                Text(sol.servicioRelacionado, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                                if (!sol.activoRelacionado.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = AppleBackground,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Filled.Language, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp))
                                            Column {
                                                Text("Activo Web", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                                                Text(sol.activoRelacionado, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

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
                                    imageVector = Icons.Filled.Chat,
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
                        imageVector = Icons.Filled.ArrowForwardIos,
                        contentDescription = "Ir al chat",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // C. EVIDENCIAS ADJUNTAS (CON CÁMARA Y ARCHIVOS - SIN DEMO)
            SeccionTitulo("Evidencias Adjuntas (${detalleVm.evidencias.size})")
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Barra de Acciones de Evidencia
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Botón de Cámara Real
                        FilledTonalButton(
                            onClick = { tomarFotoConCamara() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = TeamworkBlue.copy(alpha = 0.10f),
                                contentColor = TeamworkBlue
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Cámara", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tomar Foto", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Botón de Adjuntar Archivo
                        Button(
                            onClick = { filePickerLauncher.launch("*/*") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(Icons.Filled.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Subir Archivo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    if (detalleVm.evidencias.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AppleBackground,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AttachFile,
                                    contentDescription = null,
                                    tint = Color(0xFF8E8E93),
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "Sin evidencias adjuntas todavía",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1C1C1E)
                                )
                                Text(
                                    text = "Toma una foto o sube documentos de respaldo para el equipo técnico.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF8E8E93),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            detalleVm.evidencias.forEach { ev ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF9F9FB),
                                    border = BorderStroke(1.dp, Color(0xFFEBEBEF)),
                                    onClick = { evidenciaParaVer = ev },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = TeamworkBlue.copy(alpha = 0.12f),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = FileUtil.obtenerIconoVectorSegunMime(ev.tipoArchivo),
                                                        contentDescription = null,
                                                        tint = TeamworkBlue,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            Column {
                                                Text(
                                                    text = ev.nombreArchivo,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF1C1C1E),
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "${FileUtil.formatearTamano(ev.tamanoBytes)} • Toca para previsualizar",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFF8E8E93)
                                                )
                                            }
                                        }

                                        Icon(
                                            imageVector = Icons.Filled.Visibility,
                                            contentDescription = "Ver archivo",
                                            tint = TeamworkBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // D. Acciones de Cierre para Cliente o Técnico
            if (usuario.rol == RolUsuario.CLIENTE && sol.estado == EstadoSolicitud.RESUELTA) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SuccessGreenBg,
                    border = BorderStroke(1.dp, SuccessGreenText.copy(alpha = 0.3f)),
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
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreenText)
                            Text(
                                text = "Ticket Resuelto por Soporte",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreenText
                            )
                        }
                        Text(
                            text = "El equipo técnico ha finalizado la atención. Por favor confirma tu conformidad para cerrar el caso.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2C2C2E)
                        )
                        Button(
                            onClick = { detalleVm.confirmarCierre(sol.id, usuario, true) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreenText),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Confirmar Conformidad y Cerrar Ticket", fontWeight = FontWeight.Bold)
                        }
                    }
                }
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
            val qrBitmap = remember(sol.codigoSeguimiento) {
                QrCodeUtil.generarQrBitmap(sol.codigoSeguimiento, 480, 480)
            }

            SeccionTitulo("Pase Digital y Código QR")
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Cabecera superior del Pase
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TeamworkBlue)
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "GSM PASE TÉCNICO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = sol.codigoSeguimiento,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            BadgePill(
                                texto = sol.estado.name,
                                colorFondo = Color.White.copy(alpha = 0.2f),
                                colorTexto = Color.White
                            )
                        }
                    }

                    // Cuerpo del Pase con QR y detalles
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = sol.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1E)
                            )
                            Text(
                                text = "🏷️ ${sol.categoria.descripcion} • Prioridad: ${sol.prioridad.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(color = AppleBackground)

                        // Código QR renderizado en vivo
                        if (qrBitmap != null) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = AppleBackground,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Código QR ${sol.codigoSeguimiento}",
                                    modifier = Modifier
                                        .size(200.dp)
                                        .padding(12.dp)
                                )
                            }
                        }

                        Text(
                            text = "Muestra este código al personal técnico o compártelo para consulta y seguimiento rápido del caso.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF636366),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Botón de Compartir Pase y QR
                        Button(
                            onClick = {
                                val mensajeTexto = "🎫 *PASE DIGITAL GSM*\n" +
                                        "📌 Ticket: ${sol.codigoSeguimiento}\n" +
                                        "📝 Título: ${sol.titulo}\n" +
                                        "🏷️ Categoría: ${sol.categoria.descripcion}\n" +
                                        "⚡ Prioridad: ${sol.prioridad.name}\n" +
                                        "📊 Estado: ${sol.estado.name}\n" +
                                        "🔗 Seguimiento: https://gsm-web-delta.vercel.app/seguimiento/${sol.codigoSeguimiento}"

                                QrCodeUtil.compartirQrConTexto(
                                    context = context,
                                    codigo = sol.codigoSeguimiento,
                                    texto = mensajeTexto,
                                    tituloChooser = "Compartir Pase y QR por WhatsApp"
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Compartir Pase y QR",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Botón secundario para ir al Chat
                        OutlinedButton(
                            onClick = alIrAChat,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Filled.Chat, contentDescription = null, tint = TeamworkBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Abrir Chat del Ticket (${detalleVm.historial.size})",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // PESTAÑA 2: CONSOLA DE GESTIÓN • ADMINISTRADOR
        // -------------------------------------------------------------
        if (usuario.rol == RolUsuario.ADMINISTRADOR && pestanaInterna == 2) {
            val tecnicoAsignado = remember(sol.tecnicoId, detalleVm.tecnicosDisponibles) {
                detalleVm.tecnicosDisponibles.find { it.id == sol.tecnicoId }
            }

            // A. TARJETA RESUMEN DE ESTADO
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

            // B. TARJETA CRITICIDAD Y PRIORIDAD SLA
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

                    // Cuadrícula 2x2 de Opciones de Prioridad Estilizadas
                    val prioridadesConfig = listOf(
                        Triple(PrioridadSolicitud.BAJA, "🟢 Baja", "SLA: 72 hrs máx"),
                        Triple(PrioridadSolicitud.MEDIA, "🟡 Media", "SLA: 48 hrs máx"),
                        Triple(PrioridadSolicitud.ALTA, "🟠 Alta", "SLA: 24 hrs máx"),
                        Triple(PrioridadSolicitud.URGENTE, "🔴 Urgente", "SLA: Inmediato")
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
                                        onClick = { prioridadElegida = prio },
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

                    // Botón de Confirmación de Criticidad
                    if (prioridadElegida != sol.prioridad) {
                        Button(
                            onClick = {
                                detalleVm.clasificarYPriorizar(sol.id, sol.categoria, prioridadElegida, usuario)
                            },
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

            // C. TARJETA ASIGNACIÓN DE TÉCNICO
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

                    // Banner informativo del técnico actual asignado
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

                    if (detalleVm.tecnicosDisponibles.isEmpty()) {
                        Text(
                            text = "No hay técnicos disponibles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            detalleVm.tecnicosDisponibles.forEach { tecnico ->
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
                                                    onClick = {
                                                        tecnicoADesasignar = tecnico
                                                        mostrarDialogoDesasignar = true
                                                    },
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
                                                onClick = { detalleVm.asignarTecnico(sol.id, tecnico.id, usuario) },
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

            // D. TARJETA ACCESO AL CHAT
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
                                Icon(Icons.Filled.Chat, contentDescription = null, tint = TeamworkBlue)
                            }
                        }
                        Column {
                            Text(
                                text = "Conversación del Ticket",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${detalleVm.historial.size} mensaje(s) intercambiados",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8E8E93)
                            )
                        }
                    }
                    Button(
                        onClick = alIrAChat,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue)
                    ) {
                        Text("Abrir Chat", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // E. TARJETA ZONA DE CONTROL • CANCELACIÓN
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
                        onClick = { mostrarDialogoCancelar = true },
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

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Diálogo Modal para Solicitar Cancelación al Administrador (Cliente)
    if (mostrarDialogoSolicitarCancelacion && sol != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoSolicitarCancelacion = false },
            title = { Text("Solicitar Anulación de Ticket", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Indica el motivo por el cual solicitas anular este ticket (ej. equivocación en el título, ticket duplicado, etc.):")
                    OutlinedTextField(
                        value = motivoSolicitudCancelacion,
                        onValueChange = { motivoSolicitudCancelacion = it },
                        placeholder = { Text("Motivo de la solicitud de anulación...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val motivo = if (motivoSolicitudCancelacion.isNotBlank()) motivoSolicitudCancelacion.trim() else "Solicitud de anulación por equivocación"
                        detalleVm.solicitarCancelacion(sol.id, usuario, motivo)
                        mostrarDialogoSolicitarCancelacion = false
                        motivoSolicitudCancelacion = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue)
                ) {
                    Text("Enviar al Administrador", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoSolicitarCancelacion = false }) {
                    Text("Volver")
                }
            }
        )
    }

    // Diálogo Modal para Cancelar Solicitud (Administrador)
    if (mostrarDialogoCancelar && sol != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCancelar = false },
            title = { Text("Cancelar Solicitud", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Ingresa el motivo de cancelación para dejar registro en auditoría:")
                    OutlinedTextField(
                        value = motivoCancelacion,
                        onValueChange = { motivoCancelacion = it },
                        placeholder = { Text("Motivo de cancelación...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val motivo = if (motivoCancelacion.isNotBlank()) motivoCancelacion.trim() else "Cancelada por el usuario."
                        detalleVm.cancelar(sol.id, usuario, motivo)
                        mostrarDialogoCancelar = false
                        motivoCancelacion = ""
                        alVolver()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirmar Cancelación", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCancelar = false }) {
                    Text("Volver")
                }
            }
        )
    }

    // Modal de Confirmación para Desasignar Técnico (Exclusivo Administrador)
    if (mostrarDialogoDesasignar && tecnicoADesasignar != null && sol != null) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoDesasignar = false
                tecnicoADesasignar = null
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White,
            icon = {
                Icon(
                    Icons.Filled.PersonRemove,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "¿Desasignar Técnico?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas retirar a ${tecnicoADesasignar?.nombre} de este ticket? La solicitud volverá al estado 'En revisión' para que pueda ser reasignada a otro especialista.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        detalleVm.desasignarTecnico(sol.id, usuario)
                        mostrarDialogoDesasignar = false
                        tecnicoADesasignar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirmar Desasignación")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoDesasignar = false
                    tecnicoADesasignar = null
                }) {
                    Text("Cancelar")
                }
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
}
