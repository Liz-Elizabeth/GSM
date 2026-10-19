package com.example.gsm.data.repository

import com.example.gsm.data.local.entity.EvidenciaEntity
import com.example.gsm.data.local.model.ConteoPorTecnicoDto
import com.example.gsm.data.model.CategoriaSolicitud
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.Historial
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Solicitud
import com.example.gsm.data.model.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * REPOSITORIO PRINCIPAL (FACHADA / COORDINADOR)
 * Centraliza la comunicación entre Solicitudes, Historial, Evidencias, Notificaciones y WhatsApp.
 * Aplica control de acceso basado en roles (RBAC - OWASP M1/A01) exigido en Págs. 6, 7, 8 y 15 del PDF.
 */
class GsmRepository(
    private val localRepo: SolicitudLocalRepository,
    private val historialRepo: HistorialRepository,
    private val notificacionRepo: NotificacionRepository,
    private val whatsAppRepo: WhatsAppRepository,
    private val evidenciaRepo: EvidenciaRepository? = null // Opcional para no romper llamadas antiguas
) {

    // -------------------------------------------------------------
    // 1. FLUJOS DE CREACIÓN (Con trazabilidad, validación y código único)
    // -------------------------------------------------------------

    suspend fun crearSolicitud(
        titulo: String,
        descripcion: String,
        categoria: CategoriaSolicitud,
        prioridad: PrioridadSolicitud = PrioridadSolicitud.MEDIA,
        cliente: Usuario,
        servicioRelacionado: String? = null,
        activoRelacionado: String? = null,
        nombreArchivoEvidencia: String? = null,
        esBorrador: Boolean = false
    ): Result<Solicitud> {
        return try {
            require(titulo.isNotBlank() && descripcion.isNotBlank()) {
                "El título y la descripción son obligatorios."
            }

            val ahora = System.currentTimeMillis()
            val codigo = localRepo.generarCodigoUnico()
            val fechaLimite = localRepo.calcularSlaFechaLimite(prioridad, ahora)

            val nuevaSolicitud = Solicitud(
                id = 0,
                codigoSeguimiento = codigo,
                titulo = titulo.trim(),
                descripcion = descripcion.trim(),
                categoria = categoria,
                clienteId = cliente.id,
                tecnicoId = null,
                estado = EstadoSolicitud.RECIBIDA,
                prioridad = prioridad,
                servicioRelacionado = servicioRelacionado?.trim(),
                activoRelacionado = activoRelacionado?.trim(),
                fechaLimite = fechaLimite,
                fechaCreacion = ahora,
                fechaActualizacion = ahora
            )

            val idGenerado = localRepo.insertar(nuevaSolicitud, esBorrador).toInt()
            val solicitudGuardada = nuevaSolicitud.copy(id = idGenerado)

            // Si viene con evidencia ficticia adjunta, la registramos (Criterio 3)
            if (!nombreArchivoEvidencia.isNullOrBlank()) {
                evidenciaRepo?.adjuntarEvidenciaFicticia(
                    solicitudId = idGenerado,
                    usuarioId = cliente.id,
                    nombreArchivo = nombreArchivoEvidencia,
                    descripcion = "Evidencia inicial adjuntada al crear la solicitud",
                    ahora = ahora
                )
            }

            // Si no es borrador, registramos el primer hito formal de auditoría (Criterio 6)
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
                    evidenciaAdjunta = nombreArchivoEvidencia,
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
    // 2. TRANSICIONES DE ESTADO (Coordinación Atómica + Seguridad RBAC)
    // -------------------------------------------------------------

    /**
     * El Administrador clasifica la solicitud y define su prioridad (Criterios 13 y 14 del PDF).
     */
    suspend fun clasificarYPriorizar(
        solicitudId: Int,
        nuevaCategoria: CategoriaSolicitud,
        nuevaPrioridad: PrioridadSolicitud,
        admin: Usuario,
        comentario: String = "Solicitud clasificada y priorizada por coordinación."
    ): Result<Unit> = runCatching {
        require(admin.rol == RolUsuario.ADMINISTRADOR) {
            "Acceso denegado: Solo el administrador puede clasificar y definir prioridades."
        }
        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        localRepo.clasificarYPriorizar(solicitudId, nuevaCategoria, nuevaPrioridad, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = admin.id,
            perfil = admin.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.EN_REVISION.name,
            accion = "CLASIFICACION",
            comentario = "$comentario (Categoría: ${nuevaCategoria.descripcion}, Prioridad: ${nuevaPrioridad.name})",
            ahora = ahora
        )

        notificacionRepo.crearNotificacion(
            usuarioDestinoId = solicitud.clienteId,
            solicitudId = solicitudId,
            titulo = "Solicitud en Revisión",
            mensaje = "Tu solicitud #${solicitud.codigoSeguimiento} fue clasificada con prioridad ${nuevaPrioridad.name}.",
            tipoEvento = "CAMBIO_PRIORIDAD",
            ahora = ahora
        )
    }

    /**
     * El Administrador asigna o reasigna un técnico responsable (Criterios 15 y 16 del PDF).
     */
    suspend fun asignarTecnico(
        solicitudId: Int,
        tecnicoId: Int,
        admin: Usuario,
        comentario: String = "Técnico asignado para ejecución."
    ): Result<Unit> = runCatching {
        require(admin.rol == RolUsuario.ADMINISTRADOR) {
            "Acceso denegado: Solo el administrador puede asignar responsables técnicos."
        }
        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

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

    /**
     * El Técnico o Administrador registra un avance o comentario de seguimiento (Criterios 18, 19, 20 y 21).
     */
    suspend fun registrarAvance(
        solicitudId: Int,
        usuario: Usuario,
        comentario: String,
        esInterno: Boolean = false
    ): Result<Unit> = runCatching {
        require(usuario.rol == RolUsuario.TECNICO || usuario.rol == RolUsuario.ADMINISTRADOR) {
            "Acceso denegado: Solo técnicos o administradores pueden registrar avances técnicos."
        }
        require(comentario.isNotBlank()) { "El comentario de avance no puede estar vacío." }

        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        localRepo.actualizarEstado(solicitudId, EstadoSolicitud.EN_PROCESO, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = usuario.id,
            perfil = usuario.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.EN_PROCESO.name,
            accion = "AVANCE",
            comentario = comentario.trim(),
            esComentarioInterno = esInterno,
            ahora = ahora
        )

        if (!esInterno) {
            notificacionRepo.crearNotificacion(
                usuarioDestinoId = solicitud.clienteId,
                solicitudId = solicitudId,
                titulo = "Nuevo Avance en tu Solicitud",
                mensaje = "Se registró un avance en la solicitud #${solicitud.codigoSeguimiento}.",
                tipoEvento = "EN_PROCESO",
                ahora = ahora
            )
        }
    }

    /**
     * Técnico o Administrador solicita antecedentes adicionales al cliente (Criterio 22).
     */
    suspend fun solicitarInformacion(
        solicitudId: Int,
        usuario: Usuario,
        motivo: String
    ): Result<Unit> = runCatching {
        require(usuario.rol == RolUsuario.TECNICO || usuario.rol == RolUsuario.ADMINISTRADOR) {
            "Acceso denegado: Solo personal de la empresa puede solicitar información adicional."
        }
        require(motivo.isNotBlank()) { "Debes indicar qué información se requiere." }

        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        localRepo.actualizarEstado(solicitudId, EstadoSolicitud.PENDIENTE_INFORMACION, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = usuario.id,
            perfil = usuario.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.PENDIENTE_INFORMACION.name,
            accion = "SOLICITUD_INFO",
            comentario = motivo.trim(),
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

    /**
     * El Cliente responde a una solicitud de información aportando antecedentes (PDF Pág. 3 y 6).
     */
    suspend fun responderInformacion(
        solicitudId: Int,
        cliente: Usuario,
        respuesta: String,
        nombreArchivoEvidencia: String? = null
    ): Result<Unit> = runCatching {
        require(respuesta.isNotBlank()) { "Debes escribir los antecedentes solicitados." }

        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        require(solicitud.clienteId == cliente.id) {
            "Acceso denegado: No puedes modificar solicitudes de otro cliente."
        }

        // Si ya tenía técnico asignado vuelve a EN_PROCESO, si no vuelve a EN_REVISION
        val nuevoEstado = if (solicitud.tecnicoId != null) {
            EstadoSolicitud.EN_PROCESO
        } else {
            EstadoSolicitud.EN_REVISION
        }

        if (!nombreArchivoEvidencia.isNullOrBlank()) {
            evidenciaRepo?.adjuntarEvidenciaFicticia(
                solicitudId = solicitudId,
                usuarioId = cliente.id,
                nombreArchivo = nombreArchivoEvidencia,
                descripcion = "Antecedente adicional enviado por el cliente",
                ahora = ahora
            )
        }

        localRepo.actualizarEstado(solicitudId, nuevoEstado, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = cliente.id,
            perfil = cliente.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = nuevoEstado.name,
            accion = "APORTE_INFO",
            comentario = respuesta.trim(),
            evidenciaAdjunta = nombreArchivoEvidencia,
            ahora = ahora
        )
    }

    /**
     * El Técnico registra la solución técnica y marca como RESUELTA (Criterio 31).
     */
    suspend fun registrarResolucion(
        solicitudId: Int,
        tecnico: Usuario,
        solucion: String
    ): Result<Unit> = runCatching {
        require(tecnico.rol == RolUsuario.TECNICO || tecnico.rol == RolUsuario.ADMINISTRADOR) {
            "Acceso denegado: El cliente no puede registrar una resolución técnica."
        }
        require(solucion.isNotBlank()) { "Debes detallar la solución aplicada." }

        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        localRepo.registrarResolucion(solicitudId, solucion.trim(), ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = tecnico.id,
            perfil = tecnico.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.RESUELTA.name,
            accion = "RESOLUCION",
            comentario = "Solución: ${solucion.trim()}",
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

    /**
     * El Cliente (o Admin) confirma la conformidad y cierra formalmente la solicitud (Criterios 32 y 34).
     * Respeta restricción de Pág. 6 y 8: No se puede cerrar directamente sin estar RESUELTA.
     */
    suspend fun confirmarCierre(
        solicitudId: Int,
        usuario: Usuario,
        conforme: Boolean
    ): Result<Unit> = runCatching {
        require(usuario.rol == RolUsuario.CLIENTE || usuario.rol == RolUsuario.ADMINISTRADOR) {
            "Acceso denegado: El técnico no puede cerrar definitivamente sin confirmación del cliente."
        }
        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        require(solicitud.estado == EstadoSolicitud.RESUELTA) {
            "Flujo inválido: Solo se puede confirmar el cierre de una solicitud que esté en estado RESUELTA."
        }
        if (usuario.rol == RolUsuario.CLIENTE) {
            require(solicitud.clienteId == usuario.id) {
                "Acceso denegado: No puedes cerrar solicitudes de otro cliente."
            }
        }

        localRepo.confirmarCierre(solicitudId, conforme, ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = usuario.id,
            perfil = usuario.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.CERRADA.name,
            accion = "CIERRE",
            comentario = if (conforme) "Cliente confirma conformidad con la solución." else "Cierre registrado por administración.",
            ahora = ahora
        )
    }

    /**
     * El Cliente solicita la reapertura del caso cuando no está conforme o surge un problema (Criterio 33).
     */
    suspend fun solicitarReapertura(
        solicitudId: Int,
        cliente: Usuario,
        motivo: String
    ): Result<Unit> = runCatching {
        require(motivo.isNotBlank()) { "Debes indicar el motivo de la reapertura." }

        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        require(solicitud.estado == EstadoSolicitud.RESUELTA || solicitud.estado == EstadoSolicitud.CERRADA) {
            "Solo se puede reabrir una solicitud que haya sido marcada como Resuelta o Cerrada."
        }

        localRepo.solicitarReapertura(solicitudId, motivo.trim(), ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = cliente.id,
            perfil = cliente.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.EN_REVISION.name,
            accion = "REAPERTURA",
            comentario = "Reapertura solicitada: ${motivo.trim()}",
            ahora = ahora
        )
    }

    /**
     * El Administrador cancela una solicitud dejando motivo justificado (Pág. 7 y 10).
     */
    suspend fun cancelar(
        solicitudId: Int,
        admin: Usuario,
        motivo: String
    ): Result<Unit> = runCatching {
        require(admin.rol == RolUsuario.ADMINISTRADOR) {
            "Acceso denegado: Solo el administrador puede cancelar una solicitud."
        }
        require(motivo.isNotBlank()) { "Debes ingresar el motivo de cancelación." }

        val ahora = System.currentTimeMillis()
        val solicitud = localRepo.obtenerPorId(solicitudId)
            ?: throw IllegalArgumentException("Solicitud no encontrada.")

        localRepo.cancelar(solicitudId, motivo.trim(), ahora)

        historialRepo.registrarEvento(
            solicitudId = solicitudId,
            usuarioId = admin.id,
            perfil = admin.rol,
            estadoAnterior = solicitud.estado.name,
            estadoNuevo = EstadoSolicitud.CANCELADA.name,
            accion = "CANCELACION",
            comentario = "Motivo cancelación: ${motivo.trim()}",
            ahora = ahora
        )
    }

    // -------------------------------------------------------------
    // 3. CONSULTAS, HISTORIAL, EVIDENCIAS Y NOTIFICACIONES
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

    suspend fun adjuntarEvidencia(
        solicitudId: Int,
        usuario: Usuario,
        nombreArchivo: String,
        tipoArchivo: String = "image/png",
        tamanoBytes: Long = 250_000L,
        descripcion: String? = null
    ): Result<Long> {
        val repo = evidenciaRepo
            ?: return Result.failure(IllegalStateException("EvidenciaRepository no inicializado."))
        return repo.adjuntarEvidenciaFicticia(
            solicitudId = solicitudId,
            usuarioId = usuario.id,
            nombreArchivo = nombreArchivo,
            tipoArchivo = tipoArchivo,
            tamanoBytes = tamanoBytes,
            descripcion = descripcion
        )
    }

    fun obtenerEvidencias(solicitudId: Int): Flow<List<EvidenciaEntity>>? =
        evidenciaRepo?.obtenerPorSolicitud(solicitudId)

    fun obtenerNotificaciones(usuarioId: Int) = notificacionRepo.obtenerPorUsuario(usuarioId)

    fun contarNotificacionesNoLeidas(usuarioId: Int) = notificacionRepo.contarNoLeidas(usuarioId)

    suspend fun marcarNotificacionLeida(id: Int) = notificacionRepo.marcarComoLeida(id)

    // -------------------------------------------------------------
    // 4. DASHBOARD Y MÉTRICAS (Criterios 29 y 30)
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
