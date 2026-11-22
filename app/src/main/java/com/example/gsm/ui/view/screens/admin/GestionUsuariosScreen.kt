package com.example.gsm.ui.view.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.data.repository.UsuarioRepository
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.view.screens.admin.componentes.FilaUsuarioItem
import com.example.gsm.ui.view.screens.admin.componentes.SheetCrearUsuario
import com.example.gsm.ui.view.screens.admin.componentes.TarjetaConteoRol
import com.example.gsm.ui.view.screens.usuario.componentes.AppleBackground
import com.example.gsm.ui.view.screens.usuario.componentes.InactiveBadgeBg
import com.example.gsm.ui.view.screens.usuario.componentes.InactiveBadgeText
import com.example.gsm.ui.view.screens.usuario.componentes.OrangeBadgeText
import com.example.gsm.ui.view.screens.usuario.componentes.PurpleBadgeText
import com.example.gsm.ui.view.screens.usuario.componentes.SuccessGreenBg
import com.example.gsm.ui.view.screens.usuario.componentes.SuccessGreenText
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import com.example.gsm.ui.viewmodel.AdminDashboardViewModel

/**
 * Pantalla dedicada exclusiva para la Gestión y Creación de Usuarios, Asignación de Roles (RBAC),
 * Suspensión/Eliminación de cuentas y Reseteo de Contraseñas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    admin: Usuario,
    dashboardVm: AdminDashboardViewModel,
    alVolver: () -> Unit
) {
    var mostrarDialogoCrear by remember { mutableStateOf(false) }

    // Diálogos de acción sobre usuarios
    var usuarioParaAlternar by remember { mutableStateOf<Usuario?>(null) }
    var mostrarDialogoAlternar by remember { mutableStateOf(false) }

    var usuarioParaResetear by remember { mutableStateOf<Usuario?>(null) }
    var mostrarDialogoResetear by remember { mutableStateOf(false) }

    var usuarioParaEliminar by remember { mutableStateOf<Usuario?>(null) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    var filtroRol by remember { mutableStateOf<RolUsuario?>(null) }
    var textoBusqueda by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        dashboardVm.refrescarUsuariosDesdeNube()
    }

    val totalClientes = dashboardVm.listaUsuarios.count { it.rol == RolUsuario.CLIENTE }
    val totalTecnicos = dashboardVm.listaUsuarios.count { it.rol == RolUsuario.TECNICO }
    val totalAdmins = dashboardVm.listaUsuarios.count { it.rol == RolUsuario.ADMINISTRADOR }

    val usuariosFiltrados = remember(dashboardVm.listaUsuarios, filtroRol, textoBusqueda) {
        val q = textoBusqueda.trim().lowercase()
        dashboardVm.listaUsuarios.filter { u ->
            val coincideRol = filtroRol == null || u.rol == filtroRol
            val coincideTexto = q.isEmpty() ||
                u.nombre.lowercase().contains(q) ||
                u.correo.lowercase().contains(q) ||
                u.empresa.orEmpty().lowercase().contains(q)
            coincideRol && coincideTexto
        }
    }

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

        // 2. Tarjetas Resumen de Roles (Filtros Interactivos)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TarjetaConteoRol(
                titulo = "Clientes",
                conteo = totalClientes,
                icono = Icons.Filled.Person,
                color = TeamworkBlue,
                seleccionado = filtroRol == RolUsuario.CLIENTE,
                onClick = { filtroRol = if (filtroRol == RolUsuario.CLIENTE) null else RolUsuario.CLIENTE },
                modifier = Modifier.weight(1f)
            )
            TarjetaConteoRol(
                titulo = "Técnicos",
                conteo = totalTecnicos,
                icono = Icons.Filled.Build,
                color = OrangeBadgeText,
                seleccionado = filtroRol == RolUsuario.TECNICO,
                onClick = { filtroRol = if (filtroRol == RolUsuario.TECNICO) null else RolUsuario.TECNICO },
                modifier = Modifier.weight(1f)
            )
            TarjetaConteoRol(
                titulo = "Admins",
                conteo = totalAdmins,
                icono = Icons.Filled.AdminPanelSettings,
                color = PurpleBadgeText,
                seleccionado = filtroRol == RolUsuario.ADMINISTRADOR,
                onClick = { filtroRol = if (filtroRol == RolUsuario.ADMINISTRADOR) null else RolUsuario.ADMINISTRADOR },
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

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it },
            placeholder = { Text("Buscar por nombre, correo o empresa...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TeamworkBlue) },
            trailingIcon = {
                if (textoBusqueda.isNotEmpty()) {
                    IconButton(onClick = { textoBusqueda = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "Limpiar búsqueda")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = TeamworkBlue,
                unfocusedBorderColor = Color(0xFFE5E5EA)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Listado de Cuentas (Inset Grouped)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Usuarios Registrados (${usuariosFiltrados.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider(color = AppleBackground)

                if (usuariosFiltrados.isEmpty()) {
                    Text(
                        text = if (filtroRol != null || textoBusqueda.isNotBlank()) "Sin usuarios que coincidan con el filtro." else "No hay usuarios registrados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    usuariosFiltrados.forEachIndexed { index, itemUsuario ->
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
                        if (index < usuariosFiltrados.lastIndex) {
                            HorizontalDivider(color = AppleBackground)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Sheet inferior para Registro de Usuario (extraído a GestionUsuariosComponentes)
    if (mostrarDialogoCrear) {
        SheetCrearUsuario(
            creandoUsuario = dashboardVm.creandoUsuario,
            errorUsuario = dashboardVm.errorUsuario,
            onRegistrar = { nom, corr, pass, rolSel, emp, tel ->
                dashboardVm.crearUsuario(
                    nombre = nom,
                    correo = corr,
                    password = pass,
                    rol = rolSel,
                    empresa = emp,
                    telefono = tel,
                    onExito = { mostrarDialogoCrear = false }
                )
            },
            onDismiss = {
                mostrarDialogoCrear = false
                dashboardVm.limpiarMensajesUsuario()
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
