package com.example.gsm.ui.view.screens.admin.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.ui.view.screens.usuario.componentes.AlertRedBg
import com.example.gsm.ui.view.screens.usuario.componentes.AlertRedText
import com.example.gsm.ui.view.screens.usuario.componentes.SeccionTitulo
import com.example.gsm.ui.view.screens.usuario.componentes.SuccessGreenBg
import com.example.gsm.ui.view.screens.usuario.componentes.SuccessGreenText

/**
 * Monitor visual de cumplimiento del SLA.
 * Muestra alerta roja si hay tickets vencidos o verde si todas las solicitudes están al día.
 */
@Composable
fun MonitorSlaCard(atrasadas: Int) {
    val hayAtrasadas = atrasadas > 0

    Column(modifier = Modifier.fillMaxWidth()) {
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
                    text = "$atrasadas",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (hayAtrasadas) AlertRedText else SuccessGreenText
                )
            }
        }
    }
}
