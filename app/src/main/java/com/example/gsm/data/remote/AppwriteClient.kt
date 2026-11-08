package com.example.gsm.data.remote

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Storage

/**
 * Cliente centralizado y Singleton para Appwrite (Senior Architecture Pattern).
 * Administra la conexión con Appwrite Cloud o servidor autoalojado para el servicio de Storage.
 */
object AppwriteClient {

    // Endpoints y credenciales configurables
    var endpoint: String = "https://cloud.appwrite.io/v1"
    var projectId: String = "YOUR_PROJECT_ID" // Reemplaza con tu Project ID de Appwrite Console
    var bucketId: String = "evidencias"       // Reemplaza con tu Bucket ID de Appwrite Storage

    private var client: Client? = null
    private var storage: Storage? = null

    /**
     * Inicializa el SDK de Appwrite. Se invoca una sola vez desde MainActivity o Application.
     */
    fun initialize(
        context: Context,
        endpoint: String = "https://cloud.appwrite.io/v1",
        projectId: String = "YOUR_PROJECT_ID",
        bucketId: String = "evidencias"
    ) {
        this.endpoint = endpoint
        this.projectId = projectId
        this.bucketId = bucketId

        val c = Client(context.applicationContext)
            .setEndpoint(endpoint)
            .setProject(projectId)

        this.client = c
        this.storage = Storage(c)
    }

    /**
     * Retorna el servicio de Storage de Appwrite listo para operar.
     */
    fun getStorage(): Storage {
        return storage ?: error("AppwriteClient no ha sido inicializado. Llama a AppwriteClient.initialize(context) en MainActivity.")
    }
}
