package com.example.gsm.ui.view.screens.usuario.componentes

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
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
import com.example.gsm.data.model.PlanSoporte
import com.example.gsm.data.model.PrioridadSolicitud
import com.example.gsm.data.model.Solicitud
import com.example.gsm.ui.view.components.formatearFechaHora
import com.example.gsm.util.QrCodeUtil

private fun formatearEstado(estado: EstadoSolicitud): String = when (estado) {
    EstadoSolicitud.RECIBIDA -> "Recibida"
    EstadoSolicitud.EN_REVISION -> "En revisión"
    EstadoSolicitud.ASIGNADA -> "Asignada"
    EstadoSolicitud.EN_PROCESO -> "En proceso"
    EstadoSolicitud.PENDIENTE_INFORMACION -> "Pendiente info"
    EstadoSolicitud.RESUELTA -> "Resuelta"
    EstadoSolicitud.CERRADA -> "Cerrada"
    EstadoSolicitud.CANCELADA -> "Cancelada"
}

/**
 * Pase de Servicio Digital Enterprise (GSM Enterprise Service Pass).
 * Diseñado con estética corporativa de alta gama:
 * - Credencial de custodia de activos técnicos
 * - Mapeo de Nivel de Atención (TIER 1 PLUS, TIER 2 PRO, TIER 3 MAX) según SLA
 * - Troquelado de seguridad con QR plegable (el QR se revela al abrir el pase)
 * - Protocolo de validación técnica en terreno
 */
@Composable
fun TarjetaWalletTicket(
    solicitud: Solicitud,
    alVerDetalle: () -> Unit
) {
    val context = LocalContext.current
    var abierto by remember { mutableStateOf(false) }

    val qrBitmap = remember(solicitud.codigoSeguimiento) {
        QrCodeUtil.generarQrBitmap(solicitud.codigoSeguimiento, 480, 480)
    }

    val ahora = System.currentTimeMillis()
    val estaAtrasada = solicitud.fechaLimite != null &&
            solicitud.fechaLimite < ahora &&
            solicitud.estado != EstadoSolicitud.RESUELTA &&
            solicitud.estado != EstadoSolicitud.CERRADA &&
            solicitud.estado != EstadoSolicitud.CANCELADA

    // Determinar Tier corporativo según la prioridad / criticidad del ticket
    val (tierPlan, tierColor, slaTexto) = when (solicitud.prioridad) {
        PrioridadSolicitud.URGENTE -> Triple(PlanSoporte.MAX, Color(0xFF10B981), "< 15 MIN")
        PrioridadSolicitud.ALTA, PrioridadSolicitud.MEDIA -> Triple(PlanSoporte.PRO, TeamworkBlue, "< 4 HORAS")
        PrioridadSolicitud.BAJA -> Triple(PlanSoporte.PLUS, Color(0xFF64748B), "< 24 HORAS")
    }

    val (fondoEstado, textoEstado) = when (solicitud.estado) {
        EstadoSolicitud.CERRADA, EstadoSolicitud.RESUELTA -> Pair(SuccessGreenBg, SuccessGreenText)
        EstadoSolicitud.CANCELADA -> Pair(AlertRedBg, AlertRedText)
        EstadoSolicitud.PENDIENTE_INFORMACION -> Pair(InternalNoteBg, InternalNoteText)
        else -> Pair(TeamworkBlue.copy(alpha = 0.12f), TeamworkBlue)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. CABECERA ENTERPRISE OBSIDIAN
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = tierColor.copy(alpha = 0.25f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.VerifiedUser,
                                    contentDescription = null,
                                    tint = tierColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "GSM ENTERPRISE SERVICE PASS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.1.sp,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "CREDENCIAL DE CUSTODIA TÉCNICA",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    // Badge de Nivel de Atención / SLA
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = tierColor.copy(alpha = 0.20f)
                    ) {
                        Text(
                            text = "${tierPlan.nivel} ${tierPlan.nombre} • SLA $slaTexto",
                            style = MaterialTheme.typography.labelSmall,
                            color = tierColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // 2. CUERPO CORPORATIVO: DATOS DEL ACTIVO, CLIENTE Y SERVICIO
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fila Superior: Código Monospace y Estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = solicitud.codigoSeguimiento,
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = "• ${solicitud.categoria.descripcion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    BadgePill(
                        texto = formatearEstado(solicitud.estado),
                        colorFondo = fondoEstado,
                        colorTexto = textoEstado
                    )
                }

                // Asunto de la solicitud
                Column {
                    Text(
                        text = "INCIDENCIA / REQUERIMIENTO",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = solicitud.titulo,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 2
                    )
                }

                // Activo o Infraestructura afectada (Utilidad Enterprise)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Dns,
                            contentDescription = null,
                            tint = TeamworkBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "ACTIVO / INFRAESTRUCTURA RELACIONADA",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = solicitud.activoRelacionado ?: "Infraestructura Cloud / Servidor On-Premise",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Grid de Asignación y Fechas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Técnico Responsable
                    Column {
                        Text(
                            text = "INGENIERO RESPONSABLE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (solicitud.tecnicoId != null) Icons.Filled.Engineering else Icons.Filled.AccessTime,
                                contentDescription = null,
                                tint = if (solicitud.tecnicoId != null) TeamworkBlue else Color(0xFFE65100),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (solicitud.tecnicoId != null) "Técnico GSM #${solicitud.tecnicoId}" else "En cola de asignación",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1E293B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Emisión y SLA
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "EMISIÓN DE PASE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                        Text(
                            text = formatearFechaHora(solicitud.fechaCreacion),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 3. LÍNEA TROQUELADA DE SEGURIDAD (MICRO-PERFORACIÓN)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Muesca lateral izquierda
                Surface(
                    shape = CircleShape,
                    color = AppleBackground,
                    modifier = Modifier
                        .size(18.dp)
                        .offset(x = (-9).dp)
                ) {}

                // Línea punteada de corte
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                ) {
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    drawLine(
                        color = Color(0xFFCBD5E1),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        pathEffect = pathEffect,
                        strokeWidth = 2f
                    )
                }

                // Muesca lateral derecha
                Surface(
                    shape = CircleShape,
                    color = AppleBackground,
                    modifier = Modifier
                        .size(18.dp)
                        .offset(x = 9.dp)
                ) {}
            }

            // 4. TALÓN DE VALIDACIÓN: EL QR SE VE AL ABRIRLO
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!abierto) {
                    // ESTADO PLEGADO: Barra para desplegar credencial QR
                    Surface(
                        onClick = { abierto = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.QrCode2,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Credencial QR & Validación en Terreno",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Toca para abrir pase y presentar al ingeniero",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Abrir credencial",
                                tint = TeamworkBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = alVerDetalle) {
                            Text(
                                text = "Ver Detalle del Ticket ›",
                                fontWeight = FontWeight.Bold,
                                color = TeamworkBlue,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                } else {
                    // ESTADO ABIERTO: Muestra el QR en alta resolución y opciones
                    if (qrBitmap != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Pase QR ${solicitud.codigoSeguimiento}",
                                modifier = Modifier
                                    .size(190.dp)
                                    .padding(14.dp)
                            )
                        }
                    }

                    // Firma Criptográfica de la Credencial
                    Text(
                        text = "HASH: GSM-PASS-${solicitud.id}-${solicitud.codigoSeguimiento}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Presente este código para validar la custodia física del activo, autorizar la intervención y computar el SLA empresarial del plan contratado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF475569),
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Alerta si el SLA está vencido
                    if (estaAtrasada) {
                        Surface(
                            shape = CircleShape,
                            color = AlertRedBg,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚠️ SLA Comprometido - Protocolo de Escalamiento Activo",
                                style = MaterialTheme.typography.labelSmall,
                                color = AlertRedText,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Acciones Corporativas: Compartir y Detalle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val mensajeTexto = "🔐 *PASE DE SERVICIO ENTERPRISE - GSM*\n\n" +
                                        "📋 *Pase:* ${solicitud.codigoSeguimiento}\n" +
                                        "🏢 *Plan SLA:* ${tierPlan.nombre} (${tierPlan.nivel})\n" +
                                        "⏱️ *Garantía:* $slaTexto\n" +
                                        "💻 *Activo:* ${solicitud.activoRelacionado ?: "Infraestructura Central"}\n" +
                                        "📌 *Asunto:* ${solicitud.titulo}\n" +
                                        "🏷️ *Categoría:* ${solicitud.categoria.descripcion}\n" +
                                        "📊 *Estado:* ${solicitud.estado.name}\n\n" +
                                        "🔗 *Ticket en Línea:* https://gsm-web-delta.vercel.app/tickets/${solicitud.codigoSeguimiento}\n" +
                                        "🌐 *Web Oficial:* https://gsm-web-delta.vercel.app/"

                                QrCodeUtil.compartirQrConTexto(
                                    context = context,
                                    codigo = solicitud.codigoSeguimiento,
                                    texto = mensajeTexto,
                                    tituloChooser = "Compartir Credencial GSM Enterprise"
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compartir", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = alVerDetalle,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text("Ficha Ticket ›", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Botón para plegar el talón
                    TextButton(
                        onClick = { abierto = false },
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Plegar credencial",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}
