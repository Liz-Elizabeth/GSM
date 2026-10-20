package com.example.gsm.ui.view.screens.usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario
import com.example.gsm.ui.view.components.formatearFechaHora
import com.example.gsm.ui.viewmodel.SolicitudViewModel

private val AppleBackground = Color(0xFFF2F2F7)
private val TeamworkBlue = Color(0xFF0066FF)

/**
 * Pestaña de Perfil estilo Instagram / iOS Settings.
 * Muestra los datos del usuario, su rol RBAC y el botón de Cerrar Sesión.
 */
@Composable
fun PerfilScreen(
    usuario: Usuario,
    alCerrarSesion: () -> Unit
) {
    val iconoRol = when (usuario.rol) {
        RolUsuario.CLIENTE -> "👤"
        RolUsuario.ADMINISTRADOR -> "👔"
        RolUsuario.TECNICO -> "🔧"
    }

    val descripcionPermisos = when (usuario.rol) {
        RolUsuario.CLIENTE -> "Crear solicitudes, adjuntar evidencias, responder información y confirmar cierres."
        RolUsuario.ADMINISTRADOR -> "Control total: clasificar prioridades, asignar técnicos, cancelar y ver métricas SLA."
        RolUsuario.TECNICO -> "Gestionar tickets asignados, registrar avances, notas internas y soluciones técnicas."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. AVATAR CIRCULAR ESTILO INSTAGRAM
        Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier.size(88.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = iconoRol,
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. NOMBRE Y BADGE RBAC
        Text(
            text = usuario.nombre,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            color = TeamworkBlue.copy(alpha = 0.12f),
            shape = CircleShape
        ) {
            Text(
                text = "ROL RBAC: ${usuario.rol.name}",
                style = MaterialTheme.typography.labelMedium,
                color = TeamworkBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. DATOS DE LA CUENTA FICTICIA
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilaDatoPerfil("📧 Correo", usuario.correo)
                HorizontalDivider(color = AppleBackground)
                FilaDatoPerfil("🏢 Empresa", usuario.empresa ?: "GSM Servicios Informáticos")
                HorizontalDivider(color = AppleBackground)
                FilaDatoPerfil("📞 Teléfono", usuario.telefono ?: "+56 9 0000 0000")
                HorizontalDivider(color = AppleBackground)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "🛡️ Permisos del Perfil (RBAC)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = descripcionPermisos,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 4. BOTÓN DESTACADO DE CERRAR SESIÓN
        Button(
            onClick = alCerrarSesion,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Cerrar Sesión",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FilaDatoPerfil(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Pestaña de Alertas / Notificaciones Locales (Criterio 28).
 */
@Composable
fun NotificacionesScreen(
    solicitudVm: SolicitudViewModel,
    alIrADetalle: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = "Notificaciones",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Avisos sobre cambios de estado y asignaciones",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (solicitudVm.notificaciones.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔔", style = MaterialTheme.typography.displaySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sin notificaciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Aquí aparecerán los avisos de tus solicitudes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(solicitudVm.notificaciones) { notif ->
                    Surface(
                        onClick = {
                            solicitudVm.marcarNotificacionLeida(notif.id)
                            alIrADetalle(notif.solicitudId)
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (notif.leida) Color.White else TeamworkBlue.copy(alpha = 0.10f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = notif.titulo,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (notif.leida) Color.Black else TeamworkBlue
                                )
                                if (!notif.leida) {
                                    Surface(color = TeamworkBlue, shape = CircleShape) {
                                        Text(
                                            text = "NUEVA",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = notif.mensaje,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "🕒 ${formatearFechaHora(notif.fechaCreacion)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
