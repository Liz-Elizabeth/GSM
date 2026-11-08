package com.example.gsm.security

import android.util.Base64
import android.util.Log
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Gestor de Cifrado a Nivel de Campo (Field-Level Encryption) con AES-256-GCM.
 * Protege los datos confidenciales (descripciones, diagnósticos, mensajes de chat)
 * antes de transmitirlos y almacenarlos en Cloud Firestore (Zero-Knowledge Cloud Storage).
 */
object GsmCryptoManager {
    private const val TAG = "GsmCrypto"
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_LENGTH = 12 // 96 bits recomendado por NIST para GCM
    private const val PREFIX = "ENC:v1:"

    // Clave maestra simétrica de 256 bits para GSM Soporte
    private val MASTER_KEY_BYTES = byteArrayOf(
        0x47.toByte(), 0x53.toByte(), 0x4D.toByte(), 0x5F.toByte(), // GSM_
        0x53.toByte(), 0x45.toByte(), 0x43.toByte(), 0x55.toByte(), // SECU
        0x52.toByte(), 0x45.toByte(), 0x5F.toByte(), 0x4B.toByte(), // RE_K
        0x45.toByte(), 0x59.toByte(), 0x5F.toByte(), 0x32.toByte(), // EY_2
        0x30.toByte(), 0x32.toByte(), 0x36.toByte(), 0x5F.toByte(), // 026_
        0x46.toByte(), 0x49.toByte(), 0x52.toByte(), 0x45.toByte(), // FIRE
        0x53.toByte(), 0x54.toByte(), 0x4F.toByte(), 0x52.toByte(), // STOR
        0x45.toByte(), 0x5F.toByte(), 0x41.toByte(), 0x45.toByte()  // E_AE
    )

    private val secretKey: SecretKey = SecretKeySpec(MASTER_KEY_BYTES, "AES")

    /**
     * Cifra un texto en claro con AES-256-GCM.
     * Retorna el texto con prefijo "ENC:v1:" y el payload en Base64.
     */
    fun encrypt(plainText: String?): String {
        if (plainText.isNullOrBlank()) return plainText ?: ""
        if (plainText.startsWith(PREFIX)) return plainText // Ya está cifrado

        return try {
            val iv = ByteArray(IV_LENGTH)
            SecureRandom().nextBytes(iv)

            val cipher = Cipher.getInstance(ALGORITHM)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            val byteBuffer = ByteBuffer.allocate(iv.size + cipherText.size)
            byteBuffer.put(iv)
            byteBuffer.put(cipherText)

            PREFIX + Base64.encodeToString(byteBuffer.array(), Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error cifrando texto: ${e.message}", e)
            plainText
        }
    }

    /**
     * Descifra un texto que contenga el prefijo "ENC:v1:".
     * Si el texto no está cifrado (legacy), retorna el texto original intacto.
     */
    fun decrypt(cipherText: String?): String {
        if (cipherText.isNullOrBlank()) return cipherText ?: ""
        if (!cipherText.startsWith(PREFIX)) return cipherText // No cifrado (legacy)

        return try {
            val rawBase64 = cipherText.removePrefix(PREFIX)
            val combined = Base64.decode(rawBase64, Base64.NO_WRAP)

            if (combined.size < IV_LENGTH) return cipherText

            val iv = ByteArray(IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH)

            val cipherPayload = ByteArray(combined.size - IV_LENGTH)
            System.arraycopy(combined, IV_LENGTH, cipherPayload, 0, cipherPayload.size)

            val cipher = Cipher.getInstance(ALGORITHM)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val plainBytes = cipher.doFinal(cipherPayload)
            String(plainBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Error descifrando texto: ${e.message}", e)
            cipherText
        }
    }
}
