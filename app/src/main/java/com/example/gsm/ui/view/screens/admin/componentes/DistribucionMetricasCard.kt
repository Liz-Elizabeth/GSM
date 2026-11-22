package com.example.gsm.ui.view.screens.admin.componentes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.data.local.model.ConteoPorTecnicoDto
import com.example.gsm.ui.view.screens.usuario.componentes.AppleBackground
import com.example.gsm.ui.view.screens.usuario.componentes.SeccionTitulo
import com.example.gsm.ui.view.screens.usuario.componentes.SuccessGreenBg
import com.example.gsm.ui.view.screens.usuario.componentes.SuccessGreenText
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue

@Composable
private fun FilaMetrica(
    etiqueta: String,
    valor: Int,
    maxValor: Int,
    colorPunto: Color,
    colorBadge: Color = AppleBackground,
    colorTextoBadge: Color = Color(0xFF1C1C1E),
    onClick: (() -> Unit)? = null
) {
    val proporcion by animateFloatAsState(
        targetValue = if (maxValor > 0) (valor.toFloat() / maxValor.toFloat()).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(320),
        label = "barra$etiqueta"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(colorPunto, CircleShape)
                )
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(AppleBackground)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(proporcion)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(colorPunto.copy(alpha = 0.75f))
            )
        }
    }
}

/**
 * Tabla de métricas por estado con barras proporcionales animadas.
 */
@Composable
fun DistribucionEstadosCard(
    recibidas: Int,
    enRevision: Int,
    asignadas: Int,
    enProceso: Int,
    pendientesInfo: Int,
    resueltas: Int,
    cerradas: Int,
    canceladas: Int
) {
    val maxEstado = maxOf(1, recibidas, enRevision, asignadas, enProceso, pendientesInfo, resueltas, cerradas, canceladas)
    Column(modifier = Modifier.fillMaxWidth()) {
        SeccionTitulo("Distribución por Estado")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                FilaMetrica("Recibidas", recibidas, maxEstado, Color(0xFF64748B))
                HorizontalDivider(color = AppleBackground)
                FilaMetrica("En Revisión", enRevision, maxEstado, Color(0xFF0EA5E9))
                HorizontalDivider(color = AppleBackground)
                FilaMetrica("Asignadas", asignadas, maxEstado, Color(0xFF6366F1))
                HorizontalDivider(color = AppleBackground)
                FilaMetrica(
                    etiqueta = "En Proceso",
                    valor = enProceso,
                    maxValor = maxEstado,
                    colorPunto = TeamworkBlue,
                    colorBadge = TeamworkBlue.copy(alpha = 0.12f),
                    colorTextoBadge = TeamworkBlue
                )
                HorizontalDivider(color = AppleBackground)
                FilaMetrica("Pendientes de Info", pendientesInfo, maxEstado, Color(0xFFF59E0B))
                HorizontalDivider(color = AppleBackground)
                FilaMetrica(
                    etiqueta = "Resueltas",
                    valor = resueltas,
                    maxValor = maxEstado,
                    colorPunto = SuccessGreenText,
                    colorBadge = SuccessGreenBg,
                    colorTextoBadge = SuccessGreenText
                )
                HorizontalDivider(color = AppleBackground)
                FilaMetrica("Cerradas", cerradas, maxEstado, Color(0xFF10B981))
                HorizontalDivider(color = AppleBackground)
                FilaMetrica("Canceladas", canceladas, maxEstado, Color(0xFFD92D20))
            }
        }
    }
}

/**
 * Tabla de carga de trabajo distribuida por técnico responsable (accionable por técnico).
 */
@Composable
fun CargaTecnicosCard(
    porTecnico: List<ConteoPorTecnicoDto>,
    onSeleccionarTecnico: (Int) -> Unit = {}
) {
    val maxCarga = maxOf(1, porTecnico.maxOfOrNull { it.totalSolicitudes } ?: 1)
    Column(modifier = Modifier.fillMaxWidth()) {
        SeccionTitulo("Carga por Responsable Técnico")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                if (porTecnico.isEmpty()) {
                    Text(
                        text = "Aún no hay tickets asignados al equipo técnico.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    porTecnico.forEachIndexed { index, dto ->
                        FilaMetrica(
                            etiqueta = dto.nombreTecnico ?: "Sin asignar",
                            valor = dto.totalSolicitudes,
                            maxValor = maxCarga,
                            colorPunto = TeamworkBlue,
                            colorBadge = TeamworkBlue.copy(alpha = 0.12f),
                            colorTextoBadge = TeamworkBlue,
                            onClick = dto.tecnicoId?.let { id -> { onSeleccionarTecnico(id) } }
                        )
                        if (index < porTecnico.lastIndex) {
                            HorizontalDivider(color = AppleBackground)
                        }
                    }
                }
            }
        }
    }
}
