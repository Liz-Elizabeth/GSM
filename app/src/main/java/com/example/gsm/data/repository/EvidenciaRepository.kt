package com.example.gsm.data.repository

import com.example.gsm.data.local.dao.EvidenciaDao
import com.example.gsm.data.local.entity.EvidenciaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Sub-repositorio encargado de gestionar evidencias y archivos de prueba.
 * Cumple las restricciones de seguridad del PDF (Pág. 16) y OWASP M4:
 * - Limita los formatos permitidos.
 * - Limita el tamaño máximo de los archivos (5 MB).
 */
class EvidenciaRepository(
    private val evidenciaDao: EvidenciaDao
) {

    companion object {
        // Formatos permitidos según buenas prácticas OWASP y Pág. 16 del PDF
        val FORMATOS_PERMITIDOS = setOf(
            "image/jpeg",
            "image/png",
            "application/pdf",
            "text/plain"
        )
        const val TAMANO_MAXIMO_BYTES = 5L * 1024L * 1024L // 5 MB máximo
    }

    suspend fun adjuntarEvidenciaFicticia(
        solicitudId: Int,
        usuarioId: Int,
        nombreArchivo: String,
        tipoArchivo: String = "image/png",
        tamanoBytes: Long = 250_000L, // ~250 KB por defecto para simulación
        descripcion: String? = null,
        ahora: Long = System.currentTimeMillis()
    ): Result<Long> {
        return try {
            val nombreLimpio = nombreArchivo.trim()

            require(nombreLimpio.isNotBlank()) {
                "El nombre del archivo no puede estar vacío."
            }
            require(tipoArchivo in FORMATOS_PERMITIDOS) {
                "Formato no permitido ($tipoArchivo). Solo se admiten imágenes (JPG/PNG), PDF o TXT."
            }
            require(tamanoBytes in 1..TAMANO_MAXIMO_BYTES) {
                "El tamaño del archivo supera el límite permitido de 5 MB."
            }

            val entidad = EvidenciaEntity(
                solicitudId = solicitudId,
                usuarioId = usuarioId,
                nombreArchivo = nombreLimpio,
                tipoArchivo = tipoArchivo,
                tamanoBytes = tamanoBytes,
                uriLocal = "file:///ficticio/evidencias/$nombreLimpio",
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

    fun obtenerPorSolicitud(solicitudId: Int): Flow<List<EvidenciaEntity>> =
        evidenciaDao.obtenerEvidenciasPorSolicitud(solicitudId)

    suspend fun actualizarEstadoRevision(evidenciaId: Int, nuevoEstado: String) {
        require(nuevoEstado in setOf("PENDIENTE", "APROBADA", "RECHAZADA")) {
            "Estado de revisión inválido."
        }
        evidenciaDao.actualizarEstadoRevision(evidenciaId, nuevoEstado)
    }
}
