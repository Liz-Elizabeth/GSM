package com.example.gsm.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gsm.data.local.dao.UsuarioDao
import com.example.gsm.data.local.entity.UsuarioEntity
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.data.repository.UsuarioRepository
import com.example.gsm.ui.theme.GSMTheme
import com.example.gsm.ui.view.screens.auth.CambiarPasswordObligatorioScreen
import com.example.gsm.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba de UI / Formulario: CambiarPasswordObligatorioScreen
 * Valida la visualización y entrada de datos en el formulario de actualización obligatoria de clave.
 */
@RunWith(AndroidJUnit4::class)
class CambiarPasswordFormUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var authViewModel: AuthViewModel
    private val usuarioPrueba = Usuario(
        id = 10,
        nombre = "Juan Pérez",
        correo = "juan.perez@empresa.cl",
        password = UsuarioRepository.DEFAULT_PASSWORD,
        rol = RolUsuario.CLIENTE,
        debeCambiarPassword = true
    )

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

    // 1. Valida que el formulario cargue el mensaje de seguridad y los campos requeridos
    @Test
    fun formularioCambioPassword_muestraCamposObligatorios() {
        composeTestRule.setContent {
            GSMTheme {
                CambiarPasswordObligatorioScreen(
                    usuario = usuarioPrueba,
                    authVm = authViewModel
                )
            }
        }

        // Verifica el título informativo de la pantalla
        composeTestRule.onNodeWithText("Actualización de Seguridad Requerida").assertIsDisplayed()

        // Verifica que los dos campos de contraseña estén presentes
        composeTestRule.onNodeWithText("Nueva contraseña").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirmar nueva contraseña").assertIsDisplayed()

        // Verifica que el botón de actualización esté visible
        composeTestRule.onNodeWithText("Guardar y Continuar")
            .performScrollTo()
            .assertIsDisplayed()
    }

    // 2. Simula la escritura de la nueva contraseña y su confirmación en los campos del formulario
    @Test
    fun formularioCambioPassword_permiteEscribirNuevaClaveYConfirmacion() {
        composeTestRule.setContent {
            GSMTheme {
                CambiarPasswordObligatorioScreen(
                    usuario = usuarioPrueba,
                    authVm = authViewModel
                )
            }
        }

        // Escribe la nueva clave apuntando al label visible (el placeholder en Material 3 solo se monta al hacer foco)
        composeTestRule.onNodeWithText("Nueva contraseña")
            .performTextInput("NuevaClave2026")

        // Escribe la confirmación en el segundo campo apuntando a su label
        composeTestRule.onNodeWithText("Confirmar nueva contraseña")
            .performTextInput("NuevaClave2026")

        // Valida que el botón de confirmación se habilite y sea visible incluso con teclado abierto
        composeTestRule.onNodeWithText("Guardar y Continuar")
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()
    }
}
