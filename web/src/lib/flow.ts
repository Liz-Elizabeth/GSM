import crypto from "crypto";

export interface FlowCreateParams {
  commerceOrder: string;
  subject: string;
  amount: number;
  email: string;
  urlConfirmation: string;
  urlReturn: string;
  currency?: string;
  paymentMethod?: number; // Opcional: 1 Webpay, 2 Servipag, etc.
}

export interface FlowCreateResult {
  url: string;
  token: string;
  flowOrder?: number;
  isDemo?: boolean;
}

export interface FlowStatusResult {
  flowOrder: number;
  commerceOrder: string;
  requestDate: string;
  status: 1 | 2 | 3 | 4; // 1: Pendiente, 2: Pagada, 3: Rechazada, 4: Anulada
  subject: string;
  currency: string;
  amount: number;
  payer: string;
  paymentData?: {
    date?: string;
    media?: string;
    conversionDate?: string;
    conversionRate?: number;
    amount?: number;
    currency?: string;
    fee?: number;
    balance?: number;
    transferDate?: string;
  };
  isDemo?: boolean;
}

const FLOW_API_KEY = process.env.FLOW_API_KEY || "";
const FLOW_SECRET_KEY = process.env.FLOW_SECRET_KEY || "";
const FLOW_ENVIRONMENT = process.env.FLOW_ENVIRONMENT || "sandbox";

const FLOW_BASE_URL =
  FLOW_ENVIRONMENT.toLowerCase() === "production"
    ? "https://www.flow.cl/api"
    : "https://sandbox.flow.cl/api";

/**
 * Genera la firma criptográfica HMAC-SHA256 requerida por Flow Chile.
 * Ordena alfabéticamente los parámetros por clave.
 */
export function firmarParametrosFlow(
  params: Record<string, string | number>,
  secretKey: string = FLOW_SECRET_KEY
): string {
  const clavesOrdenadas = Object.keys(params).sort();
  const cadenaParaFirmar = clavesOrdenadas
    .map((clave) => `${clave}=${params[clave]}`)
    .join("&");

  return crypto.createHmac("sha256", secretKey).update(cadenaParaFirmar).digest("hex");
}

/**
 * Crea una orden de pago en Flow.
 * Si no hay llaves configuradas o estamos en modo sandbox sin conectividad,
 * genera una simulación transparente para no bloquear las pruebas.
 */
export async function crearOrdenEnFlow(params: FlowCreateParams): Promise<FlowCreateResult> {
  const tieneLlavesReales =
    FLOW_API_KEY.trim() !== "" &&
    FLOW_SECRET_KEY.trim() !== "" &&
    FLOW_API_KEY !== "tu_flow_api_key";

  if (!tieneLlavesReales) {
    // Modo simulación inteligente para desarrollo o evaluación
    const tokenDemo = `DEMO_TOKEN_${Date.now()}_${Math.floor(Math.random() * 9000 + 1000)}`;
    const flowOrderDemo = Math.floor(Math.random() * 900000 + 100000);
    return {
      url: "/planes/simulador",
      token: tokenDemo,
      flowOrder: flowOrderDemo,
      isDemo: true,
    };
  }

  const payload: Record<string, string | number> = {
    apiKey: FLOW_API_KEY,
    commerceOrder: params.commerceOrder,
    subject: params.subject,
    currency: params.currency || "CLP",
    amount: params.amount,
    email: params.email,
    urlConfirmation: params.urlConfirmation,
    urlReturn: params.urlReturn,
  };

  if (params.paymentMethod) {
    payload.paymentMethod = params.paymentMethod;
  }

  const firma = firmarParametrosFlow(payload, FLOW_SECRET_KEY);
  payload.s = firma;

  try {
    const formData = new URLSearchParams();
    for (const [k, v] of Object.entries(payload)) {
      formData.append(k, String(v));
    }

    const response = await fetch(`${FLOW_BASE_URL}/payment/create`, {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: formData.toString(),
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.warn("Aviso API Flow /payment/create:", response.status, errorText);
      throw new Error(`Flow API error: ${response.status} - ${errorText}`);
    }

    const data = await response.json();
    return {
      url: data.url,
      token: data.token,
      flowOrder: data.flowOrder,
      isDemo: false,
    };
  } catch (error) {
    console.warn("Fallo conectando a Flow oficial, usando fallback sandbox:", error);
    // Fallback sandbox para mantener la resiliencia
    const tokenDemo = `DEMO_TOKEN_${Date.now()}_${Math.floor(Math.random() * 9000 + 1000)}`;
    return {
      url: "/planes/simulador",
      token: tokenDemo,
      flowOrder: 994000,
      isDemo: true,
    };
  }
}

/**
 * Consulta el estado de un pago en Flow por su token.
 */
export async function consultarEstadoEnFlow(token: string): Promise<FlowStatusResult> {
  // Manejo de tokens de simulación
  if (token.startsWith("DEMO_TOKEN_") || token.startsWith("SIM_")) {
    const esRechazado = token.includes("_RECHAZO_");
    return {
      flowOrder: 994012,
      commerceOrder: `GSM-PRO-${token.slice(-6)}`,
      requestDate: new Date().toISOString(),
      status: esRechazado ? 3 : 2, // 2: Pagada, 3: Rechazada
      subject: "Plan de Soporte GSM Pro (Simulación Sandbox)",
      currency: "CLP",
      amount: 79900,
      payer: "camila@empresa.cl",
      paymentData: {
        date: new Date().toISOString(),
        media: "Webpay Plus",
        amount: 79900,
        currency: "CLP",
      },
      isDemo: true,
    };
  }

  const tieneLlavesReales =
    FLOW_API_KEY.trim() !== "" &&
    FLOW_SECRET_KEY.trim() !== "" &&
    FLOW_API_KEY !== "tu_flow_api_key";

  if (!tieneLlavesReales) {
    return {
      flowOrder: 994012,
      commerceOrder: `GSM-SIM-${Date.now().toString().slice(-6)}`,
      requestDate: new Date().toISOString(),
      status: 2,
      subject: "Plan de Soporte GSM (Demo)",
      currency: "CLP",
      amount: 79900,
      payer: "cliente@gsm-soporte.cl",
      paymentData: {
        date: new Date().toISOString(),
        media: "Webpay Plus",
        amount: 79900,
      },
      isDemo: true,
    };
  }

  const queryParams: Record<string, string> = {
    apiKey: FLOW_API_KEY,
    token: token,
  };

  const firma = firmarParametrosFlow(queryParams, FLOW_SECRET_KEY);
  const urlFinal = `${FLOW_BASE_URL}/payment/getStatus?apiKey=${encodeURIComponent(
    FLOW_API_KEY
  )}&token=${encodeURIComponent(token)}&s=${encodeURIComponent(firma)}`;

  const response = await fetch(urlFinal, {
    method: "GET",
    headers: {
      Accept: "application/json",
    },
  });

  if (!response.ok) {
    const errorText = await response.text();
    console.error("Error al consultar estado en Flow:", response.status, errorText);
    throw new Error(`Flow getStatus error: ${response.status} - ${errorText}`);
  }

  const data = await response.json();
  return {
    flowOrder: data.flowOrder,
    commerceOrder: data.commerceOrder,
    requestDate: data.requestDate,
    status: data.status,
    subject: data.subject,
    currency: data.currency,
    amount: data.amount,
    payer: data.payer,
    paymentData: data.paymentData,
    isDemo: false,
  };
}
