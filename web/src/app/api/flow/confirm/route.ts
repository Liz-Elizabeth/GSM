import { NextRequest, NextResponse } from "next/server";
import { consultarEstadoEnFlow } from "@/lib/flow";
import {
  actualizarEstadoTransaccion,
  obtenerTransaccionPorToken,
} from "@/services/transaccionesService";
import { activarSuscripcionUsuarioEnFirestore } from "@/services/usuarioService";

export async function POST(req: NextRequest) {
  try {
    let token = "";

    const contentType = req.headers.get("content-type") || "";
    if (contentType.includes("application/x-www-form-urlencoded")) {
      const formData = await req.formData();
      token = (formData.get("token") as string) || "";
    } else {
      const body = await req.json().catch(() => ({}));
      token = body.token || "";
    }

    if (!token) {
      return NextResponse.json({ error: "Token no proporcionado" }, { status: 400 });
    }

    const estadoFlow = await consultarEstadoEnFlow(token);

    let estadoTexto: "PENDIENTE" | "PAGADA" | "RECHAZADA" | "ANULADA" = "PENDIENTE";
    if (estadoFlow.status === 2) estadoTexto = "PAGADA";
    else if (estadoFlow.status === 3) estadoTexto = "RECHAZADA";
    else if (estadoFlow.status === 4) estadoTexto = "ANULADA";

    const fechaPago = estadoFlow.status === 2 ? Date.now() : null;
    const medioPago = estadoFlow.paymentData?.media || "Webpay Plus";

    await actualizarEstadoTransaccion(token, {
      estado: estadoTexto,
      flowOrder: estadoFlow.flowOrder,
      fechaPago,
      medioPago,
      rawResponse: estadoFlow as unknown as Record<string, unknown>,
    });

    if (estadoFlow.status === 2) {
      const tx = await obtenerTransaccionPorToken(token);
      if (tx) {
        await activarSuscripcionUsuarioEnFirestore({
          clienteId: tx.clienteId,
          clienteEmail: tx.clienteEmail,
          planId: tx.planId,
          flowOrder: estadoFlow.flowOrder,
          fechaPago,
        });
      }
    }

    console.log(`[Flow Webhook] Confirmación procesada para token ${token}: ${estadoTexto}`);
    return NextResponse.json({ success: true, estado: estadoTexto });
  } catch (error) {
    console.error("Error en webhook de Flow:", error);
    return NextResponse.json(
      { error: "Error procesando webhook de Flow", detalle: String(error) },
      { status: 500 }
    );
  }
}
