package com.example.gsm.ui.view.screens.usuario.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
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
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.viewmodel.SolicitudViewModel

/**
 * Barra compacta de Bandeja: buscador + QR + botón Filtros con panel desplegable y chips rápidos.
 */
@Composable
fun BarraFiltrosBandeja(
    usuario: Usuario,
    solicitudVm: SolicitudViewModel,
    usuariosFiltro: List<Usuario> = emptyList(),
    ordenarPorSla: Boolean = false,
    onCambiarOrdenSla: (Boolean) -> Unit = {},
    hayFiltroActivo: Boolean,
    onAbrirQr: () -> Unit
) {
    var filtrosExpandidos by remember { mutableStateOf(false) }
    var menuEstadoAbierto by remember { mutableStateOf(false) }
    var menuPrioridadAbierto by remember { mutableStateOf(false) }
    var menuAsignacionAbierto by remember { mutableStateOf(false) }

    val filtrosAvanzadosCount = listOfNotNull(
        solicitudVm.estadoFiltro,
        solicitudVm.prioridadFiltro,
        solicitudVm.tecnicoFiltroId,
        solicitudVm.clienteFiltroId,
        if (solicitudVm.soloAtrasadas) true else null,
        if (ordenarPorSla) true else null
    ).size

    val tecnicos = remember(usuariosFiltro) { usuariosFiltro.filter { it.rol == RolUsuario.TECNICO && it.activo } }
    val clientes = remember(usuariosFiltro) { usuariosFiltro.filter { it.rol == RolUsuario.CLIENTE && it.activo } }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Fila 1: Buscador + QR + Botón Filtros Avanzados
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
                            text = "Buscar por código o título...",
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

                FilledTonalIconButton(
                    onClick = onAbrirQr,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = TeamworkBlue.copy(alpha = 0.12f),
                        contentColor = TeamworkBlue
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = "Escanear QR", modifier = Modifier.size(22.dp))
                }

                FilledTonalIconButton(
                    onClick = { filtrosExpandidos = !filtrosExpandidos },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (filtrosExpandidos || filtrosAvanzadosCount > 0) TeamworkBlue.copy(alpha = 0.14f) else AppleBackground,
                        contentColor = if (filtrosExpandidos || filtrosAvanzadosCount > 0) TeamworkBlue else Color(0xFF475569)
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    if (filtrosAvanzadosCount > 0) {
                        BadgedBox(badge = { Badge { Text("$filtrosAvanzadosCount") } }) {
                            Icon(Icons.Filled.Tune, contentDescription = "Filtros avanzados", modifier = Modifier.size(20.dp))
                        }
                    } else {
                        Icon(Icons.Filled.Tune, contentDescription = "Filtros avanzados", modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Fila 2: Chips rápidos de 1 toque
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sinFiltrosRapidos = !hayFiltroActivo && !ordenarPorSla
                FilterChip(
                    selected = sinFiltrosRapidos,
                    onClick = {
                        solicitudVm.cambiarTextoBusqueda("", usuario)
                        solicitudVm.cambiarFiltroAtrasadas(false, usuario)
                        solicitudVm.cambiarFiltroEstado(null, usuario)
                        solicitudVm.cambiarFiltroPrioridad(null, usuario)
                        solicitudVm.cambiarFiltroTecnico(null, usuario)
                        solicitudVm.cambiarFiltroCliente(null, usuario)
                        onCambiarOrdenSla(false)
                    },
                    label = { Text("Todas") }
                )
                FilterChip(
                    selected = solicitudVm.soloAtrasadas,
                    onClick = {
                        val activar = !solicitudVm.soloAtrasadas
                        solicitudVm.cambiarFiltroEstado(null, usuario)
                        solicitudVm.cambiarFiltroAtrasadas(activar, usuario)
                    },
                    label = { Text("Atrasadas") }
                )
                FilterChip(
                    selected = solicitudVm.prioridadFiltro == PrioridadSolicitud.URGENTE,
                    onClick = {
                        val nueva = if (solicitudVm.prioridadFiltro == PrioridadSolicitud.URGENTE) null else PrioridadSolicitud.URGENTE
                        solicitudVm.cambiarFiltroPrioridad(nueva, usuario)
                    },
                    label = { Text("Urgentes") }
                )
                FilterChip(
                    selected = solicitudVm.estadoFiltro == EstadoSolicitud.EN_PROCESO && !solicitudVm.soloAtrasadas,
                    onClick = {
                        solicitudVm.cambiarFiltroAtrasadas(false, usuario)
                        val nuevo = if (solicitudVm.estadoFiltro == EstadoSolicitud.EN_PROCESO) null else EstadoSolicitud.EN_PROCESO
                        solicitudVm.cambiarFiltroEstado(nuevo, usuario)
                    },
                    label = { Text("En proceso") }
                )
                if (hayFiltroActivo || ordenarPorSla) {
                    Surface(
                        onClick = {
                            solicitudVm.cambiarTextoBusqueda("", usuario)
                            solicitudVm.cambiarFiltroAtrasadas(false, usuario)
                            solicitudVm.cambiarFiltroEstado(null, usuario)
                            solicitudVm.cambiarFiltroPrioridad(null, usuario)
                            solicitudVm.cambiarFiltroTecnico(null, usuario)
                            solicitudVm.cambiarFiltroCliente(null, usuario)
                            onCambiarOrdenSla(false)
                        },
                        shape = CircleShape,
                        color = AlertRedBg,
                        modifier = Modifier
                            .height(32.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Limpiar todos los filtros"
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = null, tint = AlertRedText, modifier = Modifier.size(14.dp))
                            Text("Limpiar", style = MaterialTheme.typography.labelSmall, color = AlertRedText, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Panel desplegable de filtros avanzados y ordenamiento
            AnimatedVisibility(
                visible = filtrosExpandidos,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = AppleBackground)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Estado
                        Box {
                            val estadoActivo = solicitudVm.estadoFiltro != null || solicitudVm.soloAtrasadas
                            val etiquetaEstado = when {
                                solicitudVm.soloAtrasadas -> "Atrasadas"
                                solicitudVm.estadoFiltro != null -> formatearEstado(solicitudVm.estadoFiltro!!)
                                else -> "Estado: Todos"
                            }
                            FiltroDropdownButton(etiquetaEstado, estadoActivo, "Filtro por estado: $etiquetaEstado") {
                                menuEstadoAbierto = true
                            }
                            DropdownMenu(expanded = menuEstadoAbierto, onDismissRequest = { menuEstadoAbierto = false }) {
                                DropdownMenuItem(
                                    text = { Text("Todos los estados", fontWeight = if (!estadoActivo) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        solicitudVm.cambiarFiltroAtrasadas(false, usuario)
                                        solicitudVm.cambiarFiltroEstado(null, usuario)
                                        menuEstadoAbierto = false
                                    },
                                    trailingIcon = if (!estadoActivo) {
                                        { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                                DropdownMenuItem(
                                    text = { Text("Solo Atrasadas (SLA vencido)", fontWeight = if (solicitudVm.soloAtrasadas) FontWeight.Bold else FontWeight.Normal) },
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
                                        text = { Text(formatearEstado(estado), fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal) },
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

                        // 2. Prioridad
                        Box {
                            val prioActiva = solicitudVm.prioridadFiltro != null
                            val etiquetaPrioridad = if (prioActiva) "Prioridad: ${formatearPrioridad(solicitudVm.prioridadFiltro!!)}" else "Prioridad: Todas"
                            FiltroDropdownButton(etiquetaPrioridad, prioActiva, "Filtro por prioridad: $etiquetaPrioridad") {
                                menuPrioridadAbierto = true
                            }
                            DropdownMenu(expanded = menuPrioridadAbierto, onDismissRequest = { menuPrioridadAbierto = false }) {
                                DropdownMenuItem(
                                    text = { Text("Todas las prioridades", fontWeight = if (!prioActiva) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        solicitudVm.cambiarFiltroPrioridad(null, usuario)
                                        menuPrioridadAbierto = false
                                    },
                                    trailingIcon = if (!prioActiva) {
                                        { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                PrioridadSolicitud.entries.forEach { prio ->
                                    val seleccionada = solicitudVm.prioridadFiltro == prio
                                    DropdownMenuItem(
                                        text = { Text(formatearPrioridad(prio), fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal) },
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

                        // 3. Asignación dinámica (Solo Admin)
                        if (usuario.rol == RolUsuario.ADMINISTRADOR && (tecnicos.isNotEmpty() || clientes.isNotEmpty())) {
                            Box {
                                val asignacionActiva = solicitudVm.tecnicoFiltroId != null || solicitudVm.clienteFiltroId != null
                                val nombreFiltro = usuariosFiltro.firstOrNull {
                                    it.id == solicitudVm.tecnicoFiltroId || it.id == solicitudVm.clienteFiltroId
                                }?.nombre?.substringBefore(" ")
                                val etiquetaAsignacion = when {
                                    solicitudVm.tecnicoFiltroId != null -> "Téc: ${nombreFiltro ?: solicitudVm.tecnicoFiltroId}"
                                    solicitudVm.clienteFiltroId != null -> "Cliente: ${nombreFiltro ?: solicitudVm.clienteFiltroId}"
                                    else -> "Asignación: Todos"
                                }
                                FiltroDropdownButton(etiquetaAsignacion, asignacionActiva, "Filtro por asignación: $etiquetaAsignacion") {
                                    menuAsignacionAbierto = true
                                }
                                DropdownMenu(expanded = menuAsignacionAbierto, onDismissRequest = { menuAsignacionAbierto = false }) {
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
                                    if (tecnicos.isNotEmpty()) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        tecnicos.forEach { tec ->
                                            DropdownMenuItem(
                                                text = { Text("Técnico: ${tec.nombre}") },
                                                onClick = {
                                                    solicitudVm.cambiarFiltroCliente(null, usuario)
                                                    solicitudVm.cambiarFiltroTecnico(tec.id, usuario)
                                                    menuAsignacionAbierto = false
                                                },
                                                trailingIcon = if (solicitudVm.tecnicoFiltroId == tec.id) {
                                                    { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                                } else null
                                            )
                                        }
                                    }
                                    if (clientes.isNotEmpty()) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        clientes.forEach { cli ->
                                            DropdownMenuItem(
                                                text = { Text("Cliente: ${cli.nombre}") },
                                                onClick = {
                                                    solicitudVm.cambiarFiltroTecnico(null, usuario)
                                                    solicitudVm.cambiarFiltroCliente(cli.id, usuario)
                                                    menuAsignacionAbierto = false
                                                },
                                                trailingIcon = if (solicitudVm.clienteFiltroId == cli.id) {
                                                    { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                                } else null
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Ordenamiento por SLA
                        FilterChip(
                            selected = ordenarPorSla,
                            onClick = { onCambiarOrdenSla(!ordenarPorSla) },
                            label = { Text(if (ordenarPorSla) "Orden: SLA próximo" else "Orden: Recientes") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroDropdownButton(
    etiqueta: String,
    activo: Boolean,
    descripcion: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (activo) TeamworkBlue.copy(alpha = 0.10f) else AppleBackground,
        border = BorderStroke(1.dp, if (activo) TeamworkBlue else Color(0xFFE5E5EA)),
        modifier = Modifier
            .height(40.dp)
            .semantics {
                role = Role.DropdownList
                contentDescription = descripcion
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
                color = if (activo) TeamworkBlue else Color(0xFF1C1C1E)
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = if (activo) TeamworkBlue else Color(0xFF8E8E93),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
