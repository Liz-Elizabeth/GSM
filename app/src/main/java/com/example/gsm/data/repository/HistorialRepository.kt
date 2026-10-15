package com.example.gsm.data.repository

import com.example.gsm.data.local.dao.HistorialDao
import com.example.gsm.data.local.entity.HistorialEntity
import com.example.gsm.model.Historial
import com.example.gsm.model.RolUsuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Sub-repositorio encargado de la bitácora de auditoría y trazabilidad cronológica.
 * Aplica la regla de negocio de ocultar notas internas a clientes.
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
            fechaCambio = ahora
        )
        return historialDao.insertarHistorial(entidad)
    }

    /**
     * Retorna el historial. Si el rol es CLIENTE, omite comentarios internos (PDF Pág. 6).
     */
    fun obtenerHistorial(solicitudId: Int, rolUsuario: RolUsuario): Flow<List<Historial>> {
        val flow = if (rolUsuario == RolUsuario.CLIENTE) {
            historialDao.obtenerHistorialVisibleCliente(solicitudId)
        } else {
            historialDao.obtenerHistorialCompleto(solicitudId)
        }
        return flow.map { lista -> lista.map { it.toDomain() } }
    }

}