package com.example.gsm.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.gsm.MainActivity
import com.example.gsm.R

/**
 * Gestor de Notificaciones Nativas y Canales de Sistema para GSM (KISS & Android Best Practices).
 * Soporta Android 8.0+ (Canales) y Android 13+ (Permisos en tiempo de ejecución POST_NOTIFICATIONS).
 */
object GsmNotificationHelper {

    private const val TAG = "GsmNotificationHelper"

    // Canales de Notificación
    const val CHANNEL_ID_TICKETS = "gsm_channel_tickets"
    const val CHANNEL_NAME_TICKETS = "Actualizaciones de Tickets GSM"
    const val CHANNEL_DESC_TICKETS = "Notificaciones de cambios de estado, asignaciones y resolución de soporte"

    const val CHANNEL_ID_CHAT = "gsm_channel_chat"
    const val CHANNEL_NAME_CHAT = "Mensajes y Chat GSM"
    const val CHANNEL_DESC_CHAT = "Notificaciones de respuestas y mensajes de soporte en tiempo real"

    // Claves de Intención para Navegación Profunda
    const val EXTRA_SOLICITUD_ID = "solicitud_id"
    const val EXTRA_CODIGO_TICKET = "codigo_ticket"

    /**
     * Registra los canales oficiales en el NotificationManager del sistema operativo.
     * Requerido a partir de Android 8.0 (API 26).
     */
    fun crearCanalesNotificacion(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return

            // 1. Canal para tickets y eventos de soporte
            val canalTickets = NotificationChannel(
                CHANNEL_ID_TICKETS,
                CHANNEL_NAME_TICKETS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_TICKETS
                enableVibration(true)
                setShowBadge(true)
            }

            // 2. Canal para mensajes y chat directo
            val canalChat = NotificationChannel(
                CHANNEL_ID_CHAT,
                CHANNEL_NAME_CHAT,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_CHAT
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(canalTickets)
            notificationManager.createNotificationChannel(canalChat)
            Log.d(TAG, "Canales de notificación nativos creados correctamente.")
        }
    }

    /**
     * Muestra una notificación emergente nativa (Heads-up) en la barra de estado de Android.
     */
    fun mostrarNotificacionNativa(
        context: Context,
        titulo: String,
        mensaje: String,
        solicitudId: Int = 0,
        codigoTicket: String? = null,
        esChat: Boolean = false,
        notificacionId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        // En Android 13+ (API 33+) verificamos permiso en ejecución
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permiso = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permiso != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Permiso POST_NOTIFICATIONS no concedido. Se omite notificación nativa.")
                return
            }
        }

        try {
            val canalId = if (esChat) CHANNEL_ID_CHAT else CHANNEL_ID_TICKETS

            // Intent para abrir MainActivity y dirigir al ticket correspondiente
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_SOLICITUD_ID, solicitudId)
                if (!codigoTicket.isNullOrBlank()) {
                    putExtra(EXTRA_CODIGO_TICKET, codigoTicket)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notificacionId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, canalId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            NotificationManagerCompat.from(context).notify(notificacionId, builder.build())
            Log.d(TAG, "Notificación nativa enviada: id=$notificacionId | canal=$canalId | titulo=$titulo")
        } catch (e: Exception) {
            Log.e(TAG, "Error al mostrar notificación nativa: ${e.message}", e)
        }
    }
}
