package com.example.gsm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
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
import com.example.gsm.util.GsmNotificationHelper
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private var solicitudIdInicialState by mutableIntStateOf(0)

    // Lanzador reactivo para solicitar permiso de notificaciones push en Android 13+ (API 33+)
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("MainActivity", "Permiso de notificaciones POST_NOTIFICATIONS concedido.")
            } else {
                Log.w("MainActivity", "Permiso de notificaciones POST_NOTIFICATIONS denegado por el usuario.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 0. Cargar motor nativo de cifrado SQLCipher
        try {
            System.loadLibrary("sqlcipher")
        } catch (t: Throwable) {
            Log.e("MainActivity", "No se pudo cargar la librería nativa sqlcipher", t)
        }

        // 1. Inicializar Canales de Notificaciones Nativas del Sistema
        GsmNotificationHelper.crearCanalesNotificacion(applicationContext)

        // 2. Solicitar permiso de notificaciones nativas en Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // 3. Inicializar y registrar token de Firebase Cloud Messaging (FCM)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("MainActivity", "FCM Registration Token: $token")
                getSharedPreferences("gsm_fcm_prefs", MODE_PRIVATE)
                    .edit()
                    .putString("fcm_token", token)
                    .apply()
            } else {
                Log.w("MainActivity", "Fallo al obtener FCM Token", task.exception)
            }
        }

        // 4. Capturar id de solicitud si la app se abrió desde una notificación push
        solicitudIdInicialState = intent.getIntExtra(GsmNotificationHelper.EXTRA_SOLICITUD_ID, 0)

        // 5. Inicializamos Base de Datos Room (con datos ficticios automáticos)
        val db = GsmDatabase.getInstance(applicationContext)

        // 6. Inicializamos Appwrite Storage para evidencias en la nube de forma segura
        AppwriteClient.initialize(
            context = applicationContext,
            endpoint = BuildConfig.APPWRITE_ENDPOINT,
            projectId = BuildConfig.APPWRITE_PROJECT_ID,
            bucketId = BuildConfig.APPWRITE_BUCKET_ID
        )

        // 7. Inicializamos los Repositorios (con soporte nativo para notificaciones Heads-up)
        val usuarioRepo = UsuarioRepository(db.usuarioDao())
        val gsmRepo = GsmRepository(
            localRepo = SolicitudLocalRepository(db.solicitudDao(), db.usuarioDao()),
            historialRepo = HistorialRepository(db.historialDao()),
            notificacionRepo = NotificacionRepository(db.notificacionDao(), applicationContext),
            whatsAppRepo = WhatsAppRepository(),
            evidenciaRepo = EvidenciaRepository(db.evidenciaDao()),
            firebaseRepo = FirebaseSolicitudRepository(),
            appwriteStorageRepo = AppwriteStorageRepository()
        )

        // 8. Sincronización bidireccional en tiempo real (Web <-> App) - Patrón KISS
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

        // 9. Creamos los ViewModels usando la fábrica
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
                        solicitudIdInicial = solicitudIdInicialState,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val id = intent.getIntExtra(GsmNotificationHelper.EXTRA_SOLICITUD_ID, 0)
        if (id > 0) {
            solicitudIdInicialState = id
        }
    }
}
