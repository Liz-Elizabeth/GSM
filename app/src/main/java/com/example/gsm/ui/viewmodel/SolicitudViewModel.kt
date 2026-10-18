package com.example.gsm.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gsm.data.repository.GsmRepository
import com.example.gsm.model.CategoriaSolicitud
import com.example.gsm.model.EstadoSolicitud
import com.example.gsm.model.PrioridadSolicitud
import com.example.gsm.model.RolUsuario
import com.example.gsm.model.Solicitud
import com.example.gsm.model.Usuario
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SolicitudViewModel(
    private val repo: GsmRepository
) : ViewModel() {

    var listaSolicitudes by mutableStateOf<List<Solicitud>>(emptyList())
        private set

    var estadoFiltro by mutableStateOf<EstadoSolicitud?>(null)
        private set

    var prioridadFiltro by mutableStateOf<PrioridadSolicitud?>(null)
        private set

    var soloAtrasadas by mutableStateOf(false)
        private set

    var textoBusqueda by mutableStateOf("")
        private set

    var mensaje by mutableStateOf<String?>(null)
        private set

    // Guarda la búsqueda actual para cerrarla antes de abrir una nueva
    private var busquedaActual: Job? = null

    fun cargarSolicitudes(usuario: Usuario) {
        // Cerramos la consulta anterior para no acumularlas en memoria
        busquedaActual?.cancel()

        busquedaActual = viewModelScope.launch {
            val idCliente = if (usuario.rol == RolUsuario.CLIENTE) usuario.id else null
            val idTecnico = if (usuario.rol == RolUsuario.TECNICO) usuario.id else null

            val flujo = if (soloAtrasadas) {
                repo.listarAtrasadas()
            } else {
                repo.listarSolicitudes(
                    estado = estadoFiltro,
                    prioridad = prioridadFiltro,
                    clienteId = idCliente,
                    tecnicoId = idTecnico
                )
            }

            flujo.collect { lista ->
                listaSolicitudes = filtrarPorBusquedaYRol(lista, usuario)
            }
        }
    }

    private fun filtrarPorBusquedaYRol(
        lista: List<Solicitud>,
        usuario: Usuario
    ): List<Solicitud> {
        return lista.filter { sol ->
            val perteneceAlUsuario = when (usuario.rol) {
                RolUsuario.CLIENTE -> sol.clienteId == usuario.id
                RolUsuario.TECNICO -> sol.tecnicoId == usuario.id
                RolUsuario.ADMINISTRADOR -> true
            }

            val coincideTexto = textoBusqueda.isBlank() ||
                sol.codigoSeguimiento.contains(textoBusqueda, ignoreCase = true) ||
                sol.titulo.contains(textoBusqueda, ignoreCase = true)

            perteneceAlUsuario && coincideTexto
        }
    }

    // Funciones simples para cambiar filtros desde la pantalla
    fun cambiarFiltroEstado(nuevoEstado: EstadoSolicitud?, usuario: Usuario) {
        estadoFiltro = nuevoEstado
        soloAtrasadas = false
        cargarSolicitudes(usuario)
    }

    fun cambiarFiltroPrioridad(nuevaPrioridad: PrioridadSolicitud?, usuario: Usuario) {
        prioridadFiltro = nuevaPrioridad
        cargarSolicitudes(usuario)
    }

    fun cambiarFiltroAtrasadas(verSoloAtrasadas: Boolean, usuario: Usuario) {
        soloAtrasadas = verSoloAtrasadas
        cargarSolicitudes(usuario)
    }

    fun cambiarTextoBusqueda(nuevoTexto: String, usuario: Usuario) {
        textoBusqueda = nuevoTexto
        cargarSolicitudes(usuario)
    }

    fun crearSolicitud(
        titulo: String,
        descripcion: String,
        categoria: CategoriaSolicitud,
        cliente: Usuario,
        servicio: String?,
        activo: String?,
        evidencia: String?,
        esBorrador: Boolean = false,
        alCrearConExito: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val resultado = repo.crearSolicitud(
                titulo = titulo,
                descripcion = descripcion,
                categoria = categoria,
                cliente = cliente,
                servicioRelacionado = servicio,
                activoRelacionado = activo,
                nombreArchivoEvidencia = evidencia,
                esBorrador = esBorrador
            )

            if (resultado.isSuccess) {
                val creada = resultado.getOrNull()
                mensaje = "Creada con código: ${creada?.codigoSeguimiento}"
                alCrearConExito()
            } else {
                mensaje = resultado.exceptionOrNull()?.message ?: "Error al crear la solicitud"
            }
        }
    }

    fun limpiarMensaje() {
        mensaje = null
    }
}
