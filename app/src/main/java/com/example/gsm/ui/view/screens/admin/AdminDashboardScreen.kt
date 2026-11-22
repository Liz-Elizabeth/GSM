package com.example.gsm.ui.view.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.view.screens.admin.componentes.CargaTecnicosCard
import com.example.gsm.ui.view.screens.admin.componentes.DistribucionEstadosCard
import com.example.gsm.ui.view.screens.admin.componentes.MatrizPrioridadesGrid
import com.example.gsm.ui.view.screens.admin.componentes.MonitorSlaCard
import com.example.gsm.ui.view.screens.usuario.componentes.AppleBackground
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import com.example.gsm.ui.viewmodel.AdminDashboardViewModel
import java.util.Locale

/**
 * Panel de Administración y Métricas con KPIs accionables y refresco en nube.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    dashboardVm: AdminDashboardViewModel,
    alIrAGestionUsuarios: () -> Unit = {},
    alVerAtrasadas: () -> Unit = {},
    alFiltrarPrioridad: (PrioridadSolicitud) -> Unit = {},
    alFiltrarTecnico: (Int) -> Unit = {},
    alVolver: () -> Unit
) {
    LaunchedEffect(Unit) {
        dashboardVm.refrescarTotalRemoto()
    }

    val totalLocal = dashboardVm.recibidas + dashboardVm.enRevision + dashboardVm.asignadas +
        dashboardVm.enProceso + dashboardVm.pendientesInfo + dashboardVm.resueltas +
        dashboardVm.cerradas + dashboardVm.canceladas

    PullToRefreshBox(
        isRefreshing = dashboardVm.refrescando,
        onRefresh = { dashboardVm.refrescarTotalRemoto() },
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .verticalScroll(rememberScrollState())
        ) {
            EncabezadoPantalla(
                titulo = "Panel de Métricas",
                alHacerClicDerecho = alVolver
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Fila compacta de KPIs globales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniKpiCard(
                    etiqueta = "Tickets Nube",
                    valor = dashboardVm.totalRemoto?.toString() ?: totalLocal.toString(),
                    modifier = Modifier.weight(1f)
                )
                MiniKpiCard(
                    etiqueta = "CSAT Promedio",
                    valor = dashboardVm.promedioCsat?.let {
                        String.format(Locale.getDefault(), "%.1f ★", it)
                    } ?: "—",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Monitor Visual de Cumplimiento de SLA (toca para abrir atrasadas)
            MonitorSlaCard(
                atrasadas = dashboardVm.atrasadas,
                onClick = alVerAtrasadas
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Cuadrícula 2x2 de Volumen por Prioridad (toca para filtrar)
            MatrizPrioridadesGrid(
                urgente = dashboardVm.prioridadUrgente,
                alta = dashboardVm.prioridadAlta,
                media = dashboardVm.prioridadMedia,
                baja = dashboardVm.prioridadBaja,
                onSeleccionarPrioridad = alFiltrarPrioridad
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Distribución por Estado
            DistribucionEstadosCard(
                recibidas = dashboardVm.recibidas,
                enRevision = dashboardVm.enRevision,
                asignadas = dashboardVm.asignadas,
                enProceso = dashboardVm.enProceso,
                pendientesInfo = dashboardVm.pendientesInfo,
                resueltas = dashboardVm.resueltas,
                cerradas = dashboardVm.cerradas,
                canceladas = dashboardVm.canceladas
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Carga de Trabajo por Responsable Técnico (toca para filtrar)
            CargaTecnicosCard(
                porTecnico = dashboardVm.porTecnico,
                onSeleccionarTecnico = alFiltrarTecnico
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Botón de Navegación a Gestión de Usuarios
            Button(
                onClick = alIrAGestionUsuarios,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Filled.Group, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Administrar Usuarios y Roles (${dashboardVm.listaUsuarios.size})",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MiniKpiCard(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }
    }
}
