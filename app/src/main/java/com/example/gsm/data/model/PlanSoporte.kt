package com.example.gsm.data.model

/**
 * Catálogo de Planes de Soporte GSM con integración de pasarela Flow (Chile).
 * Define los 3 niveles de atención: PLUS, PRO y MAX con sus acuerdos de nivel de servicio (SLA).
 */
enum class PlanSoporte(
    val idPlan: String,
    val nombre: String,
    val nivel: String, // "TIER 1", "TIER 2", "TIER 3"
    val precioMensualClp: Int,
    val precioTexto: String,
    val slaRespuesta: String,
    val limiteTicketsTexto: String,
    val horarioSoporte: String,
    val canales: List<String>,
    val beneficiosClave: List<String>,
    val colorHex: Long,
    val esRecomendado: Boolean = false,
    val insignia: String
) {
    PLUS(
        idPlan = "plus",
        nombre = "PLUS",
        nivel = "TIER 1",
        precioMensualClp = 29900,
        precioTexto = "$29.900 CLP / mes",
        slaRespuesta = "< 24 Horas",
        limiteTicketsTexto = "5 Tickets / semana (~20 al mes)",
        horarioSoporte = "5x8 (Lunes a Viernes 09:00 - 18:00)",
        canales = listOf("Portal Web & App", "Correo Electrónico"),
        beneficiosClave = listOf(
            "5 Solicitudes de soporte por semana",
            "SLA de primera respuesta < 24 hrs",
            "Cobertura en horario hábil 5x8",
            "Pase digital QR con seguimiento web",
            "Notificaciones y alertas en tiempo real"
        ),
        colorHex = 0xFF4A6572,
        esRecomendado = false,
        insignia = "Pymes & Emprendedores"
    ),

    PRO(
        idPlan = "pro",
        nombre = "PRO",
        nivel = "TIER 2",
        precioMensualClp = 79900,
        precioTexto = "$79.900 CLP / mes",
        slaRespuesta = "< 4 Horas",
        limiteTicketsTexto = "25 Tickets / semana (~100 al mes)",
        horarioSoporte = "6x12 (Lunes a Sábado 08:00 - 20:00)",
        canales = listOf("WhatsApp Corporativo", "App Móvil & Web", "Mesa Telefónica"),
        beneficiosClave = listOf(
            "25 Solicitudes de soporte por semana",
            "SLA prioritario garantizado < 4 hrs",
            "Horario extendido 6x12 (incluye Sábados)",
            "2 Sesiones de asistencia remota mensual",
            "Atención directa por WhatsApp corporativo",
            "Asignación preferente de técnicos senior"
        ),
        colorHex = 0xFF0066FF,
        esRecomendado = true,
        insignia = "⭐ Recomendado"
    ),

    MAX(
        idPlan = "max",
        nombre = "MAX",
        nivel = "TIER 3",
        precioMensualClp = 199900,
        precioTexto = "$199.900 CLP / mes",
        slaRespuesta = "< 15 Minutos",
        limiteTicketsTexto = "Tickets Ilimitados (24/7/365)",
        horarioSoporte = "24/7/365 Non-Stop Continuo",
        canales = listOf("Línea VIP 24/7", "WhatsApp Dedicado", "Ingeniero On-Site"),
        beneficiosClave = listOf(
            "Tickets e intervenciones ilimitadas semanales",
            "SLA crítico inmediato < 15 minutos",
            "Soporte ininterrumpido 24/7/365",
            "Account Manager e Ingeniero dedicado",
            "Asistencia presencial / terreno prioritaria (On-Site)",
            "Monitoreo preventivo e informes ejecutivos"
        ),
        colorHex = 0xFF10B981,
        esRecomendado = false,
        insignia = "Misión Crítica"
    );

    val cuotaTicketsSemanales: Int
        get() = when (this) {
            PLUS -> 5
            PRO -> 25
            MAX -> 999
        }

    companion object {
        fun obtenerPorId(id: String?): PlanSoporte {
            return entries.firstOrNull { it.idPlan.equals(id, ignoreCase = true) } ?: PRO
        }
    }
}
