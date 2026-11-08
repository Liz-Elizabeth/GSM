package com.example.gsm.data.repository

import android.util.Log
import com.example.gsm.data.local.dao.SolicitudDao
import com.example.gsm.data.local.model.ConteoPorTecnicoDto
import com.example.gsm.data.mapper.toDomain
import com.example.gsm.data.mapper.toEntity
import com.example.gsm.data.model.CategoriaSolicitud
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.Solicitud
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * Sub-repositorio encargado exclusivamente de la persistencia y lectura
 * de solicitudes en la base de datos local (Room).
 */
class SolicitudLocalRepository(
    private val solicitudDao: SolicitudDao
) {
    suspend fun guardarOActualizar(solicitud: Solicitud) {
        runCatching {
            val codigoLimpio = solicitud.codigoSeguimiento.trim().uppercase()
            val existente = if (codigoLimpio.isNotBlank()) {
                solicitudDao.obtenerPorCodigoSeguimiento(codigoLimpio)
            } else null

            // Clave foránea segura para evitar SQLiteConstraintException
            val usuariosValidos = listOf(1, 2, 3, 4, 5, 99)
            val clienteIdSeguro = if (solicitud.clienteId in usuariosValidos) solicitud.clienteId else 1
            val tecnicoIdSeguro = if (solicitud.tecnicoId != null && solicitud.tecnicoId in usuariosValidos) solicitud.tecnicoId else null

            val solicitudSegura = solicitud.copy(
                clienteId = clienteIdSeguro,
                tecnicoId = tecnicoIdSeguro
            )

            if (existente != null) {
                solicitudDao.actualizarSolicitud(
                    solicitudSegura.toEntity(esBorrador = false).copy(
                        id = existente.id,
                        codigoSeguimiento = existente.codigoSeguimiento
                    )
                )
                Log.d("FirebaseSync", "Solicitud local actualizada: ${existente.codigoSeguimiento} (ID local: ${existente.id})")
            } else {
                // Al insertar uno nuevo desde Firestore, usamos id = 0 para autoincremento seguro en SQLite
                val idLocalGenerado = solicitudDao.insertarSolicitud(
                    solicitudSegura.toEntity(esBorrador = false).copy(id = 0)
                )
                Log.d("FirebaseSync", "Nueva solicitud insertada en Room desde Firestore: $codigoLimpio (ID: $idLocalGenerado)")
            }
        }.onFailure { e ->
            Log.e("FirebaseSync", "Error al guardar/actualizar solicitud local ${solicitud.codigoSeguimiento}: ${e.message}", e)
        }
    }

    suspend fun actualizarInformacion(id: Int, nuevoTitulo: String, nuevaDescripcion: String, ahora: Long) {
        solicitudDao.actualizarInformacion(id, nuevoTitulo, nuevaDescripcion, ahora)
    }

    suspend fun insertar(solicitud: Solicitud, esBorrador: Boolean = false): Long {
        return solicitudDao.insertarSolicitud(solicitud.toEntity(esBorrador))
    }

    suspend fun obtenerPorId(id: Int): Solicitud? {
        return solicitudDao.obtenerSolicitudPorId(id)?.toDomain()
    }

    suspend fun obtenerPorCodigo(codigo: String): Solicitud? {
        return solicitudDao.obtenerPorCodigoSeguimiento(codigo.trim().uppercase())?.toDomain()
    }

    fun filtrar(
        estado: EstadoSolicitud? = null,
        prioridad: PrioridadSolicitud? = null,
        categoria: CategoriaSolicitud? = null,
        clienteId: Int? = null,
        tecnicoId: Int? = null
    ): Flow<List<Solicitud>> {
        return solicitudDao.filtrarSolicitudes(
            estado = estado?.name,
            prioridad = prioridad?.name,
            categoria = categoria?.name,
            clienteId = clienteId,
            tecnicoId = tecnicoId
        ).map { lista -> lista.map { it.toDomain() } }
    }

    fun obtenerAtrasadas(): Flow<List<Solicitud>> {
        val ahora = System.currentTimeMillis()
        return solicitudDao.obtenerSolicitudesAtrasadas(ahora).map { lista ->
            lista.map { it.toDomain() }
        }
    }

    fun obtenerBorradores(clienteId: Int): Flow<List<Solicitud>> {
        return solicitudDao.obtenerBorradoresPorCliente(clienteId).map { lista ->
            lista.map { it.toDomain() }
        }
    }

    // --- Métodos de actualización de estado atómico ---
    suspend fun actualizarEstado(id: Int, nuevoEstado: EstadoSolicitud, ahora: Long) {
        solicitudDao.actualizarEstado(id, nuevoEstado.name, ahora)
    }

    suspend fun clasificarYPriorizar(
        id: Int,
        categoria: CategoriaSolicitud,
        prioridad: PrioridadSolicitud,
        ahora: Long
    ) {
        val nuevaFechaLimite = calcularSlaFechaLimite(prioridad, ahora)
        solicitudDao.clasificarYPriorizar(
            solicitudId = id,
            categoria = categoria.name,
            prioridad = prioridad.name,
            nuevaFechaLimite = nuevaFechaLimite,
            ts = ahora
        )
    }

    suspend fun asignarTecnico(id: Int, tecnicoId: Int, ahora: Long) {
        solicitudDao.asignarTecnico(id, tecnicoId, ahora)
    }

    suspend fun desasignarTecnico(id: Int, ahora: Long) {
        solicitudDao.desasignarTecnico(id, ahora)
    }

    suspend fun registrarResolucion(id: Int, resolucion: String, ahora: Long) {
        solicitudDao.registrarResolucion(id, resolucion, ahora)
    }

    suspend fun confirmarCierre(id: Int, conforme: Boolean, ahora: Long) {
        solicitudDao.confirmarCierre(id, conforme, ahora)
    }

    suspend fun solicitarReapertura(id: Int, motivo: String, ahora: Long) {
        solicitudDao.solicitarReapertura(id, motivo, ahora)
    }

    suspend fun cancelar(id: Int, motivo: String, ahora: Long) {
        solicitudDao.cancelarSolicitud(id, motivo, ahora)
    }

    // --- Métricas para Panel Administrador ---
    fun contarPorEstado(estado: EstadoSolicitud): Flow<Int> =
        solicitudDao.contarPorEstado(estado.name)

    fun contarAtrasadas(): Flow<Int> =
        solicitudDao.contarAtrasadas(System.currentTimeMillis())

    fun contarPorPrioridad(prioridad: PrioridadSolicitud): Flow<Int> =
        solicitudDao.contarPorPrioridad(prioridad.name)

    fun contarPorTecnico(): Flow<List<ConteoPorTecnicoDto>> =
        solicitudDao.contarSolicitudesPorTecnico()

    // --- Funciones de utilidad interna ---
    fun generarCodigoUnico(): String {
        val anio = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val correlativo = Random.nextInt(1000, 9999)
        return "SOL-$anio-$correlativo"
    }

    fun calcularSlaFechaLimite(prioridad: PrioridadSolicitud, desdeMs: Long): Long {
        val horaMs = 3600 * 1000L
        val horas = when (prioridad) {
            PrioridadSolicitud.URGENTE -> 6L
            PrioridadSolicitud.ALTA -> 24L
            PrioridadSolicitud.MEDIA -> 72L
            PrioridadSolicitud.BAJA -> 168L
        }
        return desdeMs + (horas * horaMs)
    }
}
