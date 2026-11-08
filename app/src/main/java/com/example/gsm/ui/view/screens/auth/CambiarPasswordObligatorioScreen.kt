package com.example.gsm.ui.view.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.R
import com.example.gsm.data.model.Usuario
import com.example.gsm.data.repository.UsuarioRepository
import com.example.gsm.ui.view.screens.usuario.componentes.AppleBackground
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import com.example.gsm.ui.viewmodel.AuthViewModel

@Composable
fun CambiarPasswordObligatorioScreen(
    usuario: Usuario,
    authVm: AuthViewModel
) {
    var nuevaPassword by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Logo Oficial de GSM con Insignia de Seguridad
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 4.dp,
                color = Color.Transparent,
                modifier = Modifier.size(76.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo GSM",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                )
            }
            Surface(
                shape = CircleShape,
                color = TeamworkBlue,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(28.dp)
                    .offset(x = 4.dp, y = 4.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.LockReset,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Actualización de Seguridad Requerida",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Hola, ${usuario.nombre}. Tu cuenta fue configurada con una contraseña temporal por defecto (${UsuarioRepository.DEFAULT_PASSWORD}).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp),
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Tarjeta con Formulario de Clave
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Define tu nueva contraseña personal",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = nuevaPassword,
                    onValueChange = { nuevaPassword = it },
                    label = { Text("Nueva contraseña") },
                    placeholder = { Text("Mínimo 4 caracteres") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmacion,
                    onValueChange = { confirmacion = it },
                    label = { Text("Confirmar nueva contraseña") },
                    placeholder = { Text("Reingresa la misma clave") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                imageVector = if (confirmVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (confirmVisible) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Mensajes de error / validación
                if (authVm.errorPasswordObligatoria != null) {
                    Text(
                        text = authVm.errorPasswordObligatoria ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Botón principal
                Button(
                    onClick = {
                        authVm.cambiarPasswordObligatorio(
                            nuevaPassword = nuevaPassword,
                            confirmacion = confirmacion
                        )
                    },
                    enabled = !authVm.cambiandoPasswordObligatorio && nuevaPassword.isNotBlank() && confirmacion.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (authVm.cambiandoPasswordObligatorio) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Guardar y Continuar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Botón secundario para salir de la sesión si no desea cambiarla en este momento
        TextButton(
            onClick = { authVm.cerrarSesion() },
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Cerrar Sesión", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
