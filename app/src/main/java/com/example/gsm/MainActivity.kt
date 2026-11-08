package com.example.gsm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.gsm.data.local.GsmDatabase
import com.example.gsm.data.remote.AppwriteClient
import com.example.gsm.data.repository.*
import com.example.gsm.ui.theme.GSMTheme
import com.example.gsm.ui.view.navigation.AppNavigation
import com.example.gsm.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 0. Cargar motor nativo de cifrado SQLCipher
        try {
            System.loadLibrary("sqlcipher")
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "No se pudo cargar la librería nativa sqlcipher", t)
        }

        // 1. Inicializamos Base de Datos Room (con datos ficticios automáticos)
        val db = GsmDatabase.getInstance(applicationContext)

        // 2. Inicializamos Appwrite Storage para evidencias en la nube de forma segura
        AppwriteClient.initialize(
            context = applicationContext,
            endpoint = BuildConfig.APPWRITE_ENDPOINT,
            projectId = BuildConfig.APPWRITE_PROJECT_ID,
            bucketId = BuildConfig.APPWRITE_BUCKET_ID
        )

        // 3. Inicializamos los Repositorios
        val usuarioRepo = UsuarioRepository(db.usuarioDao())
        val gsmRepo = GsmRepository(
            localRepo = SolicitudLocalRepository(db.solicitudDao()),
            historialRepo = HistorialRepository(db.historialDao()),
            notificacionRepo = NotificacionRepository(db.notificacionDao()),
            whatsAppRepo = WhatsAppRepository(),
            evidenciaRepo = EvidenciaRepository(db.evidenciaDao()),
            firebaseRepo = FirebaseSolicitudRepository(),
            appwriteStorageRepo = AppwriteStorageRepository()
        )

        // 4. Sincronización bidireccional en tiempo real (Web <-> App) - Patrón KISS
        lifecycleScope.launch {
            kotlin.runCatching {
                db.usuarioDao().insertarUsuarios(
                    listOf(
                        com.example.gsm.data.local.entity.UsuarioEntity(
                            id = 5,
                            nombre = "Carlos Ramos (Técnico Redes)",
                            correo = "carlos@gsm.cl",
                            password = "1234",
                            rol = "TECNICO",
                            empresa = "GSM Área Infraestructura",
                            telefono = "+56900005555",
                            medioContacto = "WhatsApp"
                        ),
                        com.example.gsm.data.local.entity.UsuarioEntity(
                            id = 99,
                            nombre = "Administrador GSM (Web)",
                            correo = "adminweb@gsm.cl",
                            password = "1234",
                            rol = "ADMINISTRADOR",
                            empresa = "GSM Servicios Digitales",
                            telefono = "+56900009999",
                            medioContacto = "WhatsApp"
                        )
                    )
                )
            }
        }
        gsmRepo.iniciarSincronizacionBidireccional(lifecycleScope)

        // 5. Creamos los ViewModels usando la fábrica
        val factory = GsmViewModelFactory(gsmRepo, usuarioRepo)
        val authVm = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        val solicitudVm = ViewModelProvider(this, factory)[SolicitudViewModel::class.java]
        val detalleVm = ViewModelProvider(this, factory)[DetalleSolicitudViewModel::class.java]
        val dashboardVm = ViewModelProvider(this, factory)[AdminDashboardViewModel::class.java]

        setContent {
            GSMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        authVm = authVm,
                        solicitudVm = solicitudVm,
                        detalleVm = detalleVm,
                        dashboardVm = dashboardVm,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
