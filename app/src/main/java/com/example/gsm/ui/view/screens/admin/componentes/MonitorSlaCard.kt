package com.example.gsm.ui.view.screens.admin.componentes

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
 * Monitor visual de cumplimiento del SLA (accionable al tocar cuando hay atrasadas).
 */
@Composable
fun MonitorSlaCard(
    atrasadas: Int,
    onClick: () -> Unit = {}
) {
    val hayAtrasadas = atrasadas > 0
    val atrasadasAnim by animateIntAsState(
        targetValue = atrasadas,
        animationSpec = tween(300),
        label = "slaAtrasadas"
    )
    val colorTexto = if (hayAtrasadas) AlertRedText else SuccessGreenText

    Column(modifier = Modifier.fillMaxWidth()) {
        SeccionTitulo("Estado de Cumplimiento SLA")
        Surface(
            onClick = onClick,
            enabled = hayAtrasadas,
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
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (hayAtrasadas) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = colorTexto,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = if (hayAtrasadas) "Tickets Fuera de Plazo" else "SLA al Día",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorTexto
                        )
                        Text(
                            text = if (hayAtrasadas) "Toca para revisar los vencidos" else "Ninguna solicitud con plazo vencido",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorTexto
                        )
                    }
                }
                Text(
                    text = "$atrasadasAnim",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto
                )
            }
        }
    }
}
