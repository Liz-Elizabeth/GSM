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
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.data.repository.UsuarioRepository
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
private val InactiveBadgeBg = Color(0xFFFFEBEE)
private val InactiveBadgeText = Color(0xFFC62828)

/**
 * Pantalla dedicada exclusiva para la Gestión y Creación de Usuarios, Asignación de Roles (RBAC),
 * Suspensión/Eliminación de cuentas y Reseteo de Contraseñas.
 */
@Composable
fun GestionUsuariosScreen(
    admin: Usuario,
    dashboardVm: AdminDashboardViewModel,
    alVolver: () -> Unit
) {
    var mostrarDialogoCrear by remember { mutableStateOf(false) }

    // Estados para nuevo usuario
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf(UsuarioRepository.DEFAULT_PASSWORD) }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.TECNICO) }
    var empresa by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    // Diálogos de acción sobre usuarios
    var usuarioParaAlternar by remember { mutableStateOf<Usuario?>(null) }
    var mostrarDialogoAlternar by remember { mutableStateOf(false) }

    var usuarioParaResetear by remember { mutableStateOf<Usuario?>(null) }
    var mostrarDialogoResetear by remember { mutableStateOf(false) }

    var usuarioParaEliminar by remember { mutableStateOf<Usuario?>(null) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

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

        // Alertas de feedback en la pantalla principal
        if (dashboardVm.mensajeUsuario != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SuccessGreenBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✅ ${dashboardVm.mensajeUsuario}",
                        color = SuccessGreenText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { dashboardVm.limpiarMensajesUsuario() },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = SuccessGreenText, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (dashboardVm.errorUsuario != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = InactiveBadgeBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠️ ${dashboardVm.errorUsuario}",
                        color = InactiveBadgeText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { dashboardVm.limpiarMensajesUsuario() },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = InactiveBadgeText, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 3. Botón de Creación de Usuarios
        Button(
            onClick = {
                nombre = ""
                correo = ""
                password = UsuarioRepository.DEFAULT_PASSWORD
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
                    dashboardVm.listaUsuarios.forEachIndexed { index, itemUsuario ->
                        FilaUsuarioItem(
                            usuario = itemUsuario,
                            adminActual = admin,
                            onAlternarActivo = {
                                usuarioParaAlternar = itemUsuario
                                mostrarDialogoAlternar = true
                            },
                            onResetearPassword = {
                                usuarioParaResetear = itemUsuario
                                mostrarDialogoResetear = true
                            },
                            onEliminar = {
                                usuarioParaEliminar = itemUsuario
                                mostrarDialogoEliminar = true
                            }
                        )
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
                        supportingText = { Text("Por defecto: ${UsuarioRepository.DEFAULT_PASSWORD}. Se obligará a renovarla.") },
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

    // Modal para Alternar Activo / Desactivado
    if (mostrarDialogoAlternar && usuarioParaAlternar != null) {
        val u = usuarioParaAlternar!!
        val accion = if (u.activo) "Desactivar" else "Reactivar"
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoAlternar = false
                usuarioParaAlternar = null
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White,
            icon = {
                Icon(
                    imageVector = if (u.activo) Icons.Filled.Block else Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = if (u.activo) MaterialTheme.colorScheme.error else SuccessGreenText,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "¿$accion Cuenta?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (u.activo) {
                        "¿Deseas suspender el acceso a ${u.nombre}? El usuario no podrá iniciar sesión en la aplicación hasta que sea reactivado."
                    } else {
                        "¿Deseas reactivar la cuenta de ${u.nombre}? El usuario podrá volver a iniciar sesión con sus credenciales habituales."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        dashboardVm.alternarEstadoActivo(u, admin)
                        mostrarDialogoAlternar = false
                        usuarioParaAlternar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (u.activo) MaterialTheme.colorScheme.error else SuccessGreenText
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirmar $accion")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoAlternar = false
                    usuarioParaAlternar = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal para Resetear Contraseña a GSMPass12345
    if (mostrarDialogoResetear && usuarioParaResetear != null) {
        val u = usuarioParaResetear!!
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoResetear = false
                usuarioParaResetear = null
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Filled.LockReset,
                    contentDescription = null,
                    tint = TeamworkBlue,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Restablecer Contraseña",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Deseas restablecer la contraseña de ${u.nombre}? Su contraseña volverá a ser '${UsuarioRepository.DEFAULT_PASSWORD}' y el sistema lo obligará a definir una nueva clave personal en su próximo inicio de sesión.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        dashboardVm.resetearPassword(u, admin)
                        mostrarDialogoResetear = false
                        usuarioParaResetear = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Resetear a ${UsuarioRepository.DEFAULT_PASSWORD}")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoResetear = false
                    usuarioParaResetear = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal para Eliminar Usuario
    if (mostrarDialogoEliminar && usuarioParaEliminar != null) {
        val u = usuarioParaEliminar!!
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoEliminar = false
                usuarioParaEliminar = null
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Filled.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Eliminar Cuenta Permanentemente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Estás completamente seguro de eliminar a ${u.nombre}? Esta acción es irreversible y eliminará al usuario de los registros del sistema.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        dashboardVm.eliminarUsuario(u, admin)
                        mostrarDialogoEliminar = false
                        usuarioParaEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Eliminar Usuario")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoEliminar = false
                    usuarioParaEliminar = null
                }) {
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
private fun FilaUsuarioItem(
    usuario: Usuario,
    adminActual: Usuario,
    onAlternarActivo: () -> Unit,
    onResetearPassword: () -> Unit,
    onEliminar: () -> Unit
) {
    val (icono, fondoRol, textoRol) = when (usuario.rol) {
        RolUsuario.CLIENTE -> Triple("👤", Color(0xFFE3F2FD), TeamworkBlue)
        RolUsuario.TECNICO -> Triple("🔧", OrangeBadgeBg, OrangeBadgeText)
        RolUsuario.ADMINISTRADOR -> Triple("👔", PurpleBadgeBg, PurpleBadgeText)
    }

    val esUsuarioPropio = usuario.id == adminActual.id

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = usuario.nombre,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (esUsuarioPropio) {
                            BadgePill(
                                texto = "Tú",
                                colorFondo = Color(0xFFE0E0E0),
                                colorTexto = Color(0xFF424242)
                            )
                        }
                    }
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

            // Badges de Rol y Estado
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgePill(
                    texto = usuario.rol.name,
                    colorFondo = fondoRol,
                    colorTexto = textoRol
                )

                if (usuario.activo) {
                    BadgePill(
                        texto = "Activo",
                        colorFondo = SuccessGreenBg,
                        colorTexto = SuccessGreenText
                    )
                } else {
                    BadgePill(
                        texto = "Inactivo",
                        colorFondo = InactiveBadgeBg,
                        colorTexto = InactiveBadgeText
                    )
                }
            }
        }

        // Fila de Acciones Rápidas (Reset clave, Pausar/Activar, Eliminar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 50.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Resetear Contraseña (GSMPass12345)
            FilledTonalButton(
                onClick = onResetearPassword,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = AppleBackground,
                    contentColor = TeamworkBlue
                )
            ) {
                Icon(Icons.Filled.LockReset, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset Clave", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            if (!esUsuarioPropio) {
                // 2. Alternar Estado (Desactivar / Reactivar)
                FilledTonalButton(
                    onClick = onAlternarActivo,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (usuario.activo) InactiveBadgeBg else SuccessGreenBg,
                        contentColor = if (usuario.activo) InactiveBadgeText else SuccessGreenText
                    )
                ) {
                    Icon(
                        imageVector = if (usuario.activo) Icons.Filled.Block else Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (usuario.activo) "Desactivar" else "Reactivar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 3. Eliminar Usuario
                IconButton(
                    onClick = onEliminar,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Eliminar cuenta",
                        tint = InactiveBadgeText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
