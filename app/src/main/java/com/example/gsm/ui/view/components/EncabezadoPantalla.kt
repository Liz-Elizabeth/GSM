package com.example.gsm.ui.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/**
 * Componente reutilizable para el encabezado superior de las pantallas.
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
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        OutlinedButton(onClick = alHacerClicDerecho) {
            Text(textoBotonDerecho)
        }
    }
}
