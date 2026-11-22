package com.example.gsm.ui.view.screens.usuario

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.PlanSoporte
import com.example.gsm.data.model.Usuario
import com.example.gsm.data.repository.FlowPaymentRepository
import com.example.gsm.ui.view.components.PantallaExitoCompleta
import com.example.gsm.ui.view.screens.usuario.componentes.AppleBackground
import com.example.gsm.ui.view.screens.usuario.componentes.MetodoPagoPill
import com.example.gsm.ui.view.screens.usuario.componentes.ModalCheckoutFlow
import com.example.gsm.ui.view.screens.usuario.componentes.TarjetaPlanItem
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de Planes de Soporte GSM con integración visual de pasarela Flow (Chile).
 * Permite comparar niveles de servicio (PLUS, PRO, MAX) y simular el flujo de pago con Flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanesSoporteScreen(
    usuario: Usuario,
    authVm: com.example.gsm.ui.viewmodel.AuthViewModel? = null,
    esModoOnboarding: Boolean = false,
    alCompletarOnboarding: () -> Unit = {},
    alVolver: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val flowRepository = remember { FlowPaymentRepository() }

    var planSeleccionado by remember { mutableStateOf(PlanSoporte.PRO) }
    var tienePlanActivo by remember(usuario.estadoSuscripcion, usuario.plan) {
        mutableStateOf(
            usuario.estadoSuscripcion.equals("ACTIVA", ignoreCase = true) && !usuario.plan.equals("SIN_PLAN", ignoreCase = true)
        )
    }
    var planActivoUsuario by remember(usuario.plan) {
        mutableStateOf(
            PlanSoporte.entries.find { it.name.equals(usuario.plan, ignoreCase = true) } ?: PlanSoporte.PRO
        )
    }
    var esFacturacionAnual by remember { mutableStateOf(false) }
    var mostrarModalFlow by remember { mutableStateOf(false) }
    var mensajeExitoFlow by remember { mutableStateOf<String?>(null) }
    var metodoPagoSeleccionado by remember { mutableStateOf("WEBPAY") }
    var cargandoPagoFlow by remember { mutableStateOf(false) }
    var tokenOrdenActual by remember { mutableStateOf<String?>(null) }
    var mostrarExitoPantallaCompleta by remember { mutableStateOf(false) }

    LaunchedEffect(mostrarExitoPantallaCompleta) {
        if (mostrarExitoPantallaCompleta) {
            delay(1150)
            mostrarExitoPantallaCompleta = false
            if (esModoOnboarding) alCompletarOnboarding()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Planes de Soporte GSM",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Niveles de SLA y pasarela Flow Chile",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = if (esModoOnboarding) alCompletarOnboarding else alVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    if (esModoOnboarding) {
                        TextButton(onClick = alCompletarOnboarding) {
                            Text("Omitir", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = AppleBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. BANNER DEL PLAN ACTUAL CONTRATADO
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (tienePlanActivo) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(planActivoUsuario.colorHex).copy(alpha = 0.25f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Shield,
                                            contentDescription = null,
                                            tint = Color(planActivoUsuario.colorHex),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "TU PLAN ACTUAL",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "${planActivoUsuario.nombre} (${planActivoUsuario.nivel})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF10B981), CircleShape)
                                    )
                                    Text(
                                        text = "ACTIVO",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF34D399),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFF1E293B))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "SLA de Respuesta",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = planActivoUsuario.slaRespuesta,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Cupo Mensual",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = planActivoUsuario.limiteTicketsTexto,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Facturación",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "Flow Chile",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF60A5FA)
                                )
                            }
                        }
                    } else {
                        // Usuario nuevo sin suscripción activa
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF59E0B).copy(alpha = 0.20f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Info,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "ESTADO DE LA CUENTA",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Sin Suscripción Activa",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "INACTIVO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = "Contrata un plan de soporte a continuación para activar tu cuota de tickets y tiempos de respuesta SLA con Flow.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Notificación de éxito si se simuló el pago
            val haptic = LocalHapticFeedback.current
            LaunchedEffect(mensajeExitoFlow) {
                if (mensajeExitoFlow != null) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            val escalaCheckFlow by animateFloatAsState(
                targetValue = if (mensajeExitoFlow != null) 1f else 0.6f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
                label = "escalaCheckFlow"
            )
            AnimatedVisibility(
                visible = mensajeExitoFlow != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFDCFCE7),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF15803D),
                                modifier = Modifier
                                    .size(22.dp)
                                    .graphicsLayer {
                                        scaleX = escalaCheckFlow
                                        scaleY = escalaCheckFlow
                                    }
                            )
                            Text(
                                text = mensajeExitoFlow ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF166534),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { mensajeExitoFlow = null },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color(0xFF15803D))
                            }
                        }

                        if (esModoOnboarding) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = alCompletarOnboarding,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Comenzar a usar GSM Soporte", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // 2. CONMUTADOR MENSUAL / ANUAL
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Surface(
                        onClick = { esFacturacionAnual = false },
                        shape = RoundedCornerShape(10.dp),
                        color = if (!esFacturacionAnual) TeamworkBlue else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "Facturación Mensual",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (!esFacturacionAnual) Color.White else Color(0xFF64748B)
                            )
                        }
                    }

                    Surface(
                        onClick = { esFacturacionAnual = true },
                        shape = RoundedCornerShape(10.dp),
                        color = if (esFacturacionAnual) TeamworkBlue else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "Anual",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (esFacturacionAnual) Color.White else Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (esFacturacionAnual) Color(0xFF10B981) else Color(0xFFE2E8F0)
                            ) {
                                Text(
                                    text = "-20% OFF",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (esFacturacionAnual) Color.White else Color(0xFF0F172A),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. TARJETAS DE LOS 3 PLANES (PLUS, PRO, MAX)
            Text(
                text = "ELIGE EL NIVEL DE ATENCIÓN ADECUADO",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            PlanSoporte.entries.forEach { plan ->
                val esSeleccionado = plan == planSeleccionado
                val esElActivo = plan == planActivoUsuario
                val precioCalculado = if (esFacturacionAnual) {
                    val conDescuento = (plan.precioMensualClp * 0.80).toInt()
                    "$" + String.format("%,d", conDescuento).replace(',', '.') + " CLP / mes"
                } else {
                    plan.precioTexto
                }

                TarjetaPlanItem(
                    plan = plan,
                    precioDisplay = precioCalculado,
                    esSeleccionado = esSeleccionado,
                    esActivo = esElActivo,
                    onSelect = { planSeleccionado = plan },
                    onContratarFlow = {
                        planSeleccionado = plan
                        mostrarModalFlow = true
                    }
                )
            }

            // 4. PASARELA DE PAGO FLOW - MEDIOS ACEPTADOS
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = TeamworkBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Pasarela de Pago Flow (Chile)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Transacciones en pesos chilenos protegidas con SSL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Pills de métodos disponibles en Flow Chile
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetodoPagoPill(nombre = "💳 Webpay Plus", modifier = Modifier.weight(1f))
                        MetodoPagoPill(nombre = "⚡ MACH", modifier = Modifier.weight(1f))
                        MetodoPagoPill(nombre = "🏦 Khipu", modifier = Modifier.weight(1f))
                        MetodoPagoPill(nombre = "🧾 Servipag", modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // MODAL DE CHECKOUT FLOW (extraído a PlanesSoporteComponentes)
    if (mostrarModalFlow) {
        val plan = planSeleccionado
        ModalCheckoutFlow(
            plan = plan,
            usuario = usuario,
            esFacturacionAnual = esFacturacionAnual,
            metodoPagoSeleccionado = metodoPagoSeleccionado,
            onMetodoPagoChange = { metodoPagoSeleccionado = it },
            cargandoPagoFlow = cargandoPagoFlow,
            onPagarClick = { _ ->
                coroutineScope.launch {
                    cargandoPagoFlow = true
                    val resultado = flowRepository.crearOrdenPago(
                        plan = plan,
                        usuario = usuario,
                        esFacturacionAnual = esFacturacionAnual
                    )
                    cargandoPagoFlow = false
                    mostrarModalFlow = false

                    resultado.onSuccess { orden ->
                        tokenOrdenActual = orden.token
                        planActivoUsuario = plan
                        tienePlanActivo = true
                        authVm?.actualizarPlanUsuario(plan.name, plan.cuotaTicketsSemanales)
                        mensajeExitoFlow = "¡Orden Flow iniciada! Plan ${plan.nombre} activo con ${plan.limiteTicketsTexto} y SLA ${plan.slaRespuesta}."
                        mostrarExitoPantallaCompleta = true

                        val urlPago = if (orden.url.contains("token=")) orden.url else "${orden.url}?token=${orden.token}"
                        try {
                            CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(urlPago))
                        } catch (_: Exception) {
                        }
                    }.onFailure {
                        planActivoUsuario = plan
                        authVm?.actualizarPlanUsuario(plan.name, plan.cuotaTicketsSemanales)
                        mensajeExitoFlow = "¡Suscripción confirmada! Plan ${plan.nombre} activado exitosamente."
                        mostrarExitoPantallaCompleta = true
                    }
                }
            },
            onDismiss = { mostrarModalFlow = false }
        )
    }

    if (mostrarExitoPantallaCompleta) {
        PantallaExitoCompleta(
            titulo = "¡Plan ${planActivoUsuario.nombre} activo!",
            subtitulo = "SLA ${planActivoUsuario.slaRespuesta} • ${planActivoUsuario.limiteTicketsTexto}"
        )
    }
}
