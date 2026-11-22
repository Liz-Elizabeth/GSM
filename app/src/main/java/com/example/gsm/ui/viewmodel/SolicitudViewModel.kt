package com.example.gsm.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gsm.data.local.entity.NotificacionEntity
import com.example.gsm.data.repository.GsmRepository
import com.example.gsm.data.model.CategoriaSolicitud
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Solicitud
import com.example.gsm.data.model.Usuario
import com.example.gsm.util.ArchivoAdjunto
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SolicitudViewModel(
    private val repo: GsmRepository
) : ViewModel() {

    var listaSolicitudes by mutableStateOf<List<Solicitud>>(emptyList())
        private set

    val solicitudesActivas: List<Solicitud>
        get() = listaSolicitudes.filter { sol ->
            sol.estado != EstadoSolicitud.CANCELADA &&
            sol.estado != EstadoSolicitud.CERRADA &&
            !(sol.estado == EstadoSolicitud.RESUELTA && sol.conformidadCliente == true)
        }

    val solicitudesArchivadas: List<Solicitud>
        get() = listaSolicitudes.filter { sol ->
            sol.estado == EstadoSolicitud.CANCELADA ||
            sol.estado == EstadoSolicitud.CERRADA ||
            (sol.estado == EstadoSolicitud.RESUELTA && sol.conformidadCliente == true)
        }

    var estadoFiltro by mutableStateOf<EstadoSolicitud?>(null)
        private set

    var prioridadFiltro by mutableStateOf<PrioridadSolicitud?>(null)
        private set

    var clienteFiltroId by mutableStateOf<Int?>(null)
        private set

    var tecnicoFiltroId by mutableStateOf<Int?>(null)
        private set

    var notificaciones by mutableStateOf<List<NotificacionEntity>>(emptyList())
        private set

    var soloAtrasadas by mutableStateOf(false)
        private set

    var textoBusqueda by mutableStateOf("")
        private set

    var mensaje by mutableStateOf<String?>(null)
        private set

    var cargando by mutableStateOf(false)
        private set

    val errorSync: StateFlow<String?> = repo.ultimoErrorSync

    fun limpiarErrorSync() = repo.limpiarErrorSync()

    fun sincronizarArchivoCompleto() {
        viewModelScope.launch { repo.sincronizarArchivoRemoto() }
    }

    // Cache en memoria para filtrar texto al instante sin re-abrir Flows de Room en cada tecla
    private var listaBaseRol: List<Solicitud> = emptyList()

    // Guarda la búsqueda actual para cerrarla antes de abrir una nueva
    private var busquedaActual: Job? = null
    private var escuchaNotificaciones: Job? = null

    fun cargarSolicitudes(usuario: Usuario) {
        // Cerramos la consulta anterior para no acumularlas en memoria
        busquedaActual?.cancel()

        busquedaActual = viewModelScope.launch {
            cargando = true
            // Se consulta directo a Room (sin caché) para incluir al instante nuevos colaboradores de la empresa
            // y evitar que al cambiar de cuenta queden IDs obsoletos de la sesión anterior.
            val empresaCliente = usuario.empresa.orEmpty().trim()
            val idsEmpresa = if (usuario.rol == RolUsuario.CLIENTE && empresaCliente.isNotBlank()) {
                (repo.obtenerIdsClientesEmpresa(empresaCliente) + usuario.id).distinct()
            } else if (usuario.rol == RolUsuario.CLIENTE) {
                listOf(usuario.id)
            } else emptyList()

            val idCliente = when {
                usuario.rol == RolUsuario.CLIENTE && idsEmpresa.size <= 1 -> usuario.id
                usuario.rol == RolUsuario.CLIENTE -> null
                else -> clienteFiltroId
            }
            val idTecnico = if (usuario.rol == RolUsuario.TECNICO) usuario.id else tecnicoFiltroId

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
                listaBaseRol = filtrarSoloRol(lista, usuario, idsEmpresa)
                listaSolicitudes = aplicarBusquedaTexto(listaBaseRol)
                cargando = false
            }
        }

        escuchaNotificaciones?.cancel()
        escuchaNotificaciones = viewModelScope.launch {
            repo.obtenerNotificaciones(usuario.id).collect { lista ->
                notificaciones = lista
            }
        }
    }

    private fun filtrarSoloRol(
        lista: List<Solicitud>,
        usuario: Usuario,
        idsEmpresa: List<Int> = emptyList()
    ): List<Solicitud> {
        return lista.filter { sol ->
            when (usuario.rol) {
                RolUsuario.CLIENTE -> if (idsEmpresa.isNotEmpty()) sol.clienteId in idsEmpresa else sol.clienteId == usuario.id
                RolUsuario.TECNICO -> sol.tecnicoId == usuario.id
                RolUsuario.ADMINISTRADOR -> true
            }
        }
    }

    private fun aplicarBusquedaTexto(lista: List<Solicitud>): List<Solicitud> {
        val query = textoBusqueda.trim()
        if (query.isBlank()) return lista
        return lista.filter { sol ->
            sol.codigoSeguimiento.contains(query, ignoreCase = true) ||
                sol.titulo.contains(query, ignoreCase = true)
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

    fun cambiarFiltroCliente(nuevoClienteId: Int?, usuario: Usuario) {
        clienteFiltroId = nuevoClienteId
        cargarSolicitudes(usuario)
    }

    fun cambiarFiltroTecnico(nuevoTecnicoId: Int?, usuario: Usuario) {
        tecnicoFiltroId = nuevoTecnicoId
        cargarSolicitudes(usuario)
    }

    fun marcarNotificacionLeida(notificacionId: Int) {
        viewModelScope.launch {
            repo.marcarNotificacionLeida(notificacionId)
        }
    }

    fun cambiarFiltroAtrasadas(verSoloAtrasadas: Boolean, usuario: Usuario) {
        soloAtrasadas = verSoloAtrasadas
        cargarSolicitudes(usuario)
    }

    fun cambiarTextoBusqueda(nuevoTexto: String, usuario: Usuario) {
        textoBusqueda = nuevoTexto
        listaSolicitudes = aplicarBusquedaTexto(listaBaseRol)
    }

    fun crearSolicitud(
        titulo: String,
        descripcion: String,
        categoria: CategoriaSolicitud,
        cliente: Usuario,
        servicio: String?,
        activo: String?,
        nombreEvidencia: String? = null,
        tipoEvidencia: String? = null,
        tamanoEvidencia: Long? = null,
        uriEvidencia: String? = null,
        evidencias: List<ArchivoAdjunto> = emptyList(),
        esBorrador: Boolean = false,
        alCrearConExito: () -> Unit = {}
    ) {
        if (cargando) return
        viewModelScope.launch {
            cargando = true
            try {
                val resultado = repo.crearSolicitud(
                    titulo = titulo,
                    descripcion = descripcion,
                    categoria = categoria,
                    cliente = cliente,
                    servicioRelacionado = servicio,
                    activoRelacionado = activo,
                    nombreArchivoEvidencia = nombreEvidencia,
                    tipoArchivoEvidencia = tipoEvidencia,
                    tamanoArchivoEvidencia = tamanoEvidencia,
                    uriEvidencia = uriEvidencia,
                    evidencias = evidencias,
                    esBorrador = esBorrador
                )

                if (resultado.isSuccess) {
                    val creada = resultado.getOrNull()
                    mensaje = "Creada con código: ${creada?.codigoSeguimiento}"
                    alCrearConExito()
                } else {
                    mensaje = resultado.exceptionOrNull()?.message ?: "Error al crear la solicitud"
                }
            } finally {
                cargando = false
            }
        }
    }

    /**
     * Búsqueda instantánea por Código QR escaneado con CameraX.
     */
    fun buscarPorQr(
        codigoQr: String,
        alEncontrar: (Int) -> Unit,
        alFallar: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val codigoLimpio = when {
                codigoQr.contains("SOL-") -> {
                    val inicio = codigoQr.indexOf("SOL-")
                    codigoQr.substring(inicio).take(13).trim()
                }
                else -> codigoQr.trim()
            }

            val sol = repo.buscarPorCodigo(codigoLimpio)
            if (sol != null) {
                alEncontrar(sol.id)
            } else {
                val err = "No se encontró ningún ticket con el código '$codigoLimpio'."
                mensaje = err
                alFallar(err)
            }
        }
    }

    fun limpiarMensaje() {
        mensaje = null
    }

    /**
     * Reinicia listeners y filtros al cerrar sesión para no filtrar datos entre cuentas.
     */
    fun limpiarSesion() {
        busquedaActual?.cancel()
        escuchaNotificaciones?.cancel()
        listaBaseRol = emptyList()
        listaSolicitudes = emptyList()
        notificaciones = emptyList()
        estadoFiltro = null
        prioridadFiltro = null
        clienteFiltroId = null
        tecnicoFiltroId = null
        soloAtrasadas = false
        textoBusqueda = ""
        mensaje = null
        cargando = false
    }
}
