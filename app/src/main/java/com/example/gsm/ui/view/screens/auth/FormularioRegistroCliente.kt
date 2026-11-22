package com.example.gsm.ui.view.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.ui.view.components.shakeOnError
import com.example.gsm.ui.viewmodel.AuthViewModel

/**
 * Formulario de auto-registro de clientes extraído de LoginScreen para aislar estado de campos.
 * Conserva textos literales exactos requeridos por LoginFormUiTest.
 */
@Composable
fun FormularioRegistroCliente(
    authVm: AuthViewModel,
    brandBlue: Color,
    textDark: Color,
    textMuted: Color,
    inputColors: TextFieldColors,
    onRegistroCompletado: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var nombreRegistro by remember { mutableStateOf("") }
    var correoRegistro by remember { mutableStateOf("") }
    var empresaRegistro by remember { mutableStateOf("") }
    var telefonoRegistro by remember { mutableStateOf("") }
    var passwordRegistro by remember { mutableStateOf("") }
    var confirmarPasswordRegistro by remember { mutableStateOf("") }
    var verPasswordRegistro by remember { mutableStateOf(false) }
    var verConfirmarPasswordRegistro by remember { mutableStateOf(false) }
    var intentoRegistro by remember { mutableStateOf(false) }
    var errorShakeTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(authVm.errorRegistro) {
        if (authVm.errorRegistro != null) errorShakeTrigger++
    }

    val errorNombre = intentoRegistro && nombreRegistro.isBlank()
    val errorCorreo = intentoRegistro && (!correoRegistro.contains("@") || !correoRegistro.contains("."))
    val errorEmpresa = intentoRegistro && empresaRegistro.isBlank()
    val errorTelefono = intentoRegistro && telefonoRegistro.isBlank()
    val errorPass = intentoRegistro && passwordRegistro.trim().length < 6
    val errorConfirmar = intentoRegistro && confirmarPasswordRegistro.trim() != passwordRegistro.trim()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shakeOnError(errorShakeTrigger)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = brandBlue.copy(alpha = 0.1f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = brandBlue, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Registro de Cliente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textDark)
                    Text("Crea tu cuenta corporativa para solicitar soporte", style = MaterialTheme.typography.bodySmall, color = textMuted)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Nombre Completo *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = textDark, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = nombreRegistro,
                onValueChange = { nombreRegistro = it },
                placeholder = { Text("Ej: Matías González", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(20.dp)) },
                isError = errorNombre,
                supportingText = if (errorNombre) { { Text("Ingresa tu nombre completo") } } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
                colors = inputColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Correo Electrónico Corporativo *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = textDark, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = correoRegistro,
                onValueChange = { correoRegistro = it },
                placeholder = { Text("contacto@miempresa.cl", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(20.dp)) },
                isError = errorCorreo,
                supportingText = if (errorCorreo) { { Text("Ingresa un correo corporativo válido") } } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
                colors = inputColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Empresa / Razón Social *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = textDark, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = empresaRegistro,
                onValueChange = { empresaRegistro = it },
                placeholder = { Text("Ej: Mi Empresa SpA", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null, modifier = Modifier.size(20.dp)) },
                isError = errorEmpresa,
                supportingText = if (errorEmpresa) { { Text("Ingresa la razón social de tu empresa") } } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
                colors = inputColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Teléfono de Contacto *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = textDark, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = telefonoRegistro,
                onValueChange = { telefonoRegistro = it },
                placeholder = { Text("+56 9 1234 5678", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(20.dp)) },
                isError = errorTelefono,
                supportingText = if (errorTelefono) { { Text("Ingresa tu teléfono de contacto") } } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
                colors = inputColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Contraseña *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = textDark, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = passwordRegistro,
                onValueChange = { passwordRegistro = it },
                placeholder = { Text("Mínimo 6 caracteres", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    IconButton(onClick = { verPasswordRegistro = !verPasswordRegistro }) {
                        Icon(
                            imageVector = if (verPasswordRegistro) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Ver contraseña",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                },
                visualTransformation = if (verPasswordRegistro) VisualTransformation.None else PasswordVisualTransformation(),
                isError = errorPass,
                supportingText = if (errorPass) { { Text("Usa al menos 6 caracteres") } } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
                colors = inputColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Confirmar Contraseña *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = textDark, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = confirmarPasswordRegistro,
                onValueChange = { confirmarPasswordRegistro = it },
                placeholder = { Text("Repite la contraseña", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    IconButton(onClick = { verConfirmarPasswordRegistro = !verConfirmarPasswordRegistro }) {
                        Icon(
                            imageVector = if (verConfirmarPasswordRegistro) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Ver confirmar contraseña",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                },
                visualTransformation = if (verConfirmarPasswordRegistro) VisualTransformation.None else PasswordVisualTransformation(),
                isError = errorConfirmar,
                supportingText = if (errorConfirmar) { { Text("Las contraseñas no coinciden") } } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = inputColors,
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(
                visible = authVm.errorRegistro != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEE2E2),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(authVm.errorRegistro ?: "", color = Color(0xFF991B1B), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    intentoRegistro = true
                    val invalido = nombreRegistro.isBlank() ||
                        !correoRegistro.contains("@") ||
                        !correoRegistro.contains(".") ||
                        empresaRegistro.isBlank() ||
                        telefonoRegistro.isBlank() ||
                        passwordRegistro.trim().length < 6 ||
                        confirmarPasswordRegistro.trim() != passwordRegistro.trim()
                    if (invalido) {
                        errorShakeTrigger++
                        return@Button
                    }
                    authVm.registrarUsuario(
                        nombre = nombreRegistro,
                        correo = correoRegistro,
                        empresa = empresaRegistro,
                        telefono = telefonoRegistro,
                        password = passwordRegistro,
                        confirmarPassword = confirmarPasswordRegistro,
                        onExito = { onRegistroCompletado() }
                    )
                },
                enabled = !authVm.registrando,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandBlue),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (authVm.registrando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creando cuenta...")
                } else {
                    Text("Crear Cuenta y Seleccionar Plan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
