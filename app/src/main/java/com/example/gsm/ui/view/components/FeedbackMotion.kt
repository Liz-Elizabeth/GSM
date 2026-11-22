package com.example.gsm.ui.view.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.hypot

/**
 * Sacudida horizontal breve en compositor (sin relayout) + háptico cuando [trigger] > 0.
 */
fun Modifier.shakeOnError(trigger: Int): Modifier = composed {
    val offsetPx = remember { Animatable(0f) }
    val haptic = LocalHapticFeedback.current
    val distanciaPx = with(LocalDensity.current) { 10.dp.toPx() }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val pasos = listOf(-distanciaPx, distanciaPx, -distanciaPx * 0.6f, distanciaPx * 0.6f, 0f)
            pasos.forEach { destino ->
                offsetPx.animateTo(destino, animationSpec = tween(durationMillis = 48))
            }
        }
    }

    this.graphicsLayer { translationX = offsetPx.value }
}

/**
 * Confirmación llamativa a pantalla completa: fondo verde radial + check dibujándose trazo a trazo.
 * Bloquea toques mientras completa la transición de éxito.
 */
@Composable
fun PantallaExitoCompleta(
    titulo: String,
    subtitulo: String? = null
) {
    val haptic = LocalHapticFeedback.current
    val progresoFondo = remember { Animatable(0f) }
    val escalaCirculo = remember { Animatable(0.4f) }
    val progresoCheck = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        progresoFondo.animateTo(1f, animationSpec = tween(320, easing = FastOutSlowInEasing))
        launch {
            escalaCirculo.animateTo(1f, animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium))
        }
        progresoCheck.animateTo(1f, animationSpec = tween(280, easing = FastOutSlowInEasing))
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    val verdeEsmeralda = Color(0xFF059669)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radioMax = hypot(size.width, size.height) * 0.6f
            drawCircle(
                color = verdeEsmeralda,
                radius = radioMax * progresoFondo.value
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .graphicsLayer { alpha = progresoFondo.value },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        scaleX = escalaCirculo.value
                        scaleY = escalaCirculo.value
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val checkPath = Path().apply {
                        moveTo(w * 0.27f, h * 0.52f)
                        lineTo(w * 0.43f, h * 0.68f)
                        lineTo(w * 0.74f, h * 0.34f)
                    }
                    val trazoParcial = Path()
                    val measure = PathMeasure().apply { setPath(checkPath, false) }
                    measure.getSegment(0f, measure.length * progresoCheck.value, trazoParcial, true)
                    drawPath(
                        path = trazoParcial,
                        color = verdeEsmeralda,
                        style = Stroke(
                            width = 7.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            if (!subtitulo.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
