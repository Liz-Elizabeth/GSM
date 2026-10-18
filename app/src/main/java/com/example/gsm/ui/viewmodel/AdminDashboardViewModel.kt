package com.example.gsm.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gsm.data.local.model.ConteoPorTecnicoDto
import com.example.gsm.data.repository.GsmRepository
import com.example.gsm.model.EstadoSolicitud
import com.example.gsm.model.PrioridadSolicitud
import kotlinx.coroutines.launch

class AdminDashboardViewModel(
    private val repo: GsmRepository
) : ViewModel() {

    var recibidas by mutableStateOf(0)
        private set
    var enRevision by mutableStateOf(0)
        private set
    var pendientesInfo by mutableStateOf(0)
        private set
    var asignadas by mutableStateOf(0)
        private set
    var enProceso by mutableStateOf(0)
        private set
    var resueltas by mutableStateOf(0)
        private set
    var cerradas by mutableStateOf(0)
        private set
    var canceladas by mutableStateOf(0)
        private set
    var atrasadas by mutableStateOf(0)
        private set

    var prioridadBaja by mutableStateOf(0)
        private set
    var prioridadMedia by mutableStateOf(0)
        private set
    var prioridadAlta by mutableStateOf(0)
        private set
    var prioridadUrgente by mutableStateOf(0)
        private set

    var porTecnico by mutableStateOf<List<ConteoPorTecnicoDto>>(emptyList())
        private set

    // Se ejecuta solo al crearse el ViewModel (no hay que llamarlo a mano)
    init {
        cargarMetricas()
    }

    private fun cargarMetricas() {
        viewModelScope.launch { repo.metricasPorEstado(EstadoSolicitud.RECIBIDA).collect { recibidas = it } }
        viewModelScope.launch { repo.metricasPorEstado(EstadoSolicitud.EN_REVISION).collect { enRevision = it } }
        viewModelScope.launch { repo.metricasPorEstado(EstadoSolicitud.PENDIENTE_INFORMACION).collect { pendientesInfo = it } }
        viewModelScope.launch { repo.metricasPorEstado(EstadoSolicitud.ASIGNADA).collect { asignadas = it } }
        viewModelScope.launch { repo.metricasPorEstado(EstadoSolicitud.EN_PROCESO).collect { enProceso = it } }
        viewModelScope.launch { repo.metricasPorEstado(EstadoSolicitud.RESUELTA).collect { resueltas = it } }
        viewModelScope.launch { repo.metricasPorEstado(EstadoSolicitud.CERRADA).collect { cerradas = it } }
        viewModelScope.launch { repo.metricasPorEstado(EstadoSolicitud.CANCELADA).collect { canceladas = it } }
        viewModelScope.launch { repo.metricasAtrasadas().collect { atrasadas = it } }

        viewModelScope.launch { repo.metricasPorPrioridad(PrioridadSolicitud.BAJA).collect { prioridadBaja = it } }
        viewModelScope.launch { repo.metricasPorPrioridad(PrioridadSolicitud.MEDIA).collect { prioridadMedia = it } }
        viewModelScope.launch { repo.metricasPorPrioridad(PrioridadSolicitud.ALTA).collect { prioridadAlta = it } }
        viewModelScope.launch { repo.metricasPorPrioridad(PrioridadSolicitud.URGENTE).collect { prioridadUrgente = it } }

        viewModelScope.launch { repo.metricasPorTecnico().collect { porTecnico = it } }
    }
}
