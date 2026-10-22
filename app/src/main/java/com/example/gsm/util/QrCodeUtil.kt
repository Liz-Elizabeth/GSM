package com.example.gsm.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream

/**
 * Generador y gestor utilitario de códigos QR para tickets GSM (Principio KISS).
 * Transforma códigos de seguimiento en Bitmap y permite compartirlos como imagen por WhatsApp.
 */
object QrCodeUtil {

    fun generarQrBitmap(contenido: String, ancho: Int = 512, alto: Int = 512): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(contenido, BarcodeFormat.QR_CODE, ancho, alto)
            val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.RGB_565)

            for (x in 0 until ancho) {
                for (y in 0 until alto) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Comparte el código QR como imagen adjunta (.png) junto con el texto estructurado por WhatsApp / Apps.
     */
    fun compartirQrConTexto(
        context: Context,
        codigo: String,
        texto: String,
        tituloChooser: String = "Compartir Pase QR por WhatsApp"
    ) {
        try {
            val bitmap = generarQrBitmap(codigo, 512, 512)
            if (bitmap != null) {
                val carpetaImagenes = File(context.cacheDir, "images")
                if (!carpetaImagenes.exists()) {
                    carpetaImagenes.mkdirs()
                }

                val archivoImagen = File(carpetaImagenes, "qr_$codigo.png")
                val stream = FileOutputStream(archivoImagen)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.flush()
                stream.close()

                val uriContenido = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    archivoImagen
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uriContenido)
                    putExtra(Intent.EXTRA_TEXT, texto)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(intent, tituloChooser))
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback seguro a texto si el dispositivo tuviera problemas de escritura en caché
        val intentTexto = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
        }
        context.startActivity(Intent.createChooser(intentTexto, tituloChooser))
    }
}
