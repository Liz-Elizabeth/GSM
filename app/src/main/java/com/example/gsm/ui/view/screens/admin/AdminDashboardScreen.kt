package com.example.gsm.ui.view.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.ui.view.components.EncabezadoPantalla
import com.example.gsm.ui.viewmodel.AdminDashboardViewModel

@Composable
fun AdminDashboardScreen(
    dashboardVm: AdminDashboardViewModel,
    alVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        EncabezadoPantalla(
            titulo = "📊 Dashboard Operativo",
            alHacerClicDerecho = alVolver
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tarjeta destacada de Solicitudes Atrasadas
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("⚠️ Solicitudes Fuera de Plazo (SLA Vencido)", fontWeight = FontWeight.Bold)
                Text(
                    text = "${dashboardVm.atrasadas} atrasadas",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Métricas por Estado
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📌 Solicitudes por Estado", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Recibidas: ${dashboardVm.recibidas}")
                Text("• En Revisión: ${dashboardVm.enRevision}")
                Text("• Asignadas: ${dashboardVm.asignadas}")
                Text("• En Proceso: ${dashboardVm.enProceso}")
                Text("• Pendientes de Información: ${dashboardVm.pendientesInfo}")
                Text("• Resueltas: ${dashboardVm.resueltas}")
                Text("• Cerradas: ${dashboardVm.cerradas}")
                Text("• Canceladas: ${dashboardVm.canceladas}")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Métricas por Prioridad
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("⚡ Solicitudes por Prioridad", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("🔴 Urgente (6h): ${dashboardVm.prioridadUrgente}")
                Text("🟠 Alta (24h): ${dashboardVm.prioridadAlta}")
                Text("🟡 Media (72h): ${dashboardVm.prioridadMedia}")
                Text("🟢 Baja (168h): ${dashboardVm.prioridadBaja}")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Carga de trabajo por Técnico
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🔧 Solicitudes por Responsable Técnico", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (dashboardVm.porTecnico.isEmpty()) {
                    Text("Aún no hay solicitudes asignadas a técnicos.")
                } else {
                    dashboardVm.porTecnico.forEach { dto ->
                        Text("• ${dto.nombreTecnico ?: "Sin nombre"}: ${dto.totalSolicitudes} asignada(s)")
                    }
                }
            }
        }
    }
}
