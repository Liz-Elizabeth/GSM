package com.example.gsm.ui.view.screens.usuario

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.gsm.data.model.CategoriaSolicitud
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.DatosEvidenciaVisualizar
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.view.components.VisorEvidenciaModal
import com.example.gsm.ui.viewmodel.SolicitudViewModel
import com.example.gsm.util.ArchivoAdjunto
import com.example.gsm.util.FileUtil
import java.io.File

// Paleta Apple + Teamwork Desk
private val AppleBackground = Color(0xFFF2F2F7)
private val TeamworkBlue = Color(0xFF0066FF)

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
private fun campoTextoColores() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color(0xFFF9F9FB),
    focusedBorderColor = TeamworkBlue,
    unfocusedBorderColor = Color(0xFFE5E5EA),
    focusedLabelColor = TeamworkBlue,
    unfocusedLabelColor = Color(0xFF8E8E93)
)

/**
 * Obtiene un emoji descriptivo según la categoría para mejorar la lectura visual.
 */
private fun iconoParaCategoria(cat: CategoriaSolicitud): String = when (cat) {
    CategoriaSolicitud.INCIDENTE_TECNICO -> "🚨"
    CategoriaSolicitud.DESARROLLO_WEB -> "💻"
    CategoriaSolicitud.MANTENIMIENTO_WEB -> "🛠️"
    CategoriaSolicitud.HOSTING -> "☁️"
    CategoriaSolicitud.SOPORTE_CORREO -> "✉️"
    CategoriaSolicitud.MARKETING_DIGITAL -> "📈"
    CategoriaSolicitud.SOLICITUD_CAMBIO -> "🔄"
    CategoriaSolicitud.CONSULTA -> "❓"
    CategoriaSolicitud.ACCESO_CONFIGURACION -> "🔑"
    CategoriaSolicitud.OTRO -> "📋"
}

/**
 * Pantalla para Crear Nueva Solicitud (Patrón KISS y diseño moderno Apple HIG).
 * Rediseñada para máxima claridad, campos bien definidos y captura de fotos nativa.
 */
@Composable
fun CrearSolicitudScreen(
    cliente: Usuario,
    solicitudVm: SolicitudViewModel,
    alVolver: () -> Unit
) {
    val context = LocalContext.current

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var intentoEnvio by remember { mutableStateOf(false) }
    var categoriaSeleccionada by remember { mutableStateOf(CategoriaSolicitud.INCIDENTE_TECNICO) }
    var servicio by remember { mutableStateOf("") }
    var activo by remember { mutableStateOf("") }
    var archivoSeleccionado by remember { mutableStateOf<ArchivoAdjunto?>(null) }
    var verEvidenciaModal by remember { mutableStateOf(false) }

    // Estado y archivos para la Cámara Nativa
    var archivoFotoCamara by remember { mutableStateOf<File?>(null) }

    // Launcher de Cámara para captura de evidencias reales (sin demos)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito && archivoFotoCamara != null) {
            val file = archivoFotoCamara!!
            archivoSeleccionado = ArchivoAdjunto(
                nombre = file.name,
                tipoMime = "image/jpeg",
                tamanoBytes = file.length(),
                uriString = Uri.fromFile(file).toString()
            )
        }
    }

    fun ejecutarCapturaCamara() {
        try {
            val dir = File(context.filesDir, "evidencias").apply { if (!exists()) mkdirs() }
            val photoFile = File(dir, "EVIDENCIA_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            archivoFotoCamara = photoFile
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Log.e("CrearSolicitudScreen", "Error abriendo cámara: ${e.message}", e)
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

    fun tomarFotoConCamara() {
        val permiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permiso == PackageManager.PERMISSION_GRANTED) {
            ejecutarCapturaCamara()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Launcher para elegir archivos del dispositivo
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            archivoSeleccionado = FileUtil.obtenerDatosDeUri(context, uri)
        }
    }

    val camposValidos = titulo.isNotBlank() && descripcion.isNotBlank()

    // Función única para procesamiento seguro (KISS)
    fun procesarEnvio(esBorrador: Boolean) {
        solicitudVm.crearSolicitud(
            titulo = titulo,
            descripcion = descripcion,
            categoria = categoriaSeleccionada,
            cliente = cliente,
            servicio = servicio.ifBlank { null },
            activo = activo.ifBlank { null },
            nombreEvidencia = archivoSeleccionado?.nombre,
            tipoEvidencia = archivoSeleccionado?.tipoMime,
            tamanoEvidencia = archivoSeleccionado?.tamanoBytes,
            uriEvidencia = archivoSeleccionado?.uriString,
            esBorrador = esBorrador,
            alCrearConExito = alVolver
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Barra Superior
        EncabezadoPantalla(
            titulo = "Nuevo Ticket",
            textoBotonDerecho = "Cancelar",
            alHacerClicDerecho = alVolver
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Tarjeta Hero informativa del Solicitante
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = TeamworkBlue.copy(alpha = 0.12f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Assignment,
                            contentDescription = null,
                            tint = TeamworkBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Crear Solicitud de Soporte",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Text(
                        text = "Solicitante: ${cliente.nombre}${if (!cliente.empresa.isNullOrBlank()) " (${cliente.empresa})" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8E8E93)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. BLOQUE PRINCIPAL: INFORMACIÓN REQUERIDA
        SeccionTitulo("1. Detalle del Requerimiento *")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Campo Título
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

                // Selector de Categoría
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Tipo de Requerimiento:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF636366),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoriaSolicitud.entries.forEach { cat ->
                            val seleccionada = categoriaSeleccionada == cat
                            Surface(
                                onClick = { categoriaSeleccionada = cat },
                                shape = RoundedCornerShape(12.dp),
                                color = if (seleccionada) TeamworkBlue.copy(alpha = 0.10f) else Color(0xFFF9F9FB),
                                border = BorderStroke(
                                    width = if (seleccionada) 1.5.dp else 1.dp,
                                    color = if (seleccionada) TeamworkBlue else Color(0xFFE5E5EA)
                                ),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    Text(text = iconoParaCategoria(cat), fontSize = 14.sp)
                                    Text(
                                        text = cat.descripcion,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Medium,
                                        color = if (seleccionada) TeamworkBlue else Color(0xFF1C1C1E)
                                    )
                                    if (seleccionada) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = TeamworkBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Campo Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción del problema o solicitud *") },
                    placeholder = { Text("Indica detalladamente qué ocurre, mensajes observados o qué necesitas resolver...") },
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
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. BLOQUE SECUNDARIO: CONTEXTO DEL SERVICIO (OPCIONAL)
        SeccionTitulo("2. Contexto Técnico (Opcional)")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = servicio,
                    onValueChange = { servicio = it },
                    label = { Text("Servicio afectado (Opcional)") },
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
                    label = { Text("Activo o sitio web relacionado (Opcional)") },
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

        Spacer(modifier = Modifier.height(16.dp))

        // 5. BLOQUE DE EVIDENCIA ADJUNTA (CÁMARA REAL Y ARCHIVO)
        SeccionTitulo("3. Evidencia o Fotografía")
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
                    text = "Adjunta una captura o fotografía para acelerar la resolución:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF636366)
                )

                if (archivoSeleccionado != null) {
                    val archivo = archivoSeleccionado!!
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = TeamworkBlue.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, TeamworkBlue.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
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
                                    color = Color.White,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = FileUtil.obtenerIconoVectorSegunMime(archivo.tipoMime),
                                            contentDescription = null,
                                            tint = TeamworkBlue,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = archivo.nombre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1C1C1E),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${FileUtil.formatearTamano(archivo.tamanoBytes)} • Adjunto listo",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF8E8E93)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                TextButton(
                                    onClick = { verEvidenciaModal = true },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Filled.Visibility, contentDescription = "Ver", modifier = Modifier.size(18.dp), tint = TeamworkBlue)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ver", fontWeight = FontWeight.Bold, color = TeamworkBlue)
                                }
                                IconButton(
                                    onClick = { archivoSeleccionado = null },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Botón de Cámara Real (FileProvider)
                        FilledTonalButton(
                            onClick = { tomarFotoConCamara() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = TeamworkBlue.copy(alpha = 0.10f),
                                contentColor = TeamworkBlue
                            )
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cámara", fontWeight = FontWeight.Bold)
                        }

                        // Botón de Selector de Archivos del Dispositivo
                        OutlinedButton(
                            onClick = { filePickerLauncher.launch("*/*") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            border = BorderStroke(1.dp, Color(0xFFE5E5EA))
                        ) {
                            Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Archivo", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Feedback / Mensajes del ViewModel
        if (solicitudVm.mensaje != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = TeamworkBlue.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ℹ️ ${solicitudVm.mensaje}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TeamworkBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 6. BARRA DE ACCIÓN INFERIOR (Borrador vs Enviar Solicitud)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { procesarEnvio(esBorrador = true) },
                enabled = titulo.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (titulo.isNotBlank()) TeamworkBlue else Color(0xFFE5E5EA)),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Borrador", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    if (camposValidos) {
                        procesarEnvio(esBorrador = false)
                    } else {
                        intentoEnvio = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (camposValidos) TeamworkBlue else Color(0xFF64748B)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1.3f)
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enviar Ticket", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "🔒 Tu solicitud se sincronizará de inmediato con el equipo de soporte.",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8E8E93),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Modal para Visualizar Evidencias In-App antes de enviar
    if (verEvidenciaModal && archivoSeleccionado != null) {
        val arch = archivoSeleccionado!!
        VisorEvidenciaModal(
            evidencia = DatosEvidenciaVisualizar(
                nombreArchivo = arch.nombre,
                tipoArchivo = arch.tipoMime,
                tamanoBytes = arch.tamanoBytes,
                uriString = arch.uriString,
                estadoRevision = "PENDIENTE"
            ),
            onDismiss = { verEvidenciaModal = false }
        )
    }
}
