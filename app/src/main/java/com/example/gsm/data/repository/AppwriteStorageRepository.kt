package com.example.gsm.data.repository

import android.net.Uri
import com.example.gsm.data.remote.AppwriteClient
import io.appwrite.ID
import io.appwrite.models.InputFile
import java.io.File

/**
 * Repositorio de Appwrite Storage para la gestión de archivos y evidencias multimedia.
 * Se encarga de subir los archivos locales y retornar su URL directa en la nube de Appwrite.
 */
class AppwriteStorageRepository {

    /**
     * Sube un archivo persistido localmente a un Bucket de Appwrite Storage.
     * Retorna la URL pública de visualización/descarga del archivo.
     */
    suspend fun subirEvidencia(
        nombreArchivo: String,
        uriLocal: String
    ): Result<String> {
        return try {
            val storage = AppwriteClient.getStorage()
            val bucketId = AppwriteClient.bucketId
            val uri = Uri.parse(uriLocal)

            val file = if (uriLocal.startsWith("file://") || uri.scheme == null) {
                File(uri.path ?: uriLocal)
            } else {
                File(uri.path ?: "")
            }

            require(file.exists()) { "El archivo local de evidencia no existe en: $uriLocal" }

            // Empaquetamos el archivo con InputFile de Appwrite
            val inputFile = InputFile.fromPath(file.absolutePath)

            // Subimos a Appwrite Storage con un ID autogenerado único
            val response = storage.createFile(
                bucketId = bucketId,
                fileId = ID.unique(),
                file = inputFile
            )

            // Construimos la URL de acceso directo
            val downloadUrl = "${AppwriteClient.endpoint}/storage/buckets/$bucketId/files/${response.id}/view?project=${AppwriteClient.projectId}"
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
