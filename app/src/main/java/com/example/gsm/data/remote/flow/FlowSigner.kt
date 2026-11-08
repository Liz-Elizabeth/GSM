package com.example.gsm.data.remote.flow

import java.util.TreeMap
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Generador de firma criptográfica HMAC-SHA256 según el estándar oficial de Flow Chile.
 * Los parámetros deben ordenarse alfabéticamente por su clave antes de firmar.
 */
object FlowSigner {

    fun firmarParametros(params: Map<String, String>, secretKey: String): String {
        // Orden alfabético estricto de las claves (TreeMap)
        val sortedMap = TreeMap(params)
        val toSign = StringBuilder()

        for ((key, value) in sortedMap) {
            if (toSign.isNotEmpty()) {
                toSign.append("&")
            }
            toSign.append(key).append("=").append(value)
        }

        return hmacSha256(toSign.toString(), secretKey)
    }

    private fun hmacSha256(data: String, key: String): String {
        val sha256Hmac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
        sha256Hmac.init(secretKeySpec)
        val hash = sha256Hmac.doFinal(data.toByteArray(Charsets.UTF_8))

        // Convertir bytes a cadena hexadecimal en minúsculas
        val hexString = StringBuilder()
        for (b in hash) {
            val hex = Integer.toHexString(0xff and b.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }
}
