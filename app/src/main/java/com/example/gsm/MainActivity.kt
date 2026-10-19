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
import com.example.gsm.data.local.GsmDatabase
import com.example.gsm.data.repository.*
import com.example.gsm.ui.theme.GSMTheme
import com.example.gsm.ui.view.navigation.AppNavigation
import com.example.gsm.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Inicializamos Base de Datos Room (con datos ficticios automáticos)
        val db = GsmDatabase.getInstance(applicationContext)

        // 2. Inicializamos los Repositorios
        val usuarioRepo = UsuarioRepository(db.usuarioDao())
        val gsmRepo = GsmRepository(
            localRepo = SolicitudLocalRepository(db.solicitudDao()),
            historialRepo = HistorialRepository(db.historialDao()),
            notificacionRepo = NotificacionRepository(db.notificacionDao()),
            whatsAppRepo = WhatsAppRepository(),
            evidenciaRepo = EvidenciaRepository(db.evidenciaDao())
        )

        // 3. Creamos los 4 ViewModels usando nuestra fábrica manual
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