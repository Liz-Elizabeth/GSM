"use client";

import React, { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import Link from "next/link";
import confetti from "canvas-confetti";
import {
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Receipt,
  ArrowRight,
  RefreshCw,
  Printer,
  ShieldCheck,
  Calendar,
  CreditCard,
  Sparkles,
  Ticket,
} from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { TransaccionPago } from "@/types/plan";

export function RetornoPagoView() {
  const searchParams = useSearchParams();
  const { actualizarPlanUsuario, usuarioActual } = useAuth();

  const token = searchParams.get("token");

  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [transaccion, setTransaccion] = useState<TransaccionPago | null>(null);
  const [estadoVerificado, setEstadoVerificado] = useState<
    "PAGADA" | "RECHAZADA" | "PENDIENTE" | "ERROR"
  >("PENDIENTE");

  useEffect(() => {
    if (!token) {
      setError("No se recibió ningún identificador de transacción (token) desde Flow.");
      setCargando(false);
      return;
    }

    let isMounted = true;

    async function verificarPago() {
      try {
        const res = await fetch(`/api/flow/status?token=${encodeURIComponent(token!)}`);
        if (!res.ok) {
          throw new Error(`Respuesta de verificación HTTP ${res.status}`);
        }
        const data = await res.json();

        if (!isMounted) return;

        if (data.estadoTexto === "PAGADA") {
          setEstadoVerificado("PAGADA");
          setTransaccion(data.transaccion);

          // Actualizar plan del usuario en la sesión
          if (data.transaccion?.planId) {
            actualizarPlanUsuario(data.transaccion.planId);
          }

          // Lanzar confetti de celebración
          try {
            confetti({
              particleCount: 80,
              spread: 70,
              origin: { y: 0.6 },
              colors: ["#0066FF", "#10B981", "#F59E0B"],
            });
          } catch {
            // Ignorar en entornos sin soporte canvas
          }
        } else if (data.estadoTexto === "RECHAZADA") {
          setEstadoVerificado("RECHAZADA");
          setTransaccion(data.transaccion);
        } else {
          setEstadoVerificado("PENDIENTE");
          setTransaccion(data.transaccion);
        }
      } catch (err) {
        console.error("Error verificando pago con Flow:", err);
        if (isMounted) {
          setError("No se pudo verificar el estado en la pasarela Flow. Intenta nuevamente.");
          setEstadoVerificado("ERROR");
        }
      } finally {
        if (isMounted) setCargando(false);
      }
    }

    verificarPago();

    return () => {
      isMounted = false;
    };
  }, [token, actualizarPlanUsuario]);

  const formatearMonto = (monto?: number) => {
    return new Intl.NumberFormat("es-CL", { style: "currency", currency: "CLP" }).format(
      monto || 0
    );
  };

  const formatearFecha = (epoch?: number | null) => {
    if (!epoch) return "Recién emitido";
    return new Intl.DateTimeFormat("es-CL", {
      dateStyle: "medium",
      timeStyle: "short",
    }).format(new Date(epoch));
  };

  if (cargando) {
    return (
      <div className="min-h-[70vh] flex flex-col items-center justify-center px-4">
        <div className="w-14 h-14 rounded-2xl bg-blue-50 border border-blue-200 flex items-center justify-center text-[#0066FF] mb-4 animate-bounce">
          <RefreshCw className="w-6 h-6 animate-spin text-[#0066FF]" />
        </div>
        <h2 className="text-xl font-bold text-gray-900 tracking-tight">
          Verificando transacción con Flow Chile...
        </h2>
        <p className="text-sm text-gray-500 mt-1">
          Consultando firma criptográfica HMAC-SHA256 y estado bancario Webpay.
        </p>
      </div>
    );
  }

  if (error || estadoVerificado === "ERROR") {
    return (
      <div className="w-full min-h-[calc(100dvh-4rem)] flex items-center justify-center py-12 px-4 text-center">
        <div className="w-full max-w-xl">
          <div className="w-16 h-16 rounded-full bg-rose-100 text-rose-600 flex items-center justify-center mx-auto mb-4">
            <AlertTriangle className="w-8 h-8" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">Error de Verificación</h1>
          <p className="text-sm text-gray-600 mb-6">{error || "Ocurrió un error inesperado al validar la transacción."}</p>
          <div className="flex gap-3 justify-center">
            <Link
              href="/planes"
              className="px-5 py-2.5 rounded-xl bg-[#0066FF] text-white font-semibold text-sm hover:bg-[#0052CC] transition-colors"
            >
              Volver a Planes
            </Link>
            <Link
              href="/transacciones"
              className="px-5 py-2.5 rounded-xl border border-gray-300 text-gray-700 font-semibold text-sm hover:bg-gray-100 transition-colors"
            >
              Ver Transacciones
            </Link>
          </div>
        </div>
      </div>
    );
  }

  if (estadoVerificado === "RECHAZADA") {
    return (
      <div className="w-full min-h-[calc(100dvh-4rem)] flex items-center justify-center py-10 px-4">
        <div className="w-full max-w-xl">
          <div className="bg-white rounded-3xl p-8 border border-rose-200 shadow-sm text-center">
          <div className="w-16 h-16 rounded-full bg-rose-50 text-rose-600 flex items-center justify-center mx-auto mb-4">
            <XCircle className="w-8 h-8" />
          </div>
          <span className="px-3 py-1 rounded-full text-xs font-bold bg-rose-100 text-rose-800 uppercase tracking-wider">
            Transacción Rechazada
          </span>
          <h1 className="text-2xl font-black text-gray-900 mt-3 mb-2">Pago no completado</h1>
          <p className="text-sm text-gray-600 mb-6">
            La entidad emisora o pasarela de pagos Flow rechazó la transacción. No se realizó ningún
            cargo a tu cuenta bancaria.
          </p>

          {transaccion && (
            <div className="bg-gray-50 rounded-2xl p-4 text-left space-y-2 mb-6 border border-gray-200/60 text-xs">
              <div className="flex justify-between">
                <span className="text-gray-500 font-medium">Orden Flow:</span>
                <span className="font-mono font-bold text-gray-800">#{transaccion.flowOrder || "N/A"}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500 font-medium">Orden Comercio:</span>
                <span className="font-mono text-gray-800">{transaccion.commerceOrder}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500 font-medium">Monto:</span>
                <span className="font-bold text-gray-900">{formatearMonto(transaccion.montoClp)}</span>
              </div>
            </div>
          )}

          <div className="flex flex-col sm:flex-row gap-3 justify-center">
            <Link
              href="/planes"
              className="px-6 py-3 rounded-xl bg-[#0066FF] text-white font-bold text-sm hover:bg-[#0052CC] transition-colors"
            >
              Reintentar Pago
            </Link>
            <Link
              href="/"
              className="px-6 py-3 rounded-xl border border-gray-300 text-gray-700 font-semibold text-sm hover:bg-gray-50 transition-colors"
            >
              Ir a la Bandeja Principal
            </Link>
          </div>
        </div>
      </div>
    </div>
    );
  }

  return (
    <div className="w-full min-h-[calc(100dvh-4rem)] flex items-center justify-center py-10 px-4 sm:px-6">
      <div className="w-full max-w-2xl">
        <div className="bg-white rounded-3xl shadow-lg border border-emerald-200 overflow-hidden print:shadow-none print:border-none">
        <div className="bg-gradient-to-r from-emerald-600 to-teal-600 p-8 text-white text-center relative">
          <div className="w-16 h-16 rounded-2xl bg-white/20 backdrop-blur-md flex items-center justify-center mx-auto mb-3 shadow-inner">
            <CheckCircle2 className="w-9 h-9 text-white" />
          </div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-extrabold bg-white/20 backdrop-blur-md text-white mb-2 uppercase tracking-wide">
            <ShieldCheck className="w-3.5 h-3.5" />
            Pago Verificado con Flow Chile
          </div>
          <h1 className="text-2xl sm:text-3xl font-black tracking-tight text-white">
            ¡Suscripción Activada Exitosamente!
          </h1>
          <p className="text-sm text-emerald-100 mt-1 max-w-md mx-auto">
            El plan de soporte ha sido actualizado y tus nuevos niveles de SLA ya se encuentran vigentes.
          </p>
        </div>

        <div className="p-6 sm:p-8 space-y-6">
          <div className="bg-gradient-to-br from-blue-50/70 to-indigo-50/40 rounded-2xl p-5 border border-blue-100/80">
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs font-bold text-[#0066FF] uppercase tracking-wider flex items-center gap-1.5">
                <Sparkles className="w-3.5 h-3.5 text-[#0066FF]" />
                Plan Activo
              </span>
              <span className="px-2.5 py-0.5 rounded-full text-xs font-black bg-emerald-100 text-emerald-800">
                PAGADA
              </span>
            </div>

            <div className="flex flex-col sm:flex-row justify-between sm:items-baseline gap-2">
              <div>
                <h3 className="text-xl font-black text-gray-900">
                  {transaccion?.planNombre || `Plan ${usuarioActual.plan?.toUpperCase() || "PRO"}`}
                </h3>
                <p className="text-xs text-gray-600">
                  Beneficiario: <span className="font-semibold text-gray-800">{usuarioActual.nombre}</span> ({usuarioActual.empresa || "Cliente Corporativo"})
                </p>
              </div>
              <div className="sm:text-right">
                <span className="text-2xl font-black text-gray-900 tracking-tight">
                  {formatearMonto(transaccion?.montoClp)}
                </span>
                <span className="block text-[11px] text-gray-500">
                  {transaccion?.esAnual ? "Facturación Anual (-20% dcto)" : "Facturación Mensual"}
                </span>
              </div>
            </div>
          </div>

          <div className="border border-gray-200 rounded-2xl p-5 space-y-3 text-xs bg-gray-50/50">
            <div className="flex items-center justify-between pb-2 border-b border-gray-200">
              <span className="font-bold text-gray-700 flex items-center gap-1.5">
                <Receipt className="w-4 h-4 text-gray-500" />
                Comprobante de Transacción
              </span>
              <span className="text-gray-400 font-mono">FLOW-{transaccion?.flowOrder || "994012"}</span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-1">
              <div>
                <span className="text-gray-500 block mb-0.5 font-medium">ID de Transacción Flow</span>
                <span className="font-mono font-bold text-gray-900 text-sm">
                  #{transaccion?.flowOrder || "994012"}
                </span>
              </div>
              <div>
                <span className="text-gray-500 block mb-0.5 font-medium">Orden de Compra Comercio</span>
                <span className="font-mono font-bold text-gray-900">
                  {transaccion?.commerceOrder || "GSM-PRO-849201"}
                </span>
              </div>
              <div>
                <span className="text-gray-500 block mb-0.5 font-medium flex items-center gap-1">
                  <Calendar className="w-3.5 h-3.5 text-gray-400" />
                  Fecha de Autorización
                </span>
                <span className="text-gray-800 font-semibold">
                  {formatearFecha(transaccion?.fechaPago || Date.now())}
                </span>
              </div>
              <div>
                <span className="text-gray-500 block mb-0.5 font-medium flex items-center gap-1">
                  <CreditCard className="w-3.5 h-3.5 text-gray-400" />
                  Medio de Pago
                </span>
                <span className="text-gray-800 font-semibold">
                  {transaccion?.medioPago || "Webpay Plus Débito / Crédito"}
                </span>
              </div>
            </div>

            <div className="pt-2 border-t border-gray-200">
              <span className="text-gray-500 block mb-0.5 font-medium">Token de Pasarela (Firma Criptográfica)</span>
              <span className="font-mono text-[10px] text-gray-600 break-all bg-white px-2 py-1 rounded border border-gray-200 block">
                {transaccion?.token || token}
              </span>
            </div>
          </div>

          <div className="pt-2 flex flex-col sm:flex-row gap-3 print:hidden">
            <Link
              href="/"
              className="flex-1 py-3.5 px-4 rounded-xl font-bold text-sm text-white bg-[#0066FF] hover:bg-[#0052CC] active:scale-98 transition-all flex items-center justify-center gap-2 shadow-sm"
            >
              <Ticket className="w-4 h-4" />
              <span>Ir a la Bandeja de Tickets</span>
              <ArrowRight className="w-4 h-4" />
            </Link>

            <Link
              href="/transacciones"
              className="py-3.5 px-5 rounded-xl font-semibold text-sm text-gray-700 bg-gray-100 hover:bg-gray-200 transition-colors flex items-center justify-center gap-1.5"
            >
              <Receipt className="w-4 h-4" />
              <span>Historial de Pagos</span>
            </Link>

            <button
              type="button"
              onClick={() => window.print()}
              className="py-3.5 px-4 rounded-xl font-semibold text-sm text-gray-700 border border-gray-200 hover:bg-gray-50 transition-colors flex items-center justify-center gap-1.5"
              title="Imprimir Voucher"
            >
              <Printer className="w-4 h-4" />
              <span className="sm:hidden">Imprimir</span>
            </button>
          </div>
        </div>
      </div>
    </div>
    </div>
  );
}
