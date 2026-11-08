package com.example.gsm.ui.view.screens.usuario

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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

private val ChatBackground = Color(0xFFECE5DD)
private val TeamworkBlue = Color(0xFF0066FF)

/**
 * Función auxiliar para agrupar mensajes por fecha ("Hoy", "Ayer" o "14 de Octubre").
 */
private fun obtenerEtiquetaFecha(fechaMs: Long): String {
    if (fechaMs <= 0) return "Hoy"
    val ahora = System.currentTimeMillis()
    val calHoy = Calendar.getInstance().apply { timeInMillis = ahora }
    val calMsg = Calendar.getInstance().apply { timeInMillis = fechaMs }

    val esHoy = calHoy.get(Calendar.YEAR) == calMsg.get(Calendar.YEAR) &&
            calHoy.get(Calendar.DAY_OF_YEAR) == calMsg.get(Calendar.DAY_OF_YEAR)
    if (esHoy) return "Hoy"

    val calAyer = Calendar.getInstance().apply { timeInMillis = ahora - 86400000L }
    val esAyer = calAyer.get(Calendar.YEAR) == calMsg.get(Calendar.YEAR) &&
            calAyer.get(Calendar.DAY_OF_YEAR) == calMsg.get(Calendar.DAY_OF_YEAR)
    if (esAyer) return "Ayer"

    return SimpleDateFormat("dd 'de' MMMM", Locale("es", "ES")).format(Date(fechaMs))
}

/**
 * Pantalla de Chat estilo WhatsApp / Instagram Direct (KISS & Social Experience).
 *
 * Diseñada para que cualquier usuario la use de inmediato sin curva de aprendizaje:
 * 1. Cabecera con avatar, estado de presencia en línea ("● En línea") y código del ticket.
 * 2. Conversación con separadores de fecha ("Hoy", "Ayer") y burbujas con checks de lectura (✓✓).
 * 3. Miniaturas de imágenes incrustadas dentro de la conversación con visor in-app.
 * 4. Barra inferior de entrada tipo cápsula con botón de cámara, galería y respuestas rápidas (⚡).
 * 5. Elevación suave con el teclado (imePadding) y auto-scroll fluido.
 */
@Composable
fun ChatTicketScreen(
    solicitudId: Int,
    usuario: Usuario,
    detalleVm: DetalleSolicitudViewModel,
    alVolver: () -> Unit
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
    var mostrarMenuAdjuntos by remember { mutableStateOf(false) }
    var mostrarPlantillasRapidas by remember { mutableStateOf(false) }
    var evidenciaSeleccionada by remember { mutableStateOf<ArchivoAdjunto?>(null) }
    var evidenciaParaVer by remember { mutableStateOf<DatosEvidenciaVisualizar?>(null) }

    // Manejador de Cámara Nativa Segura
    var archivoFotoCamara by remember { mutableStateOf<File?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito && archivoFotoCamara != null) {
            val file = archivoFotoCamara!!
            evidenciaSeleccionada = ArchivoAdjunto(
                nombre = file.name,
                tipoMime = "image/jpeg",
                tamanoBytes = file.length(),
                uriString = Uri.fromFile(file).toString()
            )
        }
    }

    fun ejecutarCapturaCamara() {
        try {
            val dir = File(context.filesDir, "chat_evidencias").apply { if (!exists()) mkdirs() }
            val photoFile = File(dir, "CHAT_IMG_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            archivoFotoCamara = photoFile
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

    // Manejador de Galería de Fotos / Documentos
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            evidenciaSeleccionada = FileUtil.obtenerDatosDeUri(context, uri)
        }
    }

    // Auto-scroll al último mensaje al cambiar historial
    LaunchedEffect(detalleVm.historial.size) {
        if (detalleVm.historial.isNotEmpty()) {
            listState.animateScrollToItem(detalleVm.historial.size - 1)
        }
    }

    // Función de envío único (KISS)
    fun enviarMensajeActual() {
        val tieneTexto = textoMensaje.isNotBlank()
        val tieneAdjunto = evidenciaSeleccionada != null
        if (!tieneTexto && !tieneAdjunto) return

        val textoFinal = if (tieneTexto) textoMensaje.trim() else "📷 Foto adjunta"
        val rutaAdjunto = evidenciaSeleccionada?.uriString

        detalleVm.enviarMensajeChat(
            solicitudId = solicitudId,
            usuario = usuario,
            texto = textoFinal,
            esInterno = esNotaInterna,
            evidenciaAdjunta = rutaAdjunto
        )

        textoMensaje = ""
        evidenciaSeleccionada = null
        mostrarPlantillasRapidas = false

        coroutineScope.launch {
            if (detalleVm.historial.isNotEmpty()) {
                listState.animateScrollToItem(detalleVm.historial.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatBackground)
            .imePadding()
            .navigationBarsPadding()
    ) {
        // -------------------------------------------------------------
        // 1. CABECERA ESTILO INSTAGRAM / WHATSAPP (Top App Bar)
        // -------------------------------------------------------------
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

                // Avatar circular con presencia online
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
                    // Punto verde de presencia ("En línea")
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF22C55E),
                        border = BorderStroke(2.dp, Color.White),
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.BottomEnd)
                    ) {}
                }

                // Datos de Soporte y Código del Ticket
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

                // Badge de estado o botón de información
                if (sol != null) {
                    Surface(
                        color = TeamworkBlue.copy(alpha = 0.10f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = sol.estado.name.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            color = TeamworkBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 2. CONVERSACIÓN: MENSAJES CON SEPARADORES DE FECHA
        // -------------------------------------------------------------
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
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(detalleVm.historial) { index, evento ->
                    // Agrupador de fecha flotante estilo WhatsApp
                    val etiquetaActual = obtenerEtiquetaFecha(evento.fechaCambio)
                    val etiquetaAnterior = if (index > 0) obtenerEtiquetaFecha(detalleVm.historial[index - 1].fechaCambio) else null

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

        // -------------------------------------------------------------
        // 3. BARRA INFERIOR DE ESCRITURA ESTILO INSTAGRAM / WHATSAPP
        // -------------------------------------------------------------
        if (!estaCerrado) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Pestaña rápida de Nota Interna (exclusiva para Técnicos y Admins)
                    if (usuario.rol == RolUsuario.ADMINISTRADOR || usuario.rol == RolUsuario.TECNICO) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = { esNotaInterna = !esNotaInterna },
                                shape = RoundedCornerShape(12.dp),
                                color = if (esNotaInterna) Color(0xFFFEF0C7) else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (esNotaInterna) Color(0xFFF79009) else Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (esNotaInterna) Icons.Filled.Lock else Icons.Filled.Public,
                                        contentDescription = null,
                                        tint = if (esNotaInterna) Color(0xFFB54708) else Color(0xFF64748B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (esNotaInterna) "Modo: Nota Interna 🔒 (Oculto al cliente)" else "Modo: Respuesta Pública 🌐",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (esNotaInterna) Color(0xFFB54708) else Color(0xFF64748B),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Previsualización de imagen adjunta antes de enviar
                    if (evidenciaSeleccionada != null) {
                        val adj = evidenciaSeleccionada!!
                        val bitmap = remember(adj.uriString) {
                            try { FileUtil.cargarBitmapDesdeUri(context, adj.uriString) } catch (e: Exception) { null }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Miniatura",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Icon(Icons.Filled.Image, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(24.dp))
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = adj.nombre,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = FileUtil.formatearTamano(adj.tamanoBytes),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF8E8E93)
                                    )
                                }

                                IconButton(
                                    onClick = { evidenciaSeleccionada = null },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Quitar foto", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    // Respuestas Rápidas (Snippets) desplegables
                    if (mostrarPlantillasRapidas) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val plantillas = listOf(
                                "Revisando requerimiento 🔍",
                                "Favor enviar captura de pantalla 📸",
                                "El servicio ha sido restablecido ✅",
                                "Todo probado y funcionando ok 👍",
                                "¿Podrías confirmar funcionamiento? 💬"
                            )
                            plantillas.forEach { plantilla ->
                                Surface(
                                    onClick = {
                                        textoMensaje = plantilla
                                        mostrarPlantillasRapidas = false
                                    },
                                    shape = CircleShape,
                                    color = Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    Text(
                                        text = plantilla,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Fila Principal de Entrada de Texto
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Botón de Respuestas Rápidas (⚡)
                        IconButton(
                            onClick = { mostrarPlantillasRapidas = !mostrarPlantillasRapidas },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = "Plantillas rápidas",
                                tint = if (mostrarPlantillasRapidas) TeamworkBlue else Color(0xFF64748B)
                            )
                        }

                        // Botón de Cámara Directa (📷)
                        IconButton(
                            onClick = { dispararCamara() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Cámara",
                                tint = Color(0xFF64748B)
                            )
                        }

                        // Botón de Galería / Adjuntos (📎)
                        IconButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AttachFile,
                                contentDescription = "Adjuntar archivo",
                                tint = Color(0xFF64748B)
                            )
                        }

                        // Campo de Texto estilo Cápsula (Instagram Direct / WhatsApp)
                        OutlinedTextField(
                            value = textoMensaje,
                            onValueChange = { textoMensaje = it },
                            placeholder = {
                                Text(
                                    text = if (esNotaInterna) "Nota privada..." else "Mensaje...",
                                    fontSize = 14.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            minLines = 1,
                            maxLines = 4,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (esNotaInterna) Color(0xFFFFFBEB) else Color(0xFFF8FAFC),
                                unfocusedContainerColor = if (esNotaInterna) Color(0xFFFFFBEB) else Color(0xFFF8FAFC),
                                focusedBorderColor = if (esNotaInterna) Color(0xFFF59E0B) else TeamworkBlue,
                                unfocusedBorderColor = if (esNotaInterna) Color(0xFFFDE68A) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Botón de Enviar Circular
                        val puedeEnviar = textoMensaje.isNotBlank() || evidenciaSeleccionada != null
                        Surface(
                            onClick = { enviarMensajeActual() },
                            enabled = puedeEnviar,
                            shape = CircleShape,
                            color = if (puedeEnviar) (if (esNotaInterna) Color(0xFFD97706) else TeamworkBlue) else Color(0xFFCBD5E1),
                            shadowElevation = if (puedeEnviar) 2.dp else 0.dp,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Enviar mensaje",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Banner de conversación finalizada
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF1F5F9)
                    ) {
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

    // Modal para Visualizar Fotos a Pantalla Completa
    if (evidenciaParaVer != null) {
        VisorEvidenciaModal(
            evidencia = evidenciaParaVer!!,
            onDismiss = { evidenciaParaVer = null }
        )
    }
}
