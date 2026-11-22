package com.example.gsm.repository

import com.example.gsm.data.local.entity.UsuarioEntity
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.data.repository.UsuarioRepository
import com.example.gsm.fakes.FakeUsuarioDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Prueba de Lógica de Negocio: UsuarioRepository
 * Valida autenticación, seguridad de credenciales y reglas de control de acceso (RBAC).
 */
class UsuarioRepositoryTest {

    private lateinit var fakeUsuarioDao: FakeUsuarioDao
    private lateinit var usuarioRepository: UsuarioRepository

    private val adminUsuario = Usuario(
        id = 1,
        nombre = "Administrador GSM",
        correo = "admin@gsm.cl",
        password = "adminSecretPassword",
        rol = RolUsuario.ADMINISTRADOR,
        empresa = "GSM SpA",
        telefono = "+56911112222",
        medioContacto = "WhatsApp",
        serviciosAsociados = null,
        plan = "MAX",
        ticketsDisponibles = 999,
        estadoSuscripcion = "ACTIVA",
        activo = true,
        debeCambiarPassword = false,
        fechaCreacion = System.currentTimeMillis(),
        fechaActualizacion = System.currentTimeMillis()
    )

    private val tecnicoUsuario = Usuario(
        id = 2,
        nombre = "Técnico Soporte",
        correo = "tecnico@gsm.cl",
        password = "tecnicoPassword1",
        rol = RolUsuario.TECNICO,
        empresa = "GSM SpA",
        telefono = "+56933334444",
        medioContacto = "WhatsApp",
        serviciosAsociados = null,
        plan = "SIN_PLAN",
        ticketsDisponibles = 0,
        estadoSuscripcion = "INACTIVA",
        activo = true,
        debeCambiarPassword = false,
        fechaCreacion = System.currentTimeMillis(),
        fechaActualizacion = System.currentTimeMillis()
    )

    @Before
    fun setUp() {
        runBlocking {
            fakeUsuarioDao = FakeUsuarioDao()
            usuarioRepository = UsuarioRepository(fakeUsuarioDao)

            // Precarga de usuarios de prueba en el DAO en memoria
            fakeUsuarioDao.insertarUsuario(
                UsuarioEntity(
                    id = 1,
                    nombre = adminUsuario.nombre,
                    correo = adminUsuario.correo,
                    password = adminUsuario.password,
                    rol = adminUsuario.rol.name,
                    empresa = adminUsuario.empresa,
                    telefono = adminUsuario.telefono,
                    activo = true,
                    debeCambiarPassword = false,
                    fechaCreacion = adminUsuario.fechaCreacion,
                    fechaActualizacion = adminUsuario.fechaActualizacion
                )
            )

            fakeUsuarioDao.insertarUsuario(
                UsuarioEntity(
                    id = 2,
                    nombre = tecnicoUsuario.nombre,
                    correo = tecnicoUsuario.correo,
                    password = tecnicoUsuario.password,
                    rol = tecnicoUsuario.rol.name,
                    empresa = tecnicoUsuario.empresa,
                    telefono = tecnicoUsuario.telefono,
                    activo = true,
                    debeCambiarPassword = false,
                    fechaCreacion = tecnicoUsuario.fechaCreacion,
                    fechaActualizacion = tecnicoUsuario.fechaActualizacion
                )
            )
        }
    }

    // 1. Regla: Validación de campos vacíos al autenticar
    @Test
    fun autenticar_conCamposVacios_retornaFallo() = runBlocking {
        val resultado = usuarioRepository.autenticar("", "")
        assertTrue("No debe permitir login con campos vacíos", resultado.isFailure)
    }

    // 2. Regla: Cuentas desactivadas no pueden iniciar sesión
    @Test
    fun autenticar_conCuentaDesactivada_bloqueaAcceso() = runBlocking {
        fakeUsuarioDao.cambiarEstadoActivo(id = 2, activo = false)

        val resultado = usuarioRepository.autenticar("tecnico@gsm.cl", "tecnicoPassword1")
        assertTrue("Debe rechazar el acceso si la cuenta está inactiva", resultado.isFailure)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("desactivada") == true)
    }

    // 3. Regla: Credenciales correctas entregan el usuario de dominio
    @Test
    fun autenticar_conCredencialesValidas_retornaUsuarioExitoso() = runBlocking {
        val resultado = usuarioRepository.autenticar("admin@gsm.cl", "adminSecretPassword")

        assertTrue("El inicio de sesión debe ser exitoso", resultado.isSuccess)
        val usuario = resultado.getOrNull()
        assertNotNull(usuario)
        assertEquals("Administrador GSM", usuario?.nombre)
        assertEquals(RolUsuario.ADMINISTRADOR, usuario?.rol)
    }

    // 4. Regla de seguridad: Cambio obligatorio de clave prohíbe la clave por defecto
    @Test
    fun completarCambioPasswordObligatorio_conClavePorDefecto_fallaPorSeguridad() = runBlocking {
        val resultado = usuarioRepository.completarCambioPasswordObligatorio(
            usuarioId = 2,
            nuevaPassword = UsuarioRepository.DEFAULT_PASSWORD
        )

        assertTrue("Debe impedir usar la clave por defecto como nueva contraseña", resultado.isFailure)
    }

    // 5. Regla RBAC: Solo el Administrador puede resetear contraseñas de otros
    @Test
    fun resetearPasswordPorDefecto_usuarioNoAdmin_fallaPorRBAC() = runBlocking {
        val resultado = usuarioRepository.resetearPasswordPorDefecto(
            usuarioId = 1,
            admin = tecnicoUsuario // El técnico NO tiene permisos de administrador
        )

        assertTrue("Un técnico no puede resetear contraseñas de usuarios", resultado.isFailure)
    }

    // 6. Regla de integridad: El administrador no puede desactivar su propia cuenta
    @Test
    fun cambiarEstadoActivo_adminDesactivandoSuPropiaCuenta_esBloqueado() = runBlocking {
        val resultado = usuarioRepository.cambiarEstadoActivo(
            usuarioId = adminUsuario.id,
            activo = false,
            admin = adminUsuario
        )

        assertTrue("El administrador no debe poder auto-desactivarse", resultado.isFailure)
    }

    // 7. Regla Multiusuario: Colaborador hereda el plan y bolsa de tickets de la empresa
    @Test
    fun agregarColaboradorEmpresa_heredaPlanYCuotaTicketsCompartida() = runBlocking {
        val empleador = adminUsuario.copy(id = 10, rol = RolUsuario.CLIENTE, empresa = "Pyme Alfa", plan = "PRO", ticketsDisponibles = 25)
        fakeUsuarioDao.insertarUsuario(
            UsuarioEntity(
                id = 10,
                nombre = empleador.nombre,
                correo = "dueno@pymealfa.cl",
                password = "password123",
                rol = RolUsuario.CLIENTE.name,
                empresa = "Pyme Alfa",
                plan = "PRO",
                ticketsDisponibles = 25,
                estadoSuscripcion = "ACTIVA",
                activo = true
            )
        )

        val resultado = usuarioRepository.agregarColaboradorEmpresa(
            nombre = "Camila Empleada",
            correo = "camila@pymealfa.cl",
            telefono = "+56988887777",
            passwordTemporal = "ClaveTemp123",
            empleador = empleador
        )

        assertTrue("Debe crear el colaborador exitosamente", resultado.isSuccess)
        val creado = resultado.getOrNull()
        assertNotNull(creado)
        assertEquals("Pyme Alfa", creado?.empresa)
        assertEquals("PRO", creado?.plan)
        assertEquals(25, creado?.ticketsDisponibles)
        assertTrue("Debe exigir cambio de clave en primer ingreso", creado?.debeCambiarPassword == true)
    }

    // 8. Regla Multiusuario: No permite modificar colaboradores de otra empresa
    @Test
    fun alternarEstadoColaborador_otraEmpresa_fallaPorSeguridad() = runBlocking {
        val empleador = adminUsuario.copy(id = 10, rol = RolUsuario.CLIENTE, empresa = "Pyme Alfa")
        val resultado = usuarioRepository.alternarEstadoColaborador(
            colaboradorId = adminUsuario.id, // pertenece a "GSM SpA"
            activo = false,
            empleador = empleador
        )

        assertTrue("No debe permitir desactivar usuarios de otra organización", resultado.isFailure)
    }
}
