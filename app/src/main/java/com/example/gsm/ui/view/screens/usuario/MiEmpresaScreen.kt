package com.example.gsm.ui.view.screens.usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.Usuario
import com.example.gsm.data.repository.UsuarioRepository
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.view.screens.usuario.componentes.*
import com.example.gsm.ui.viewmodel.AuthViewModel

/**
 * Vista "Mi Empresa / Equipo" (Multiusuario por Organización).
 * Permite al cliente ver la bolsa compartida de tickets de su empresa y agregar o desactivar colaboradores.
 */
@Composable
fun MiEmpresaScreen(
    usuario: Usuario,
    authVm: AuthViewModel,
    alVolver: () -> Unit
) {
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf(UsuarioRepository.DEFAULT_PASSWORD) }

    LaunchedEffect(usuario.id, usuario.empresa) {
        authVm.limpiarMensajesEquipo()
        authVm.cargarEquipoEmpresa()
    }

    val equipo = authVm.equipoEmpresa

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        EncabezadoPantalla(
            titulo = "Mi Empresa y Equipo",
            alHacerClicDerecho = alVolver
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Tarjeta resumen de la organización y bolsa compartida de tickets
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = TeamworkBlue.copy(alpha = 0.12f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Business, contentDescription = null, tint = TeamworkBlue)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = usuario.empresa?.takeIf { it.isNotBlank() } ?: "Mi Organización",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E)
                        )
                        Text(
                            text = "Plan ${usuario.plan} • ${equipo.size} integrante(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF636366)
                        )
                    }
                    BadgePill(
                        texto = "${usuario.ticketsDisponibles} tickets",
                        colorFondo = SuccessGreenBg,
                        colorTexto = SuccessGreenText
                    )
                }

                Text(
                    text = "Todos los colaboradores activos comparten la bolsa de tickets de la empresa y pueden visualizar las solicitudes de la organización.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF636366)
                )

                Button(
                    onClick = {
                        authVm.limpiarMensajesEquipo()
                        mostrarDialogoAgregar = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Agregar Colaborador", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Mensajes de éxito / error
        authVm.mensajeEquipo?.let { msg ->
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SuccessGreenBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "✓ $msg",
                    color = SuccessGreenText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        authVm.errorEquipo?.let { err ->
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AlertRedBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚠️ $err",
                    color = AlertRedText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        SeccionTitulo("Integrantes de la Organización (${equipo.size})")

        if (equipo.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Group, contentDescription = null, tint = Color(0xFF8E8E93), modifier = Modifier.size(36.dp))
                    Text(
                        text = "Aún no hay colaboradores registrados en ${usuario.empresa}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF636366)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                equipo.forEach { miembro ->
                    val esUsuarioActual = miembro.id == usuario.id
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (miembro.activo) TeamworkBlue.copy(alpha = 0.14f) else Color(0xFFE5E5EA),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = miembro.nombre.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = if (miembro.activo) TeamworkBlue else Color(0xFF8E8E93),
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (esUsuarioActual) "${miembro.nombre} (Tú)" else miembro.nombre,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1C1C1E)
                                        )
                                    }
                                    Text(
                                        text = miembro.correo,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF8E8E93)
                                    )
                                    BadgePill(
                                        texto = if (miembro.activo) "Activo" else "Inactivo",
                                        colorFondo = if (miembro.activo) SuccessGreenBg else AlertRedBg,
                                        colorTexto = if (miembro.activo) SuccessGreenText else AlertRedText
                                    )
                                }
                            }

                            if (!esUsuarioActual) {
                                OutlinedButton(
                                    onClick = { authVm.alternarEstadoColaborador(miembro.id, !miembro.activo) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (miembro.activo) "Desactivar" else "Reactivar",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (miembro.activo) AlertRedText else SuccessGreenText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (mostrarDialogoAgregar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAgregar = false },
            title = { Text("Nuevo Colaborador", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Se unirá a ${usuario.empresa} compartiendo el plan ${usuario.plan}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF636366)
                    )
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre completo *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo corporativo *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña temporal *") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authVm.agregarColaborador(nombre, correo, telefono, password) {
                            nombre = ""
                            correo = ""
                            telefono = ""
                            password = UsuarioRepository.DEFAULT_PASSWORD
                            mostrarDialogoAgregar = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoAgregar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
