package com.example.gsm.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.LruCache
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL

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

    // Caché en memoria (16 MB) para evitar re-decodificar miniaturas al scrollear el chat
    private val bitmapCache = object : LruCache<String, Bitmap>(16 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    /**
     * Decodifica imágenes fuera del hilo principal y las reutiliza desde caché.
     */
    suspend fun cargarBitmapAsync(context: Context, uriString: String): Bitmap? {
        if (uriString.isBlank() || uriString.startsWith("file:///demo")) return null
        bitmapCache.get(uriString)?.let { return it }
        return withContext(Dispatchers.IO) {
            cargarBitmapDesdeUri(context, uriString)?.also { decoded ->
                bitmapCache.put(uriString, decoded)
            }
        }
    }

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

            // Comprime imágenes a máx 1600px / JPEG 82% para ahorrar almacenamiento local y ancho de banda
            if (tipo.startsWith("image/")) {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                val sample = calcularInSampleSize(bounds.outWidth, bounds.outHeight, 1600)
                val opts = BitmapFactory.Options().apply { inSampleSize = sample }
                val bmp = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                if (bmp != null) {
                    archivoDestino.outputStream().use { salida ->
                        bmp.compress(Bitmap.CompressFormat.JPEG, 82, salida)
                    }
                    bmp.recycle()
                } else {
                    context.contentResolver.openInputStream(uri)?.use { entrada ->
                        archivoDestino.outputStream().use { salida -> entrada.copyTo(salida) }
                    }
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use { entrada ->
                    archivoDestino.outputStream().use { salida ->
                        entrada.copyTo(salida)
                    }
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

    private fun calcularInSampleSize(width: Int, height: Int, maxDimPx: Int): Int {
        var inSampleSize = 1
        if (height > maxDimPx || width > maxDimPx) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= maxDimPx || (halfWidth / inSampleSize) >= maxDimPx) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Decodifica imágenes locales, content:// o URLs https:// con downsampling (máx 1280px)
     * para evitar saturar el LruCache con fotos de cámara de alta resolución.
     */
    fun cargarBitmapDesdeUri(context: Context, uriString: String, maxDimPx: Int = 1280): Bitmap? {
        if (uriString.isBlank() || uriString.startsWith("file:///demo")) return null
        return try {
            val uri = Uri.parse(uriString)
            when {
                uri.scheme == "http" || uri.scheme == "https" -> {
                    val bytes = URL(uriString).openStream().use { input ->
                        val out = ByteArrayOutputStream()
                        input.copyTo(out)
                        out.toByteArray()
                    }
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                    val opts = BitmapFactory.Options().apply {
                        inSampleSize = calcularInSampleSize(bounds.outWidth, bounds.outHeight, maxDimPx)
                    }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                }
                uri.scheme == "file" -> {
                    val path = uri.path
                    if (path != null && File(path).exists()) {
                        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeFile(path, bounds)
                        val opts = BitmapFactory.Options().apply {
                            inSampleSize = calcularInSampleSize(bounds.outWidth, bounds.outHeight, maxDimPx)
                        }
                        BitmapFactory.decodeFile(path, opts)
                    } else null
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        val w = info.size.width
                        val h = info.size.height
                        if (w > maxDimPx || h > maxDimPx) {
                            val scale = maxDimPx.toFloat() / maxOf(w, h)
                            decoder.setTargetSize((w * scale).toInt().coerceAtLeast(1), (h * scale).toInt().coerceAtLeast(1))
                        }
                    }
                }
                else -> {
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

    /**
     * Crea un archivo temporal seguro en filesDir/evidencias y su URI con FileProvider para cámara.
     */
    fun crearArchivoFotoTemporal(context: Context, prefijo: String = "FOTO"): Pair<File, Uri> {
        val dir = File(context.filesDir, "evidencias").apply { if (!exists()) mkdirs() }
        val photoFile = File(dir, "${prefijo}_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        return Pair(photoFile, uri)
    }
}
