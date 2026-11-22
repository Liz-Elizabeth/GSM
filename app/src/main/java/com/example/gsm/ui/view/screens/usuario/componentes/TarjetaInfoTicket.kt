package com.example.gsm.ui.view.screens.usuario.componentes

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.CategoriaSolicitud
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.Solicitud
import com.example.gsm.ui.view.components.shakeOnError
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tarjeta de información, metadatos, edición inline y compartir de una solicitud.
 * Aísla el estado de edición para evitar recomponer DetalleSolicitudScreen al escribir.
 */
@Composable
fun TarjetaInfoTicket(
    sol: Solicitud,
    onActualizarInformacion: (
        titulo: String,
        descripcion: String,
        categoria: CategoriaSolicitud,
        servicio: String?,
        activo: String?
    ) -> Unit
) {
    val context = LocalContext.current
    var modoEdicion by remember { mutableStateOf(false) }
    var tituloEditado by remember(sol.titulo) { mutableStateOf(sol.titulo) }
    var descripcionEditada by remember(sol.descripcion) { mutableStateOf(sol.descripcion) }
    var categoriaEditada by remember(sol.categoria) { mutableStateOf(sol.categoria) }
    var servicioEditado by remember(sol.servicioRelacionado) { mutableStateOf(sol.servicioRelacionado ?: "") }
    var activoEditado by remember(sol.activoRelacionado) { mutableStateOf(sol.activoRelacionado ?: "") }
    var intentoGuardar by remember { mutableStateOf(false) }
    var errorShakeTrigger by remember { mutableIntStateOf(0) }

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
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Compartir resumen del ticket por WhatsApp, correo o cualquier app instalada
            FilledTonalIconButton(
                onClick = {
                    val resumen = buildString {
                        appendLine("Ticket #${sol.codigoSeguimiento}")
                        appendLine("Título: ${sol.titulo}")
                        appendLine("Estado: ${formatearEstado(sol.estado)} | Prioridad: ${formatearPrioridad(sol.prioridad)}")
                        if (!sol.resolucion.isNullOrBlank()) {
                            appendLine("Resolución: ${sol.resolucion}")
                        } else {
                            appendLine("Detalle: ${sol.descripcion}")
                        }
                    }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Resumen Ticket #${sol.codigoSeguimiento}")
                        putExtra(Intent.EXTRA_TEXT, resumen)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartir ticket"))
                },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(Icons.Filled.Share, contentDescription = "Compartir resumen", modifier = Modifier.size(16.dp))
            }
            if (!modoEdicion && sol.estado != EstadoSolicitud.CERRADA && sol.estado != EstadoSolicitud.CANCELADA) {
                FilledTonalButton(
                    onClick = {
                        tituloEditado = sol.titulo
                        descripcionEditada = sol.descripcion
                        categoriaEditada = sol.categoria
                        servicioEditado = sol.servicioRelacionado ?: ""
                        activoEditado = sol.activoRelacionado ?: ""
                        intentoGuardar = false
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shakeOnError(errorShakeTrigger),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = tituloEditado,
                        onValueChange = { tituloEditado = it },
                        label = { Text("Título del Ticket *") },
                        isError = intentoGuardar && tituloEditado.isBlank(),
                        supportingText = if (intentoGuardar && tituloEditado.isBlank()) {
                            { Text("El título es obligatorio") }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
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
                                selected = categoriaEditada == cat,
                                onClick = { categoriaEditada = cat },
                                label = { Text(cat.descripcion) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = descripcionEditada,
                        onValueChange = { descripcionEditada = it },
                        label = { Text("Descripción Detallada *") },
                        isError = intentoGuardar && descripcionEditada.isBlank(),
                        supportingText = if (intentoGuardar && descripcionEditada.isBlank()) {
                            { Text("Describe el requerimiento antes de guardar") }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = servicioEditado,
                        onValueChange = { servicioEditado = it },
                        label = { Text("Servicio afectado (opcional)") },
                        placeholder = { Text("Ej: Hosting Pyme, Correo Corporativo") },
                        leadingIcon = {
                            Icon(Icons.Filled.Build, contentDescription = null, tint = Color(0xFF8E8E93), modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = activoEditado,
                        onValueChange = { activoEditado = it },
                        label = { Text("Activo o sitio web (opcional)") },
                        placeholder = { Text("Ej: www.miempresa.cl o Servidor VPS") },
                        leadingIcon = {
                            Icon(Icons.Filled.Language, contentDescription = null, tint = Color(0xFF8E8E93), modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
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
                                    onActualizarInformacion(
                                        tituloEditado,
                                        descripcionEditada,
                                        categoriaEditada,
                                        servicioEditado.ifBlank { null },
                                        activoEditado.ifBlank { null }
                                    )
                                    modoEdicion = false
                                } else {
                                    intentoGuardar = true
                                    errorShakeTrigger++
                                }
                            },
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
}
