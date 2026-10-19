package com.example.gsm.ui.view.screens.usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.data.model.CategoriaSolicitud
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.viewmodel.SolicitudViewModel

private val AppleBackground = Color(0xFFF2F2F7)

@Composable
private fun appleInputColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent
)

@Composable
fun CrearSolicitudScreen(
    cliente: Usuario,
    solicitudVm: SolicitudViewModel,
    alVolver: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf(CategoriaSolicitud.INCIDENTE_TECNICO) }
    var servicio by remember { mutableStateOf("") }
    var activo by remember { mutableStateOf("") }
    var adjuntarEvidenciaDemo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        EncabezadoPantalla(
            titulo = "Nueva Solicitud",
            alHacerClicDerecho = alVolver
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Campos principales estilo tarjeta limpia
        TextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título del requerimiento *") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = appleInputColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción detallada *") },
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = appleInputColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Selector de Categoría con píldoras suaves
        Text(
            text = "CATEGORÍA",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
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
                    label = { Text(cat.descripcion) },
                    shape = CircleShape,
                    border = null
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Campos opcionales
        TextField(
            value = servicio,
            onValueChange = { servicio = it },
            label = { Text("Servicio relacionado (Opcional, ej: Hosting Pyme)") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = appleInputColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = activo,
            onValueChange = { activo = it },
            label = { Text("Activo / Sitio web (Opcional, ej: www.mipyme.cl)") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = appleInputColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Fila de adjunto estilo ajuste de iOS (Tarjeta blanca + Switch)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Adjuntar evidencia demo",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "captura_error.png (350 KB)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = adjuntarEvidenciaDemo,
                    onCheckedChange = { adjuntarEvidenciaDemo = it }
                )
            }
        }

        // Mensaje de feedback (validación o estado)
        if (solicitudVm.mensaje != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = solicitudVm.mensaje ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Botones de acción (Tonal + Primario) con altura cómoda (50.dp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    solicitudVm.crearSolicitud(
                        titulo = titulo,
                        descripcion = descripcion,
                        categoria = categoriaSeleccionada,
                        cliente = cliente,
                        servicio = servicio.ifBlank { null },
                        activo = activo.ifBlank { null },
                        evidencia = if (adjuntarEvidenciaDemo) "captura_error.png" else null,
                        esBorrador = true,
                        alCrearConExito = alVolver
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Borrador", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    solicitudVm.crearSolicitud(
                        titulo = titulo,
                        descripcion = descripcion,
                        categoria = categoriaSeleccionada,
                        cliente = cliente,
                        servicio = servicio.ifBlank { null },
                        activo = activo.ifBlank { null },
                        evidencia = if (adjuntarEvidenciaDemo) "captura_error.png" else null,
                        esBorrador = false,
                        alCrearConExito = alVolver
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Enviar Solicitud", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}