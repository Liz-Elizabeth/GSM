package com.example.gsm.ui.view.screens.usuario

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import com.example.gsm.data.model.CategoriaSolicitud
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.viewmodel.SolicitudViewModel
import com.example.gsm.util.ArchivoAdjunto
import com.example.gsm.util.FileUtil
import com.example.gsm.ui.view.components.VisorEvidenciaModal
import com.example.gsm.ui.view.components.DatosEvidenciaVisualizar

// Paleta Apple + Teamwork
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
private fun appleInputColors() = TextFieldDefaults.colors(
    focusedContainerColor = AppleBackground,
    unfocusedContainerColor = AppleBackground,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent
)

@Composable
fun CrearSolicitudScreen(
    cliente: Usuario,
    solicitudVm: SolicitudViewModel,
    alVolver: () -> Unit
) {
    val context = LocalContext.current

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf(CategoriaSolicitud.INCIDENTE_TECNICO) }
    var servicio by remember { mutableStateOf("") }
    var activo by remember { mutableStateOf("") }
    var archivoSeleccionado by remember { mutableStateOf<ArchivoAdjunto?>(null) }
    var verEvidenciaModal by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            archivoSeleccionado = FileUtil.obtenerDatosDeUri(context, uri)
        }
    }

    val camposValidos = titulo.isNotBlank() && descripcion.isNotBlank()

    // Función única para no duplicar la llamada en ambos botones (KISS)
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
        // Barra Superior
        EncabezadoPantalla(
            titulo = "Nueva Solicitud",
            textoBotonDerecho = "Cancelar",
            alHacerClicDerecho = alVolver
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 1. BLOQUE PRINCIPAL: INFORMACIÓN OBLIGATORIA
        SeccionTitulo("1. Información Principal (Obligatorio)")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    placeholder = { Text("Asunto o título corto del problema *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = appleInputColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de Categoría integrado dentro de la ficha
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Categoría del ticket:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CategoriaSolicitud.entries.forEach { cat ->
                            FilterChip(
                                selected = categoriaSeleccionada == cat,
                                onClick = { categoriaSeleccionada = cat },
                                label = { Text(cat.descripcion) },
                                shape = CircleShape,
                                border = null
                            )
                        }
                    }
                }

                TextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    placeholder = { Text("Describe qué ocurre, pasos o mensaje de error *") },
                    minLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = appleInputColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. BLOQUE SECUNDARIO: CONTEXTO Y ARCHIVOS (Opcional)
        SeccionTitulo("2. Contexto Técnico y Evidencias Reales")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = servicio,
                    onValueChange = { servicio = it },
                    placeholder = { Text("🛠️ Servicio afectado (ej: Hosting Pyme)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = appleInputColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = activo,
                    onValueChange = { activo = it },
                    placeholder = { Text("🌐 Activo o sitio web (ej: www.mipyme.cl)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = appleInputColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = AppleBackground)

                // SELECTOR DE ARCHIVOS REALES DEL DISPOSITIVO
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "📎 Archivo o Evidencia Adjunta:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (archivoSeleccionado != null) {
                        val archivo = archivoSeleccionado!!
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TeamworkBlue.copy(alpha = 0.08f),
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
                                    Icon(
                                        imageVector = FileUtil.obtenerIconoVectorSegunMime(archivo.tipoMime),
                                        contentDescription = null,
                                        tint = TeamworkBlue,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Column {
                                        Text(
                                            text = archivo.nombre,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${FileUtil.formatearTamano(archivo.tamanoBytes)} • ${archivo.tipoMime}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = { verEvidenciaModal = true },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Filled.Visibility, contentDescription = "Ver", modifier = Modifier.size(18.dp), tint = TeamworkBlue)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ver", fontWeight = FontWeight.Bold, color = TeamworkBlue)
                                    }
                                    IconButton(onClick = { archivoSeleccionado = null }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { filePickerLauncher.launch("*/*") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cargar del Dispositivo")
                            }

                            FilledTonalButton(
                                onClick = {
                                    archivoSeleccionado = ArchivoAdjunto(
                                        nombre = "captura_error_500.png",
                                        tipoMime = "image/png",
                                        tamanoBytes = 358_000L,
                                        uriString = "file:///demo/captura_error_500.png"
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Smartphone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Demo")
                            }
                        }
                    }
                }
            }
        }

        // Banner de mensaje o error del ViewModel
        if (solicitudVm.mensaje != null) {
            Spacer(modifier = Modifier.height(12.dp))
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
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. BARRA DE ACCIÓN INFERIOR (Borrador vs Enviar Ticket)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalButton(
                onClick = { procesarEnvio(esBorrador = true) },
                enabled = titulo.isNotBlank(),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Borrador", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { procesarEnvio(esBorrador = false) },
                enabled = camposValidos,
                colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Enviar Ticket", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

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
