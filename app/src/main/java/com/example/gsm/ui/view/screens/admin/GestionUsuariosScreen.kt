package com.example.gsm.ui.view.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.view.screens.usuario.componentes.AppleBackground
import com.example.gsm.ui.view.screens.usuario.componentes.BadgePill
import com.example.gsm.ui.view.screens.usuario.componentes.SuccessGreenBg
import com.example.gsm.ui.view.screens.usuario.componentes.SuccessGreenText
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import com.example.gsm.ui.viewmodel.AdminDashboardViewModel

private val PurpleBadgeBg = Color(0xFFF3E8FF)
private val PurpleBadgeText = Color(0xFF7E22CE)
private val OrangeBadgeBg = Color(0xFFFFF3E0)
private val OrangeBadgeText = Color(0xFFE65100)

/**
 * Pantalla dedicada exclusiva para la Gestión y Creación de Usuarios y Asignación de Roles (RBAC).
 */
@Composable
fun GestionUsuariosScreen(
    dashboardVm: AdminDashboardViewModel,
    alVolver: () -> Unit
) {
    var mostrarDialogoCrear by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.TECNICO) }
    var empresa by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    val totalClientes = dashboardVm.listaUsuarios.count { it.rol == RolUsuario.CLIENTE }
    val totalTecnicos = dashboardVm.listaUsuarios.count { it.rol == RolUsuario.TECNICO }
    val totalAdmins = dashboardVm.listaUsuarios.count { it.rol == RolUsuario.ADMINISTRADOR }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Encabezado con Botón Volver
        EncabezadoPantalla(
            titulo = "Gestión de Usuarios",
            alHacerClicDerecho = alVolver
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Tarjetas Resumen de Roles (Fila de 3 Métricas Rápidas)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TarjetaConteoRol(
                titulo = "Clientes",
                conteo = totalClientes,
                icono = "👤",
                color = TeamworkBlue,
                modifier = Modifier.weight(1f)
            )
            TarjetaConteoRol(
                titulo = "Técnicos",
                conteo = totalTecnicos,
                icono = "🔧",
                color = OrangeBadgeText,
                modifier = Modifier.weight(1f)
            )
            TarjetaConteoRol(
                titulo = "Admins",
                conteo = totalAdmins,
                icono = "👔",
                color = PurpleBadgeText,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Botón de Creación de Usuarios
        Button(
            onClick = {
                nombre = ""
                correo = ""
                password = ""
                rolSeleccionado = RolUsuario.TECNICO
                empresa = ""
                telefono = ""
                dashboardVm.limpiarMensajesUsuario()
                mostrarDialogoCrear = true
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Crear Nuevo Usuario",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Listado de Cuentas (Inset Grouped)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Usuarios Registrados (${dashboardVm.listaUsuarios.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider(color = AppleBackground)

                if (dashboardVm.listaUsuarios.isEmpty()) {
                    Text(
                        text = "No hay usuarios registrados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    dashboardVm.listaUsuarios.forEachIndexed { index, usuario ->
                        FilaUsuarioItem(usuario = usuario)
                        if (index < dashboardVm.listaUsuarios.size - 1) {
                            HorizontalDivider(color = AppleBackground)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Modal de Registro de Usuario y Rol RBAC
    if (mostrarDialogoCrear) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoCrear = false
                dashboardVm.limpiarMensajesUsuario()
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "👑 Crear Nueva Cuenta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo electrónico *") },
                        placeholder = { Text("usuario@gsm.cl") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña inicial *") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Rol asignado (Control de Acceso RBAC):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = rolSeleccionado == RolUsuario.CLIENTE,
                            onClick = { rolSeleccionado = RolUsuario.CLIENTE },
                            label = { Text("👤 Cliente", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = rolSeleccionado == RolUsuario.TECNICO,
                            onClick = { rolSeleccionado = RolUsuario.TECNICO },
                            label = { Text("🔧 Técnico", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = rolSeleccionado == RolUsuario.ADMINISTRADOR,
                            onClick = { rolSeleccionado = RolUsuario.ADMINISTRADOR },
                            label = { Text("👑 Admin", style = MaterialTheme.typography.labelSmall) }
                        )
                    }

                    OutlinedTextField(
                        value = empresa,
                        onValueChange = { empresa = it },
                        label = { Text(if (rolSeleccionado == RolUsuario.CLIENTE) "Empresa solicitante" else "Organización") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono de contacto") },
                        placeholder = { Text("+56 9 1234 5678") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (dashboardVm.errorUsuario != null) {
                        Text(
                            text = dashboardVm.errorUsuario ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (dashboardVm.mensajeUsuario != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreenBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✅ ${dashboardVm.mensajeUsuario}",
                                color = SuccessGreenText,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        dashboardVm.crearUsuario(
                            nombre = nombre,
                            correo = correo,
                            password = password,
                            rol = rolSeleccionado,
                            empresa = empresa,
                            telefono = telefono,
                            onExito = {
                                mostrarDialogoCrear = false
                            }
                        )
                    },
                    enabled = !dashboardVm.creandoUsuario,
                    colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue)
                ) {
                    Text(if (dashboardVm.creandoUsuario) "Creando..." else "Registrar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoCrear = false
                        dashboardVm.limpiarMensajesUsuario()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaConteoRol(
    titulo: String,
    conteo: Int,
    icono: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = icono, style = MaterialTheme.typography.titleMedium)
            Text(
                text = conteo.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilaUsuarioItem(usuario: Usuario) {
    val (icono, fondoRol, textoRol) = when (usuario.rol) {
        RolUsuario.CLIENTE -> Triple("👤", Color(0xFFE3F2FD), TeamworkBlue)
        RolUsuario.TECNICO -> Triple("🔧", OrangeBadgeBg, OrangeBadgeText)
        RolUsuario.ADMINISTRADOR -> Triple("👔", PurpleBadgeBg, PurpleBadgeText)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = AppleBackground,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icono, style = MaterialTheme.typography.titleMedium)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = usuario.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = usuario.correo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!usuario.empresa.isNullOrBlank() || !usuario.telefono.isNullOrBlank()) {
                    val detalle = listOfNotNull(
                        usuario.empresa?.let { "🏢 $it" },
                        usuario.telefono?.let { "📞 $it" }
                    ).joinToString(" • ")
                    Text(
                        text = detalle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        BadgePill(
            texto = usuario.rol.name,
            colorFondo = fondoRol,
            colorTexto = textoRol
        )
    }
}
