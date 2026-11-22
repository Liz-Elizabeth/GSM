package com.example.gsm.viewmodel

import com.example.gsm.data.local.entity.UsuarioEntity
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.repository.UsuarioRepository
import com.example.gsm.fakes.FakeUsuarioDao
import com.example.gsm.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias de AuthViewModel usando FakeUsuarioDao (sin dispositivo ni Firestore).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeUsuarioDao: FakeUsuarioDao
    private lateinit var usuarioRepo: UsuarioRepository
    private lateinit var authVm: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeUsuarioDao = FakeUsuarioDao()
        runBlocking {
            fakeUsuarioDao.insertarUsuario(
                UsuarioEntity(
                    id = 1,
                    nombre = "Camila Rojas",
                    correo = "cliente@pyme.cl",
                    password = "secretPassword",
                    rol = RolUsuario.CLIENTE.name,
                    empresa = "Pyme SpA",
                    activo = true,
                    debeCambiarPassword = false
                )
            )
            fakeUsuarioDao.insertarUsuario(
                UsuarioEntity(
                    id = 2,
                    nombre = "Usuario Nuevo",
                    correo = "nuevo@gsm.cl",
                    password = UsuarioRepository.DEFAULT_PASSWORD,
                    rol = RolUsuario.TECNICO.name,
                    activo = true,
                    debeCambiarPassword = true
                )
            )
        }
        usuarioRepo = UsuarioRepository(fakeUsuarioDao)
        authVm = AuthViewModel(usuarioRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun iniciarSesion_credencialesValidas_actualizaUsuarioActualSinError() = runTest(testDispatcher) {
        authVm.iniciarSesion("cliente@pyme.cl", "secretPassword")
        advanceUntilIdle()

        assertNotNull(authVm.usuarioActual)
        assertEquals("cliente@pyme.cl", authVm.usuarioActual?.correo)
        assertNull(authVm.mensajeError)
        assertFalse(authVm.cargando)
    }

    @Test
    fun iniciarSesion_passwordIncorrecta_exponeMensajeError() = runTest(testDispatcher) {
        authVm.iniciarSesion("cliente@pyme.cl", "claveMala")
        advanceUntilIdle()

        assertNull(authVm.usuarioActual)
        assertNotNull(authVm.mensajeError)
    }

    @Test
    fun registrarUsuario_passwordsDistintas_marcaErrorSinCrearCuenta() = runTest(testDispatcher) {
        var exitoInvocado = false
        authVm.registrarUsuario(
            nombre = "Ana Pérez",
            correo = "ana@empresa.cl",
            empresa = "Empresa SpA",
            telefono = "+56912345678",
            password = "passValida1",
            confirmarPassword = "otraPassword",
            onExito = { exitoInvocado = true }
        )

        assertFalse(exitoInvocado)
        assertEquals("Las contraseñas no coinciden.", authVm.errorRegistro)
    }

    @Test
    fun registrarUsuario_datosValidos_creaClienteEIniciaSesion() = runTest(testDispatcher) {
        var rolCreado: RolUsuario? = null
        authVm.registrarUsuario(
            nombre = "Ana Pérez",
            correo = "ana@empresa.cl",
            empresa = "Empresa SpA",
            telefono = "+56912345678",
            password = "passValida1",
            confirmarPassword = "passValida1",
            onExito = { rolCreado = it.rol }
        )
        advanceUntilIdle()

        assertEquals(RolUsuario.CLIENTE, rolCreado)
        assertEquals("ana@empresa.cl", authVm.usuarioActual?.correo)
        assertNull(authVm.errorRegistro)
    }

    @Test
    fun cambiarPasswordObligatorio_nuevaClaveValida_remueveFlagYActualizaUsuario() = runTest(testDispatcher) {
        authVm.iniciarSesion("nuevo@gsm.cl", UsuarioRepository.DEFAULT_PASSWORD)
        advanceUntilIdle()
        assertTrue(authVm.usuarioActual?.debeCambiarPassword == true)

        var completado = false
        authVm.cambiarPasswordObligatorio("nuevaClaveSegura9", "nuevaClaveSegura9") {
            completado = true
        }
        advanceUntilIdle()

        assertTrue(completado)
        assertFalse(authVm.usuarioActual?.debeCambiarPassword ?: true)
        assertNull(authVm.errorPasswordObligatoria)
    }

    @Test
    fun cerrarSesion_limpiaUsuarioActualYEstadosDeError() = runTest(testDispatcher) {
        authVm.iniciarSesion("cliente@pyme.cl", "secretPassword")
        advanceUntilIdle()
        assertNotNull(authVm.usuarioActual)

        authVm.cerrarSesion()

        assertNull(authVm.usuarioActual)
        assertTrue(authVm.equipoEmpresa.isEmpty())
        assertNull(authVm.mensajeError)
    }
}
