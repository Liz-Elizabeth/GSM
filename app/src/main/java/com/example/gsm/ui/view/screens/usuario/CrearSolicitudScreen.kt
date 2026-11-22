package com.example.gsm.ui.view.screens.usuario

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.gsm.data.model.CategoriaSolicitud
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.DatosEvidenciaVisualizar
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.view.components.PantallaExitoCompleta
import com.example.gsm.ui.view.components.VisorEvidenciaModal
import com.example.gsm.ui.view.components.shakeOnError
import com.example.gsm.ui.view.screens.usuario.componentes.AppleBackground
import com.example.gsm.ui.view.screens.usuario.componentes.SeccionTitulo
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import com.example.gsm.ui.viewmodel.SolicitudViewModel
import com.example.gsm.util.ArchivoAdjunto
import com.example.gsm.util.FileUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

private const val MAX_EVIDENCIAS_TICKET = 5

@Composable
private fun campoTextoColores() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color(0xFFF9F9FB),
    focusedBorderColor = TeamworkBlue,
    unfocusedBorderColor = Color(0xFFE5E5EA),
    focusedLabelColor = TeamworkBlue,
    unfocusedLabelColor = Color(0xFF8E8E93)
)

/**
 * Pantalla para Crear Nueva Solicitud.
 * Permite adjuntar múltiples evidencias (cámara y selector múltiple) y mantiene
 * los botones de acción fijos en la parte inferior para enviar sin hacer scroll extra.
 */
@Composable
fun CrearSolicitudScreen(
    cliente: Usuario,
    solicitudVm: SolicitudViewModel,
    alVolver: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var intentoEnvio by remember { mutableStateOf(false) }
    var errorShakeTrigger by remember { mutableIntStateOf(0) }
    var mostrarExitoCreacion by remember { mutableStateOf(false) }

    LaunchedEffect(mostrarExitoCreacion) {
        if (mostrarExitoCreacion) {
            delay(1150)
            alVolver()
        }
    }
    var categoriaSeleccionada by remember { mutableStateOf(CategoriaSolicitud.INCIDENTE_TECNICO) }
    var servicio by remember { mutableStateOf("") }
    var activo by remember { mutableStateOf("") }
    var contextoExpandido by remember { mutableStateOf(false) }

    // Lista de múltiples evidencias; se evita duplicar por uriString
    var archivosSeleccionados by remember { mutableStateOf<List<ArchivoAdjunto>>(emptyList()) }
    var evidenciaAVisualizar by remember { mutableStateOf<ArchivoAdjunto?>(null) }

    // Se persiste la ruta con rememberSaveable por si Android destruye la Activity al volver de la cámara
    var rutaFotoCamara by rememberSaveable { mutableStateOf<String?>(null) }

    fun agregarAdjuntos(nuevos: List<ArchivoAdjunto>) {
        if (nuevos.isEmpty()) return
        val combinados = (archivosSeleccionados + nuevos)
            .distinctBy { it.uriString }
            .take(MAX_EVIDENCIAS_TICKET)
        if (archivosSeleccionados.size + nuevos.size > MAX_EVIDENCIAS_TICKET) {
            Toast.makeText(context, "Máximo $MAX_EVIDENCIAS_TICKET evidencias por ticket", Toast.LENGTH_SHORT).show()
        }
        archivosSeleccionados = combinados
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        val ruta = rutaFotoCamara
        if (exito && ruta != null) {
            val file = File(ruta)
            if (file.exists() && file.length() > 0L) {
                agregarAdjuntos(
                    listOf(
                        ArchivoAdjunto(
                            nombre = file.name,
                            tipoMime = "image/jpeg",
                            tamanoBytes = file.length(),
                            uriString = Uri.fromFile(file).toString()
                        )
                    )
                )
            }
        }
        rutaFotoCamara = null
    }

    fun ejecutarCapturaCamara() {
        try {
            val (photoFile, uri) = FileUtil.crearArchivoFotoTemporal(context, "EVIDENCIA")
            rutaFotoCamara = photoFile.absolutePath
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Log.e("CrearSolicitudScreen", "Error abriendo cámara: ${e.message}", e)
            Toast.makeText(context, "No se pudo abrir la cámara: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) ejecutarCapturaCamara()
        else Toast.makeText(context, "Se requiere permiso de cámara para capturar fotos", Toast.LENGTH_SHORT).show()
    }

    fun tomarFotoConCamara() {
        val permiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permiso == PackageManager.PERMISSION_GRANTED) ejecutarCapturaCamara()
        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // GetMultipleContents permite elegir varios archivos/fotos en una sola apertura del selector
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val adjuntos = uris.map { FileUtil.obtenerDatosDeUri(context, it) }
            agregarAdjuntos(adjuntos)
        }
    }

    val camposValidos = titulo.isNotBlank() && descripcion.isNotBlank()

    fun procesarEnvio() {
        val primera = archivosSeleccionados.firstOrNull()
        solicitudVm.crearSolicitud(
            titulo = titulo,
            descripcion = descripcion,
            categoria = categoriaSeleccionada,
            cliente = cliente,
            servicio = servicio.ifBlank { null },
            activo = activo.ifBlank { null },
            nombreEvidencia = primera?.nombre,
            tipoEvidencia = primera?.tipoMime,
            tamanoEvidencia = primera?.tamanoBytes,
            uriEvidencia = primera?.uriString,
            evidencias = archivosSeleccionados,
            esBorrador = false,
            alCrearConExito = { mostrarExitoCreacion = true }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppleBackground)
        ) {
            // Contenido desplazable; el footer de acciones queda fijo abajo
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
            EncabezadoPantalla(
                titulo = "Nuevo Ticket",
                textoBotonDerecho = "Cancelar",
                alHacerClicDerecho = alVolver
            )

            // Subtítulo compacto con organización y cuota disponible (evita tarjeta introductoria duplicada)
            val resumenSolicitante = buildString {
                append(cliente.nombre)
                if (!cliente.empresa.isNullOrBlank()) append(" · ${cliente.empresa}")
                if (cliente.rol == RolUsuario.CLIENTE) append(" · ${cliente.ticketsDisponibles} disponibles")
            }
            Text(
                text = resumenSolicitante,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF636366),
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 14.dp)
            )

            // 1. DETALLE PRINCIPAL
            SeccionTitulo("Detalle del requerimiento")
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .shakeOnError(errorShakeTrigger)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Asunto o título corto *") },
                        placeholder = { Text("Ej: Error 500 al ingresar al sistema") },
                        leadingIcon = {
                            Icon(Icons.Filled.Title, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(20.dp))
                        },
                        isError = intentoEnvio && titulo.isBlank(),
                        supportingText = {
                            if (intentoEnvio && titulo.isBlank()) {
                                Text("El asunto o título es obligatorio", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = campoTextoColores(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Categoría",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF636366),
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoriaSolicitud.entries.forEach { cat ->
                            FilterChip(
                                selected = categoriaSeleccionada == cat,
                                onClick = { categoriaSeleccionada = cat },
                                label = { Text(cat.descripcion) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción del problema *") },
                        placeholder = { Text("Indica qué ocurre, mensajes observados o qué necesitas resolver...") },
                        isError = intentoEnvio && descripcion.isBlank(),
                        supportingText = {
                            if (intentoEnvio && descripcion.isBlank()) {
                                Text("Describe detalladamente el problema o requerimiento", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(12.dp),
                        colors = campoTextoColores(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Contexto técnico opcional colapsado por defecto para mantener limpia la vista inicial
                    TextButton(
                        onClick = { contextoExpandido = !contextoExpandido },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = if (contextoExpandido) Icons.Filled.ExpandLess else Icons.Filled.Add,
                            contentDescription = null,
                            tint = TeamworkBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (contextoExpandido) "Ocultar servicio / activo" else "Agregar servicio o activo (opcional)",
                            color = TeamworkBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    AnimatedVisibility(visible = contextoExpandido) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = servicio,
                                onValueChange = { servicio = it },
                                label = { Text("Servicio afectado") },
                                placeholder = { Text("Ej: Hosting Pyme, Correo Corporativo") },
                                leadingIcon = {
                                    Icon(Icons.Filled.Build, contentDescription = null, tint = Color(0xFF8E8E93), modifier = Modifier.size(20.dp))
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = campoTextoColores(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = activo,
                                onValueChange = { activo = it },
                                label = { Text("Activo o sitio web") },
                                placeholder = { Text("Ej: www.miempresa.cl o Servidor VPS") },
                                leadingIcon = {
                                    Icon(Icons.Filled.Language, contentDescription = null, tint = Color(0xFF8E8E93), modifier = Modifier.size(20.dp))
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = campoTextoColores(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. EVIDENCIAS MÚLTIPLES (CÁMARA Y SELECTOR MÚLTIPLE SIEMPRE DISPONIBLES HASTA EL LÍMITE)
            SeccionTitulo("Evidencias (${archivosSeleccionados.size}/$MAX_EVIDENCIAS_TICKET)")
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (archivosSeleccionados.size < MAX_EVIDENCIAS_TICKET) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { tomarFotoConCamara() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = TeamworkBlue.copy(alpha = 0.10f),
                                    contentColor = TeamworkBlue
                                )
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cámara", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { filePickerLauncher.launch("*/*") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                border = BorderStroke(1.dp, Color(0xFFE5E5EA))
                            ) {
                                Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Archivos", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    if (archivosSeleccionados.isEmpty()) {
                        Text(
                            text = "Opcional: adjunta fotos o documentos para acelerar la resolución.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E8E93)
                        )
                    } else {
                        archivosSeleccionados.forEach { archivo ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = TeamworkBlue.copy(alpha = 0.06f),
                                border = BorderStroke(1.dp, TeamworkBlue.copy(alpha = 0.22f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(34.dp)) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = FileUtil.obtenerIconoVectorSegunMime(archivo.tipoMime),
                                                    contentDescription = null,
                                                    tint = TeamworkBlue,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = archivo.nombre,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1C1C1E),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = FileUtil.formatearTamano(archivo.tamanoBytes),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF8E8E93)
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { evidenciaAVisualizar = archivo },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Filled.Visibility, contentDescription = "Ver", tint = TeamworkBlue, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                archivosSeleccionados = archivosSeleccionados.filterNot { it.uriString == archivo.uriString }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Filled.Close, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (solicitudVm.mensaje != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TeamworkBlue.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = solicitudVm.mensaje ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = TeamworkBlue,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // 3. BARRA FIJA INFERIOR (Enviar Ticket)
        val enviando = solicitudVm.cargando
        Surface(
            color = Color.White,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = {
                        if (camposValidos) {
                            procesarEnvio()
                        } else {
                            intentoEnvio = true
                            errorShakeTrigger++
                            coroutineScope.launch { scrollState.animateScrollTo(0) }
                        }
                    },
                    enabled = !enviando && !mostrarExitoCreacion,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (camposValidos) TeamworkBlue else Color(0xFF64748B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (enviando) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviando...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviar Ticket", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        }

        if (mostrarExitoCreacion) {
            val codigo = solicitudVm.mensaje?.substringAfter(": ")?.trim()
            PantallaExitoCompleta(
                titulo = "¡Ticket enviado!",
                subtitulo = codigo?.let { "Código #$it" } ?: "Registrado en mesa de ayuda"
            )
        }
    }

    if (evidenciaAVisualizar != null) {
        val arch = evidenciaAVisualizar!!
        VisorEvidenciaModal(
            evidencia = DatosEvidenciaVisualizar(
                nombreArchivo = arch.nombre,
                tipoArchivo = arch.tipoMime,
                tamanoBytes = arch.tamanoBytes,
                uriString = arch.uriString
            ),
            onDismiss = { evidenciaAVisualizar = null }
        )
    }
}
