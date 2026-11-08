export type NivelPlan = "TIER 1" | "TIER 2" | "TIER 3";

export interface PlanSoporteWeb {
  id: "plus" | "pro" | "max";
  nombre: string;
  nivel: NivelPlan;
  precioMensualClp: number;
  precioTexto: string;
  slaRespuesta: string;
  limiteTicketsTexto: string;
  horarioSoporte: string;
  canales: string[];
  beneficiosClave: string[];
  colorHex: string;
  esRecomendado?: boolean;
  insignia: string;
}

export const PLANES_SOPORTE: PlanSoporteWeb[] = [
  {
    id: "plus",
    nombre: "PLUS",
    nivel: "TIER 1",
    precioMensualClp: 29900,
    precioTexto: "$29.900 CLP / mes",
    slaRespuesta: "< 24 Horas",
    limiteTicketsTexto: "5 Tickets / semana (~20 al mes)",
    horarioSoporte: "5x8 (Lunes a Viernes 09:00 - 18:00)",
    canales: ["Portal Web & App", "Correo Electrónico"],
    beneficiosClave: [
      "5 Solicitudes de soporte por semana",
      "SLA de primera respuesta garantizado < 24 hrs",
      "Cobertura técnica en horario hábil 5x8",
      "Pase digital QR con seguimiento web",
      "Notificaciones y alertas en tiempo real",
    ],
    colorHex: "#4A6572",
    esRecomendado: false,
    insignia: "Pymes & Emprendedores",
  },
  {
    id: "pro",
    nombre: "PRO",
    nivel: "TIER 2",
    precioMensualClp: 79900,
    precioTexto: "$79.900 CLP / mes",
    slaRespuesta: "< 4 Horas",
    limiteTicketsTexto: "25 Tickets / semana (~100 al mes)",
    horarioSoporte: "6x12 (Lunes a Sábado 08:00 - 20:00)",
    canales: ["WhatsApp Corporativo", "App Móvil & Web", "Mesa Telefónica"],
    beneficiosClave: [
      "25 Solicitudes de soporte por semana",
      "SLA prioritario garantizado < 4 hrs",
      "Horario extendido 6x12 (incluye Sábados)",
      "2 Sesiones de asistencia remota mensual",
      "Atención directa por WhatsApp corporativo",
      "Asignación preferente de técnicos senior",
    ],
    colorHex: "#0066FF",
    esRecomendado: true,
    insignia: "⭐ Recomendado",
  },
  {
    id: "max",
    nombre: "MAX",
    nivel: "TIER 3",
    precioMensualClp: 199900,
    precioTexto: "$199.900 CLP / mes",
    slaRespuesta: "< 15 Minutos",
    limiteTicketsTexto: "Tickets Ilimitados (24/7/365)",
    horarioSoporte: "24/7/365 Non-Stop Continuo",
    canales: ["Línea VIP 24/7", "WhatsApp Dedicado", "Ingeniero On-Site"],
    beneficiosClave: [
      "Tickets e intervenciones ilimitadas semanales",
      "SLA crítico inmediato < 15 minutos",
      "Soporte ininterrumpido 24/7/365",
      "Account Manager e Ingeniero dedicado",
      "Asistencia presencial / terreno prioritaria (On-Site)",
      "Monitoreo preventivo e informes ejecutivos mensuales",
    ],
    colorHex: "#10B981",
    esRecomendado: false,
    insignia: "Misión Crítica",
  },
];

export type EstadoTransaccion = "PENDIENTE" | "PAGADA" | "RECHAZADA" | "ANULADA";

export interface TransaccionPago {
  id: string; // Token de Flow o ID único
  commerceOrder: string; // Ej: GSM-PRO-1731034000
  flowOrder?: number | null;
  token: string;
  planId: "plus" | "pro" | "max";
  planNombre: string;
  montoClp: number;
  esAnual: boolean;
  estado: EstadoTransaccion;
  clienteId: number;
  clienteNombre: string;
  clienteEmail: string;
  medioPago?: string | null; // "Webpay", "Tarjeta de Crédito", "MACH", etc.
  fechaCreacion: number; // epoch ms
  fechaPago?: number | null; // epoch ms
  urlRetorno?: string;
  rawResponse?: Record<string, unknown> | null;
}
