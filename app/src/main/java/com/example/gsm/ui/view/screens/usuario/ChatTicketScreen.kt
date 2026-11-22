package com.example.gsm.ui.view.screens.usuario

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.DatosEvidenciaVisualizar
import com.example.gsm.ui.view.components.VisorEvidenciaModal
import com.example.gsm.ui.view.screens.usuario.componentes.*
import com.example.gsm.ui.viewmodel.DetalleSolicitudViewModel
import com.example.gsm.util.ArchivoAdjunto
import com.example.gsm.util.FileUtil
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val formatoFechaChat = SimpleDateFormat("dd 'de' MMMM", Locale.forLanguageTag("es-ES"))

private fun obtenerEtiquetaFecha(fechaMs: Long, inicioHoyMs: Long, inicioAyerMs: Long): String {
    if (fechaMs <= 0 || fechaMs >= inicioHoyMs) return "Hoy"
    if (fechaMs >= inicioAyerMs) return "Ayer"

    return synchronized(formatoFechaChat) {
        formatoFechaChat.format(Date(fechaMs))
    }
}

/**
 * Pantalla de Chat estilo WhatsApp / Instagram Direct.
 * La barra inferior de escritura vive en BarraEntradaChat para aislar recomposiciones al teclear.
 */
@Composable
fun ChatTicketScreen(
    solicitudId: Int,
    usuario: Usuario,
    detalleVm: DetalleSolicitudViewModel,
    alVolver: () -> Unit,
    alIrADetalle: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(solicitudId, usuario.rol) {
        detalleVm.cargarDetalle(solicitudId, usuario.rol)
    }

    val sol = detalleVm.solicitud
    val estaCerrado = sol?.estado == EstadoSolicitud.CERRADA || sol?.estado == EstadoSolicitud.CANCELADA

    var textoMensaje by remember { mutableStateOf("") }
    var esNotaInterna by remember { mutableStateOf(false) }
    var evidenciaSeleccionada by remember { mutableStateOf<ArchivoAdjunto?>(null) }
    var evidenciaParaVer by remember { mutableStateOf<DatosEvidenciaVisualizar?>(null) }
    var enviandoLocal by remember { mutableStateOf(false) }

    // Se guarda la ruta con rememberSaveable para no perderla si Android recrea la Activity al abrir la cámara
    var rutaFotoCamara by rememberSaveable { mutableStateOf<String?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        val ruta = rutaFotoCamara
        if (exito && ruta != null) {
            val file = File(ruta)
            if (file.exists() && file.length() > 0L) {
                evidenciaSeleccionada = ArchivoAdjunto(
                    nombre = file.name,
                    tipoMime = "image/jpeg",
                    tamanoBytes = file.length(),
                    uriString = Uri.fromFile(file).toString()
                )
            }
        }
        rutaFotoCamara = null
    }

    fun ejecutarCapturaCamara() {
        try {
            val (photoFile, uri) = FileUtil.crearArchivoFotoTemporal(context, "CHAT_IMG")
            rutaFotoCamara = photoFile.absolutePath
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Log.e("ChatTicketScreen", "Error al abrir cámara: ${e.message}", e)
            Toast.makeText(context, "No se pudo abrir la cámara: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            ejecutarCapturaCamara()
        } else {
            Toast.makeText(context, "Se requiere permiso de cámara para adjuntar fotos", Toast.LENGTH_SHORT).show()
        }
    }

    fun dispararCamara() {
        val permiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permiso == PackageManager.PERMISSION_GRANTED) {
            ejecutarCapturaCamara()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            evidenciaSeleccionada = FileUtil.obtenerDatosDeUri(context, uri)
        }
    }

    LaunchedEffect(detalleVm.historial.size) {
        if (detalleVm.historial.isNotEmpty()) {
            listState.animateScrollToItem(detalleVm.historial.size - 1)
        }
    }

    fun enviarMensajeActual() {
        if (enviandoLocal || detalleVm.enviandoMensajeChat) return
        val tieneTexto = textoMensaje.isNotBlank()
        val tieneAdjunto = evidenciaSeleccionada != null
        if (!tieneTexto && !tieneAdjunto) return

        enviandoLocal = true
        val textoFinal = if (tieneTexto) textoMensaje.trim() else "📷 Foto adjunta"
        val rutaAdjunto = evidenciaSeleccionada?.uriString

        textoMensaje = ""
        evidenciaSeleccionada = null

        detalleVm.enviarMensajeChat(
            solicitudId = solicitudId,
            usuario = usuario,
            texto = textoFinal,
            esInterno = esNotaInterna,
            evidenciaAdjunta = rutaAdjunto
        )

        coroutineScope.launch {
            kotlinx.coroutines.delay(400)
            enviandoLocal = false
            if (detalleVm.historial.isNotEmpty()) {
                listState.animateScrollToItem(detalleVm.historial.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatBackground)
    ) {
        // 1. CABECERA
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = alVolver) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color(0xFF1C1C1E)
                    )
                }

                Box {
                    Surface(
                        shape = CircleShape,
                        color = TeamworkBlue.copy(alpha = 0.12f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.SupportAgent,
                                contentDescription = null,
                                tint = TeamworkBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF22C55E),
                        border = BorderStroke(2.dp, Color.White),
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.BottomEnd)
                    ) {}
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (usuario.rol == RolUsuario.CLIENTE) "Soporte Técnico GSM" else (sol?.titulo ?: "Chat del Ticket"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "● En línea",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "• #${sol?.codigoSeguimiento ?: "TK"}",
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8E8E93),
                            fontSize = 11.sp
                        )
                    }
                }

                if (sol != null) {
                    Surface(
                        color = TeamworkBlue.copy(alpha = 0.10f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = formatearEstado(sol.estado),
                            style = MaterialTheme.typography.labelSmall,
                            color = TeamworkBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                if (alIrADetalle != null) {
                    IconButton(onClick = alIrADetalle, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Ver detalle del ticket",
                            tint = TeamworkBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 2. CONVERSACIÓN
        if (detalleVm.historial.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "👋 ¡Hola, ${usuario.nombre}!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E)
                        )
                        Text(
                            text = "Este es el canal directo de soporte técnico para tu solicitud.\nEscribe tu consulta o adjunta una fotografía del problema.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val (inicioHoyMs, inicioAyerMs) = remember {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val hoy = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, -1)
                Pair(hoy, cal.timeInMillis)
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(
                    items = detalleVm.historial,
                    key = { _, evento -> evento.id }
                ) { index, evento ->
                    val etiquetaActual = obtenerEtiquetaFecha(evento.fechaCambio, inicioHoyMs, inicioAyerMs)
                    val etiquetaAnterior = if (index > 0) {
                        obtenerEtiquetaFecha(detalleVm.historial[index - 1].fechaCambio, inicioHoyMs, inicioAyerMs)
                    } else null

                    if (etiquetaActual != etiquetaAnterior) {
                        ChipFechaConversacion(fechaTexto = etiquetaActual)
                    }

                    BurbujaChatWhatsApp(
                        evento = evento,
                        usuarioActualId = usuario.id,
                        usuarioActualRol = usuario.rol,
                        alVerEvidencia = { evidenciaParaVer = it }
                    )
                }
            }
        }

        // 3. BARRA INFERIOR DE ESCRITURA
        if (!estaCerrado) {
            BarraEntradaChat(
                rolUsuario = usuario.rol,
                textoMensaje = textoMensaje,
                onTextoChange = { textoMensaje = it },
                esNotaInterna = esNotaInterna,
                onToggleNotaInterna = { esNotaInterna = !esNotaInterna },
                evidenciaSeleccionada = evidenciaSeleccionada,
                onQuitarEvidencia = { evidenciaSeleccionada = null },
                enviando = enviandoLocal || detalleVm.enviandoMensajeChat,
                onTomarFoto = { dispararCamara() },
                onAdjuntarImagen = { galleryLauncher.launch("image/*") },
                onEnviar = { enviarMensajeActual() }
            )
        } else {
            Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(shape = CircleShape, color = Color(0xFFF1F5F9)) {
                        Text(
                            text = "🔒 Ticket finalizado. La conversación está cerrada.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }

    if (evidenciaParaVer != null) {
        VisorEvidenciaModal(
            evidencia = evidenciaParaVer!!,
            onDismiss = { evidenciaParaVer = null }
        )
    }
}
