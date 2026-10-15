package com.example.gsm.data.repository

import com.example.gsm.model.Solicitud
import kotlinx.coroutines.delay


/**
 * Construye el mensaje estructurado formal sin exponer datos sensibles.
 */
class WhatsAppRepository {

    /**
     * Construye el mensaje estructurado formal sin exponer datos sensibles.
     */
    fun crearMensajeEstructurado(solicitud: Solicitud, nombreCliente: String? = null): String {
        return buildString {
            appendLine(" *SOLICITUD DE ASISTENCIA GSM*")
            appendLine("----------------------------------")
            appendLine(" *Código:* ${solicitud.codigoSeguimiento}")
            appendLine(" *Título:* ${solicitud.titulo}")
            appendLine("️ *Categoría:* ${solicitud.categoria.descripcion}")
            if (!nombreCliente.isNullOrBlank()) {
                appendLine(" *Cliente:* $nombreCliente")
            }
            appendLine(" *Prioridad:* ${solicitud.prioridad.name}")
            appendLine("----------------------------------")
            appendLine(" Puedes seguir el estado en la app GSM.")
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
