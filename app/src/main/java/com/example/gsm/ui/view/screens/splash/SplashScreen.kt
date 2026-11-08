package com.example.gsm.ui.view.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.R
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import kotlinx.coroutines.delay

/**
 * Pantalla de Bienvenida (Splash Screen) con arte corporativo oficial Full-Screen.
 * Presenta una transición fluida al iniciar la aplicación mientras se preparan los servicios.
 */
@Composable
fun SplashScreen(
    onFinalizado: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animación suave de entrada (Fade-In)
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        // Progreso sutil de carga
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
        delay(200L)
        onFinalizado()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030A1A)),
        contentAlignment = Alignment.Center
    ) {
        // 1. Imagen Full-Screen con arte oficial (Isotipo central + Red Global)
        Image(
            painter = painterResource(id = R.drawable.splash_gsm),
            contentDescription = "GSM Soporte",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(alphaAnim.value)
        )

        // 2. Indicador sutil de inicialización en la base
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 42.dp)
                .alpha(alphaAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LinearProgressIndicator(
                progress = { progressAnim.value },
                modifier = Modifier
                    .width(140.dp)
                    .height(3.dp),
                color = TeamworkBlue,
                trackColor = Color.White.copy(alpha = 0.2f)
            )

            Text(
                text = "Iniciando plataforma...",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
