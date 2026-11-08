package com.example.gsm.ui.view.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.R
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.ui.viewmodel.AuthViewModel

private val SlateBackground = Color(0xFFF8FAFC)
private val BrandBlue = Color(0xFF0066FF)
private val BorderSubtle = Color(0xFFE2E8F0)
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)

@Composable
private fun modernInputColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = BrandBlue,
    unfocusedBorderColor = BorderSubtle,
    cursorColor = BrandBlue,
    focusedLeadingIconColor = BrandBlue,
    unfocusedLeadingIconColor = Color(0xFF94A3B8)
)

/**
 * Pantalla de Acceso Corporativo GSM y Auto-Registro de Clientes (con diseño empresarial y Flow Onboarding).
 */
@Composable
fun LoginScreen(
    authVm: AuthViewModel,
    onRegistroCompletado: () -> Unit = {}
) {
    // 0 = Iniciar Sesión, 1 = Crear Cuenta (Clientes)
    var pestanaActiva by remember { mutableIntStateOf(0) }

    // Estados para Iniciar Sesión
    var correoLogin by remember { mutableStateOf("") }
    var passwordLogin by remember { mutableStateOf("") }
    var verPasswordLogin by remember { mutableStateOf(false) }

    // Estados para Crear Cuenta (6 campos exactos del requerimiento)
    var nombreRegistro by remember { mutableStateOf("") }
    var correoRegistro by remember { mutableStateOf("") }
    var empresaRegistro by remember { mutableStateOf("") }
    var telefonoRegistro by remember { mutableStateOf("") }
    var passwordRegistro by remember { mutableStateOf("") }
    var confirmarPasswordRegistro by remember { mutableStateOf("") }
    var verPasswordRegistro by remember { mutableStateOf(false) }
    var verConfirmarPasswordRegistro by remember { mutableStateOf(false) }

    // Estados para el diálogo de Recuperar Contraseña
    var mostrarDialogoRecuperar by remember { mutableStateOf(false) }
    var correoRecuperar by remember { mutableStateOf("") }
    var nuevaPassword by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. CABECERA Y BRANDING CORPORATIVO ---
        Spacer(modifier = Modifier.height(8.dp))
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo GSM Soporte",
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "GSM Soporte",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = TextDark,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "Ecosistema de Gestión y Soporte Técnico",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. SELECTOR DE PESTAÑAS (LOGIN vs REGISTRO) ---
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFE2E8F0).copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab Iniciar Sesión
                val loginBg by animateColorAsState(
                    targetValue = if (pestanaActiva == 0) Color.White else Color.Transparent,
                    label = "tabLoginBg"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(loginBg)
                        .clickable {
                            pestanaActiva = 0
                            authVm.limpiarErrorRegistro()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        fontWeight = if (pestanaActiva == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (pestanaActiva == 0) TextDark else TextMuted,
                        fontSize = 14.sp
                    )
                }

                // Tab Crear Cuenta
                val registroBg by animateColorAsState(
                    targetValue = if (pestanaActiva == 1) Color.White else Color.Transparent,
                    label = "tabRegistroBg"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(registroBg)
                        .clickable {
                            pestanaActiva = 1
                            authVm.limpiarErrorRegistro()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Crear Cuenta",
                        fontWeight = if (pestanaActiva == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (pestanaActiva == 1) TextDark else TextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 3. CONTENIDO PRINCIPAL SEGÚN PESTAÑA ---
        if (pestanaActiva == 0) {
            // ==========================================
            // SECCIÓN: INICIAR SESIÓN
            // ==========================================
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Bienvenido de nuevo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Ingresa tus credenciales para acceder",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Correo electrónico",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = correoLogin,
                        onValueChange = { correoLogin = it },
                        placeholder = { Text("contacto@miempresa.cl", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = modernInputColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Contraseña",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = passwordLogin,
                        onValueChange = { passwordLogin = it },
                        placeholder = { Text("Ingresa tu contraseña", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { verPasswordLogin = !verPasswordLogin }) {
                                Icon(
                                    imageVector = if (verPasswordLogin) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Ver contraseña",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        },
                        visualTransformation = if (verPasswordLogin) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            authVm.iniciarSesion(correoLogin, passwordLogin)
                        }),
                        shape = RoundedCornerShape(12.dp),
                        colors = modernInputColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(
                            onClick = {
                                correoRecuperar = correoLogin
                                nuevaPassword = ""
                                confirmarPassword = ""
                                authVm.limpiarEstadoRecuperacion()
                                mostrarDialogoRecuperar = true
                            }
                        ) {
                            Text(
                                text = "¿Olvidaste tu contraseña?",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (authVm.mensajeError != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = authVm.mensajeError ?: "",
                                    color = Color(0xFF991B1B),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { authVm.iniciarSesion(correoLogin, passwordLogin) },
                        enabled = !authVm.cargando,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (authVm.cargando) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verificando...")
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Iniciar Sesión", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Acceso Rápido Demo para Evaluación Académica
            Text(
                text = "ACCESO RÁPIDO DEMO (1 CLIC)",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 4.dp, bottom = 8.dp)
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
                        Text(text = "👤 Entrar como Cliente (Camila - Pyme SpA)", fontWeight = FontWeight.Medium, color = TextDark)
                    }
                    HorizontalDivider(color = SlateBackground)
                    TextButton(
                        onClick = { authVm.iniciarSesionDemo(RolUsuario.ADMINISTRADOR) },
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "👔 Entrar como Administrador (Roberto)", fontWeight = FontWeight.Medium, color = TextDark)
                    }
                    HorizontalDivider(color = SlateBackground)
                    TextButton(
                        onClick = { authVm.iniciarSesionDemo(RolUsuario.TECNICO) },
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "🔧 Entrar como Técnico (Diego)", fontWeight = FontWeight.Medium, color = TextDark)
                    }
                }
            }
        } else {
            // ==========================================
            // SECCIÓN: CREAR NUEVA CUENTA (CLIENTE / USUARIO)
            // ==========================================
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrandBlue.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Registro de Cliente",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "Crea tu cuenta corporativa para solicitar soporte",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. Nombre Completo *
                    Text(
                        text = "Nombre Completo *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = nombreRegistro,
                        onValueChange = { nombreRegistro = it },
                        placeholder = { Text("Ej: Matías González", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = modernInputColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Correo Electrónico Corporativo *
                    Text(
                        text = "Correo Electrónico Corporativo *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = correoRegistro,
                        onValueChange = { correoRegistro = it },
                        placeholder = { Text("contacto@miempresa.cl", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = modernInputColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Empresa / Razón Social *
                    Text(
                        text = "Empresa / Razón Social *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = empresaRegistro,
                        onValueChange = { empresaRegistro = it },
                        placeholder = { Text("Ej: Mi Empresa SpA", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Business, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = modernInputColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Teléfono de Contacto *
                    Text(
                        text = "Teléfono de Contacto *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = telefonoRegistro,
                        onValueChange = { telefonoRegistro = it },
                        placeholder = { Text("+56 9 1234 5678", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = modernInputColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. Contraseña *
                    Text(
                        text = "Contraseña *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = passwordRegistro,
                        onValueChange = { passwordRegistro = it },
                        placeholder = { Text("Mínimo 6 caracteres", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
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
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = modernInputColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 6. Confirmar Contraseña *
                    Text(
                        text = "Confirmar Contraseña *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = confirmarPasswordRegistro,
                        onValueChange = { confirmarPasswordRegistro = it },
                        placeholder = { Text("Repite la contraseña", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
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
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(12.dp),
                        colors = modernInputColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Mensaje de Error de Registro
                    if (authVm.errorRegistro != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = authVm.errorRegistro ?: "",
                                    color = Color(0xFF991B1B),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            authVm.registrarUsuario(
                                nombre = nombreRegistro,
                                correo = correoRegistro,
                                empresa = empresaRegistro,
                                telefono = telefonoRegistro,
                                password = passwordRegistro,
                                confirmarPassword = confirmarPasswordRegistro,
                                onExito = {
                                    onRegistroCompletado()
                                }
                            )
                        },
                        enabled = !authVm.registrando,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
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

        Spacer(modifier = Modifier.height(60.dp))
    }

    // ==========================================
    // MODAL: RESTABLECER CONTRASEÑA
    // ==========================================
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
                    Icon(Icons.Filled.LockReset, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(22.dp))
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
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = authVm.mensajeRecuperacion ?: "",
                                color = Color(0xFF166534),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (authVm.errorRecuperacion != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = authVm.errorRecuperacion ?: "",
                                color = Color(0xFF991B1B),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = correoRecuperar,
                        onValueChange = { correoRecuperar = it },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = modernInputColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = nuevaPassword,
                        onValueChange = { nuevaPassword = it },
                        label = { Text("Nueva contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = modernInputColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmarPassword,
                        onValueChange = { confirmarPassword = it },
                        label = { Text("Confirmar contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = modernInputColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authVm.recuperarPassword(correoRecuperar, nuevaPassword, confirmarPassword)
                    },
                    enabled = !authVm.recuperandoPassword,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text(if (authVm.recuperandoPassword) "Guardando..." else "Restablecer")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoRecuperar = false
                        authVm.limpiarEstadoRecuperacion()
                    }
                ) {
                    Text("Cerrar", color = TextMuted)
                }
            }
        )
    }
}
