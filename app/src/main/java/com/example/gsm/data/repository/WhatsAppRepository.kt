package com.example.gsm.data.repository

import com.example.gsm.data.model.Solicitud
import kotlinx.coroutines.delay

/**
 * Construye el mensaje estructurado formal sin exponer datos sensibles (PDF Pág. 13).
 */
class WhatsAppRepository {

    /**
     * Construye el mensaje estructurado con código, título, categoría y referencia de seguimiento
     * (Cumple Criterios 7, 8, 38 y 39 del PDF).
     */
    fun crearMensajeEstructurado(solicitud: Solicitud, nombreCliente: String? = null): String {
        return buildString {
            appendLine("🔔 *SOLICITUD DE ASISTENCIA GSM*")
            appendLine("----------------------------------")
            appendLine("📌 *Código:* ${solicitud.codigoSeguimiento}")
            appendLine("📝 *Título:* ${solicitud.titulo}")
            appendLine("🏷️ *Categoría:* ${solicitud.categoria.descripcion}")
            if (!nombreCliente.isNullOrBlank()) {
                appendLine("👤 *Cliente (Ficticio):* $nombreCliente")
            }
            appendLine("⚡ *Prioridad:* ${solicitud.prioridad.name}")
            appendLine("🔗 *Seguimiento:* https://gsm-web-delta.vercel.app/seguimiento/${solicitud.codigoSeguimiento}")
            appendLine("----------------------------------")
            appendLine("Puedes consultar el estado en la app GSM con tu código único.")
        }
    }

    /**
     * Simula el retardo de envío por red (ej: 1.2 segundos).
     * Devuelve true cuando finaliza para disparar la animación en la pantalla.
     */
    suspend fun simularEnvio(solicitud: Solicitud, duracionSimuladaMs: Long = 1200L): Boolean {
        delay(duracionSimuladaMs)
        return true
    }
}
