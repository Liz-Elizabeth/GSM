package com.example.gsm.ui.view.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gsm.model.RolUsuario
import com.example.gsm.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(authVm: AuthViewModel) {
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📱 GSM Soporte",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Gestión y Seguimiento de Solicitudes",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (authVm.mensajeError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = authVm.mensajeError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { authVm.iniciarSesion(correo, password) },
            enabled = !authVm.cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (authVm.cargando) "Verificando..." else "Iniciar Sesión")
        }

        Spacer(modifier = Modifier.height(28.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Acceso Rápido Demo (1 Clic)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { authVm.iniciarSesionDemo(RolUsuario.CLIENTE) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("👤 Entrar como Cliente (Camila)")
        }

        OutlinedButton(
            onClick = { authVm.iniciarSesionDemo(RolUsuario.ADMINISTRADOR) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("👔 Entrar como Administrador (Roberto)")
        }

        OutlinedButton(
            onClick = { authVm.iniciarSesionDemo(RolUsuario.TECNICO) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔧 Entrar como Técnico (Diego)")
        }
    }
}
