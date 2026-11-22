package com.example.gsm.service

import android.util.Log
import com.example.gsm.data.local.GsmDatabase
import com.example.gsm.data.local.entity.NotificacionEntity
import com.example.gsm.util.GsmNotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Servicio de Notificaciones Push Nativas con Firebase Cloud Messaging (FCM).
 */
class GsmFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo FCM Token recibido")
        getSharedPreferences("gsm_fcm_prefs", MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensaje Push recibido")

        val titulo = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "GSM Soporte Técnico"

        val rawCuerpo = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Tienes una nueva actualización en tu solicitud."
        val cuerpo = if (rawCuerpo.startsWith("ENC:")) {
            "Tienes un nuevo mensaje en tu ticket."
        } else {
            rawCuerpo
        }

        val solicitudIdDirecto = remoteMessage.data["solicitudId"]?.toIntOrNull() ?: 0
        val codigoTicket = remoteMessage.data["codigoSeguimiento"]
        val tipoEvento = remoteMessage.data["tipo"] ?: "PUSH_REMOTO"
        val usuarioDestinoId = remoteMessage.data["usuarioDestinoId"]?.toIntOrNull() ?: 1
        val esChat = tipoEvento.contains("CHAT", ignoreCase = true) ||
            tipoEvento.contains("MENSAJE", ignoreCase = true)

        // Resuelve solicitudId por código si el payload solo trajo codigoSeguimiento para abrir el ticket correcto
        serviceScope.launch {
            try {
                val db = GsmDatabase.getInstance(applicationContext)
                val solicitudIdResuelto = if (!codigoTicket.isNullOrBlank()) {
                    db.solicitudDao().obtenerPorCodigoSeguimiento(codigoTicket)?.id ?: solicitudIdDirecto
                } else {
                    solicitudIdDirecto
                }

                GsmNotificationHelper.mostrarNotificacionNativa(
                    context = applicationContext,
                    titulo = titulo,
                    mensaje = cuerpo,
                    solicitudId = solicitudIdResuelto,
                    codigoTicket = codigoTicket,
                    esChat = esChat
                )

                db.notificacionDao().insertar(
                    NotificacionEntity(
                        usuarioDestinoId = usuarioDestinoId,
                        solicitudId = solicitudIdResuelto,
                        titulo = titulo,
                        mensaje = cuerpo,
                        tipoEvento = tipoEvento,
                        leida = false,
                        fechaCreacion = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error al procesar notificación push: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "GsmFCMService"
    }
}
