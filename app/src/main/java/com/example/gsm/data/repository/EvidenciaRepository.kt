package com.example.gsm.data.repository

import com.example.gsm.data.local.dao.EvidenciaDao
import com.example.gsm.data.local.entity.EvidenciaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Sub-repositorio encargado de gestionar evidencias y archivos reales o de prueba.
 * Permite cargar cualquier tipo de archivo (imágenes, PDFs, logs, documentos)
 * y protege la app limitando el tamaño a un máximo de 15 MB (OWASP Input Validation).
 */
class EvidenciaRepository(
    private val evidenciaDao: EvidenciaDao
) {

    companion object {
        const val TAMANO_MAXIMO_BYTES = 15L * 1024L * 1024L // 15 MB máximo
    }

    /**
     * Adjunta un archivo real o simulado a una solicitud.
     */
    suspend fun adjuntarEvidencia(
        solicitudId: Int,
        usuarioId: Int,
        nombreArchivo: String,
        tipoArchivo: String = "image/png",
        tamanoBytes: Long = 250_000L,
        uriLocal: String? = null,
        descripcion: String? = null,
        ahora: Long = System.currentTimeMillis()
    ): Result<Long> {
        return try {
            val nombreLimpio = nombreArchivo.trim()

            require(nombreLimpio.isNotBlank()) {
                "El nombre del archivo no puede estar vacío."
            }
            require(tamanoBytes in 1..TAMANO_MAXIMO_BYTES) {
                "El tamaño del archivo supera el límite permitido de 15 MB."
            }

            val entidad = EvidenciaEntity(
                solicitudId = solicitudId,
                usuarioId = usuarioId,
                nombreArchivo = nombreLimpio,
                tipoArchivo = tipoArchivo.ifBlank { "application/octet-stream" },
                tamanoBytes = tamanoBytes,
                uriLocal = uriLocal ?: "file:///evidencias/$nombreLimpio",
                descripcion = descripcion?.trim(),
                estadoRevision = "PENDIENTE",
                fechaIncorporacion = ahora
            )

            val id = evidenciaDao.insertarEvidencia(entidad)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adjuntarEvidenciaFicticia(
        solicitudId: Int,
        usuarioId: Int,
        nombreArchivo: String,
        tipoArchivo: String = "image/png",
        tamanoBytes: Long = 250_000L,
        descripcion: String? = null,
        ahora: Long = System.currentTimeMillis()
    ): Result<Long> {
        return adjuntarEvidencia(
            solicitudId = solicitudId,
            usuarioId = usuarioId,
            nombreArchivo = nombreArchivo,
            tipoArchivo = tipoArchivo,
            tamanoBytes = tamanoBytes,
            uriLocal = null,
            descripcion = descripcion,
            ahora = ahora
        )
    }

    fun obtenerPorSolicitud(solicitudId: Int): Flow<List<EvidenciaEntity>> =
        evidenciaDao.obtenerEvidenciasPorSolicitud(solicitudId)

    suspend fun actualizarEstadoRevision(evidenciaId: Int, nuevoEstado: String) {
        require(nuevoEstado in setOf("PENDIENTE", "APROBADA", "RECHAZADA")) {
            "Estado de revisión inválido."
        }
        evidenciaDao.actualizarEstadoRevision(evidenciaId, nuevoEstado)
    }
}
