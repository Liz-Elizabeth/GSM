package com.example.gsm.ui.view.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.gsm.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.ui.viewmodel.AuthViewModel

private val AppleBackground = Color(0xFFF2F2F7)
private val TeamworkBlue = Color(0xFF0066FF)

@Composable
private fun appleInputColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent
)

@Composable
fun LoginScreen(authVm: AuthViewModel) {
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Estados para el diálogo de Recuperar / Cambiar Contraseña
    var mostrarDialogoRecuperar by remember { mutableStateOf(false) }
    var correoRecuperar by remember { mutableStateOf("") }
    var nuevaPassword by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Logo Oficial de GSM (Branding)
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo GSM",
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(20.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "GSM Soporte",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Gestión y Seguimiento de Solicitudes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 2. Campos de credenciales limpios
        TextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = appleInputColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = appleInputColors(),
            modifier = Modifier.fillMaxWidth()
        )

        // Enlace para recuperar contraseña (KISS)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            TextButton(
                onClick = {
                    correoRecuperar = correo
                    nuevaPassword = ""
                    confirmarPassword = ""
                    authVm.limpiarEstadoRecuperacion()
                    mostrarDialogoRecuperar = true
                }
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    style = MaterialTheme.typography.bodySmall,
                    color = TeamworkBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (authVm.mensajeError != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = authVm.mensajeError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Botón principal con altura cómoda (50.dp)
        Button(
            onClick = { authVm.iniciarSesion(correo, password) },
            enabled = !authVm.cargando,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!authVm.cargando) {
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (authVm.cargando) "Verificando..." else "Iniciar Sesión",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Acceso Rápido Demo como Grupo de Celdas iOS (Inset Grouped)
        Text(
            text = "ACCESO RÁPIDO DEMO (1 CLIC)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 4.dp, bottom = 6.dp)
        )

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                TextButton(
                    onClick = { authVm.iniciarSesionDemo(RolUsuario.CLIENTE) },
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "👤 Entrar como Cliente (Camila)",
                        fontWeight = FontWeight.Medium
                    )
                }

                HorizontalDivider(color = AppleBackground)

                TextButton(
                    onClick = { authVm.iniciarSesionDemo(RolUsuario.ADMINISTRADOR) },
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "👔 Entrar como Administrador (Roberto)",
                        fontWeight = FontWeight.Medium
                    )
                }

                HorizontalDivider(color = AppleBackground)

                TextButton(
                    onClick = { authVm.iniciarSesionDemo(RolUsuario.TECNICO) },
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔧 Entrar como Técnico (Diego)",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // DIÁLOGO MODAL: RECUPERAR / CAMBIAR CONTRASEÑA
    if (mostrarDialogoRecuperar) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoRecuperar = false
                authVm.limpiarEstadoRecuperacion()
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LockReset, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Restablecer Contraseña",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (authVm.mensajeRecuperacion != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✅ ${authVm.mensajeRecuperacion}",
                                color = Color(0xFF2E7D32),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Ingresa el correo de tu cuenta y la nueva contraseña que desees utilizar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = correoRecuperar,
                            onValueChange = { correoRecuperar = it },
                            label = { Text("Correo registrado") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = nuevaPassword,
                            onValueChange = { nuevaPassword = it },
                            label = { Text("Nueva contraseña") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = confirmarPassword,
                            onValueChange = { confirmarPassword = it },
                            label = { Text("Repetir nueva contraseña") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (authVm.errorRecuperacion != null) {
                            Text(
                                text = authVm.errorRecuperacion ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (authVm.mensajeRecuperacion != null) {
                    Button(
                        onClick = {
                            mostrarDialogoRecuperar = false
                            correo = correoRecuperar
                            password = nuevaPassword
                            authVm.limpiarEstadoRecuperacion()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue)
                    ) {
                        Text("Iniciar Sesión Ahora")
                    }
                } else {
                    Button(
                        onClick = {
                            authVm.recuperarPassword(correoRecuperar, nuevaPassword, confirmarPassword)
                        },
                        enabled = !authVm.recuperandoPassword,
                        colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue)
                    ) {
                        Text(if (authVm.recuperandoPassword) "Guardando..." else "Guardar Clave")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoRecuperar = false
                        authVm.limpiarEstadoRecuperacion()
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}
