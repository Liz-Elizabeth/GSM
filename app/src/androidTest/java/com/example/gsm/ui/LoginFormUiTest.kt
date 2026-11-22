package com.example.gsm.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gsm.data.local.dao.UsuarioDao
import com.example.gsm.data.local.entity.UsuarioEntity
import com.example.gsm.data.repository.UsuarioRepository
import com.example.gsm.ui.theme.GSMTheme
import com.example.gsm.ui.view.screens.auth.LoginScreen
import com.example.gsm.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba de UI / Formulario: LoginScreen
 * Valida la entrada de texto en los campos del formulario de login y la navegación a registro.
 */
@RunWith(AndroidJUnit4::class)
class LoginFormUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setUp() {
        val fakeDao = object : UsuarioDao {
            override suspend fun insertarUsuario(usuario: UsuarioEntity): Long = 1L
            override suspend fun insertarUsuarios(usuarios: List<UsuarioEntity>) {}
            override suspend fun actualizarUsuario(usuario: UsuarioEntity) {}
            override fun obtenerUsuarios(): Flow<List<UsuarioEntity>> = flowOf(emptyList())
            override suspend fun obtenerUsuarioPorId(id: Int): UsuarioEntity? = null
            override suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity? = null
            override suspend fun autenticar(correo: String, password: String): UsuarioEntity? = null
            override suspend fun cambiarEstadoActivo(id: Int, activo: Boolean, ts: Long) {}
            override suspend fun actualizarCredenciales(id: Int, password: String, debeCambiar: Boolean, ts: Long) {}
            override suspend fun actualizarPlanSuscripcion(id: Int, plan: String, tickets: Int, estado: String, ts: Long) {}
            override suspend fun descontarTicketDisponible(id: Int): Int = 1
            override suspend fun eliminarUsuario(id: Int) {}
            override fun obtenerTecnicosDisponibles(): Flow<List<UsuarioEntity>> = flowOf(emptyList())
            override fun obtenerClientes(): Flow<List<UsuarioEntity>> = flowOf(emptyList())
            override fun obtenerUsuariosPorRol(rol: String): Flow<List<UsuarioEntity>> = flowOf(emptyList())
            override fun obtenerEquipoPorEmpresa(empresa: String): Flow<List<UsuarioEntity>> = flowOf(emptyList())
            override suspend fun obtenerIdsPorEmpresa(empresa: String): List<Int> = emptyList()
            override suspend fun actualizarPlanEmpresa(empresa: String, plan: String, tickets: Int, estado: String, ts: Long) {}
            override suspend fun descontarTicketEmpresa(empresa: String, ts: Long): Int = 1
            override fun contarSolicitudesCreadas(clienteId: Int): Flow<Int> = flowOf(0)
            override fun contarSolicitudesAsignadas(tecnicoId: Int): Flow<Int> = flowOf(0)
        }
        val usuarioRepo = UsuarioRepository(fakeDao)
        authViewModel = AuthViewModel(usuarioRepo)
    }

    // 1. Simula el llenado del formulario de inicio de sesión
    @Test
    fun formularioLogin_permiteIngresarCredenciales_yMuestraBotonAcceso() {
        composeTestRule.setContent {
            GSMTheme {
                LoginScreen(authVm = authViewModel)
            }
        }

        // Verifica que la pantalla cargue el encabezado de bienvenida
        composeTestRule.onNodeWithText("Bienvenido de nuevo")
            .performScrollTo()
            .assertIsDisplayed()

        // Escribe correo electrónico en el campo correspondiente
        composeTestRule.onNodeWithText("contacto@miempresa.cl")
            .performScrollTo()
            .performTextInput("soporte@cliente.cl")

        // Escribe contraseña en el campo correspondiente
        composeTestRule.onNodeWithText("Ingresa tu contraseña")
            .performScrollTo()
            .performTextInput("ClaveSegura123")

        // Verifica que el botón de Iniciar Sesión esté presente y visible (scroll por si el teclado lo tapa)
        composeTestRule.onAllNodesWithText("Iniciar Sesión")[1]
            .performScrollTo()
            .assertIsDisplayed()
    }

    // 2. Simula el cambio hacia la pestaña del formulario de registro corporativo
    @Test
    fun formularioLogin_alPulsarPestanaRegistro_muestraCamposDeRegistroCliente() {
        composeTestRule.setContent {
            GSMTheme {
                LoginScreen(authVm = authViewModel)
            }
        }

        // Hace clic en la pestaña "Crear Cuenta"
        composeTestRule.onNodeWithText("Crear Cuenta")
            .performScrollTo()
            .performClick()

        // Verifica que el formulario cambie a "Registro de Cliente"
        composeTestRule.onNodeWithText("Registro de Cliente")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Nombre Completo *")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
