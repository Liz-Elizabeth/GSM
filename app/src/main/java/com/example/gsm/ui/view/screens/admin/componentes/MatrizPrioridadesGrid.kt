package com.example.gsm.ui.view.screens.admin.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.ui.view.screens.usuario.componentes.SeccionTitulo
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue

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

/**
 * Cuadrícula 2x2 que clasifica el volumen de tickets por prioridad con su respectivo SLA.
 */
@Composable
fun MatrizPrioridadesGrid(
    urgente: Int,
    alta: Int,
    media: Int,
    baja: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SeccionTitulo("Volumen por Prioridad")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TarjetaKpiPrioridad(
                    titulo = "🔴 Urgente",
                    sla = "6 horas",
                    cantidad = urgente,
                    modifier = Modifier.weight(1f)
                )
                TarjetaKpiPrioridad(
                    titulo = "🟠 Alta",
                    sla = "24 horas",
                    cantidad = alta,
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
                    cantidad = media,
                    modifier = Modifier.weight(1f)
                )
                TarjetaKpiPrioridad(
                    titulo = "🟢 Baja",
                    sla = "168 horas",
                    cantidad = baja,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
