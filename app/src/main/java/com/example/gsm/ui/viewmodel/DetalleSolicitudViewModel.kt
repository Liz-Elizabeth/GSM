package com.example.gsm.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gsm.data.local.entity.EvidenciaEntity
import com.example.gsm.data.repository.GsmRepository
import com.example.gsm.data.repository.UsuarioRepository
import com.example.gsm.data.model.CategoriaSolicitud
import com.example.gsm.data.model.Historial
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Solicitud
import com.example.gsm.data.model.Usuario
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DetalleSolicitudViewModel(
    private val repo: GsmRepository,
    private val usuarioRepo: UsuarioRepository
) : ViewModel() {

    var solicitud by mutableStateOf<Solicitud?>(null)
        private set

    var historial by mutableStateOf<List<Historial>>(emptyList())
        private set

    var evidencias by mutableStateOf<List<EvidenciaEntity>>(emptyList())
        private set

    var tecnicosDisponibles by mutableStateOf<List<Usuario>>(emptyList())
        private set

    var mensajeWhatsApp by mutableStateOf("")
        private set

    var enviandoWhatsApp by mutableStateOf(false)
        private set

    var mensaje by mutableStateOf<String?>(null)
        private set

    var cargando by mutableStateOf(true)
        private set

    private var escuchaHistorial: Job? = null
    private var escuchaEvidencias: Job? = null

    init {
        viewModelScope.launch {
            usuarioRepo.obtenerTecnicosDisponibles().collect { lista ->
                tecnicosDisponibles = lista
            }
        }
    }

    fun cargarDetalle(solicitudId: Int, rol: RolUsuario) {
        cargando = true
        viewModelScope.launch {
            val encontrada = repo.obtenerPorId(solicitudId)
            solicitud = encontrada
            if (encontrada != null) {
                mensajeWhatsApp = repo.formatearMensajeWhatsApp(encontrada)
                if (encontrada.codigoSeguimiento.isNotBlank()) {
                    repo.iniciarSincronizacionHistorial(
                        codigoSeguimiento = encontrada.codigoSeguimiento,
                        solicitudIdLocal = encontrada.id,
                        scope = viewModelScope
                    )
                }
            }
            cargando = false
        }

        escuchaHistorial?.cancel()
        escuchaHistorial = viewModelScope.launch {
            repo.obtenerHistorial(solicitudId, rol).collect { lista ->
                historial = lista
            }
        }

        escuchaEvidencias?.cancel()
        escuchaEvidencias = viewModelScope.launch {
            repo.obtenerEvidencias(solicitudId)?.collect { lista ->
                evidencias = lista
            }
        }
    }

    fun adjuntarEvidencia(
        solicitudId: Int,
        usuario: Usuario,
        nombre: String,
        tipo: String,
        tamano: Long,
        uri: String
    ) {
        viewModelScope.launch {
            val res = repo.adjuntarEvidencia(
                solicitudId = solicitudId,
                usuario = usuario,
                nombreArchivo = nombre,
                tipoArchivo = tipo,
                tamanoBytes = tamano,
                uriLocal = uri,
                descripcion = "Evidencia adjuntada por " + usuario.nombre
            )
            if (res.isSuccess) {
                mensaje = "Archivo '" + nombre + "' adjuntado exitosamente."
            } else {
                mensaje = res.exceptionOrNull()?.message ?: "Error al adjuntar archivo."
            }
        }
    }

    fun actualizarInformacion(
        solicitudId: Int,
        nuevoTitulo: String,
        nuevaDescripcion: String,
        usuario: Usuario
    ) {
        viewModelScope.launch {
            val res = repo.actualizarInformacionSolicitud(solicitudId, nuevoTitulo, nuevaDescripcion, usuario)
            if (res.isSuccess) {
                mensaje = "Solicitud actualizada correctamente."
                cargarDetalle(solicitudId, usuario.rol)
            } else {
                mensaje = res.exceptionOrNull()?.message ?: "Error al actualizar."
            }
            }
    }

    // Función ayudante para no repetir el mismo if/else en los 9 botones
    private fun ejecutarCambio(
        solicitudId: Int,
        rol: RolUsuario,
        mensajeExito: String,
        operacion: suspend () -> Result<Unit>
    ) {
        viewModelScope.launch {
            val resultado = operacion()
            if (resultado.isSuccess) {
                mensaje = mensajeExito
                cargarDetalle(solicitudId, rol)
            } else {
                mensaje = resultado.exceptionOrNull()?.message ?: "Error en la operación"
            }
        }
    }

    // --- BOTONES DEL ADMINISTRADOR ---

    fun clasificarYPriorizar(
        solicitudId: Int,
        categoria: CategoriaSolicitud,
        prioridad: PrioridadSolicitud,
        admin: Usuario
    ) {
        ejecutarCambio(solicitudId, admin.rol, "Clasificada y priorizada") {
            repo.clasificarYPriorizar(solicitudId, categoria, prioridad, admin)
        }
    }

    fun asignarTecnico(solicitudId: Int, tecnicoId: Int, admin: Usuario) {
        ejecutarCambio(solicitudId, admin.rol, "Técnico asignado correctamente") {
            repo.asignarTecnico(solicitudId, tecnicoId, admin)
        }
    }

    fun desasignarTecnico(
        solicitudId: Int,
        admin: Usuario,
        motivo: String = "Técnico desasignado por el administrador"
    ) {
        ejecutarCambio(solicitudId, admin.rol, "Técnico desasignado correctamente") {
            repo.desasignarTecnico(solicitudId, admin, motivo)
        }
    }

    fun solicitarCancelacion(solicitudId: Int, cliente: Usuario, motivo: String) {
        ejecutarCambio(solicitudId, cliente.rol, "Solicitud de cancelación enviada") {
            repo.solicitarCancelacion(solicitudId, cliente, motivo)
        }
    }

    fun cancelar(solicitudId: Int, admin: Usuario, motivo: String) {
        ejecutarCambio(solicitudId, admin.rol, "Solicitud cancelada") {
            repo.cancelar(solicitudId, admin, motivo)
        }
    }

    // --- MENSAJERÍA DIRECTA DE CHAT (KISS) ---

    fun enviarMensajeChat(
        solicitudId: Int,
        usuario: Usuario,
        texto: String,
        esInterno: Boolean = false,
        evidenciaAdjunta: String? = null
    ) {
        viewModelScope.launch {
            repo.enviarMensajeChat(solicitudId, usuario, texto, esInterno, evidenciaAdjunta)
        }
    }

    // --- BOTONES DEL TÉCNICO ---

    fun registrarAvance(
        solicitudId: Int,
        usuario: Usuario,
        comentario: String,
        esInterno: Boolean
    ) {
        ejecutarCambio(solicitudId, usuario.rol, "Avance guardado en el historial") {
            repo.registrarAvance(solicitudId, usuario, comentario, esInterno)
        }
    }

    fun solicitarInformacion(solicitudId: Int, usuario: Usuario, motivo: String) {
        ejecutarCambio(solicitudId, usuario.rol, "Se solicitó información al cliente") {
            repo.solicitarInformacion(solicitudId, usuario, motivo)
        }
    }

    fun registrarResolucion(solicitudId: Int, tecnico: Usuario, solucion: String) {
        ejecutarCambio(solicitudId, tecnico.rol, "Solución registrada (Estado: RESUELTA)") {
            repo.registrarResolucion(solicitudId, tecnico, solucion)
        }
    }

    // --- BOTONES DEL CLIENTE ---

    fun responderInformacion(solicitudId: Int, cliente: Usuario, respuesta: String) {
        ejecutarCambio(solicitudId, cliente.rol, "Información enviada") {
            repo.responderInformacion(solicitudId, cliente, respuesta)
        }
    }

    fun confirmarCierre(solicitudId: Int, usuario: Usuario, conforme: Boolean) {
        ejecutarCambio(solicitudId, usuario.rol, "Solicitud cerrada con conformidad") {
            repo.confirmarCierre(solicitudId, usuario, conforme)
        }
    }

    fun solicitarReapertura(solicitudId: Int, cliente: Usuario, motivo: String) {
        ejecutarCambio(solicitudId, cliente.rol, "Solicitud reabierta (En Revisión)") {
            repo.solicitarReapertura(solicitudId, cliente, motivo)
        }
    }

    // --- BOTÓN DE WHATSAPP ---

    fun simularEnvioWhatsApp() {
        val sol = solicitud ?: return
        viewModelScope.launch {
            enviandoWhatsApp = true
            repo.simularEnvioWhatsApp(sol)
            enviandoWhatsApp = false
            mensaje = "Mensaje enviado a WhatsApp (${sol.codigoSeguimiento})"
        }
    }

    fun limpiarMensaje() {
        mensaje = null
    }
}
