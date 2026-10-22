package com.example.gsm.ui.view.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.view.screens.admin.componentes.CargaTecnicosCard
import com.example.gsm.ui.view.screens.admin.componentes.DistribucionEstadosCard
import com.example.gsm.ui.view.screens.admin.componentes.MatrizPrioridadesGrid
import com.example.gsm.ui.view.screens.admin.componentes.MonitorSlaCard
import com.example.gsm.ui.view.screens.usuario.componentes.AppleBackground
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import com.example.gsm.ui.viewmodel.AdminDashboardViewModel

/**
 * Pantalla orquestadora del Panel de Administración y Métricas (Dashboard).
 * Arquitectura modular limpia (Clean UI / KISS) siguiendo la estética Apple HIG + Teamwork Desk.
 */
@Composable
fun AdminDashboardScreen(
    dashboardVm: AdminDashboardViewModel,
    alIrAGestionUsuarios: () -> Unit = {},
    alVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Encabezado de Navegación
        EncabezadoPantalla(
            titulo = "Panel de Métricas",
            alHacerClicDerecho = alVolver
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Monitor Visual de Cumplimiento de SLA (Alerta / Al Día)
        MonitorSlaCard(atrasadas = dashboardVm.atrasadas)

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Cuadrícula 2x2 de Volumen por Prioridad y SLA
        MatrizPrioridadesGrid(
            urgente = dashboardVm.prioridadUrgente,
            alta = dashboardVm.prioridadAlta,
            media = dashboardVm.prioridadMedia,
            baja = dashboardVm.prioridadBaja
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Distribución por Estado (Lista Inset Grouped estilo iOS)
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

        // 5. Carga de Trabajo por Responsable Técnico
        CargaTecnicosCard(porTecnico = dashboardVm.porTecnico)

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Botón de Navegación a Gestión de Usuarios (Pantalla Dedicada)
        Button(
            onClick = alIrAGestionUsuarios,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "👥 Administrar Usuarios y Roles (${dashboardVm.listaUsuarios.size})",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
