package com.example.gsm.data.remote

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Cliente centralizado para Cloud Firestore (Patrón KISS).
 *
 * En Android, Firebase Firestore ya tiene la persistencia offline en disco
 * activada AUTOMÁTICAMENTE por defecto (SQLite interno de Firebase).
 * No requiere configuraciones manuales ni dependencias adicionales.
 */
object FirebaseClient {

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}
