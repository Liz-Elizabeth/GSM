package com.example.gsm.data.remote.flow

import com.google.gson.annotations.SerializedName

/**
 * Modelos de datos para la API de Pasarela Flow (Chile).
 * https://www.flow.cl/docs/api.html
 */

data class FlowCreateResponse(
    @SerializedName("url") val url: String,
    @SerializedName("token") val token: String,
    @SerializedName("flowOrder") val flowOrder: Long
)

data class FlowStatusResponse(
    @SerializedName("flowOrder") val flowOrder: Long? = null,
    @SerializedName("commerceOrder") val commerceOrder: String? = null,
    @SerializedName("requestDate") val requestDate: String? = null,
    @SerializedName("status") val status: Int? = null, // 1: Pendiente, 2: Pagada, 3: Rechazada, 4: Anulada
    @SerializedName("subject") val subject: String? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("payer") val payer: String? = null,
    @SerializedName("paymentData") val paymentData: FlowPaymentData? = null
)

data class FlowPaymentData(
    @SerializedName("date") val date: String? = null,
    @SerializedName("media") val media: String? = null, // Webpay, MACH, Khipu, etc.
    @SerializedName("conversionDate") val conversionDate: String? = null,
    @SerializedName("conversionRate") val conversionRate: Double? = null,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("fee") val fee: Double? = null,
    @SerializedName("balance") val balance: Double? = null,
    @SerializedName("transferDate") val transferDate: String? = null
)
