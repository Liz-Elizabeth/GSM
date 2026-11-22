package com.example.gsm.fakes

import com.example.gsm.data.remote.flow.FlowApiService
import com.example.gsm.data.remote.flow.FlowCreateResponse
import com.example.gsm.data.remote.flow.FlowPaymentData
import com.example.gsm.data.remote.flow.FlowStatusResponse
import java.io.IOException

/**
 * Fake de FlowApiService para pruebas unitarias de lógica de negocio sin llamadas de red reales.
 * Permite inspeccionar los parámetros enviados (firma HMAC, montos, orden) y simular respuestas o caídas de red.
 */
class FakeFlowApiService : FlowApiService {

    var ultimosParametrosCrear: Map<String, String> = emptyMap()
        private set

    var ultimaApiKeyConsulta: String? = null
        private set
    var ultimoTokenConsulta: String? = null
        private set
    var ultimaFirmaConsulta: String? = null
        private set

    var simularErrorRed: Boolean = false
    var estadoSimulado: Int = 2 // 1: Pendiente, 2: Pagada, 3: Rechazada, 4: Anulada
    var montoSimulado: Double = 79900.0

    override suspend fun crearOrdenPago(params: Map<String, String>): FlowCreateResponse {
        ultimosParametrosCrear = params.toMap()

        if (simularErrorRed) {
            throw IOException("Simulación de caída o timeout en Flow Sandbox")
        }

        return FlowCreateResponse(
            url = "https://sandbox.flow.cl/app/web/pay.php",
            token = "FLOW_SANDBOX_TOKEN_ABC123",
            flowOrder = 100500L
        )
    }

    override suspend fun consultarEstadoPago(
        apiKey: String,
        token: String,
        firma: String
    ): FlowStatusResponse {
        ultimaApiKeyConsulta = apiKey
        ultimoTokenConsulta = token
        ultimaFirmaConsulta = firma

        if (simularErrorRed) {
            throw IOException("Error de conexión al consultar estado en Flow")
        }

        return FlowStatusResponse(
            flowOrder = 100500L,
            commerceOrder = "GSM-PRO-123456",
            requestDate = "2026-11-13 11:30:00",
            status = estadoSimulado,
            subject = "Suscripción GSM Soporte PRO",
            currency = "CLP",
            amount = montoSimulado,
            payer = "cliente@empresa.cl",
            paymentData = FlowPaymentData(
                date = "2026-11-13 11:31:00",
                media = "Webpay",
                amount = montoSimulado,
                currency = "CLP"
            )
        )
    }
}
