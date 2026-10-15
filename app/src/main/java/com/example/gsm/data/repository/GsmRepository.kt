package com.example.gsm.data.repository

import com.example.gsm.data.local.model.ConteoPorTecnicoDto
import com.example.gsm.model.CategoriaSolicitud
import com.example.gsm.model.EstadoSolicitud
import com.example.gsm.model.Historial
import com.example.gsm.model.PrioridadSolicitud
import com.example.gsm.model.RolUsuario
import com.example.gsm.model.Solicitud
import com.example.gsm.model.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * REPOSITORIO PRINCIPAL (FACHADA / COORDINADOR)
 * Centraliza la comunicación entre Solicitudes, Historial, Notificaciones y WhatsApp.
 */
class GsmRepository(
    private val localRepo: SolicitudLocalRepository,
    private val historialRepo: HistorialRepository,
    private val notificacionRepo: NotificacionRepository,
    private val whatsAppRepo: WhatsAppRepository
) {

    // -------------------------------------------------------------
    // 1. FLUJOS DE CREACIÓN (Con trazabilidad y código único)
    // -------------------------------------------------------------

    suspend fun crearSolicitud(
        titulo: String,
        descripcion: String,
        categoria: CategoriaSolicitud,
        prioridad: PrioridadSolicitud,
        cliente: Usuario,
        servicioRelacionado: String? = null,
        activoRelacionado: String? = null,
        esBorrador: Boolean = false
    ): Result<Solicitud> {
        return try {
            val ahora = System.currentTimeMillis()
            val codigo = localRepo.generarCodigoUnico()
            val fechaLimite = localRepo.calcularSlaFechaLimite(prioridad, ahora)

            val nuevaSolicitud = Solicitud(
                id = 0,
                codigoSeguimiento = codigo,
                titulo = titulo,
                descripcion = descripcion,
                categoria = categoria,
                clienteId = cliente.id,
                tecnicoId = null,
                estado = EstadoSolicitud.RECIBIDA,
                prioridad = prioridad,
                servicioRelacionado = servicioRelacionado,
                activoRelacionado = activoRelacionado,
                fechaLimite = fechaLimite,
                fechaCreacion = ahora,
                fechaActualizacion = ahora
            )

            val idGenerado = localRepo.insertar(nuevaSolicitud, esBorrador).toInt()
            val solicitudGuardada = nuevaSolicitud.copy(id = idGenerado)

            // Si no es borrador, registramos el primer hito formal de auditoría
            if (!esBorrador) {
                historialRepo.registrarEvento(
                    solicitudId = idGenerado,
                    usuarioId = cliente.id,
                    perfil = cliente.rol,
                    estadoAnterior = null,
                    estadoNuevo = EstadoSolicitud.RECIBIDA.name,
                    accion = "CREACION",
                    comentario = "Solicitud ingresada formalmente por el cliente.",
                    esComentarioInterno = false,
                    ahora = ahora
                )
            }

            Result.success(solicitudGuardada)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun guardarBorrador(
        titulo: String,
        descripcion: String,
        categoria: CategoriaSolicitud,
        cliente: Usuario
    ): Result<Solicitud> {
        return crearSolicitud(
            titulo = titulo,
            descripcion = descripcion,
            categoria = categoria,
            prioridad = PrioridadSolicitud.BAJA,
            cliente = cliente,
            esBorrador = true
        )
    }

    // -------------------------------------------------------------
    // 2. TRANSICIONES DE ESTADO (Coordinación Atómica)
    // -------------------------------------------------------------

    suspend fun asignarTecnico(
        solicitudId: Int,
        tecnicoId: Int,
        admin: Usuario,
        comentario: String = "Técnico asignado para ejecución."
    ) {
        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId) ?: return

        localRepo.asignarTecnico(solicitudId, tecnicoId, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = admin.id,
            perfil = admin.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.ASIGNADA.name,
            accion = "ASIGNACION",
            comentario = comentario,
            ahora = ahora
        )

        notificacionRepo.crearNotificacion(
            usuarioDestinoId = tecnicoId,
            solicitudId = solicitudId,
            titulo = "Nueva Solicitud Asignada",
            mensaje = "Se te ha asignado la solicitud #${solicitud.codigoSeguimiento}",
            tipoEvento = "ASIGNACION",
            ahora = ahora
        )
    }

    suspend fun registrarAvance(
        solicitudId: Int,
        usuario: Usuario,
        comentario: String,
        esInterno: Boolean = false
    ) {
        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId) ?: return

        localRepo.actualizarEstado(solicitudId, EstadoSolicitud.EN_PROCESO, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = usuario.id,
            perfil = usuario.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.EN_PROCESO.name,
            accion = "AVANCE",
            comentario = comentario,
            esComentarioInterno = esInterno,
            ahora = ahora
        )
    }

    suspend fun solicitarInformacion(
        solicitudId: Int,
        usuario: Usuario,
        motivo: String
    ) {
        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId) ?: return

        localRepo.actualizarEstado(solicitudId, EstadoSolicitud.PENDIENTE_INFORMACION, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = usuario.id,
            perfil = usuario.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.PENDIENTE_INFORMACION.name,
            accion = "SOLICITUD_INFO",
            comentario = motivo,
            ahora = ahora
        )

        notificacionRepo.crearNotificacion(
            usuarioDestinoId = solicitud.clienteId,
            solicitudId = solicitudId,
            titulo = "Información Requerida",
            mensaje = "Se requieren antecedentes adicionales para tu solicitud #${solicitud.codigoSeguimiento}.",
            tipoEvento = "PENDIENTE_INFORMACION",
            ahora = ahora
        )
    }

    suspend fun registrarResolucion(
        solicitudId: Int,
        tecnico: Usuario,
        solucion: String
    ) {
        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId) ?: return

        localRepo.registrarResolucion(solicitudId, solucion, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = tecnico.id,
            perfil = tecnico.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.RESUELTA.name,
            accion = "RESOLUCION",
            comentario = "Solución: $solucion",
            ahora = ahora
        )

        notificacionRepo.crearNotificacion(
            usuarioDestinoId = solicitud.clienteId,
            solicitudId = solicitudId,
            titulo = "Solicitud Resuelta",
            mensaje = "Tu requerimiento #${solicitud.codigoSeguimiento} fue resuelto. Por favor confirma conformidad.",
            tipoEvento = "RESUELTA",
            ahora = ahora
        )
    }

    suspend fun confirmarCierre(
        solicitudId: Int,
        cliente: Usuario,
        conforme: Boolean
    ) {
        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId) ?: return

        localRepo.confirmarCierre(solicitudId, conforme, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = cliente.id,
            perfil = cliente.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.CERRADA.name,
            accion = "CIERRE",
            comentario = if (conforme) "Conforme con la solución." else "Cierre sin conformidad total.",
            ahora = ahora
        )
    }

    suspend fun solicitarReapertura(
        solicitudId: Int,
        cliente: Usuario,
        motivo: String
    ) {
        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId) ?: return

        localRepo.solicitarReapertura(solicitudId, motivo, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = cliente.id,
            perfil = cliente.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.EN_REVISION.name,
            accion = "REAPERTURA",
            comentario = "Reapertura solicitada: $motivo",
            ahora = ahora
        )
    }

    suspend fun cancelar(
        solicitudId: Int,
        admin: Usuario,
        motivo: String
    ) {
        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId) ?: return

        localRepo.cancelar(solicitudId, motivo, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = admin.id,
            perfil = admin.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.CANCELADA.name,
            accion = "CANCELACION",
            comentario = "Motivo cancelación: $motivo",
            ahora = ahora
        )
    }

    // -------------------------------------------------------------
    // 3. CONSULTAS, HISTORIAL Y NOTIFICACIONES
    // -------------------------------------------------------------

    fun listarSolicitudes(
        estado: EstadoSolicitud? = null,
        prioridad: PrioridadSolicitud? = null,
        clienteId: Int? = null,
        tecnicoId: Int? = null,
        categoria: CategoriaSolicitud? = null
    ): Flow<List<Solicitud>> = localRepo.filtrar(estado, prioridad, categoria, clienteId, tecnicoId)

    suspend fun obtenerPorId(id: Int): Solicitud? = localRepo.obtenerPorId(id)

    suspend fun buscarPorCodigo(codigo: String): Solicitud? = localRepo.obtenerPorCodigo(codigo)

    fun listarAtrasadas(): Flow<List<Solicitud>> = localRepo.obtenerAtrasadas()

    fun listarBorradores(clienteId: Int): Flow<List<Solicitud>> = localRepo.obtenerBorradores(clienteId)

    fun obtenerHistorial(solicitudId: Int, rol: RolUsuario): Flow<List<Historial>> =
        historialRepo.obtenerHistorial(solicitudId, rol)

    fun obtenerNotificaciones(usuarioId: Int) = notificacionRepo.obtenerPorUsuario(usuarioId)

    fun contarNotificacionesNoLeidas(usuarioId: Int) = notificacionRepo.contarNoLeidas(usuarioId)

    suspend fun marcarNotificacionLeida(id: Int) = notificacionRepo.marcarComoLeida(id)

    // -------------------------------------------------------------
    // 4. DASHBOARD Y MÉTRICAS
    // -------------------------------------------------------------

    fun metricasPorEstado(estado: EstadoSolicitud): Flow<Int> = localRepo.contarPorEstado(estado)
    fun metricasAtrasadas(): Flow<Int> = localRepo.contarAtrasadas()
    fun metricasPorPrioridad(prioridad: PrioridadSolicitud): Flow<Int> = localRepo.contarPorPrioridad(prioridad)
    fun metricasPorTecnico(): Flow<List<ConteoPorTecnicoDto>> = localRepo.contarPorTecnico()

    // -------------------------------------------------------------
    // 5. APARTADO WHATSAPP (Mensaje + Animación simulada)
    // -------------------------------------------------------------

    fun formatearMensajeWhatsApp(solicitud: Solicitud, clienteNombre: String? = null): String =
        whatsAppRepo.crearMensajeEstructurado(solicitud, clienteNombre)

    suspend fun simularEnvioWhatsApp(solicitud: Solicitud): Boolean =
        whatsAppRepo.simularEnvio(solicitud)

}