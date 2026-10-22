package com.example.gsm.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.FileProvider
import java.io.File

data class ArchivoAdjunto(
    val nombre: String,
    val tipoMime: String,
    val tamanoBytes: Long,
    val uriString: String
)

/**
 * Utilitario simple (KISS) para persistir evidencias localmente y asegurar acceso permanente.
 * Copia los archivos al almacenamiento privado de la app (filesDir) para evitar que Android
 * revoque permisos de lectura temporales de content:// tras reiniciar la app.
 */
object FileUtil {

    fun obtenerDatosDeUri(context: Context, uri: Uri): ArchivoAdjunto {
        var nombre = "archivo_evidencia"
        var tamano = 0L
        val tipo = context.contentResolver.getType(uri) ?: "application/octet-stream"

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex != -1) {
                        val nameValue = cursor.getString(nameIndex)
                        if (!nameValue.isNullOrBlank()) {
                            nombre = nameValue
                        }
                    }
                    if (sizeIndex != -1) {
                        tamano = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // PERSISTENCIA LOCAL PERMANENTE (KISS):
        // Copiamos los bytes a la carpeta privada 'evidencias/' dentro de context.filesDir.
        // Así el archivo NUNCA se pierde ni caduca cuando la app se cierra o se reinicia.
        val uriPersistidaString = try {
            val dirEvidencias = File(context.filesDir, "evidencias").apply { mkdirs() }
            val nombreLimpio = nombre.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val archivoDestino = File(dirEvidencias, "evi_${System.currentTimeMillis()}_$nombreLimpio")

            context.contentResolver.openInputStream(uri)?.use { entrada ->
                archivoDestino.outputStream().use { salida ->
                    entrada.copyTo(salida)
                }
            }

            if (tamano <= 0L && archivoDestino.exists()) {
                tamano = archivoDestino.length()
            }

            Uri.fromFile(archivoDestino).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            uri.toString() // Respaldo en caso de error
        }

        return ArchivoAdjunto(
            nombre = nombre,
            tipoMime = tipo,
            tamanoBytes = if (tamano > 0) tamano else 1024L,
            uriString = uriPersistidaString
        )
    }

    fun formatearTamano(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes.toDouble() / (1024 * 1024))
            bytes >= 1024 -> "${bytes / 1024} KB"
            else -> "$bytes bytes"
        }
    }

    fun obtenerIconoVectorSegunMime(tipoMime: String): ImageVector {
        return when {
            tipoMime.startsWith("image/") -> Icons.Filled.Image
            tipoMime == "application/pdf" -> Icons.Filled.PictureAsPdf
            tipoMime.startsWith("text/") -> Icons.AutoMirrored.Filled.Article
            tipoMime.contains("spreadsheet") || tipoMime.contains("excel") -> Icons.Filled.TableChart
            tipoMime.contains("word") || tipoMime.contains("document") -> Icons.Filled.Description
            tipoMime.contains("zip") || tipoMime.contains("rar") -> Icons.Filled.FolderZip
            else -> Icons.Filled.AttachFile
        }
    }

    fun obtenerIconoSegunMime(tipoMime: String): String {
        return when {
            tipoMime.startsWith("image/") -> "🖼️"
            tipoMime == "application/pdf" -> "📄"
            tipoMime.startsWith("text/") -> "📝"
            tipoMime.contains("spreadsheet") || tipoMime.contains("excel") -> "📊"
            tipoMime.contains("word") || tipoMime.contains("document") -> "📑"
            tipoMime.contains("zip") || tipoMime.contains("rar") -> "🗜️"
            else -> "📎"
        }
    }

    fun cargarBitmapDesdeUri(context: Context, uriString: String): Bitmap? {
        if (uriString.isBlank() || uriString.startsWith("file:///demo")) return null
        return try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file") {
                val path = uri.path
                if (path != null && File(path).exists()) {
                    BitmapFactory.decodeFile(path)
                } else null
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    }
                } else {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun leerTextoDesdeUri(context: Context, uriString: String, maxChars: Int = 3000): String? {
        if (uriString.isBlank() || uriString.startsWith("file:///demo")) return null
        return try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file") {
                val path = uri.path
                if (path != null && File(path).exists()) {
                    File(path).bufferedReader().use { reader ->
                        val buffer = CharArray(maxChars)
                        val read = reader.read(buffer, 0, maxChars)
                        if (read > 0) String(buffer, 0, read) else null
                    }
                } else null
            } else {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                    val buffer = CharArray(maxChars)
                    val read = reader.read(buffer, 0, maxChars)
                    if (read > 0) String(buffer, 0, read) else null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun abrirArchivoExterno(context: Context, uriString: String, tipoMime: String) {
        try {
            val uri = Uri.parse(uriString)
            val uriCompartible: Uri = if (uri.scheme == "file") {
                val path = uri.path
                if (path != null) {
                    val archivo = File(path)
                    if (archivo.exists()) {
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            archivo
                        )
                    } else uri
                } else uri
            } else {
                uri
            }

            val mimeEfectivo = if (tipoMime.isNotBlank()) tipoMime else "*/*"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uriCompartible, mimeEfectivo)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Abrir archivo con..."))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
