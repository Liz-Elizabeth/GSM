package com.example.gsm.ui.view.screens.admin.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.data.repository.UsuarioRepository
import com.example.gsm.ui.view.components.shakeOnError
import com.example.gsm.ui.view.screens.usuario.componentes.AppleBackground
import com.example.gsm.ui.view.screens.usuario.componentes.BadgePill
import com.example.gsm.ui.view.screens.usuario.componentes.InactiveBadgeBg
import com.example.gsm.ui.view.screens.usuario.componentes.InactiveBadgeText
import com.example.gsm.ui.view.screens.usuario.componentes.OrangeBadgeBg
import com.example.gsm.ui.view.screens.usuario.componentes.OrangeBadgeText
import com.example.gsm.ui.view.screens.usuario.componentes.PurpleBadgeBg
import com.example.gsm.ui.view.screens.usuario.componentes.PurpleBadgeText
import com.example.gsm.ui.view.screens.usuario.componentes.SuccessGreenBg
import com.example.gsm.ui.view.screens.usuario.componentes.SuccessGreenText
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetCrearUsuario(
    creandoUsuario: Boolean,
    errorUsuario: String?,
    onRegistrar: (nombre: String, correo: String, password: String, rol: RolUsuario, empresa: String, telefono: String) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf(UsuarioRepository.DEFAULT_PASSWORD) }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.CLIENTE) }
    var empresa by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var errorShakeTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(errorUsuario) {
        if (errorUsuario != null) errorShakeTrigger++
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shakeOnError(errorShakeTrigger)
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Crear nueva cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "Asigna un rol y credenciales al nuevo integrante del sistema.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre completo *") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electrónico *") },
                placeholder = { Text("usuario@gsm.cl") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña inicial *") },
                supportingText = { Text("Por defecto: ${UsuarioRepository.DEFAULT_PASSWORD}. Se obligará a renovarla.") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Rol asignado (RBAC):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = rolSeleccionado == RolUsuario.CLIENTE,
                    onClick = { rolSeleccionado = RolUsuario.CLIENTE },
                    label = { Text("Cliente", style = MaterialTheme.typography.labelSmall) }
                )
                FilterChip(
                    selected = rolSeleccionado == RolUsuario.TECNICO,
                    onClick = { rolSeleccionado = RolUsuario.TECNICO },
                    label = { Text("Técnico", style = MaterialTheme.typography.labelSmall) }
                )
                FilterChip(
                    selected = rolSeleccionado == RolUsuario.ADMINISTRADOR,
                    onClick = { rolSeleccionado = RolUsuario.ADMINISTRADOR },
                    label = { Text("Admin", style = MaterialTheme.typography.labelSmall) }
                )
            }

            OutlinedTextField(
                value = empresa,
                onValueChange = { empresa = it },
                label = { Text(if (rolSeleccionado == RolUsuario.CLIENTE) "Empresa solicitante" else "Organización") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono de contacto") },
                placeholder = { Text("+56 9 1234 5678") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(
                visible = errorUsuario != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Text(errorUsuario ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) { Text("Cancelar") }
                Button(
                    onClick = { onRegistrar(nombre, correo, password, rolSeleccionado, empresa, telefono) },
                    enabled = !creandoUsuario,
                    colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.4f).height(48.dp)
                ) {
                    Text(if (creandoUsuario) "Creando..." else "Registrar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TarjetaConteoRol(
    titulo: String,
    conteo: Int,
    icono: ImageVector,
    color: Color,
    seleccionado: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (seleccionado) color.copy(alpha = 0.08f) else Color.White,
        border = BorderStroke(if (seleccionado) 1.5.dp else 1.dp, if (seleccionado) color else Color.Transparent),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icono, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(text = conteo.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(text = titulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun FilaUsuarioItem(
    usuario: Usuario,
    adminActual: Usuario,
    onAlternarActivo: () -> Unit,
    onResetearPassword: () -> Unit,
    onEliminar: () -> Unit
) {
    val (fondoRol, textoRol) = when (usuario.rol) {
        RolUsuario.CLIENTE -> Pair(Color(0xFFE3F2FD), TeamworkBlue)
        RolUsuario.TECNICO -> Pair(OrangeBadgeBg, OrangeBadgeText)
        RolUsuario.ADMINISTRADOR -> Pair(PurpleBadgeBg, PurpleBadgeText)
    }
    val esUsuarioPropio = usuario.id == adminActual.id
    val iniciales = remember(usuario.nombre) {
        usuario.nombre.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "U" }
    }
    var menuAbierto by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = fondoRol, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = iniciales,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = textoRol
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (usuario.activo) SuccessGreenText else InactiveBadgeText, CircleShape)
                    )
                    Text(
                        text = usuario.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (esUsuarioPropio) {
                        BadgePill("Tú", Color(0xFFE0E0E0), Color(0xFF424242))
                    }
                }
                Text(
                    text = usuario.correo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val detalle = listOfNotNull(
                    usuario.empresa?.takeIf { it.isNotBlank() },
                    usuario.telefono?.takeIf { it.isNotBlank() }
                ).joinToString(" • ")
                if (detalle.isNotBlank()) {
                    Text(
                        text = detalle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            BadgePill(usuario.rol.name, fondoRol, textoRol)
            Box {
                IconButton(onClick = { menuAbierto = true }, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Acciones de usuario", tint = Color(0xFF64748B))
                }
                DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                    DropdownMenuItem(
                        text = { Text("Restablecer clave") },
                        leadingIcon = { Icon(Icons.Filled.LockReset, contentDescription = null, tint = TeamworkBlue) },
                        onClick = {
                            menuAbierto = false
                            onResetearPassword()
                        }
                    )
                    if (!esUsuarioPropio) {
                        DropdownMenuItem(
                            text = { Text(if (usuario.activo) "Desactivar cuenta" else "Reactivar cuenta") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (usuario.activo) Icons.Filled.Block else Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = if (usuario.activo) InactiveBadgeText else SuccessGreenText
                                )
                            },
                            onClick = {
                                menuAbierto = false
                                onAlternarActivo()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Eliminar cuenta", color = InactiveBadgeText) },
                            leadingIcon = { Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = InactiveBadgeText) },
                            onClick = {
                                menuAbierto = false
                                onEliminar()
                            }
                        )
                    }
                }
            }
        }
    }
}
