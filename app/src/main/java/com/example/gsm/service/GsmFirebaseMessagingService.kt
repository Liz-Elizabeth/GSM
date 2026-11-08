package com.example.gsm.service

import android.util.Log
import com.example.gsm.data.local.GsmDatabase
import com.example.gsm.data.local.entity.NotificacionEntity
import com.example.gsm.util.GsmNotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Servicio de Notificaciones Push Nativas con Firebase Cloud Messaging (FCM).
 * Maneja la recepción de cargas útiles en primer plano, segundo plano y estado terminado.
 */
class GsmFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo FCM Token recibido: $token")
        // Almacenamos el token para asociación de usuario
        getSharedPreferences("gsm_fcm_prefs", MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensaje Push recibido desde: ${remoteMessage.from}")

        // 1. Extraer título y cuerpo desde la notificación o data payload
        val titulo = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: remoteMessage.data["titulo"]
            ?: "GSM Soporte Técnico"

        val cuerpo = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["mensaje"]
            ?: remoteMessage.data["message"]
            ?: "Tienes una nueva actualización en tu solicitud."

        val solicitudId = remoteMessage.data["solicitudId"]?.toIntOrNull()
            ?: remoteMessage.data["solicitud_id"]?.toIntOrNull()
            ?: 0

        val codigoTicket = remoteMessage.data["codigo"]
            ?: remoteMessage.data["codigoSeguimiento"]

        val tipoEvento = remoteMessage.data["tipo"]
            ?: remoteMessage.data["tipoEvento"]
            ?: "PUSH_REMOTO"

        val esChat = tipoEvento.contains("CHAT", ignoreCase = true) ||
                tipoEvento.contains("MENSAJE", ignoreCase = true)

        // 2. Desplegar notificación emergente nativa con NotificationManager
        GsmNotificationHelper.mostrarNotificacionNativa(
            context = applicationContext,
            titulo = titulo,
            mensaje = cuerpo,
            solicitudId = solicitudId,
            codigoTicket = codigoTicket,
            esChat = esChat
        )

        // 3. Persistir en la base de datos Room para la bandeja de notificaciones interna
        val usuarioDestinoId = remoteMessage.data["usuarioDestinoId"]?.toIntOrNull() ?: 1
        serviceScope.launch {
            try {
                val db = GsmDatabase.getInstance(applicationContext)
                db.notificacionDao().insertar(
                    NotificacionEntity(
                        usuarioDestinoId = usuarioDestinoId,
                        solicitudId = solicitudId,
                        titulo = titulo,
                        mensaje = cuerpo,
                        tipoEvento = tipoEvento,
                        leida = false,
                        fechaCreacion = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error al persistir notificación push en Room: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "GsmFCMService"
    }
}
