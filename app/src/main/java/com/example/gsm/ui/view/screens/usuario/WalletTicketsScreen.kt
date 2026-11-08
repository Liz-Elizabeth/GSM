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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.gsm.R
import com.example.gsm.data.model.CategoriaSolicitud
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.QrScannerModal
import com.example.gsm.ui.view.components.QrNoEncontradoDialog
import com.example.gsm.ui.view.components.SkeletonPaseWallet
import com.example.gsm.ui.view.screens.usuario.componentes.AppleBackground
import com.example.gsm.ui.view.screens.usuario.componentes.TarjetaWalletTicket
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import com.example.gsm.ui.viewmodel.SolicitudViewModel

// Paleta local Apple Input
private val AppleInputFill = Color(0xFFF2F2F7)

/**
 * Pantalla Billetera de Pases Digitales (Estilo Apple Wallet).
 * Rediseñada con buscador rápido, filtros mediante menús desplegables (DropdownMenu)
 * y presentación visual de tickets troquelados con códigos QR.
 */
@Composable
fun WalletTicketsScreen(
    usuario: Usuario,
    solicitudVm: SolicitudViewModel,
    alIrADetalle: (Int) -> Unit,
    alIrAPlanes: () -> Unit = {}
) {
    var textoBusqueda by remember { mutableStateOf("") }
    var filtroEstado by remember { mutableStateOf("ACTIVAS") } // "ACTIVAS", "TODAS", "ATRASADAS", o EstadoSolicitud.name
    var filtroPrioridad by remember { mutableStateOf<PrioridadSolicitud?>(null) }
    var filtroCategoria by remember { mutableStateOf<CategoriaSolicitud?>(null) }

    // Estados para controlar los menús desplegables de filtro
    var menuEstadoAbierto by remember { mutableStateOf(false) }
    var menuPrioridadAbierto by remember { mutableStateOf(false) }
    var menuCategoriaAbierto by remember { mutableStateOf(false) }

    var mostrarQrScanner by remember { mutableStateOf(false) }
    var qrNoEncontradoCodigo by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(usuario.id) {
        solicitudVm.cargarSolicitudes(usuario)
    }

    // Filtrado reactivo de pases en la billetera
    val listaFiltrada = remember(
        solicitudVm.listaSolicitudes,
        textoBusqueda,
        filtroEstado,
        filtroPrioridad,
        filtroCategoria
    ) {
        solicitudVm.listaSolicitudes.filter { sol ->
            // Filtro por texto
            val coincideTexto = textoBusqueda.isBlank() ||
                    sol.codigoSeguimiento.contains(textoBusqueda, ignoreCase = true) ||
                    sol.titulo.contains(textoBusqueda, ignoreCase = true)

            // Filtro por estado o vigencia
            val coincideEstado = when (filtroEstado) {
                "ACTIVAS" -> sol.estado != EstadoSolicitud.CERRADA && sol.estado != EstadoSolicitud.CANCELADA
                "TODAS" -> true
                "ATRASADAS" -> {
                    val ahora = System.currentTimeMillis()
                    sol.fechaLimite != null && sol.fechaLimite < ahora &&
                            sol.estado != EstadoSolicitud.RESUELTA &&
                            sol.estado != EstadoSolicitud.CERRADA &&
                            sol.estado != EstadoSolicitud.CANCELADA
                }
                else -> sol.estado.name == filtroEstado
            }

            // Filtro por prioridad
            val coincidePrio = filtroPrioridad == null || sol.prioridad == filtroPrioridad

            // Filtro por categoría
            val coincideCat = filtroCategoria == null || sol.categoria == filtroCategoria

            coincideTexto && coincideEstado && coincidePrio && coincideCat
        }
    }

    val hayFiltroActivo = filtroEstado != "ACTIVAS" ||
            filtroPrioridad != null ||
            filtroCategoria != null ||
            textoBusqueda.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 1. ENCABEZADO APPLE WALLET
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🎟️ Pases Digitales",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Surface(
                        color = TeamworkBlue.copy(alpha = 0.12f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${listaFiltrada.size} pases",
                            style = MaterialTheme.typography.labelSmall,
                            color = TeamworkBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Billetera de credenciales y pases con QR GSM",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Botón de Escáner rápido de Pase con CameraX
            FilledTonalButton(
                onClick = { mostrarQrScanner = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color.White,
                    contentColor = TeamworkBlue
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 1.dp)
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Escanear", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // BANNER DE PLAN DE SOPORTE ENTERPRISE (FLOW CHILE)
        Surface(
            onClick = alIrAPlanes,
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F172A),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "GSM Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "NIVEL PRO (TIER 2)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.8.sp
                            )
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981).copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "SLA < 4H",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Planes de Soporte y Pasarela Flow Chile ›",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Ver planes",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. BARRA UNIFICADA: BUSCADOR + FILTROS DESPLEGABLES (DROPDOWN MENUS)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Buscador de pases
                TextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    placeholder = {
                        Text(
                            text = "Buscar por código o asunto...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8E8E93)
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (textoBusqueda.isNotEmpty()) {
                            IconButton(onClick = { textoBusqueda = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Limpiar", modifier = Modifier.size(16.dp))
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
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = AppleBackground)

                // FILTROS DESPLEGABLES
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // A. MENÚ DESPLEGABLE: ESTADO / VIGENCIA
                    Box {
                        val estadoActivo = filtroEstado != "ACTIVAS"
                        val etiquetaEstado = when (filtroEstado) {
                            "ACTIVAS" -> "⚡ Pases Activos"
                            "TODAS" -> "📚 Historial Completo"
                            "ATRASADAS" -> "⚠️ Solo Atrasadas"
                            else -> filtroEstado.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                        }

                        Surface(
                            onClick = { menuEstadoAbierto = true },
                            shape = RoundedCornerShape(10.dp),
                            color = if (estadoActivo) TeamworkBlue.copy(alpha = 0.10f) else Color(0xFFF2F2F7),
                            border = BorderStroke(
                                1.dp,
                                if (estadoActivo) TeamworkBlue else Color(0xFFE5E5EA)
                            ),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                                text = { Text("⚡ Pases Activos (En atención)", fontWeight = if (filtroEstado == "ACTIVAS") FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    filtroEstado = "ACTIVAS"
                                    menuEstadoAbierto = false
                                },
                                trailingIcon = if (filtroEstado == "ACTIVAS") {
                                    { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                            DropdownMenuItem(
                                text = { Text("📚 Historial Completo (Todos)", fontWeight = if (filtroEstado == "TODAS") FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    filtroEstado = "TODAS"
                                    menuEstadoAbierto = false
                                },
                                trailingIcon = if (filtroEstado == "TODAS") {
                                    { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                            DropdownMenuItem(
                                text = { Text("⚠️ Solo Atrasadas (SLA vencido)", fontWeight = if (filtroEstado == "ATRASADAS") FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    filtroEstado = "ATRASADAS"
                                    menuEstadoAbierto = false
                                },
                                trailingIcon = if (filtroEstado == "ATRASADAS") {
                                    { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            EstadoSolicitud.entries.forEach { est ->
                                val seleccionado = filtroEstado == est.name
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = est.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        filtroEstado = est.name
                                        menuEstadoAbierto = false
                                    },
                                    trailingIcon = if (seleccionado) {
                                        { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    // B. MENÚ DESPLEGABLE: PRIORIDAD (SLA)
                    Box {
                        val prioActiva = filtroPrioridad != null
                        val etiquetaPrio = if (prioActiva) "Prioridad: ${filtroPrioridad!!.name}" else "Prioridad: Todas"

                        Surface(
                            onClick = { menuPrioridadAbierto = true },
                            shape = RoundedCornerShape(10.dp),
                            color = if (prioActiva) TeamworkBlue.copy(alpha = 0.10f) else Color(0xFFF2F2F7),
                            border = BorderStroke(
                                1.dp,
                                if (prioActiva) TeamworkBlue else Color(0xFFE5E5EA)
                            ),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = etiquetaPrio,
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
                                text = { Text("Todas las prioridades", fontWeight = if (filtroPrioridad == null) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    filtroPrioridad = null
                                    menuPrioridadAbierto = false
                                },
                                trailingIcon = if (filtroPrioridad == null) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            PrioridadSolicitud.entries.forEach { prio ->
                                val seleccionada = filtroPrioridad == prio
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
                                        filtroPrioridad = prio
                                        menuPrioridadAbierto = false
                                    },
                                    trailingIcon = if (seleccionada) {
                                        { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    // C. MENÚ DESPLEGABLE: CATEGORÍA
                    Box {
                        val catActiva = filtroCategoria != null
                        val etiquetaCat = if (catActiva) "🏷️ ${filtroCategoria!!.descripcion}" else "Categoría: Todas"

                        Surface(
                            onClick = { menuCategoriaAbierto = true },
                            shape = RoundedCornerShape(10.dp),
                            color = if (catActiva) TeamworkBlue.copy(alpha = 0.10f) else Color(0xFFF2F2F7),
                            border = BorderStroke(
                                1.dp,
                                if (catActiva) TeamworkBlue else Color(0xFFE5E5EA)
                            ),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = etiquetaCat,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (catActiva) FontWeight.Bold else FontWeight.Medium,
                                    color = if (catActiva) TeamworkBlue else Color(0xFF1C1C1E),
                                    maxLines = 1
                                )
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                    tint = if (catActiva) TeamworkBlue else Color(0xFF8E8E93),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = menuCategoriaAbierto,
                            onDismissRequest = { menuCategoriaAbierto = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas las categorías", fontWeight = if (filtroCategoria == null) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    filtroCategoria = null
                                    menuCategoriaAbierto = false
                                },
                                trailingIcon = if (filtroCategoria == null) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            CategoriaSolicitud.entries.forEach { cat ->
                                val seleccionada = filtroCategoria == cat
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = cat.descripcion,
                                            fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        filtroCategoria = cat
                                        menuCategoriaAbierto = false
                                    },
                                    trailingIcon = if (seleccionada) {
                                        { Icon(Icons.Filled.Check, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    // D. BOTÓN LIMPIAR FILTROS
                    if (hayFiltroActivo) {
                        Surface(
                            onClick = {
                                textoBusqueda = ""
                                filtroEstado = "ACTIVAS"
                                filtroPrioridad = null
                                filtroCategoria = null
                            },
                            shape = CircleShape,
                            color = Color(0xFFFFECEB),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Limpiar filtros",
                                    tint = Color(0xFFD92D20),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Restablecer",
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

        // 3. LISTADO DE PASES DIGITALES, SKELETONS O ESTADO VACÍO
        if (solicitudVm.cargando && listaFiltrada.isEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(2) {
                    SkeletonPaseWallet()
                }
            }
        } else if (listaFiltrada.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AppleBackground,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🎟️", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sin Pases en tu Billetera",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (hayFiltroActivo)
                            "No hay pases que coincidan con los filtros seleccionados."
                        else
                            "No tienes solicitudes registradas en este momento.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8E8E93),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    if (hayFiltroActivo) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = {
                                textoBusqueda = ""
                                filtroEstado = "ACTIVAS"
                                filtroPrioridad = null
                                filtroCategoria = null
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Restablecer a Pases Activos", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(
                    items = listaFiltrada,
                    key = { it.id }
                ) { sol ->
                    TarjetaWalletTicket(
                        solicitud = sol,
                        alVerDetalle = { alIrADetalle(sol.id) }
                    )
                }
            }
        }
    }

    // Modal de Escáner CameraX invocado desde la Wallet
    if (mostrarQrScanner) {
        QrScannerModal(
            onDismiss = { mostrarQrScanner = false },
            onQrDetectado = { codigoDetectado ->
                mostrarQrScanner = false
                solicitudVm.buscarPorQr(
                    codigoQr = codigoDetectado,
                    alEncontrar = { idTicket ->
                        alIrADetalle(idTicket)
                    },
                    alFallar = {
                        qrNoEncontradoCodigo = codigoDetectado
                    }
                )
            }
        )
    }

    // Modal si el QR no coincide con ningún ticket existente
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
