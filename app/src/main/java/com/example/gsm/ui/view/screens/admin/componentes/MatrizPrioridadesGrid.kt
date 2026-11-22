package com.example.gsm.ui.view.screens.admin.componentes

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.ui.view.screens.usuario.componentes.SeccionTitulo
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue

@Composable
private fun TarjetaKpiPrioridad(
    titulo: String,
    colorPunto: Color,
    sla: String,
    cantidad: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cantidadAnim by animateIntAsState(
        targetValue = cantidad,
        animationSpec = tween(300),
        label = "kpi$titulo"
    )
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(colorPunto, CircleShape)
                )
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = cantidadAnim.toString(),
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
 * Cuadrícula 2x2 accionable que clasifica el volumen de tickets por prioridad con su respectivo SLA.
 */
@Composable
fun MatrizPrioridadesGrid(
    urgente: Int,
    alta: Int,
    media: Int,
    baja: Int,
    onSeleccionarPrioridad: (PrioridadSolicitud) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SeccionTitulo("Volumen por Prioridad")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TarjetaKpiPrioridad(
                    titulo = "Urgente",
                    colorPunto = Color(0xFFD92D20),
                    sla = "6 horas",
                    cantidad = urgente,
                    onClick = { onSeleccionarPrioridad(PrioridadSolicitud.URGENTE) },
                    modifier = Modifier.weight(1f)
                )
                TarjetaKpiPrioridad(
                    titulo = "Alta",
                    colorPunto = Color(0xFFE65100),
                    sla = "24 horas",
                    cantidad = alta,
                    onClick = { onSeleccionarPrioridad(PrioridadSolicitud.ALTA) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TarjetaKpiPrioridad(
                    titulo = "Media",
                    colorPunto = Color(0xFFD97706),
                    sla = "72 horas",
                    cantidad = media,
                    onClick = { onSeleccionarPrioridad(PrioridadSolicitud.MEDIA) },
                    modifier = Modifier.weight(1f)
                )
                TarjetaKpiPrioridad(
                    titulo = "Baja",
                    colorPunto = Color(0xFF16A34A),
                    sla = "168 horas",
                    cantidad = baja,
                    onClick = { onSeleccionarPrioridad(PrioridadSolicitud.BAJA) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
