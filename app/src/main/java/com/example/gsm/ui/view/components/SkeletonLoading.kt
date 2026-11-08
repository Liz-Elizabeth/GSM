package com.example.gsm.ui.view.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

// Paleta suave de Skeleton Shimmer
private val ShimmerBase = Color(0xFFE5E5EA)
private val ShimmerHighlight = Color(0xFFF7F7FA)

/**
 * Pincel animado único compartido (Elimina recomposiciones redundantes y jank en Compose).
 */
@Composable
fun rememberSkeletonBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "skeleton_transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeleton_translate"
    )

    return Brush.linearGradient(
        colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
        start = Offset(translateAnim - 600f, translateAnim - 600f),
        end = Offset(translateAnim, translateAnim)
    )
}

val LocalSkeletonBrush = staticCompositionLocalOf<Brush?> { null }

/**
 * Efecto Shimmer reutilizable para Jetpack Compose (Patrón KISS).
 * Aplica una animación infinita suave simulando carga de contenido.
 */
@Composable
fun Modifier.skeletonShimmer(): Modifier {
    val brush = LocalSkeletonBrush.current ?: rememberSkeletonBrush()
    return this.background(brush)
}

/**
 * Bloque básico de Skeleton con esquinas redondeadas.
 * Consume el Brush animado compartido para no instanciar animaciones en bucle por cada elemento.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    val brush = LocalSkeletonBrush.current ?: rememberSkeletonBrush()
    Box(
        modifier = modifier.background(brush, shape)
    )
}

/**
 * Skeleton para la tarjeta de ticket en la Bandeja (`TarjetaSolicitudItem`).
 */
@Composable
fun SkeletonTarjetaTicket() {
    val brush = rememberSkeletonBrush()
    CompositionLocalProvider(LocalSkeletonBrush provides brush) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Fila superior: Código + Badge estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonBox(
                        modifier = Modifier
                            .width(110.dp)
                            .height(20.dp),
                        shape = RoundedCornerShape(6.dp)
                    )
                    SkeletonBox(
                        modifier = Modifier
                            .width(80.dp)
                            .height(22.dp),
                        shape = CircleShape
                    )
                }

                // Título
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(22.dp),
                    shape = RoundedCornerShape(6.dp)
                )

                // Categoría y Prioridad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonBox(
                        modifier = Modifier
                            .width(130.dp)
                            .height(16.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                    SkeletonBox(
                        modifier = Modifier
                            .width(70.dp)
                            .height(18.dp),
                        shape = CircleShape
                    )
                }

                // Fecha
                SkeletonBox(
                    modifier = Modifier
                        .width(160.dp)
                        .height(14.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }
    }
}

/**
 * Skeleton para el Pase de Avión en la Billetera (`TarjetaWalletTicket`).
 */
@Composable
fun SkeletonPaseWallet() {
    val brush = rememberSkeletonBrush()
    CompositionLocalProvider(LocalSkeletonBrush provides brush) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Cabecera superior
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(0.dp)
                )

                // Cuerpo del pase (Ruta CLI ✈️ GSM)
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeletonBox(modifier = Modifier.size(width = 60.dp, height = 32.dp))
                        SkeletonBox(modifier = Modifier.size(width = 80.dp, height = 24.dp))
                        SkeletonBox(modifier = Modifier.size(width = 60.dp, height = 32.dp))
                    }

                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(20.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SkeletonBox(modifier = Modifier.size(width = 110.dp, height = 16.dp))
                        SkeletonBox(modifier = Modifier.size(width = 90.dp, height = 16.dp))
                    }
                }

                // Perforación troquelada
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    shape = RoundedCornerShape(0.dp)
                )

                // Barra inferior para desplegar QR
                Box(modifier = Modifier.padding(16.dp)) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Skeleton para la pantalla del Detalle del Ticket (`DetalleSolicitudScreen`).
 */
@Composable
fun SkeletonDetalleTicket() {
    val brush = rememberSkeletonBrush()
    CompositionLocalProvider(LocalSkeletonBrush provides brush) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        // Selector segmentado superior
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(14.dp)
        )

        // Tarjeta principal de información
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonBox(modifier = Modifier.size(width = 90.dp, height = 26.dp), shape = CircleShape)
                    SkeletonBox(modifier = Modifier.size(width = 100.dp, height = 26.dp), shape = CircleShape)
                }

                // Título
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(26.dp),
                    shape = RoundedCornerShape(6.dp)
                )

                // Fecha
                SkeletonBox(
                    modifier = Modifier
                        .width(150.dp)
                        .height(16.dp),
                    shape = RoundedCornerShape(4.dp)
                )

                // Caja de descripción
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Tarjeta de acceso al Chat
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp)
        )

        // Tarjeta de Evidencias
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .width(140.dp)
                        .height(20.dp),
                    shape = RoundedCornerShape(6.dp)
                )
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}
}
