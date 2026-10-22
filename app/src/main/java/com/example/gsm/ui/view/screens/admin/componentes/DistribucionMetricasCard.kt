package com.example.gsm.ui.view.screens.admin.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/**
 * Tabla de métricas por estado estilo Inset Grouped de iOS Settings.
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
    Column(modifier = Modifier.fillMaxWidth()) {
        SeccionTitulo("Distribución por Estado")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                FilaMetrica("📥 Recibidas", recibidas)
                HorizontalDivider(color = AppleBackground)
                FilaMetrica("🔍 En Revisión", enRevision)
                HorizontalDivider(color = AppleBackground)
                FilaMetrica("👤 Asignadas", asignadas)
                HorizontalDivider(color = AppleBackground)
                FilaMetrica(
                    etiqueta = "🔧 En Proceso",
                    valor = enProceso,
                    colorBadge = TeamworkBlue.copy(alpha = 0.12f),
                    colorTextoBadge = TeamworkBlue
                )
                HorizontalDivider(color = AppleBackground)
                FilaMetrica("❓ Pendientes de Info", pendientesInfo)
                HorizontalDivider(color = AppleBackground)
                FilaMetrica(
                    etiqueta = "✅ Resueltas",
                    valor = resueltas,
                    colorBadge = SuccessGreenBg,
                    colorTextoBadge = SuccessGreenText
                )
                HorizontalDivider(color = AppleBackground)
                FilaMetrica("🔒 Cerradas", cerradas)
                HorizontalDivider(color = AppleBackground)
                FilaMetrica("🚫 Canceladas", canceladas)
            }
        }
    }
}

/**
 * Tabla de carga de trabajo distribuida por técnico responsable (Workload).
 */
@Composable
fun CargaTecnicosCard(porTecnico: List<ConteoPorTecnicoDto>) {
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
                            etiqueta = "👨‍💻 ${dto.nombreTecnico ?: "Sin asignar"}",
                            valor = dto.totalSolicitudes,
                            colorBadge = TeamworkBlue.copy(alpha = 0.12f),
                            colorTextoBadge = TeamworkBlue
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
