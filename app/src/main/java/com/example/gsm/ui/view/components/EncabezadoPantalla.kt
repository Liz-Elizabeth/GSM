package com.example.gsm.ui.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/**
 * Componente reutilizable para el encabezado superior de las pantallas.
 * Estilo híbrido Apple (Large Title / Navigation Bar) + Teamwork Desk.
 */
@Composable
fun EncabezadoPantalla(
    titulo: String,
    textoBotonDerecho: String = "Volver",
    alHacerClicDerecho: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Botón de retroceso limpio estilo iOS (FilledTonal suave o TextButton)
        FilledTonalButton(
            onClick = alHacerClicDerecho,
            shape = CircleShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(
                text = "‹ $textoBotonDerecho",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}