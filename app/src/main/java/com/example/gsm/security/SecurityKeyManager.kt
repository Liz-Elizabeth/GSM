package com.example.gsm.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.SecureRandom

/**
 * Gestor de claves y passphrases criptográficas seguras para SQLCipher (Room Database).
 * Garantiza que la clave de cifrado local se genere con SecureRandom y se resguarde localmente.
 */
object SecurityKeyManager {
    private const val PREFS_NAME = "gsm_secure_vault"
    private const val KEY_DB_PASSPHRASE = "gsm_db_passphrase_v1"

    /**
     * Obtiene o genera una contraseña criptográfica aleatoria de 256 bits para SQLCipher.
     */
    fun obtenerPassphrase(context: Context): ByteArray {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val guardada = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (guardada != null) {
            return Base64.decode(guardada, Base64.NO_WRAP)
        }

        // Generamos clave criptográfica aleatoria de 256 bits (32 bytes)
        val randomBytes = ByteArray(32)
        SecureRandom().nextBytes(randomBytes)
        val encoded = Base64.encodeToString(randomBytes, Base64.NO_WRAP)
        prefs.edit().putString(KEY_DB_PASSPHRASE, encoded).apply()
        return randomBytes
    }
}
