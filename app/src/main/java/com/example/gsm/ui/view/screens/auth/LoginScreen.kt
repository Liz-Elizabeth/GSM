package com.example.gsm.ui.view.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Logo / Ícono estilo App de iOS (Squircle blanco)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "📱",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }

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

        if (authVm.mensajeError != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = authVm.mensajeError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Botón principal con altura cómoda (50.dp)
        Button(
            onClick = { authVm.iniciarSesion(correo, password) },
            enabled = !authVm.cargando,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = if (authVm.cargando) "Verificando..." else "Iniciar Sesión",
                fontWeight = FontWeight.SemiBold
            )
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
}