package com.example.gsm.ui.view.screens.usuario

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.PlanSoporte
import com.example.gsm.data.model.Usuario
import com.example.gsm.data.repository.FlowPaymentRepository
import com.example.gsm.ui.view.screens.usuario.componentes.AppleBackground
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import kotlinx.coroutines.launch

/**
 * Pantalla de Planes de Soporte GSM con integración visual de pasarela Flow (Chile).
 * Permite comparar niveles de servicio (PLUS, PRO, MAX) y simular el flujo de pago con Flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanesSoporteScreen(
    usuario: Usuario,
    alVolver: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val flowRepository = remember { FlowPaymentRepository() }

    var planSeleccionado by remember { mutableStateOf(PlanSoporte.PRO) }
    var planActivoUsuario by remember { mutableStateOf(PlanSoporte.PRO) }
    var esFacturacionAnual by remember { mutableStateOf(false) }
    var mostrarModalFlow by remember { mutableStateOf(false) }
    var mensajeExitoFlow by remember { mutableStateOf<String?>(null) }
    var metodoPagoSeleccionado by remember { mutableStateOf("WEBPAY") }
    var cargandoPagoFlow by remember { mutableStateOf(false) }
    var tokenOrdenActual by remember { mutableStateOf<String?>(null) }

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
                    IconButton(onClick = alVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF0F172A)
                        )
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
                }
            }

            // Notificación de éxito si se simuló el pago
            AnimatedVisibility(visible = mensajeExitoFlow != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFDCFCE7),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(22.dp)
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

    // MODAL DE SIMULACIÓN DE CHECKOUT FLOW
    if (mostrarModalFlow) {
        val plan = planSeleccionado
        val montoTotal = if (esFacturacionAnual) {
            (plan.precioMensualClp * 12 * 0.80).toInt()
        } else {
            plan.precioMensualClp
        }
        val montoTotalTexto = "$" + String.format("%,d", montoTotal).replace(',', '.') + " CLP"

        AlertDialog(
            onDismissRequest = { mostrarModalFlow = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0066FF).copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Payment,
                                contentDescription = null,
                                tint = TeamworkBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Checkout Seguro Flow",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mesa de Ayuda GSM • Suscripción SLA",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Resumen de la orden
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Plan seleccionado:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "${plan.nombre} (${plan.nivel})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(plan.colorHex)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "SLA de Respuesta:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = plan.slaRespuesta,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Facturación:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = if (esFacturacionAnual) "Anual (20% OFF aplicado)" else "Mensual recurrente",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            HorizontalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total a Pagar:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = montoTotalTexto,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TeamworkBlue
                                )
                            }
                        }
                    }

                    // Selección de medio de pago Flow
                    Text(
                        text = "Selecciona medio de pago en Flow:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )

                    listOf(
                        "WEBPAY" to "💳 Webpay Plus (Crédito y Débito)",
                        "MACH" to "⚡ MACH Pagos",
                        "KHIPU" to "🏦 Khipu (Transferencia en línea)",
                        "SERVIPAG" to "🧾 Servipag"
                    ).forEach { (id, label) ->
                        Surface(
                            onClick = { metodoPagoSeleccionado = id },
                            shape = RoundedCornerShape(10.dp),
                            color = if (metodoPagoSeleccionado == id) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                1.dp,
                                if (metodoPagoSeleccionado == id) TeamworkBlue else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (metodoPagoSeleccionado == id) FontWeight.Bold else FontWeight.Normal,
                                    color = if (metodoPagoSeleccionado == id) TeamworkBlue else Color(0xFF1E293B)
                                )
                                RadioButton(
                                    selected = metodoPagoSeleccionado == id,
                                    onClick = { metodoPagoSeleccionado = id },
                                    colors = RadioButtonDefaults.colors(selectedColor = TeamworkBlue)
                                )
                            }
                        }
                    }

                    Text(
                        text = "🔒 Serás redirigido de forma simulada al ambiente de pago protegido de Flow.cl para ${usuario.correo}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
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
                                mensajeExitoFlow = "¡Orden Flow iniciada! Plan ${plan.nombre} activo con ${plan.limiteTicketsTexto} y SLA ${plan.slaRespuesta}."

                                // Abrimos pasarela segura oficial de Flow / Webpay Sandbox
                                val urlPago = if (orden.url.contains("token=")) orden.url else "${orden.url}?token=${orden.token}"
                                try {
                                    val customTabsIntent = CustomTabsIntent.Builder().build()
                                    customTabsIntent.launchUrl(context, Uri.parse(urlPago))
                                } catch (e: Exception) {
                                    // Fallback si no hay navegador compatible instalado
                                }
                            }.onFailure {
                                planActivoUsuario = plan
                                mensajeExitoFlow = "¡Suscripción confirmada! Plan ${plan.nombre} activado exitosamente."
                            }
                        }
                    },
                    enabled = !cargandoPagoFlow,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (cargandoPagoFlow) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Conectando con Flow...", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pagar $montoTotalTexto con Flow",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarModalFlow = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar", color = Color(0xFF64748B))
                }
            }
        )
    }
}

/**
 * Tarjeta individual para presentar un plan de soporte.
 */
@Composable
private fun TarjetaPlanItem(
    plan: PlanSoporte,
    precioDisplay: String,
    esSeleccionado: Boolean,
    esActivo: Boolean,
    onSelect: () -> Unit,
    onContratarFlow: () -> Unit
) {
    val colorBorde = if (esActivo) {
        Color(0xFF10B981)
    } else if (esSeleccionado) {
        TeamworkBlue
    } else {
        Color(0xFFE2E8F0)
    }

    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(if (esSeleccionado || esActivo) 2.dp else 1.dp, colorBorde),
        shadowElevation = if (esSeleccionado) 3.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cabecera del plan con badge y color de acento
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(plan.colorHex).copy(alpha = 0.12f),
                                Color.White
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
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
                            shape = CircleShape,
                            color = Color(plan.colorHex),
                            modifier = Modifier.size(10.dp)
                        ) {}
                        Text(
                            text = "${plan.nombre} • ${plan.nivel}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (esActivo) Color(0xFFDCFCE7) else Color(plan.colorHex).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (esActivo) "✓ PLAN ACTUAL" else plan.insignia,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (esActivo) Color(0xFF166534) else Color(plan.colorHex),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Cuerpo del plan: Precio y SLA
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = precioDisplay,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = plan.limiteTicketsTexto,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "SLA GARANTIZADO",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = plan.slaRespuesta,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Black,
                                color = Color(plan.colorHex)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Lista de características clave
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    plan.beneficiosClave.forEach { beneficio ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color(plan.colorHex),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = beneficio,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF334155),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Canales de atención
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    plan.canales.forEach { canal ->
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = canal,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF475569),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Botón de Contratación con Flow
                if (esActivo) {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Plan Actualmente Activo", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onContratarFlow,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (plan.esRecomendado) TeamworkBlue else Color(0xFF0F172A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Contratar con Flow ›",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetodoPagoPill(nombre: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
