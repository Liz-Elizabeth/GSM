package com.example.gsm.data.repository

import com.example.gsm.data.local.dao.HistorialDao
import com.example.gsm.data.local.entity.HistorialEntity
import com.example.gsm.data.mapper.toDomain
import com.example.gsm.data.model.Historial
import com.example.gsm.data.model.RolUsuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Sub-repositorio encargado de la bitácora de auditoría y trazabilidad cronológica.
 * Aplica la regla de negocio de ocultar notas internas a clientes (PDF Pág. 6).
 */
class HistorialRepository(
    private val historialDao: HistorialDao
) {

    suspend fun registrarEvento(
        solicitudId: Int,
        usuarioId: Int,
        perfil: RolUsuario,
        estadoAnterior: String?,
        estadoNuevo: String,
        accion: String,
        comentario: String,
        esComentarioInterno: Boolean = false,
        evidenciaAdjunta: String? = null,
        ahora: Long = System.currentTimeMillis()
    ): Long {
        val entidad = HistorialEntity(
            solicitudId = solicitudId,
            usuarioId = usuarioId,
            estadoAnterior = estadoAnterior,
            estadoNuevo = estadoNuevo,
            accion = accion,
            comentario = comentario,
            perfilUsuario = perfil.name,
            esComentarioInterno = esComentarioInterno,
            evidenciaAdjunta = evidenciaAdjunta,
            fechaCambio = ahora
        )
        return historialDao.insertarHistorial(entidad)
    }

    suspend fun registrarSiNoExiste(
        solicitudId: Int,
        usuarioId: Int,
        perfil: RolUsuario,
        estadoAnterior: String?,
        estadoNuevo: String,
        accion: String,
        comentario: String,
        esComentarioInterno: Boolean = false,
        evidenciaAdjunta: String? = null,
        fechaCambio: Long
    ) {
        kotlin.runCatching {
            if (solicitudId <= 0) return
            val duplicados = historialDao.contarMensajesDuplicados(solicitudId, fechaCambio, comentario.trim())
            if (duplicados == 0) {
                historialDao.insertarHistorial(
                    HistorialEntity(
                        solicitudId = solicitudId,
                        usuarioId = if (usuarioId > 0) usuarioId else 1,
                        estadoAnterior = estadoAnterior,
                        estadoNuevo = estadoNuevo,
                        accion = accion,
                        comentario = comentario.trim(),
                        perfilUsuario = perfil.name,
                        esComentarioInterno = esComentarioInterno,
                        evidenciaAdjunta = evidenciaAdjunta,
                        fechaCambio = fechaCambio
                    )
                )
            }
        }.onFailure { e ->
            android.util.Log.w("FirebaseSync", "Aviso seguro al guardar historial en Room: ${e.message}")
        }
    }

    /**
     * Retorna el historial. Si el rol es CLIENTE, omite comentarios internos (PDF Pág. 6).
     * Aplica deduplicación en memoria para garantizar que mensajes duplicados no se rendericen.
     */
    fun obtenerHistorial(solicitudId: Int, rolUsuario: RolUsuario): Flow<List<Historial>> {
        val flow = if (rolUsuario == RolUsuario.CLIENTE) {
            historialDao.obtenerHistorialVisibleCliente(solicitudId)
        } else {
            historialDao.obtenerHistorialCompleto(solicitudId)
        }
        return flow.map { lista ->
            lista
                .distinctBy { entidad ->
                    // Agrupa por id único de entidad si existe, o por combinación de solicitud, comentario y ventana de 3s
                    "${entidad.solicitudId}_${entidad.comentario.trim()}_${entidad.fechaCambio / 3000L}"
                }
                .map { it.toDomain() }
        }
    }
}
