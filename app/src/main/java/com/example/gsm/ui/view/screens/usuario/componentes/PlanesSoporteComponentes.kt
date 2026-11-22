package com.example.gsm.ui.view.screens.usuario.componentes

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.PlanSoporte
import com.example.gsm.data.model.Usuario

@Composable
fun TarjetaPlanItem(
    plan: PlanSoporte,
    precioDisplay: String,
    esSeleccionado: Boolean,
    esActivo: Boolean,
    onSelect: () -> Unit,
    onContratarFlow: () -> Unit
) {
    val colorBorde = when {
        esActivo -> Color(0xFF10B981)
        esSeleccionado -> TeamworkBlue
        else -> Color(0xFFE2E8F0)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(plan.colorHex).copy(alpha = 0.12f), Color.White)))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = CircleShape, color = Color(plan.colorHex), modifier = Modifier.size(10.dp)) {}
                        Text("${plan.nombre} • ${plan.nivel}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
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

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(precioDisplay, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                        Text(plan.limiteTicketsTexto, style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF8FAFC), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), horizontalAlignment = Alignment.End) {
                            Text("SLA GARANTIZADO", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            Text(plan.slaRespuesta, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, color = Color(plan.colorHex))
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    plan.beneficiosClave.forEach { beneficio ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color(plan.colorHex), modifier = Modifier.size(16.dp))
                            Text(beneficio, style = MaterialTheme.typography.bodySmall, color = Color(0xFF334155), fontSize = 12.sp)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    plan.canales.forEach { canal ->
                        Surface(shape = CircleShape, color = Color(0xFFF1F5F9)) {
                            Text(canal, style = MaterialTheme.typography.labelSmall, color = Color(0xFF475569), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (esActivo) {
                    OutlinedButton(onClick = {}, enabled = false, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Plan Actualmente Activo", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onContratarFlow,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (plan.esRecomendado) TeamworkBlue else Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Contratar con Flow ›", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MetodoPagoPill(nombre: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
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

@Composable
fun ModalCheckoutFlow(
    plan: PlanSoporte,
    usuario: Usuario,
    esFacturacionAnual: Boolean,
    metodoPagoSeleccionado: String,
    onMetodoPagoChange: (String) -> Unit,
    cargandoPagoFlow: Boolean,
    onPagarClick: (montoTotalTexto: String) -> Unit,
    onDismiss: () -> Unit
) {
    val montoTotal = if (esFacturacionAnual) (plan.precioMensualClp * 12 * 0.80).toInt() else plan.precioMensualClp
    val montoTotalTexto = "$" + String.format("%,d", montoTotal).replace(',', '.') + " CLP"

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = CircleShape, color = TeamworkBlue.copy(alpha = 0.12f), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Payment, contentDescription = null, tint = TeamworkBlue, modifier = Modifier.size(20.dp))
                    }
                }
                Column {
                    Text("Checkout Seguro Flow", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Mesa de Ayuda GSM • Suscripción SLA", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF8FAFC), border = BorderStroke(1.dp, Color(0xFFE2E8F0)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Plan seleccionado:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                            Text("${plan.nombre} (${plan.nivel})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(plan.colorHex))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SLA de Respuesta:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                            Text(plan.slaRespuesta, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Facturación:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                            Text(
                                text = if (esFacturacionAnual) "Anual (20% OFF aplicado)" else "Mensual recurrente",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        HorizontalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Total a Pagar:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text(montoTotalTexto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = TeamworkBlue)
                        }
                    }
                }

                Text("Selecciona medio de pago en Flow:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF475569))

                listOf(
                    "WEBPAY" to "💳 Webpay Plus (Crédito y Débito)",
                    "MACH" to "⚡ MACH Pagos",
                    "KHIPU" to "🏦 Khipu (Transferencia en línea)",
                    "SERVIPAG" to "🧾 Servipag"
                ).forEach { (id, label) ->
                    Surface(
                        onClick = { onMetodoPagoChange(id) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (metodoPagoSeleccionado == id) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (metodoPagoSeleccionado == id) TeamworkBlue else Color(0xFFE2E8F0)),
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
                                onClick = { onMetodoPagoChange(id) },
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
                onClick = { onPagarClick(montoTotalTexto) },
                enabled = !cargandoPagoFlow,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (cargandoPagoFlow) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Conectando con Flow...", style = MaterialTheme.typography.bodySmall)
                } else {
                    Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pagar $montoTotalTexto con Flow", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar", color = Color(0xFF64748B))
            }
        }
    )
}
