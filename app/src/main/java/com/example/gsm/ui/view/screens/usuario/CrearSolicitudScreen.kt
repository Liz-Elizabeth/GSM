package com.example.gsm.ui.view.screens.usuario

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.model.CategoriaSolicitud
import com.example.gsm.model.Usuario
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.viewmodel.SolicitudViewModel

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
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        EncabezadoPantalla(
            titulo = "Nueva Solicitud",
            alHacerClicDerecho = alVolver
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título del requerimiento *") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción detallada *") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Categoría:", fontWeight = FontWeight.SemiBold)
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

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = servicio,
            onValueChange = { servicio = it },
            label = { Text("Servicio relacionado (Opcional, ej: Hosting Pyme)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = activo,
            onValueChange = { activo = it },
            label = { Text("Activo / Sitio web (Opcional, ej: www.mipyme.cl)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row {
            Checkbox(
                checked = adjuntarEvidenciaDemo,
                onCheckedChange = { adjuntarEvidenciaDemo = it }
            )
            Text(
                text = "Adjuntar evidencia ficticia (captura_error.png - 350 KB)",
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (solicitudVm.mensaje != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = solicitudVm.mensaje ?: "",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
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
                modifier = Modifier.weight(1f)
            ) {
                Text("Guardar Borrador")
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
                modifier = Modifier.weight(1f)
            ) {
                Text("Enviar Solicitud")
            }
        }
    }
}
