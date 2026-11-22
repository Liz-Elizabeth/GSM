package com.example.gsm.ui.view.screens.usuario

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.screens.usuario.componentes.AppleBackground
import com.example.gsm.ui.view.screens.usuario.componentes.PurpleBadgeBg
import com.example.gsm.ui.view.screens.usuario.componentes.PurpleBadgeText
import com.example.gsm.ui.view.screens.usuario.componentes.SeccionTitulo
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import com.example.gsm.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

/**
 * Pestaña de Perfil estilo iOS Settings.
 * Agrupa datos de contacto, accesos a cuenta/archivo/empresa y edición en ModalBottomSheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    usuario: Usuario,
    authVm: AuthViewModel,
    archivadosCount: Int = 0,
    alIrAGestionUsuarios: () -> Unit = {},
    alIrADashboard: () -> Unit = {},
    alIrAPlanes: () -> Unit = {},
    alIrAMiEmpresa: () -> Unit = {},
    alIrAArchivo: () -> Unit = {},
    alCerrarSesion: () -> Unit
) {
    var mostrarSheetEditar by remember { mutableStateOf(false) }
    var nombreEdit by remember(usuario) { mutableStateOf(usuario.nombre) }
    var telefonoEdit by remember(usuario) { mutableStateOf(usuario.telefono ?: "") }
    var empresaEdit by remember(usuario) { mutableStateOf(usuario.empresa ?: "") }
    var cambiarPassExpandido by remember { mutableStateOf(false) }
    var nuevaPasswordEdit by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val iniciales = remember(usuario.nombre) {
        usuario.nombre.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "U" }
    }
    val etiquetaRol = when (usuario.rol) {
        RolUsuario.CLIENTE -> "Cliente"
        RolUsuario.TECNICO -> "Técnico"
        RolUsuario.ADMINISTRADOR -> "Administrador"
    }
    // Evita que un colaborador edite su empresa desde perfil y pierda acceso a tickets compartidos
    val empresaBloqueada = usuario.rol == RolUsuario.CLIENTE && !usuario.empresa.isNullOrBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. CABECERA LIMPIA CON INICIALES Y ROL
            Surface(
                shape = CircleShape,
                color = TeamworkBlue.copy(alpha = 0.14f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = iniciales,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TeamworkBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = usuario.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
            Text(
                text = usuario.correo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(color = TeamworkBlue.copy(alpha = 0.10f), shape = CircleShape) {
                Text(
                    text = etiquetaRol,
                    style = MaterialTheme.typography.labelMedium,
                    color = TeamworkBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. DATOS DE CONTACTO + BOTÓN EDITAR
            Box(modifier = Modifier.fillMaxWidth()) { SeccionTitulo("Información de contacto") }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilaDatoPerfil("Empresa", usuario.empresa?.takeIf { it.isNotBlank() } ?: "Sin especificar")
                    HorizontalDivider(color = AppleBackground)
                    FilaDatoPerfil("Teléfono", usuario.telefono?.takeIf { it.isNotBlank() } ?: "No registrado")
                    HorizontalDivider(color = AppleBackground)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                nombreEdit = usuario.nombre
                                telefonoEdit = usuario.telefono ?: ""
                                empresaEdit = usuario.empresa ?: ""
                                nuevaPasswordEdit = ""
                                cambiarPassExpandido = false
                                authVm.limpiarMensajesPerfil()
                                mostrarSheetEditar = true
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Editar datos y contraseña",
                            style = MaterialTheme.typography.titleSmall,
                            color = TeamworkBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. SECCIÓN CUENTA Y TICKETS (Agrupada)
            Box(modifier = Modifier.fillMaxWidth()) { SeccionTitulo("Cuenta y tickets") }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    FilaMenuPerfil(
                        icono = Icons.Filled.Payment,
                        colorIcono = Color(0xFF0F172A),
                        fondoIcono = Color(0xFFF1F5F9),
                        titulo = "Plan ${usuario.plan}",
                        subtitulo = if (usuario.rol == RolUsuario.CLIENTE) {
                            "${usuario.ticketsDisponibles} tickets disponibles · Ver planes"
                        } else {
                            "Catálogo de planes y niveles SLA"
                        },
                        onClick = alIrAPlanes
                    )
                    if (usuario.rol == RolUsuario.CLIENTE && !usuario.empresa.isNullOrBlank()) {
                        HorizontalDivider(color = AppleBackground, modifier = Modifier.padding(horizontal = 16.dp))
                        FilaMenuPerfil(
                            icono = Icons.Filled.Group,
                            colorIcono = TeamworkBlue,
                            fondoIcono = TeamworkBlue.copy(alpha = 0.12f),
                            titulo = "Mi Empresa y Equipo",
                            subtitulo = "Colaboradores de ${usuario.empresa}",
                            onClick = alIrAMiEmpresa
                        )
                    }
                    HorizontalDivider(color = AppleBackground, modifier = Modifier.padding(horizontal = 16.dp))
                    FilaMenuPerfil(
                        icono = Icons.Filled.Inventory2,
                        colorIcono = Color(0xFF475569),
                        fondoIcono = Color(0xFF64748B).copy(alpha = 0.12f),
                        titulo = "Archivo de Tickets",
                        subtitulo = "Solicitudes cerradas y canceladas",
                        badgeTexto = if (archivadosCount > 0) "$archivadosCount" else null,
                        onClick = alIrAArchivo
                    )
                }
            }

            // 4. SECCIÓN ADMINISTRACIÓN (Solo Administrador)
            if (usuario.rol == RolUsuario.ADMINISTRADOR) {
                Spacer(modifier = Modifier.height(18.dp))
                Box(modifier = Modifier.fillMaxWidth()) { SeccionTitulo("Administración") }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        FilaMenuPerfil(
                            icono = Icons.Filled.BarChart,
                            colorIcono = PurpleBadgeText,
                            fondoIcono = PurpleBadgeBg,
                            titulo = "Panel de Métricas y SLAs",
                            subtitulo = "CSAT, tiempos de respuesta y técnicos",
                            onClick = alIrADashboard
                        )
                        HorizontalDivider(color = AppleBackground, modifier = Modifier.padding(horizontal = 16.dp))
                        FilaMenuPerfil(
                            icono = Icons.Filled.ManageAccounts,
                            colorIcono = TeamworkBlue,
                            fondoIcono = TeamworkBlue.copy(alpha = 0.12f),
                            titulo = "Administrar Usuarios y Roles",
                            subtitulo = "Cuentas, reseteo de claves y altas",
                            onClick = alIrAGestionUsuarios
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 5. CERRAR SESIÓN
            Button(
                onClick = alCerrarSesion,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
        )
    }

    // SHEET MODERNO PARA ACTUALIZAR DATOS Y CONTRASEÑA
    if (mostrarSheetEditar) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                mostrarSheetEditar = false
                authVm.limpiarMensajesPerfil()
            },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Actualizar perfil",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tu correo (${usuario.correo}) y rol están protegidos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = nombreEdit,
                    onValueChange = { nombreEdit = it },
                    label = { Text("Nombre completo *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = telefonoEdit,
                    onValueChange = { telefonoEdit = it },
                    label = { Text("Teléfono de contacto") },
                    placeholder = { Text("+56 9 1234 5678") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = empresaEdit,
                    onValueChange = { if (!empresaBloqueada) empresaEdit = it },
                    enabled = !empresaBloqueada,
                    label = { Text("Empresa u Organización") },
                    supportingText = if (empresaBloqueada) {
                        { Text("Vinculado a tu equipo; un cambio debe hacerlo administración.") }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Sección expandible opcional para cambiar contraseña desde Perfil
                TextButton(
                    onClick = { cambiarPassExpandido = !cambiarPassExpandido },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (cambiarPassExpandido) Icons.Filled.ExpandLess else Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TeamworkBlue
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (cambiarPassExpandido) "Ocultar cambio de contraseña" else "Cambiar contraseña",
                        color = TeamworkBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                AnimatedVisibility(visible = cambiarPassExpandido) {
                    OutlinedTextField(
                        value = nuevaPasswordEdit,
                        onValueChange = { nuevaPasswordEdit = it },
                        label = { Text("Nueva contraseña (mín. 6 caracteres)") },
                        singleLine = true,
                        visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                                Icon(
                                    imageVector = if (mostrarPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Mostrar contraseña"
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (authVm.errorPerfil != null) {
                    Text(
                        text = authVm.errorPerfil ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            mostrarSheetEditar = false
                            authVm.limpiarMensajesPerfil()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            authVm.actualizarPerfil(
                                nombre = nombreEdit,
                                telefono = telefonoEdit,
                                empresa = empresaEdit,
                                nuevaPassword = if (cambiarPassExpandido) nuevaPasswordEdit else null,
                                onExito = {
                                    mostrarSheetEditar = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Datos actualizados correctamente")
                                    }
                                }
                            )
                        },
                        enabled = !authVm.guardandoPerfil,
                        colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (authVm.guardandoPerfil) "Guardando..." else "Guardar cambios",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaMenuPerfil(
    icono: ImageVector,
    colorIcono: Color,
    fondoIcono: Color,
    titulo: String,
    subtitulo: String,
    badgeTexto: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Surface(shape = CircleShape, color = fondoIcono, modifier = Modifier.size(38.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1C1E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8E8E93),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (!badgeTexto.isNullOrBlank()) {
                Surface(shape = CircleShape, color = AppleBackground) {
                    Text(
                        text = badgeTexto,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF8E8E93), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun FilaDatoPerfil(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1C1C1E)
        )
    }
}
