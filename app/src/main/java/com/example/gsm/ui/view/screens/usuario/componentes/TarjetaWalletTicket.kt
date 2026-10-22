package com.example.gsm.ui.view.screens.usuario.componentes

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gsm.data.model.EstadoSolicitud
import com.example.gsm.data.model.Solicitud
import com.example.gsm.ui.view.components.formatearFechaHora
import com.example.gsm.util.QrCodeUtil

/**
 * Tarjeta de Pase Digital con estética Apple Wallet / Entrada de Concierto.
 * Principio KISS: Modular, autocontenida y reactiva.
 */
@Composable
fun TarjetaWalletTicket(
    solicitud: Solicitud,
    alVerDetalle: () -> Unit
) {
    val context = LocalContext.current
    val qrBitmap = remember(solicitud.codigoSeguimiento) {
        QrCodeUtil.generarQrBitmap(solicitud.codigoSeguimiento, 400, 400)
    }

    val ahora = System.currentTimeMillis()
    val estaAtrasada = solicitud.fechaLimite != null &&
            solicitud.fechaLimite < ahora &&
            solicitud.estado != EstadoSolicitud.RESUELTA &&
            solicitud.estado != EstadoSolicitud.CERRADA &&
            solicitud.estado != EstadoSolicitud.CANCELADA

    val (fondoEstado, textoEstado) = when (solicitud.estado) {
        EstadoSolicitud.CERRADA, EstadoSolicitud.RESUELTA -> Pair(SuccessGreenBg, SuccessGreenText)
        EstadoSolicitud.CANCELADA -> Pair(AlertRedBg, AlertRedText)
        EstadoSolicitud.PENDIENTE_INFORMACION -> Pair(InternalNoteBg, InternalNoteText)
        else -> Pair(TeamworkBlue.copy(alpha = 0.12f), TeamworkBlue)
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. CABECERA DEL PASE (Banner superior temático)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TeamworkBlue)
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GSM PASE TÉCNICO",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = solicitud.categoria.descripcion,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 2. CUERPO DE DATOS PRINCIPALES
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = solicitud.codigoSeguimiento,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TeamworkBlue
                    )

                    BadgePill(
                        texto = solicitud.estado.name,
                        colorFondo = fondoEstado,
                        colorTexto = textoEstado
                    )
                }

                Text(
                    text = solicitud.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Registrado el ${formatearFechaHora(solicitud.fechaCreacion)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 3. LÍNEA TROQUELADA DE PERFORACIÓN (Efecto Entrada de Evento)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Muesca izquierda
                Surface(
                    shape = CircleShape,
                    color = AppleBackground,
                    modifier = Modifier
                        .size(16.dp)
                        .offset(x = (-8).dp)
                ) {}

                // Línea punteada
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                ) {
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.7f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        pathEffect = pathEffect,
                        strokeWidth = 2f
                    )
                }

                // Muesca derecha
                Surface(
                    shape = CircleShape,
                    color = AppleBackground,
                    modifier = Modifier
                        .size(16.dp)
                        .offset(x = 8.dp)
                ) {}
            }

            // 4. ZONA INFERIOR CON CÓDIGO QR Y ACCIONES
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Código QR renderizado en vivo
                if (qrBitmap != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = AppleBackground,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Pase QR ${solicitud.codigoSeguimiento}",
                            modifier = Modifier
                                .size(170.dp)
                                .padding(8.dp)
                        )
                    }
                }

                // Alerta de SLA / Prioridad
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BadgePill(
                        texto = "⚡ Prioridad: ${solicitud.prioridad.name}",
                        colorFondo = AppleBackground,
                        colorTexto = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (estaAtrasada) {
                        BadgePill(
                            texto = "⚠️ SLA Vencido",
                            colorFondo = AlertRedBg,
                            colorTexto = AlertRedText
                        )
                    }
                }

                // Botones de acción del Pase
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val mensajeTexto = "🎫 *PASE DIGITAL GSM*\n" +
                                    "📌 Ticket: ${solicitud.codigoSeguimiento}\n" +
                                    "📝 Título: ${solicitud.titulo}\n" +
                                    "🏷️ Categoría: ${solicitud.categoria.descripcion}\n" +
                                    "⚡ Prioridad: ${solicitud.prioridad.name}\n" +
                                    "📊 Estado: ${solicitud.estado.name}\n" +
                                    "🔗 Seguimiento: https://gsm-web-delta.vercel.app/seguimiento/${solicitud.codigoSeguimiento}"

                            QrCodeUtil.compartirQrConTexto(
                                context = context,
                                codigo = solicitud.codigoSeguimiento,
                                texto = mensajeTexto,
                                tituloChooser = "Compartir Pase y QR por WhatsApp"
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Compartir",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Compartir", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = alVerDetalle,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Ficha Ticket ›", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
