package com.example.gsm.data.remote.flow

import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Interfaz Retrofit para el consumo de la API REST de Flow (Chile).
 */
interface FlowApiService {

    @FormUrlEncoded
    @POST("payment/create")
    suspend fun crearOrdenPago(
        @FieldMap params: Map<String, String>
    ): FlowCreateResponse

    @GET("payment/getStatus")
    suspend fun consultarEstadoPago(
        @Query("apiKey") apiKey: String,
        @Query("token") token: String,
        @Query("s") firma: String
    ): FlowStatusResponse
}
