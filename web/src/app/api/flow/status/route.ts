import { NextRequest, NextResponse } from "next/server";
import { consultarEstadoEnFlow } from "@/lib/flow";
import {
  actualizarEstadoTransaccion,
  obtenerTransaccionPorToken,
} from "@/services/transaccionesService";
import { activarSuscripcionUsuarioEnFirestore } from "@/services/usuarioService";

export async function GET(req: NextRequest) {
  try {
    const { searchParams } = new URL(req.url);
    const token = searchParams.get("token");

    if (!token) {
      return NextResponse.json({ error: "Token de pago faltante" }, { status: 400 });
    }

    // 1. Consultar estado en Flow (oficial o simulación sandbox)
    const estadoFlow = await consultarEstadoEnFlow(token);

    // Mapear status numérico de Flow
    // 1: Pendiente, 2: Pagada, 3: Rechazada, 4: Anulada
    let estadoTexto: "PENDIENTE" | "PAGADA" | "RECHAZADA" | "ANULADA" = "PENDIENTE";
    if (estadoFlow.status === 2) estadoTexto = "PAGADA";
    else if (estadoFlow.status === 3) estadoTexto = "RECHAZADA";
    else if (estadoFlow.status === 4) estadoTexto = "ANULADA";

    // 2. Actualizar transacción en Firestore
    const fechaPago = estadoFlow.status === 2 ? Date.now() : null;
    const medioPago = estadoFlow.paymentData?.media || "Webpay Plus";

    await actualizarEstadoTransaccion(token, {
      estado: estadoTexto,
      flowOrder: estadoFlow.flowOrder,
      fechaPago,
      medioPago,
      rawResponse: estadoFlow as unknown as Record<string, unknown>,
    });

    const txActualizada = await obtenerTransaccionPorToken(token);

    // 3. Si el pago fue aprobado por Flow (status 2 = Pagada), activar la suscripción del cliente
    if (estadoFlow.status === 2 && txActualizada) {
      await activarSuscripcionUsuarioEnFirestore({
        clienteId: txActualizada.clienteId,
        clienteEmail: txActualizada.clienteEmail,
        planId: txActualizada.planId,
        flowOrder: estadoFlow.flowOrder,
        fechaPago,
      });
    }

    return NextResponse.json({
      success: true,
      status: estadoFlow.status,
      estadoTexto,
      flowOrder: estadoFlow.flowOrder,
      commerceOrder: estadoFlow.commerceOrder,
      monto: estadoFlow.amount,
      medioPago,
      payer: estadoFlow.payer,
      transaccion: txActualizada,
      isDemo: estadoFlow.isDemo,
    });
  } catch (error) {
    console.error("Error en /api/flow/status:", error);
    return NextResponse.json(
      { error: "Error consultando el estado del pago", detalle: String(error) },
      { status: 500 }
    );
  }
}
