package com.example.gsm.ui.view.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.viewmodel.AdminDashboardViewModel

// Paleta Apple + Teamwork
private val AppleBackground = Color(0xFFF2F2F7)
private val TeamworkBlue = Color(0xFF0066FF)
private val AlertRedBg = Color(0xFFFFECEB)
private val AlertRedText = Color(0xFFD92D20)
private val SuccessGreenBg = Color(0xFFE8F8EE)
private val SuccessGreenText = Color(0xFF1B7F37)

@Composable
private fun SeccionTitulo(texto: String) {
    Text(
        text = texto.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
private fun FilaMetrica(
    etiqueta: String,
    valor: Int,
    colorBadge: Color = AppleBackground,
    colorTextoBadge: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Surface(
            color = colorBadge,
            shape = CircleShape
        ) {
            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = colorTextoBadge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun TarjetaKpiPrioridad(
    titulo: String,
    sla: String,
    cantidad: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = cantidad.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TeamworkBlue
            )
            Text(
                text = "SLA: $sla",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AdminDashboardScreen(
    dashboardVm: AdminDashboardViewModel,
    alVolver: () -> Unit
) {
    val hayAtrasadas = dashboardVm.atrasadas > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        EncabezadoPantalla(
            titulo = "Panel de Métricas",
            alHacerClicDerecho = alVolver
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 1. MONITOR DE SLA (Rojo si hay vencidas, Verde si está al día)
        SeccionTitulo("Estado de Cumplimiento SLA")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (hayAtrasadas) AlertRedBg else SuccessGreenBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (hayAtrasadas) "⚠️ Tickets Fuera de Plazo" else "✅ SLA al Día",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (hayAtrasadas) AlertRedText else SuccessGreenText
                    )
                    Text(
                        text = if (hayAtrasadas) "Requieren atención inmediata" else "Ninguna solicitud con plazo vencido",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hayAtrasadas) AlertRedText else SuccessGreenText
                    )
                }
                Text(
                    text = "${dashboardVm.atrasadas}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (hayAtrasadas) AlertRedText else SuccessGreenText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. WIDGETS DE PRIORIDAD (Cuadrícula 2x2 estilo Apple Health / Teamwork)
        SeccionTitulo("Volumen por Prioridad")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TarjetaKpiPrioridad(
                    titulo = "🔴 Urgente",
                    sla = "6 horas",
                    cantidad = dashboardVm.prioridadUrgente,
                    modifier = Modifier.weight(1f)
                )
                TarjetaKpiPrioridad(
                    titulo = "🟠 Alta",
                    sla = "24 horas",
                    cantidad = dashboardVm.prioridadAlta,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TarjetaKpiPrioridad(
                    titulo = "🟡 Media",
                    sla = "72 horas",
                    cantidad = dashboardVm.prioridadMedia,
                    modifier = Modifier.weight(1f)
                )
                TarjetaKpiPrioridad(
                    titulo = "🟢 Baja",
                    sla = "168 horas",
                    cantidad = dashboardVm.prioridadBaja,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. FLUJO DE ESTADOS (Lista agrupada estilo iOS Settings)
        SeccionTitulo("Distribución por Estado")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                FilaMetrica("📥 Recibidas", dashboardVm.recibidas)
                HorizontalDivider(color = AppleBackground)
                FilaMetrica("🔍 En Revisión", dashboardVm.enRevision)
                HorizontalDivider(color = AppleBackground)
                FilaMetrica("👤 Asignadas", dashboardVm.asignadas)
                HorizontalDivider(color = AppleBackground)
                FilaMetrica(
                    etiqueta = "🔧 En Proceso",
                    valor = dashboardVm.enProceso,
                    colorBadge = TeamworkBlue.copy(alpha = 0.12f),
                    colorTextoBadge = TeamworkBlue
                )
                HorizontalDivider(color = AppleBackground)
                FilaMetrica("❓ Pendientes de Info", dashboardVm.pendientesInfo)
                HorizontalDivider(color = AppleBackground)
                FilaMetrica(
                    etiqueta = "✅ Resueltas",
                    valor = dashboardVm.resueltas,
                    colorBadge = SuccessGreenBg,
                    colorTextoBadge = SuccessGreenText
                )
                HorizontalDivider(color = AppleBackground)
                FilaMetrica("🔒 Cerradas", dashboardVm.cerradas)
                HorizontalDivider(color = AppleBackground)
                FilaMetrica("🚫 Canceladas", dashboardVm.canceladas)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. CARGA DE TRABAJO POR TÉCNICO (Teamwork Workload)
        SeccionTitulo("Carga por Responsable Técnico")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                if (dashboardVm.porTecnico.isEmpty()) {
                    Text(
                        text = "Aún no hay tickets asignados al equipo técnico.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    dashboardVm.porTecnico.forEachIndexed { index, dto ->
                        FilaMetrica(
                            etiqueta = "👨‍💻 ${dto.nombreTecnico ?: "Sin asignar"}",
                            valor = dto.totalSolicitudes,
                            colorBadge = TeamworkBlue.copy(alpha = 0.12f),
                            colorTextoBadge = TeamworkBlue
                        )
                        if (index < dashboardVm.porTecnico.lastIndex) {
                            HorizontalDivider(color = AppleBackground)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}