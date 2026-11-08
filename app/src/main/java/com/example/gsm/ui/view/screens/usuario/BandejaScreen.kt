package com.example.gsm.ui.view.screens.usuario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.QrScannerModal
import com.example.gsm.ui.view.components.QrNoEncontradoDialog
import com.example.gsm.ui.view.components.SkeletonTarjetaTicket
import com.example.gsm.ui.view.components.TarjetaSolicitudItem
import com.example.gsm.ui.viewmodel.SolicitudViewModel

// Paleta Apple + Teamwork Desk
private val AppleBackground = Color(0xFFF2F2F7)
private val AppleInputFill = Color(0xFFF2F2F7)
private val TeamworkBlue = Color(0xFF0066FF)

/**
 * Pantalla Principal de la Bandeja de Tickets GSM (Patrón KISS).
 * Rediseñada con filtros mediante menús desplegables (DropdownMenu),
 * barra de búsqueda moderna y diseño visual limpio estilo Apple HIG.
 */
@Composable
fun BandejaScreen(
    usuario: Usuario,
    solicitudVm: SolicitudViewModel,
    alIrACrear: () -> Unit,
    alIrADetalle: (Int) -> Unit,
    alIrADashboard: () -> Unit,
    alCerrarSesion: () -> Unit
) {
    var mostrarQrScanner by remember { mutableStateOf(false) }
    var qrNoEncontradoCodigo by remember { mutableStateOf<String?>(null) }

    // Estados para controlar apertura de los Menús Desplegables de Filtro
    var menuEstadoAbierto by remember { mutableStateOf(false) }
    var menuPrioridadAbierto by remember { mutableStateOf(false) }
    var menuAsignacionAbierto by remember { mutableStateOf(false) }

    LaunchedEffect(usuario.id) {
        solicitudVm.cargarSolicitudes(usuario)
    }

    // Comprobación reactiva si hay algún filtro activo
    val hayFiltroActivo = solicitudVm.estadoFiltro != null ||
            solicitudVm.prioridadFiltro != null ||
            solicitudVm.soloAtrasadas ||
            solicitudVm.clienteFiltroId != null ||
            solicitudVm.tecnicoFiltroId != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 1. HEADER LIMPIO (Título + Badge de Rol + Botón directo a Nuevo Ticket)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Bandeja GSM",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Surface(
                        color = TeamworkBlue.copy(alpha = 0.12f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = usuario.rol.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = TeamworkBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Hola, ${usuario.nombre}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Botones de acción en la cabecera
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (usuario.rol == RolUsuario.ADMINISTRADOR) {
                    IconButton(
                        onClick = alIrADashboard,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BarChart,
                            contentDescription = "Panel de Métricas",
                            tint = TeamworkBlue
                        )
                    }
                }

                Button(
                    onClick = alIrACrear,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Nuevo", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. BARRA DE HERRAMIENTAS: BÚSQUEDA + MENÚS DESPLEGABLES DE FILTRO
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // A. Buscador Spotlight con botón '✕' + Lector QR con CameraX
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = solicitudVm.textoBusqueda,
                        onValueChange = { solicitudVm.cambiarTextoBusqueda(it, usuario) },
                        placeholder = {
                            Text(
                                text = "Buscar por código, título o tema...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF8E8E93)
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (solicitudVm.textoBusqueda.isNotEmpty()) {
                                IconButton(onClick = { solicitudVm.cambiarTextoBusqueda("", usuario) }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Limpiar búsqueda", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = AppleInputFill,
                            unfocusedContainerColor = AppleInputFill,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilledTonalButton(
                        onClick = { mostrarQrScanner = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = TeamworkBlue.copy(alpha = 0.12f),
                            contentColor = TeamworkBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = "Escanear QR",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "QR",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = AppleBackground)

                // B. FILTROS MEDIANTE MENÚS DESPLEGABLES (DROPDOWN MENUS)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. MENÚ DESPLEGABLE: ESTADO
                    Box {
                        val estadoActivo = solicitudVm.estadoFiltro != null || solicitudVm.soloAtrasadas
                        val etiquetaEstado = when {
                            solicitudVm.soloAtrasadas -> "⚠️ Atrasadas"
                            solicitudVm.estadoFiltro != null -> solicitudVm.estadoFiltro!!.name
                                .replace("_", " ")
                                .lowercase()
                                .replaceFirstChar { it.uppercase() }
                            else -> "Estado: Todos"
                        }

                        Surface(
                            onClick = { menuEstadoAbierto = true },
                            shape = RoundedCornerShape(10.dp),
                            color = if (estadoActivo) TeamworkBlue.copy(alpha = 0.10f) else Color(0xFFF2F2F7),
                            border = BorderStroke(
                                1.dp,
                                if (estadoActivo) TeamworkBlue else Color(0xFFE5E5EA)
                            ),
                            modifier = Modifier
                                .height(44.dp)
                                .semantics {
                                    role = Role.DropdownList
                                    contentDescription = "Filtro por estado: $etiquetaEstado"
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = etiquetaEstado,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (estadoActivo) FontWeight.Bold else FontWeight.Medium,
                                    color = if (estadoActivo) TeamworkBlue else Color(0xFF1C1C1E)
                                )
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                    tint = if (estadoActivo) TeamworkBlue else Color(0xFF8E8E93),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = menuEstadoAbierto,
                            onDismissRequest = { menuEstadoAbierto = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Todos los estados",
                                        fontWeight = if (solicitudVm.estadoFiltro == null && !solicitudVm.soloAtrasadas) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    solicitudVm.cambiarFiltroAtrasadas(false, usuario)
                                    solicitudVm.cambiarFiltroEstado(null, usuario)
                                    menuEstadoAbierto = false
                                },
                                trailingIcon = if (solicitudVm.estadoFiltro == null && !solicitudVm.soloAtrasadas) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                } else null
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "⚠️ Solo Atrasadas (SLA vencido)",
                                        fontWeight = if (solicitudVm.soloAtrasadas) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    solicitudVm.cambiarFiltroAtrasadas(true, usuario)
                                    solicitudVm.cambiarFiltroEstado(null, usuario)
                                    menuEstadoAbierto = false
                                },
                                trailingIcon = if (solicitudVm.soloAtrasadas) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                } else null
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            EstadoSolicitud.entries.forEach { estado ->
                                val seleccionado = solicitudVm.estadoFiltro == estado && !solicitudVm.soloAtrasadas
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = estado.name.replace("_", " ")
                                                .lowercase()
                                                .replaceFirstChar { it.uppercase() },
                                            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        solicitudVm.cambiarFiltroAtrasadas(false, usuario)
                                        solicitudVm.cambiarFiltroEstado(estado, usuario)
                                        menuEstadoAbierto = false
                                    },
                                    trailingIcon = if (seleccionado) {
                                        { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    // 2. MENÚ DESPLEGABLE: PRIORIDAD (SLA)
                    Box {
                        val prioActiva = solicitudVm.prioridadFiltro != null
                        val etiquetaPrioridad = if (prioActiva) "Prioridad: ${solicitudVm.prioridadFiltro!!.name}" else "Prioridad: Todas"

                        Surface(
                            onClick = { menuPrioridadAbierto = true },
                            shape = RoundedCornerShape(10.dp),
                            color = if (prioActiva) TeamworkBlue.copy(alpha = 0.10f) else Color(0xFFF2F2F7),
                            border = BorderStroke(
                                1.dp,
                                if (prioActiva) TeamworkBlue else Color(0xFFE5E5EA)
                            ),
                            modifier = Modifier
                                .height(44.dp)
                                .semantics {
                                    role = Role.DropdownList
                                    contentDescription = "Filtro por prioridad: $etiquetaPrioridad"
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = etiquetaPrioridad,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (prioActiva) FontWeight.Bold else FontWeight.Medium,
                                    color = if (prioActiva) TeamworkBlue else Color(0xFF1C1C1E)
                                )
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                    tint = if (prioActiva) TeamworkBlue else Color(0xFF8E8E93),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = menuPrioridadAbierto,
                            onDismissRequest = { menuPrioridadAbierto = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Todas las prioridades",
                                        fontWeight = if (solicitudVm.prioridadFiltro == null) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    solicitudVm.cambiarFiltroPrioridad(null, usuario)
                                    menuPrioridadAbierto = false
                                },
                                trailingIcon = if (solicitudVm.prioridadFiltro == null) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                } else null
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            PrioridadSolicitud.entries.forEach { prio ->
                                val seleccionada = solicitudVm.prioridadFiltro == prio
                                val emoji = when (prio) {
                                    PrioridadSolicitud.BAJA -> "🟢"
                                    PrioridadSolicitud.MEDIA -> "🟡"
                                    PrioridadSolicitud.ALTA -> "🟠"
                                    PrioridadSolicitud.URGENTE -> "🔴"
                                }
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "$emoji ${prio.name}",
                                            fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        solicitudVm.cambiarFiltroPrioridad(prio, usuario)
                                        menuPrioridadAbierto = false
                                    },
                                    trailingIcon = if (seleccionada) {
                                        { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    // 3. MENÚ DESPLEGABLE: ASIGNACIÓN / TÉCNICO (Solo Administrador)
                    if (usuario.rol == RolUsuario.ADMINISTRADOR) {
                        Box {
                            val asignacionActiva = solicitudVm.tecnicoFiltroId != null || solicitudVm.clienteFiltroId != null
                            val etiquetaAsignacion = when {
                                solicitudVm.tecnicoFiltroId == 3 -> "🔧 Téc: Diego"
                                solicitudVm.tecnicoFiltroId == 4 -> "🔧 Téc: Valentina"
                                solicitudVm.clienteFiltroId == 1 -> "👤 Cliente: Camila"
                                else -> "Asignación: Todos"
                            }

                            Surface(
                                onClick = { menuAsignacionAbierto = true },
                                shape = RoundedCornerShape(10.dp),
                                color = if (asignacionActiva) TeamworkBlue.copy(alpha = 0.10f) else Color(0xFFF2F2F7),
                                border = BorderStroke(
                                    1.dp,
                                    if (asignacionActiva) TeamworkBlue else Color(0xFFE5E5EA)
                                ),
                                modifier = Modifier
                                    .height(44.dp)
                                    .semantics {
                                        role = Role.DropdownList
                                        contentDescription = "Filtro por asignación: $etiquetaAsignacion"
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = etiquetaAsignacion,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (asignacionActiva) FontWeight.Bold else FontWeight.Medium,
                                        color = if (asignacionActiva) TeamworkBlue else Color(0xFF1C1C1E)
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = null,
                                        tint = if (asignacionActiva) TeamworkBlue else Color(0xFF8E8E93),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = menuAsignacionAbierto,
                                onDismissRequest = { menuAsignacionAbierto = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todos los técnicos y clientes") },
                                    onClick = {
                                        solicitudVm.cambiarFiltroTecnico(null, usuario)
                                        solicitudVm.cambiarFiltroCliente(null, usuario)
                                        menuAsignacionAbierto = false
                                    },
                                    trailingIcon = if (!asignacionActiva) {
                                        { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                DropdownMenuItem(
                                    text = { Text("🔧 Técnico: Diego") },
                                    onClick = {
                                        solicitudVm.cambiarFiltroCliente(null, usuario)
                                        solicitudVm.cambiarFiltroTecnico(3, usuario)
                                        menuAsignacionAbierto = false
                                    },
                                    trailingIcon = if (solicitudVm.tecnicoFiltroId == 3) {
                                        { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                                DropdownMenuItem(
                                    text = { Text("🔧 Técnico: Valentina") },
                                    onClick = {
                                        solicitudVm.cambiarFiltroCliente(null, usuario)
                                        solicitudVm.cambiarFiltroTecnico(4, usuario)
                                        menuAsignacionAbierto = false
                                    },
                                    trailingIcon = if (solicitudVm.tecnicoFiltroId == 4) {
                                        { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                                DropdownMenuItem(
                                    text = { Text("👤 Cliente: Camila") },
                                    onClick = {
                                        solicitudVm.cambiarFiltroTecnico(null, usuario)
                                        solicitudVm.cambiarFiltroCliente(1, usuario)
                                        menuAsignacionAbierto = false
                                    },
                                    trailingIcon = if (solicitudVm.clienteFiltroId == 1) {
                                        { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    // 4. BOTÓN "LIMPIAR FILTROS" (Aparece cuando hay filtros seleccionados)
                    if (hayFiltroActivo) {
                        Surface(
                            onClick = {
                                solicitudVm.cambiarFiltroAtrasadas(false, usuario)
                                solicitudVm.cambiarFiltroEstado(null, usuario)
                                solicitudVm.cambiarFiltroPrioridad(null, usuario)
                                solicitudVm.cambiarFiltroTecnico(null, usuario)
                                solicitudVm.cambiarFiltroCliente(null, usuario)
                            },
                            shape = CircleShape,
                            color = Color(0xFFFFECEB),
                            modifier = Modifier
                                .height(44.dp)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Limpiar todos los filtros"
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = null,
                                    tint = Color(0xFFD92D20),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Limpiar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFD92D20),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. CABECERA DE RESULTADOS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SOLICITUDES REGISTRADAS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Surface(
                shape = CircleShape,
                color = if (solicitudVm.listaSolicitudes.isNotEmpty()) TeamworkBlue.copy(alpha = 0.12f) else Color(0xFFE5E5EA)
            ) {
                Text(
                    text = "${solicitudVm.listaSolicitudes.size} tickets",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (solicitudVm.listaSolicitudes.isNotEmpty()) TeamworkBlue else Color(0xFF636366),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filtro inteligente de bandeja: por defecto solo tickets activos (no cerrados ni archivados)
        val solicitudesVisibles = if (hayFiltroActivo || solicitudVm.estadoFiltro != null) {
            solicitudVm.listaSolicitudes
        } else {
            solicitudVm.solicitudesActivas
        }

        // 4. LISTA DE TICKETS, SKELETONS DE CARGA O ESTADO VACÍO
        if (solicitudVm.cargando && solicitudesVisibles.isEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(4) {
                    SkeletonTarjetaTicket()
                }
            }
        } else if (solicitudesVisibles.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AppleBackground,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Inbox,
                                contentDescription = null,
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Bandeja al día",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (hayFiltroActivo || solicitudVm.textoBusqueda.isNotEmpty()) {
                            "No se encontraron tickets con los filtros actuales."
                        } else {
                            "No tienes solicitudes pendientes en este momento."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8E8E93),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    if (hayFiltroActivo) {
                        OutlinedButton(
                            onClick = {
                                solicitudVm.cambiarTextoBusqueda("", usuario)
                                solicitudVm.cambiarFiltroAtrasadas(false, usuario)
                                solicitudVm.cambiarFiltroEstado(null, usuario)
                                solicitudVm.cambiarFiltroPrioridad(null, usuario)
                                solicitudVm.cambiarFiltroTecnico(null, usuario)
                                solicitudVm.cambiarFiltroCliente(null, usuario)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Quitar todos los filtros", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = alIrACrear,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Crear primera solicitud", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(
                    items = solicitudesVisibles,
                    key = { it.id }
                ) { solicitud ->
                    TarjetaSolicitudItem(
                        solicitud = solicitud,
                        alHacerClic = { alIrADetalle(solicitud.id) }
                    )
                }
            }
        }
    }

    // Modal de Escaneo con CameraX
    if (mostrarQrScanner) {
        QrScannerModal(
            onDismiss = { mostrarQrScanner = false },
            onQrDetectado = { codigoLeido ->
                mostrarQrScanner = false
                solicitudVm.buscarPorQr(
                    codigoQr = codigoLeido,
                    alEncontrar = { idTicket ->
                        alIrADetalle(idTicket)
                    },
                    alFallar = {
                        qrNoEncontradoCodigo = codigoLeido
                    }
                )
            }
        )
    }

    // Modal animado si el QR no coincide con ningún ticket existente
    if (qrNoEncontradoCodigo != null) {
        QrNoEncontradoDialog(
            codigoEscaneado = qrNoEncontradoCodigo!!,
            onReintentar = {
                qrNoEncontradoCodigo = null
                mostrarQrScanner = true
            },
            onCerrar = {
                qrNoEncontradoCodigo = null
            }
        )
    }
}
