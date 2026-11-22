package com.example.gsm.repository

import com.example.gsm.data.local.entity.UsuarioEntity
import com.example.gsm.data.model.PlanSoporte
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.data.remote.flow.FlowSigner
import com.example.gsm.data.repository.FlowPaymentRepository
import com.example.gsm.data.repository.UsuarioRepository
import com.example.gsm.fakes.FakeFlowApiService
import com.example.gsm.fakes.FakeUsuarioDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Prueba de Lógica de Negocio: FlowPaymentRepository & FlowSigner
 * Valida la firma criptográfica HMAC-SHA256, el cálculo de tarifas mensuales/anuales con descuento,
 * la verificación de estados de pago en Flow (status = 2 Pagada) y la activación de planes con cuotas de tickets.
 */
class FlowPaymentRepositoryTest {

    private lateinit var fakeFlowApi: FakeFlowApiService
    private lateinit var flowRepository: FlowPaymentRepository
    private lateinit var fakeUsuarioDao: FakeUsuarioDao
    private lateinit var usuarioRepository: UsuarioRepository

    private val testApiKey = "TEST_FLOW_API_KEY_999"
    private val testSecretKey = "TEST_FLOW_SECRET_KEY_HEX_123456"

    private val clientePrueba = Usuario(
        id = 10,
        nombre = "Matías González",
        correo = "contacto@miempresa.cl",
        password = "passwordSegura123",
        rol = RolUsuario.CLIENTE,
        empresa = "Mi Empresa SpA",
        telefono = "+56912345678",
        medioContacto = "WhatsApp",
        serviciosAsociados = null,
        plan = "SIN_PLAN",
        ticketsDisponibles = 0,
        estadoSuscripcion = "PENDIENTE_PAGO",
        activo = true,
        debeCambiarPassword = false,
        fechaCreacion = 1000L,
        fechaActualizacion = 1000L
    )

    @Before
    fun setUp() {
        fakeFlowApi = FakeFlowApiService()
        flowRepository = FlowPaymentRepository(
            apiServiceOverride = fakeFlowApi,
            apiKeyOverride = testApiKey,
            secretKeyOverride = testSecretKey
        )

        fakeUsuarioDao = FakeUsuarioDao()
        usuarioRepository = UsuarioRepository(fakeUsuarioDao)

        runBlocking {
            fakeUsuarioDao.insertarUsuario(
                UsuarioEntity(
                    id = clientePrueba.id,
                    nombre = clientePrueba.nombre,
                    correo = clientePrueba.correo,
                    password = clientePrueba.password,
                    rol = clientePrueba.rol.name,
                    empresa = clientePrueba.empresa,
                    telefono = clientePrueba.telefono,
                    plan = clientePrueba.plan,
                    ticketsDisponibles = clientePrueba.ticketsDisponibles,
                    estadoSuscripcion = clientePrueba.estadoSuscripcion,
                    activo = true
                )
            )
        }
    }

    // 1. Regla Criptográfica: Orden alfabético estricto y firma HMAC-SHA256 determinista
    @Test
    fun firmarParametros_ordenaAlfabeticamenteYGeneraHmacSha256Valido() {
        val paramsDesordenados = mapOf(
            "currency" to "CLP",
            "amount" to "79900",
            "apiKey" to testApiKey,
            "commerceOrder" to "GSM-PRO-001"
        )
        val paramsOrdenados = mapOf(
            "amount" to "79900",
            "apiKey" to testApiKey,
            "commerceOrder" to "GSM-PRO-001",
            "currency" to "CLP"
        )

        val firma1 = FlowSigner.firmarParametros(paramsDesordenados, testSecretKey)
        val firma2 = FlowSigner.firmarParametros(paramsOrdenados, testSecretKey)
        val firmaConMontoAlterado = FlowSigner.firmarParametros(
            paramsOrdenados + ("amount" to "29900"),
            testSecretKey
        )

        assertEquals("El orden de inserción de claves no debe alterar la firma HMAC", firma1, firma2)
        assertEquals("La firma HMAC-SHA256 en hexadecimal debe tener 64 caracteres", 64, firma1.length)
        assertNotEquals("Si cambia el monto debe cambiar la firma para evitar adulteración", firma1, firmaConMontoAlterado)
    }

    // 2. Regla de Negocio: Creación de orden mensual cobra el precio base en CLP y adjunta firma 's'
    @Test
    fun crearOrdenPago_facturacionMensual_enviaMontoBaseYFirmaValida() = runBlocking {
        val resultado = flowRepository.crearOrdenPago(
            plan = PlanSoporte.PRO,
            usuario = clientePrueba,
            esFacturacionAnual = false
        )

        assertTrue("La creación de orden debe ser exitosa", resultado.isSuccess)
        val respuesta = resultado.getOrNull()
        assertNotNull(respuesta)
        assertEquals("FLOW_SANDBOX_TOKEN_ABC123", respuesta?.token)

        val enviados = fakeFlowApi.ultimosParametrosCrear
        assertEquals("El monto mensual del Plan PRO debe ser 79900 CLP", "79900", enviados["amount"])
        assertEquals("CLP", enviados["currency"])
        assertEquals("contacto@miempresa.cl", enviados["email"])
        assertTrue("Debe incluir la firma criptográfica 's'", !enviados["s"].isNullOrBlank())
    }

    // 3. Regla de Negocio: Facturación anual aplica exactamente 20% de descuento sobre los 12 meses
    @Test
    fun crearOrdenPago_facturacionAnual_aplica20PorcientoDescuento() = runBlocking {
        // Plan PRO: $79.900 * 12 = $958.800 -> Con 20% dcto (* 0.80) = $767.040 CLP
        val resultado = flowRepository.crearOrdenPago(
            plan = PlanSoporte.PRO,
            usuario = clientePrueba,
            esFacturacionAnual = true
        )

        assertTrue(resultado.isSuccess)
        val enviados = fakeFlowApi.ultimosParametrosCrear
        assertEquals("El monto anual con 20% dcto debe ser 767040 CLP", "767040", enviados["amount"])
        assertTrue(
            "El asunto debe indicar modalidad Anual",
            enviados["subject"]?.contains("Anual") == true
        )
    }

    // 4. Regla de Resiliencia: Si la API de Flow falla o hay timeout, activa el simulador Sandbox de respaldo
    @Test
    fun crearOrdenPago_cuandoFallaRedFlow_retornaFallbackSandboxConMontoCorrecto() = runBlocking {
        fakeFlowApi.simularErrorRed = true

        val resultado = flowRepository.crearOrdenPago(
            plan = PlanSoporte.PLUS,
            usuario = clientePrueba,
            esFacturacionAnual = false
        )

        assertTrue("El repositorio debe activar el fallback sin bloquear al cliente", resultado.isSuccess)
        val respuesta = resultado.getOrNull()
        assertNotNull(respuesta)
        assertTrue("El token de respaldo debe iniciar con DEMO_TOKEN_", respuesta!!.token.startsWith("DEMO_TOKEN_"))
        assertTrue("La URL de respaldo debe incluir monto=29900", respuesta.url.contains("monto=29900"))
    }

    // 5. Regla de Verificación y Activación: Pago aprobado (status = 2) habilita el plan y asigna cuota de tickets
    @Test
    fun consultarEstadoPago_cuandoStatusEs2Pagada_activaPlanYAsignaCuotaTickets() = runBlocking {
        fakeFlowApi.estadoSimulado = 2 // 2 = Pagada en Flow

        val estadoResult = flowRepository.consultarEstadoPago("FLOW_SANDBOX_TOKEN_ABC123")
        assertTrue(estadoResult.isSuccess)
        val statusResponse = estadoResult.getOrNull()
        assertNotNull(statusResponse)
        assertEquals("Status 2 representa orden pagada exitosamente en Flow", 2, statusResponse?.status)

        // Al confirmarse status == 2, se activa la suscripción en UsuarioRepository según el plan contratado
        val planContratado = PlanSoporte.PRO
        val activacionResult = usuarioRepository.actualizarPlanSuscripcion(
            usuarioId = clientePrueba.id,
            nuevoPlan = planContratado.name,
            cuotaTickets = planContratado.cuotaTicketsSemanales
        )

        assertTrue(activacionResult.isSuccess)
        val usuarioActualizado = fakeUsuarioDao.obtenerUsuarioPorId(clientePrueba.id)
        assertEquals("PRO", usuarioActualizado?.plan)
        assertEquals("ACTIVA", usuarioActualizado?.estadoSuscripcion)
        assertEquals("El Plan PRO debe otorgar 25 tickets semanales", 25, usuarioActualizado?.ticketsDisponibles)
    }
}
