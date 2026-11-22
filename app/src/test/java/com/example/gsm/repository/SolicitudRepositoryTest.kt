package com.example.gsm.repository

import com.example.gsm.data.local.entity.SolicitudEntity
import com.example.gsm.data.model.CategoriaSolicitud
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.repository.SolicitudLocalRepository
import com.example.gsm.fakes.FakeSolicitudDao
import com.example.gsm.fakes.FakeUsuarioDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Prueba de Lógica de Negocio: SolicitudLocalRepository
 * Valida acuerdos de nivel de servicio (SLA), generación de códigos institucionales y ciclo de vida de tickets.
 */
class SolicitudRepositoryTest {

    private lateinit var fakeSolicitudDao: FakeSolicitudDao
    private lateinit var fakeUsuarioDao: FakeUsuarioDao
    private lateinit var solicitudRepository: SolicitudLocalRepository

    @Before
    fun setUp() {
        fakeSolicitudDao = FakeSolicitudDao()
        fakeUsuarioDao = FakeUsuarioDao()
        solicitudRepository = SolicitudLocalRepository(fakeSolicitudDao, fakeUsuarioDao)
    }

    // 1. Regla: Cálculo exacto de plazos SLA según la prioridad asignada
    @Test
    fun calcularSlaFechaLimite_calculaHorasExactasSegunPrioridad() {
        val horaBase = 1000000000L
        val unaHoraMs = 3600 * 1000L

        val limiteUrgente = solicitudRepository.calcularSlaFechaLimite(PrioridadSolicitud.URGENTE, horaBase)
        val limiteAlta = solicitudRepository.calcularSlaFechaLimite(PrioridadSolicitud.ALTA, horaBase)
        val limiteMedia = solicitudRepository.calcularSlaFechaLimite(PrioridadSolicitud.MEDIA, horaBase)
        val limiteBaja = solicitudRepository.calcularSlaFechaLimite(PrioridadSolicitud.BAJA, horaBase)

        assertEquals("Urgente debe ser +6 horas", horaBase + (6 * unaHoraMs), limiteUrgente)
        assertEquals("Alta debe ser +24 horas", horaBase + (24 * unaHoraMs), limiteAlta)
        assertEquals("Media debe ser +72 horas", horaBase + (72 * unaHoraMs), limiteMedia)
        assertEquals("Baja debe ser +168 horas", horaBase + (168 * unaHoraMs), limiteBaja)
    }

    // 2. Regla: Formato oficial del código correlativo institucional (SOL-YYYY-XXXX)
    @Test
    fun generarCodigoUnico_cumpleFormatoOficialInstitucional() {
        val anioActual = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val codigo = solicitudRepository.generarCodigoUnico()

        assertTrue("El código debe iniciar con SOL-$anioActual-", codigo.startsWith("SOL-$anioActual-"))
        assertTrue("El código debe coincidir con el patrón SOL-YYYY-XXXX", codigo.matches(Regex("^SOL-\\d{4}-\\d{4}$")))
    }

    // 3. Regla del ciclo de vida: Asignar un técnico actualiza el estado a ASIGNADA
    @Test
    fun asignarTecnico_actualizaEstadoAAsignada() = runBlocking {
        val idTicket = fakeSolicitudDao.insertarSolicitud(
            SolicitudEntity(
                id = 1,
                codigoSeguimiento = "SOL-2026-1001",
                titulo = "Cámara de seguridad sin imagen",
                descripcion = "Falla de alimentación en switch PoE",
                categoria = CategoriaSolicitud.INCIDENTE_TECNICO.name,
                clienteId = 1,
                tecnicoId = null,
                estado = EstadoSolicitud.RECIBIDA.name,
                prioridad = PrioridadSolicitud.ALTA.name,
                fechaCreacion = 1000L,
                fechaActualizacion = 1000L
            )
        )

        val ahora = 2000L
        solicitudRepository.asignarTecnico(idTicket.toInt(), tecnicoId = 5, ahora = ahora)

        val ticketActualizado = fakeSolicitudDao.obtenerSolicitudPorId(idTicket.toInt())
        assertNotNull(ticketActualizado)
        assertEquals("El estado debe cambiar a ASIGNADA", "ASIGNADA", ticketActualizado?.estado)
        assertEquals("Debe registrar el ID del técnico asignado", 5, ticketActualizado?.tecnicoId)
    }

    // 4. Regla del ciclo de vida: Registrar solución pasa el estado a RESUELTA
    @Test
    fun registrarResolucion_actualizaEstadoAResuelta() = runBlocking {
        val idTicket = fakeSolicitudDao.insertarSolicitud(
            SolicitudEntity(
                id = 2,
                codigoSeguimiento = "SOL-2026-1002",
                titulo = "Revisión preventiva de servidor",
                descripcion = "Mantenimiento mensual",
                categoria = CategoriaSolicitud.MANTENIMIENTO_WEB.name,
                clienteId = 1,
                tecnicoId = 2,
                estado = EstadoSolicitud.EN_PROCESO.name,
                prioridad = PrioridadSolicitud.MEDIA.name,
                fechaCreacion = 1000L,
                fechaActualizacion = 1000L
            )
        )

        val ahora = 4000L
        val detalleSolucion = "Se aplicaron actualizaciones de seguridad y el servicio opera normal."
        solicitudRepository.registrarResolucion(idTicket.toInt(), resolucion = detalleSolucion, ahora = ahora)

        val ticketResuelto = fakeSolicitudDao.obtenerSolicitudPorId(idTicket.toInt())
        assertNotNull(ticketResuelto)
        assertEquals("El estado debe ser RESUELTA", "RESUELTA", ticketResuelto?.estado)
        assertEquals(detalleSolucion, ticketResuelto?.resolucion)
        assertEquals(ahora, ticketResuelto?.fechaResolucion)
    }
}
