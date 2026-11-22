package com.example.gsm.ui.view.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val ShimmerBase = Color(0xFFE5E5EA)
private val ShimmerHighlight = Color(0xFFF7F7FA)

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

@Composable
fun Modifier.skeletonShimmer(): Modifier {
    val brush = LocalSkeletonBrush.current ?: rememberSkeletonBrush()
    return this.background(brush)
}

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    val brush = LocalSkeletonBrush.current ?: rememberSkeletonBrush()
    Box(modifier = modifier.background(brush, shape))
}

@Composable
private fun SkeletonSurfaceCard(
    corner: Dp = 16.dp,
    padding: Dp = 16.dp,
    spacing: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(shape = RoundedCornerShape(corner), color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(padding), verticalArrangement = Arrangement.spacedBy(spacing), content = content)
    }
}

@Composable
fun SkeletonTarjetaTicket() {
    CompositionLocalProvider(LocalSkeletonBrush provides rememberSkeletonBrush()) {
        SkeletonSurfaceCard {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SkeletonBox(Modifier.size(110.dp, 20.dp), RoundedCornerShape(6.dp))
                SkeletonBox(Modifier.size(80.dp, 22.dp), CircleShape)
            }
            SkeletonBox(Modifier.fillMaxWidth(0.85f).height(22.dp), RoundedCornerShape(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SkeletonBox(Modifier.size(130.dp, 16.dp), RoundedCornerShape(4.dp))
                SkeletonBox(Modifier.size(70.dp, 18.dp), CircleShape)
            }
            SkeletonBox(Modifier.size(160.dp, 14.dp), RoundedCornerShape(4.dp))
        }
    }
}

@Composable
fun SkeletonPaseWallet() {
    CompositionLocalProvider(LocalSkeletonBrush provides rememberSkeletonBrush()) {
        SkeletonSurfaceCard(corner = 20.dp, padding = 0.dp, spacing = 0.dp) {
            SkeletonBox(Modifier.fillMaxWidth().height(42.dp), RoundedCornerShape(0.dp))
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    SkeletonBox(Modifier.size(60.dp, 32.dp))
                    SkeletonBox(Modifier.size(80.dp, 24.dp))
                    SkeletonBox(Modifier.size(60.dp, 32.dp))
                }
                SkeletonBox(Modifier.fillMaxWidth(0.7f).height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SkeletonBox(Modifier.size(110.dp, 16.dp))
                    SkeletonBox(Modifier.size(90.dp, 16.dp))
                }
            }
            SkeletonBox(Modifier.fillMaxWidth().height(2.dp), RoundedCornerShape(0.dp))
            Box(modifier = Modifier.padding(16.dp)) {
                SkeletonBox(Modifier.fillMaxWidth().height(46.dp), RoundedCornerShape(12.dp))
            }
        }
    }
}

@Composable
fun SkeletonDetalleTicket() {
    CompositionLocalProvider(LocalSkeletonBrush provides rememberSkeletonBrush()) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SkeletonBox(Modifier.fillMaxWidth().height(44.dp), RoundedCornerShape(14.dp))
            SkeletonSurfaceCard(padding = 18.dp, spacing = 14.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonBox(Modifier.size(90.dp, 26.dp), CircleShape)
                    SkeletonBox(Modifier.size(100.dp, 26.dp), CircleShape)
                }
                SkeletonBox(Modifier.fillMaxWidth(0.8f).height(26.dp), RoundedCornerShape(6.dp))
                SkeletonBox(Modifier.size(150.dp, 16.dp), RoundedCornerShape(4.dp))
                SkeletonBox(Modifier.fillMaxWidth().height(90.dp), RoundedCornerShape(12.dp))
            }
            SkeletonBox(Modifier.fillMaxWidth().height(60.dp), RoundedCornerShape(16.dp))
            SkeletonSurfaceCard {
                SkeletonBox(Modifier.size(140.dp, 20.dp), RoundedCornerShape(6.dp))
                SkeletonBox(Modifier.fillMaxWidth().height(50.dp), RoundedCornerShape(12.dp))
            }
        }
    }
}
