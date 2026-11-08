import { NextRequest, NextResponse } from "next/server";
import { crearOrdenEnFlow } from "@/lib/flow";
import { PLANES_SOPORTE, TransaccionPago } from "@/types/plan";
import { guardarTransaccion } from "@/services/transaccionesService";

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const { planId, esAnual, clienteId, clienteNombre, clienteEmail } = body;

    const plan = PLANES_SOPORTE.find((p) => p.id === planId);
    if (!plan) {
      return NextResponse.json({ error: "Plan inválido" }, { status: 400 });
    }

    const montoFinal = esAnual
      ? Math.round(plan.precioMensualClp * 12 * 0.8)
      : plan.precioMensualClp;

    const timestamp = Date.now();
    const sufijoAleatorio = Math.floor(Math.random() * 9000 + 1000);
    const commerceOrder = `GSM-${plan.id.toUpperCase()}-${timestamp.toString().slice(-6)}-${sufijoAleatorio}`;

    // Host base dinámico
    const host = req.headers.get("host") || "localhost:3000";
    const protocol = host.includes("localhost") ? "http" : "https";
    const baseUrl = process.env.NEXT_PUBLIC_APP_URL || `${protocol}://${host}`;

    const urlConfirmation = `${baseUrl}/api/flow/confirm`;
    const urlReturn = `${baseUrl}/planes/retorno`;

    const resultadoFlow = await crearOrdenEnFlow({
      commerceOrder,
      subject: `GSM Soporte - Plan ${plan.nombre} (${esAnual ? "Anual" : "Mensual"})`,
      amount: montoFinal,
      email: clienteEmail || "cliente@gsm.cl",
      urlConfirmation,
      urlReturn,
      currency: "CLP",
    });

    // Guardar transacción inicial en Firestore
    const nuevaTx: TransaccionPago = {
      id: resultadoFlow.token,
      commerceOrder,
      flowOrder: resultadoFlow.flowOrder || null,
      token: resultadoFlow.token,
      planId: plan.id,
      planNombre: `Plan ${plan.nombre}`,
      montoClp: montoFinal,
      esAnual: Boolean(esAnual),
      estado: "PENDIENTE",
      clienteId: Number(clienteId || 1),
      clienteNombre: clienteNombre || "Cliente GSM",
      clienteEmail: clienteEmail || "cliente@gsm.cl",
      fechaCreacion: timestamp,
      urlRetorno: urlReturn,
    };

    await guardarTransaccion(nuevaTx);

    // Determinar URL de redirección al cliente
    const urlRedireccion = resultadoFlow.isDemo
      ? `/planes/simulador?token=${resultadoFlow.token}&order=${commerceOrder}&plan=${plan.id}&monto=${montoFinal}`
      : `${resultadoFlow.url}?token=${resultadoFlow.token}`;

    return NextResponse.json({
      success: true,
      token: resultadoFlow.token,
      flowOrder: resultadoFlow.flowOrder,
      commerceOrder,
      urlRedireccion,
      isDemo: resultadoFlow.isDemo,
    });
  } catch (error) {
    console.error("Error en /api/flow/create:", error);
    return NextResponse.json(
      { error: "Error al generar la orden de pago en Flow", detalle: String(error) },
      { status: 500 }
    );
  }
}
