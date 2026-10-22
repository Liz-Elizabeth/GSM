package com.example.gsm.ui.view.screens.usuario.componentes

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// -------------------------------------------------------------------------
// Paleta de Colores Base (Apple HIG + Teamwork Desk)
// -------------------------------------------------------------------------
val AppleBackground = Color(0xFFF2F2F7)
val TeamworkBlue = Color(0xFF0066FF)
val InternalNoteBg = Color(0xFFFFF8E6)
val InternalNoteText = Color(0xFFB25000)
val SuccessGreenBg = Color(0xFFE8F8EE)
val SuccessGreenText = Color(0xFF1B7F37)
val AlertRedBg = Color(0xFFFFECEB)
val AlertRedText = Color(0xFFD92D20)

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
