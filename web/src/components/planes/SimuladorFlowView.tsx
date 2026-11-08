"use client";

import React, { useState } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { ShieldCheck, CreditCard, AlertCircle, ArrowLeft, CheckCircle2, Lock } from "lucide-react";
import Link from "next/link";

export function SimuladorFlowView() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const token = searchParams.get("token") || `DEMO_TOKEN_${Date.now()}`;
  const order = searchParams.get("order") || "GSM-PRO-DEMO";
  const plan = searchParams.get("plan") || "pro";
  const monto = searchParams.get("monto") || "79900";

  const [procesando, setProcesando] = useState(false);
  const [tarjetaNumero, setTarjetaNumero] = useState("4500 •••• •••• 1234");
  const [cuotas, setCuotas] = useState("0");

  const formatearMonto = (num: string | number) => {
    return new Intl.NumberFormat("es-CL", { style: "currency", currency: "CLP" }).format(
      Number(num) || 0
    );
  };

  const simularPago = (exitoso: boolean) => {
    setProcesando(true);
    setTimeout(() => {
      if (exitoso) {
        router.push(`/planes/retorno?token=${token}`);
      } else {
        router.push(`/planes/retorno?token=${token}_RECHAZO_`);
      }
    }, 1200);
  };

  return (
    <div className="w-full min-h-[calc(100dvh-4rem)] flex flex-col justify-center items-center py-10 px-4 sm:px-6">
      <div className="w-full max-w-md">
        {/* Banner Ambiente Sandbox Flow */}
        <div className="mb-4 bg-amber-500/10 border border-amber-500/20 text-amber-900 rounded-2xl p-3 flex items-center justify-between text-xs font-medium">
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-amber-500 animate-ping" />
            <span>Entorno Sandbox de Pruebas: Flow Chile</span>
          </div>
          <span className="px-2 py-0.5 bg-amber-100 rounded-md font-bold text-[10px] text-amber-800 uppercase">
            Simulador
          </span>
        </div>

        {/* Tarjeta Pasarela Webpay / Flow */}
        <div className="bg-white rounded-3xl shadow-xl border border-gray-200/80 overflow-hidden">
          <div className="bg-gradient-to-r from-[#003B95] to-[#0066FF] p-6 text-white text-center relative">
            <div className="flex items-center justify-center gap-2 mb-2">
              <div className="w-8 h-8 rounded-lg bg-white/20 backdrop-blur-md flex items-center justify-center font-black text-sm">
                F
              </div>
              <span className="font-extrabold text-lg tracking-tight">flow</span>
              <span className="text-white/60 mx-1">×</span>
              <span className="font-bold text-sm tracking-widest text-emerald-300">WEBPAY PLUS</span>
            </div>
            <p className="text-xs text-white/80">Pasarela Segura de Pagos Electrónicos</p>

            <div className="absolute top-4 right-4 flex items-center gap-1 text-[10px] bg-white/10 px-2 py-1 rounded-full backdrop-blur-md text-white/90">
              <Lock className="w-3 h-3 text-emerald-300" />
              <span>TLS 1.3 256-bit</span>
            </div>
          </div>

          <div className="p-6 border-b border-gray-100 bg-gray-50/50">
            <div className="flex justify-between items-center mb-2">
              <span className="text-xs text-gray-500 font-medium">Comercio</span>
              <span className="text-xs font-semibold text-gray-800">GSM Soporte Técnico SpA</span>
            </div>
            <div className="flex justify-between items-center mb-2">
              <span className="text-xs text-gray-500 font-medium">Orden de Compra</span>
              <span className="text-xs font-mono font-bold text-gray-700 bg-gray-200/60 px-2 py-0.5 rounded">
                {order}
              </span>
            </div>
            <div className="flex justify-between items-center mb-3">
              <span className="text-xs text-gray-500 font-medium">Concepto</span>
              <span className="text-xs font-semibold text-blue-600 uppercase">
                Plan {plan} - GSM SLA
              </span>
            </div>

            <div className="pt-3 border-t border-gray-200 flex justify-between items-baseline">
              <span className="text-sm font-bold text-gray-900">Total a Pagar</span>
              <span className="text-2xl font-black text-gray-900 tracking-tight">
                {formatearMonto(monto)}
              </span>
            </div>
          </div>

          <div className="p-6 space-y-4">
            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">
                Tarjeta de Prueba
              </label>
              <div className="relative">
                <input
                  type="text"
                  value={tarjetaNumero}
                  onChange={(e) => setTarjetaNumero(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 bg-gray-50/50 text-sm font-mono text-gray-800 focus:outline-hidden focus:ring-2 focus:ring-[#0066FF]"
                />
                <CreditCard className="w-5 h-5 text-gray-400 absolute right-3 top-2.5" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">
                Opciones de Cuotas
              </label>
              <select
                value={cuotas}
                onChange={(e) => setCuotas(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 bg-white text-sm text-gray-800 focus:outline-hidden focus:ring-2 focus:ring-[#0066FF]"
              >
                <option value="0">Sin cuotas (Débito / Crédito 1 pago)</option>
                <option value="3">3 Cuotas sin interés</option>
                <option value="6">6 Cuotas sin interés</option>
                <option value="12">12 Cuotas precio contado</option>
              </select>
            </div>

            <div className="pt-3 space-y-2.5">
              <button
                type="button"
                disabled={procesando}
                onClick={() => simularPago(true)}
                className="w-full py-3.5 px-4 rounded-xl text-sm font-bold text-white bg-emerald-600 hover:bg-emerald-700 active:scale-98 transition-all flex items-center justify-center gap-2 shadow-md hover:shadow-lg disabled:opacity-50"
              >
                {procesando ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    <span>Conectando con Flow Chile...</span>
                  </>
                ) : (
                  <>
                    <CheckCircle2 className="w-4 h-4" />
                    <span>Autorizar Pago Webpay (Aprobado)</span>
                  </>
                )}
              </button>

              <button
                type="button"
                disabled={procesando}
                onClick={() => simularPago(false)}
                className="w-full py-2.5 px-4 rounded-xl text-xs font-semibold text-rose-700 bg-rose-50 hover:bg-rose-100 border border-rose-200 transition-colors flex items-center justify-center gap-1.5 disabled:opacity-50"
              >
                <AlertCircle className="w-3.5 h-3.5 text-rose-500" />
                <span>Simular Tarjeta Rechazada (Error de Fondos)</span>
              </button>
            </div>

            <div className="pt-2 text-center">
              <Link
                href="/planes"
                className="inline-flex items-center gap-1 text-xs text-gray-500 hover:text-gray-800 font-medium transition-colors"
              >
                <ArrowLeft className="w-3 h-3" />
                <span>Cancelar y volver al catálogo de planes</span>
              </Link>
            </div>
          </div>

          <div className="bg-gray-50 px-6 py-3 border-t border-gray-100 flex items-center justify-between text-[11px] text-gray-500">
            <span className="flex items-center gap-1">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" />
              Certificado Flow Sandbox v2.4
            </span>
            <span className="font-mono text-[10px] text-gray-400">ID: {token.slice(-10)}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
