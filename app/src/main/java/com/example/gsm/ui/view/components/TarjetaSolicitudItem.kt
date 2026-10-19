package com.example.gsm.ui.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.model.EstadoSolicitud
import com.example.gsm.model.Solicitud

/**
 * Componente reutilizable para mostrar una tarjeta de solicitud en la bandeja.
 */
@Composable
fun TarjetaSolicitudItem(
    solicitud: Solicitud,
    alHacerClic: () -> Unit
) {
    val ahora = System.currentTimeMillis()
    val estaAtrasada = solicitud.fechaLimite != null &&
        solicitud.fechaLimite < ahora &&
        solicitud.estado != EstadoSolicitud.RESUELTA &&
        solicitud.estado != EstadoSolicitud.CERRADA &&
        solicitud.estado != EstadoSolicitud.CANCELADA

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { alHacerClic() },
        colors = CardDefaults.cardColors(
            containerColor = if (estaAtrasada) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = solicitud.codigoSeguimiento,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (estaAtrasada) "⚠️ ATRASADA • ${solicitud.estado.name}" else solicitud.estado.name,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = solicitud.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Categoría: ${solicitud.categoria.descripcion} | Prioridad: ${solicitud.prioridad.name}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
