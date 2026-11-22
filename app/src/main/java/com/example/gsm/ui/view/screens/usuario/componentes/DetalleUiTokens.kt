package com.example.gsm.ui.view.screens.usuario.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.PrioridadSolicitud

// -------------------------------------------------------------------------
// Paleta de Colores Base (Apple HIG + Teamwork Desk)
// -------------------------------------------------------------------------
val AppleBackground = Color(0xFFF2F2F7)
val AppleInputFill = Color(0xFFF2F2F7)
val ChatBackground = Color(0xFFECE5DD)
val TeamworkBlue = Color(0xFF0066FF)
val InternalNoteBg = Color(0xFFFFF8E6)
val InternalNoteText = Color(0xFFB25000)
val SuccessGreenBg = Color(0xFFE8F8EE)
val SuccessGreenText = Color(0xFF1B7F37)
val AlertRedBg = Color(0xFFFFECEB)
val AlertRedText = Color(0xFFD92D20)
val PurpleBadgeBg = Color(0xFFF3E8FF)
val PurpleBadgeText = Color(0xFF7E22CE)
val OrangeBadgeBg = Color(0xFFFFF3E0)
val OrangeBadgeText = Color(0xFFE65100)
val InactiveBadgeBg = Color(0xFFFFEBEE)
val InactiveBadgeText = Color(0xFFC62828)

/**
 * Título de sección en mayúsculas estilo iOS Settings.
 */
@Composable
fun SeccionTitulo(texto: String) {
    Text(
        text = texto.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

/**
 * Píldora de estado o etiqueta con forma circular y colores semánticos suaves.
 */
@Composable
fun BadgePill(
    texto: String,
    colorFondo: Color,
    colorTexto: Color
) {
    Surface(
        color = colorFondo,
        shape = CircleShape
    ) {
        Text(
            text = texto.replace("_", " "),
            style = MaterialTheme.typography.labelSmall,
            color = colorTexto,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

fun formatearEstado(estado: EstadoSolicitud): String = when (estado) {
    EstadoSolicitud.RECIBIDA -> "Recibida"
    EstadoSolicitud.EN_REVISION -> "En revisión"
    EstadoSolicitud.ASIGNADA -> "Asignada"
    EstadoSolicitud.EN_PROCESO -> "En proceso"
    EstadoSolicitud.PENDIENTE_INFORMACION -> "Pendiente info"
    EstadoSolicitud.RESUELTA -> "Resuelta"
    EstadoSolicitud.CERRADA -> "Cerrada"
    EstadoSolicitud.CANCELADA -> "Cancelada"
}

fun formatearPrioridad(prioridad: PrioridadSolicitud): String = when (prioridad) {
    PrioridadSolicitud.BAJA -> "Baja"
    PrioridadSolicitud.MEDIA -> "Media"
    PrioridadSolicitud.ALTA -> "Alta"
    PrioridadSolicitud.URGENTE -> "Urgente"
}

/**
 * Tarjeta estándar de estado vacío reutilizada entre bandejas y archivo.
 */
@Composable
fun EstadoVacioCard(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier,
    accion: (@Composable () -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = AppleBackground,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8E8E93),
                textAlign = TextAlign.Center
            )
            if (accion != null) {
                Spacer(modifier = Modifier.height(14.dp))
                accion()
            }
        }
    }
}
