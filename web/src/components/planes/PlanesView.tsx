"use client";

import React, { useState } from "react";
import Link from "next/link";
import {
  Check,
  Shield,
  Sparkles,
  Zap,
  Clock,
  CreditCard,
  Receipt,
  HelpCircle,
  AlertCircle,
  ExternalLink,
} from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { PLANES_SOPORTE, PlanSoporteWeb } from "@/types/plan";
import { Container } from "@/components/common/Container";

export function PlanesView() {
  const { usuarioActual } = useAuth();
  const [esAnual, setEsAnual] = useState(false);
  const [cargandoPlanId, setCargandoPlanId] = useState<string | null>(null);
  const [modalCheckout, setModalCheckout] = useState<PlanSoporteWeb | null>(null);
  const [emailPagador, setEmailPagador] = useState(usuarioActual.correo || "cliente@empresa.cl");
  const [errorPago, setErrorPago] = useState<string | null>(null);

  const planActivoId = usuarioActual.plan || "pro";

  const calcularPrecio = (precioMensual: number) => {
    if (esAnual) {
      const anualConDcto = Math.round(precioMensual * 12 * 0.8);
      return {
        monto: anualConDcto,
        texto: new Intl.NumberFormat("es-CL", { style: "currency", currency: "CLP" }).format(
          anualConDcto
        ),
        equivalenteMensual: new Intl.NumberFormat("es-CL", {
          style: "currency",
          currency: "CLP",
        }).format(Math.round(anualConDcto / 12)),
      };
    }
    return {
      monto: precioMensual,
      texto: new Intl.NumberFormat("es-CL", { style: "currency", currency: "CLP" }).format(
        precioMensual
      ),
      equivalenteMensual: null,
    };
  };

  const iniciarCheckout = async (plan: PlanSoporteWeb) => {
    setErrorPago(null);
    setCargandoPlanId(plan.id);

    try {
      const res = await fetch("/api/flow/create", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          planId: plan.id,
          esAnual,
          clienteId: usuarioActual.id,
          clienteNombre: usuarioActual.nombre,
          clienteEmail: emailPagador,
        }),
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.detalle || "Error iniciando pago con Flow");
      }

      const data = await res.json();
      if (data.urlRedireccion) {
        window.location.href = data.urlRedireccion;
      } else {
        throw new Error("No se recibió URL de redirección desde la pasarela");
      }
    } catch (err) {
      console.error("Error en checkout Flow:", err);
      setErrorPago(err instanceof Error ? err.message : "Error al procesar con Flow");
      setCargandoPlanId(null);
    }
  };

  return (
    <Container className="py-10 sm:py-14">
      {/* Encabezado Principal */}
      <div className="text-center max-w-3xl mx-auto mb-10">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-bold bg-blue-50 text-[#0066FF] border border-blue-100 mb-3">
          <Sparkles className="w-3.5 h-3.5" />
          Acuerdos de Nivel de Servicio (SLA) & Pasarela Flow Chile
        </div>
        <h1 className="text-3xl sm:text-4xl font-black text-gray-900 tracking-tight">
          Planes de Soporte y Continuidad Operacional
        </h1>
        <p className="mt-3 text-base text-gray-600">
          Escoge el nivel de respuesta y atención técnica garantizada que tu empresa necesita.
          Pagos en línea seguros con Webpay Plus a través de Flow.
        </p>

        {/* Toggle Facturación Mensual vs Anual */}
        <div className="mt-8 inline-flex items-center p-1.5 rounded-2xl bg-gray-100 border border-gray-200">
          <button
            type="button"
            onClick={() => setEsAnual(false)}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              !esAnual ? "bg-white text-gray-900 shadow-xs" : "text-gray-500 hover:text-gray-900"
            }`}
          >
            Facturación Mensual
          </button>
          <button
            type="button"
            onClick={() => setEsAnual(true)}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5 ${
              esAnual ? "bg-white text-[#0066FF] shadow-xs" : "text-gray-500 hover:text-gray-900"
            }`}
          >
            <span>Facturación Anual</span>
            <span className="px-1.5 py-0.5 rounded-md text-[10px] font-black bg-emerald-100 text-emerald-800">
              -20% DCTO
            </span>
          </button>
        </div>
      </div>

      {/* Grid de 3 Planes */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8 items-stretch mb-16">
        {PLANES_SOPORTE.map((plan) => {
          const precio = calcularPrecio(plan.precioMensualClp);
          const esPlanActual = plan.id === planActivoId;
          const esRecomendado = plan.esRecomendado;

          return (
            <div
              key={plan.id}
              className={`relative rounded-3xl bg-white border flex flex-col justify-between transition-all duration-200 ${
                esRecomendado
                  ? "border-[#0066FF] shadow-xl ring-2 ring-[#0066FF]/20 md:-translate-y-2"
                  : "border-gray-200 shadow-sm hover:shadow-md"
              }`}
            >
              {esRecomendado && (
                <div className="absolute -top-3.5 left-1/2 -translate-x-1/2 px-4 py-1 rounded-full bg-[#0066FF] text-white text-xs font-black shadow-md flex items-center gap-1">
                  <Sparkles className="w-3.5 h-3.5" />
                  <span>MÁS POPULAR</span>
                </div>
              )}

              <div className="p-7">
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <span className="text-[11px] font-extrabold uppercase tracking-wider text-gray-400">
                      {plan.nivel}
                    </span>
                    <h3 className="text-2xl font-black text-gray-900">{plan.nombre}</h3>
                  </div>
                  <span className="px-2.5 py-1 rounded-lg text-xs font-bold bg-gray-100 text-gray-700">
                    {plan.insignia}
                  </span>
                </div>

                <div className="mb-6">
                  <div className="flex items-baseline gap-1">
                    <span className="text-3xl sm:text-4xl font-black text-gray-900 tracking-tight">
                      {precio.texto}
                    </span>
                    <span className="text-xs text-gray-500 font-semibold">
                      {esAnual ? "/ año" : "/ mes"}
                    </span>
                  </div>
                  {esAnual && precio.equivalenteMensual && (
                    <span className="block text-xs font-semibold text-emerald-600 mt-1">
                      Equivalente a {precio.equivalenteMensual} / mes
                    </span>
                  )}
                </div>

                <div className="bg-gray-50 rounded-2xl p-4 space-y-2.5 mb-6 border border-gray-100">
                  <div className="flex items-center justify-between text-xs">
                    <span className="text-gray-500 font-medium flex items-center gap-1.5">
                      <Clock className="w-3.5 h-3.5 text-[#0066FF]" />
                      SLA Respuesta:
                    </span>
                    <span className="font-bold text-gray-900">{plan.slaRespuesta}</span>
                  </div>
                  <div className="flex items-center justify-between text-xs">
                    <span className="text-gray-500 font-medium flex items-center gap-1.5">
                      <Zap className="w-3.5 h-3.5 text-amber-500" />
                      Límite Solicitudes:
                    </span>
                    <span className="font-semibold text-gray-800 text-right">
                      {plan.limiteTicketsTexto}
                    </span>
                  </div>
                  <div className="flex items-center justify-between text-xs">
                    <span className="text-gray-500 font-medium flex items-center gap-1.5">
                      <Shield className="w-3.5 h-3.5 text-emerald-600" />
                      Cobertura:
                    </span>
                    <span className="font-semibold text-gray-800">{plan.horarioSoporte}</span>
                  </div>
                </div>

                <div className="space-y-3">
                  <span className="text-xs font-bold text-gray-900 uppercase tracking-wider block">
                    Beneficios Incluidos:
                  </span>
                  <ul className="space-y-2 text-xs text-gray-600">
                    {plan.beneficiosClave.map((beneficio, idx) => (
                      <li key={idx} className="flex items-start gap-2">
                        <div className="w-4 h-4 rounded-full bg-emerald-100 text-emerald-700 flex items-center justify-center shrink-0 mt-0.5">
                          <Check className="w-3 h-3 stroke-[3]" />
                        </div>
                        <span>{beneficio}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>

              <div className="p-7 pt-0">
                {esPlanActual ? (
                  <button
                    type="button"
                    disabled
                    className="w-full py-3.5 px-4 rounded-2xl font-bold text-sm bg-gray-100 text-gray-500 cursor-default flex items-center justify-center gap-2 border border-gray-200"
                  >
                    <Check className="w-4 h-4" />
                    <span>Tu Plan Activo Actual</span>
                  </button>
                ) : (
                  <button
                    type="button"
                    disabled={cargandoPlanId !== null}
                    onClick={() => setModalCheckout(plan)}
                    className={`w-full py-3.5 px-4 rounded-2xl font-bold text-sm transition-all flex items-center justify-center gap-2 active:scale-98 shadow-sm ${
                      esRecomendado
                        ? "bg-[#0066FF] hover:bg-[#0052CC] text-white shadow-[#0066FF]/20"
                        : "bg-gray-900 hover:bg-black text-white"
                    } disabled:opacity-50`}
                  >
                    <CreditCard className="w-4 h-4" />
                    <span>Contratar con Flow / Webpay</span>
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Barra de Acceso al Historial */}
      <div className="bg-white rounded-3xl p-6 border border-gray-200/80 shadow-xs flex flex-col sm:flex-row items-center justify-between gap-4 mb-16">
        <div className="flex items-center gap-4 text-left">
          <div className="w-12 h-12 rounded-2xl bg-blue-50 text-[#0066FF] flex items-center justify-center shrink-0">
            <Receipt className="w-6 h-6" />
          </div>
          <div>
            <h4 className="font-bold text-gray-900 text-base">Historial de Pagos y Comprobantes</h4>
            <p className="text-xs text-gray-500">
              Revisa tus órdenes de compra, números de transacción Flow y certificados de pago.
            </p>
          </div>
        </div>

        <Link
          href="/transacciones"
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl text-xs font-bold text-[#0066FF] bg-blue-50 hover:bg-blue-100 transition-colors"
        >
          <span>Ver todas las Transacciones</span>
          <ExternalLink className="w-3.5 h-3.5" />
        </Link>
      </div>

      {/* FAQ */}
      <div className="max-w-3xl mx-auto">
        <h3 className="text-xl font-bold text-gray-900 text-center mb-6">
          Preguntas Frecuentes sobre Facturación y Soporte
        </h3>
        <div className="space-y-4">
          <div className="bg-white rounded-2xl p-5 border border-gray-200">
            <h4 className="font-bold text-sm text-gray-900 mb-1 flex items-center gap-2">
              <HelpCircle className="w-4 h-4 text-[#0066FF]" />
              ¿Qué medios de pago acepta la pasarela Flow?
            </h4>
            <p className="text-xs text-gray-600 leading-relaxed">
              Flow Chile integra directamente Webpay Plus (Tarjetas de Crédito, Débito Redcompra y
              Prepago), MACH, Khipu, Servipag y transferencias bancarias directas.
            </p>
          </div>

          <div className="bg-white rounded-2xl p-5 border border-gray-200">
            <h4 className="font-bold text-sm text-gray-900 mb-1 flex items-center gap-2">
              <HelpCircle className="w-4 h-4 text-[#0066FF]" />
              ¿El cambio de SLA es inmediato tras pagar?
            </h4>
            <p className="text-xs text-gray-600 leading-relaxed">
              Sí. Al validarse la transacción en la pasarela Flow, la base de datos de Firebase
              actualiza tu perfil automáticamente. Cualquier ticket que generes tendrá la nueva
              prioridad y tiempo de atención de inmediato.
            </p>
          </div>

          <div className="bg-white rounded-2xl p-5 border border-gray-200">
            <h4 className="font-bold text-sm text-gray-900 mb-1 flex items-center gap-2">
              <HelpCircle className="w-4 h-4 text-[#0066FF]" />
              ¿Se emite factura electrónica?
            </h4>
            <p className="text-xs text-gray-600 leading-relaxed">
              Sí, todas las transacciones emitidas a través de GSM generan automáticamente factura
              exenta o afecta según el giro comercial de tu empresa (Innovatech SpA).
            </p>
          </div>
        </div>
      </div>

      {/* Modal Checkout */}
      {modalCheckout && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 sm:p-7 shadow-2xl border border-gray-200 animate-in fade-in zoom-in-95 duration-150">
            <div className="flex justify-between items-center mb-4">
              <div>
                <span className="text-[10px] font-extrabold uppercase tracking-wider text-blue-600">
                  Pasarela Flow Chile
                </span>
                <h3 className="text-xl font-black text-gray-900">Confirmar Suscripción</h3>
              </div>
              <button
                type="button"
                onClick={() => setModalCheckout(null)}
                className="w-8 h-8 rounded-full bg-gray-100 hover:bg-gray-200 text-gray-600 flex items-center justify-center font-bold text-sm"
              >
                ✕
              </button>
            </div>

            <div className="bg-gray-50 rounded-2xl p-4 border border-gray-200/60 mb-4">
              <div className="flex justify-between items-baseline mb-1">
                <span className="font-bold text-gray-900">Plan {modalCheckout.nombre}</span>
                <span className="font-black text-lg text-gray-900">
                  {calcularPrecio(modalCheckout.precioMensualClp).texto}
                </span>
              </div>
              <div className="text-xs text-gray-500">
                Modalidad: <span className="font-semibold text-gray-800">{esAnual ? "Anual (-20% dcto)" : "Mensual"}</span>
              </div>
              <div className="text-xs text-gray-500 mt-1">
                Garantía SLA: <span className="font-semibold text-blue-600">{modalCheckout.slaRespuesta}</span>
              </div>
            </div>

            <div className="space-y-3 mb-6">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Correo electrónico para comprobante
                </label>
                <input
                  type="email"
                  value={emailPagador}
                  onChange={(e) => setEmailPagador(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm font-medium text-gray-800 focus:outline-hidden focus:ring-2 focus:ring-[#0066FF]"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Cliente Solicitante
                </label>
                <div className="px-3.5 py-2 rounded-xl bg-gray-100 text-xs font-semibold text-gray-700">
                  {usuarioActual.nombre} ({usuarioActual.empresa || "Empresa GSM"})
                </div>
              </div>
            </div>

            {errorPago && (
              <div className="mb-4 p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center gap-2">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{errorPago}</span>
              </div>
            )}

            <div className="space-y-2">
              <button
                type="button"
                disabled={cargandoPlanId !== null}
                onClick={() => iniciarCheckout(modalCheckout)}
                className="w-full py-3.5 px-4 rounded-xl font-bold text-sm text-white bg-[#0066FF] hover:bg-[#0052CC] active:scale-98 transition-all flex items-center justify-center gap-2 shadow-md hover:shadow-lg disabled:opacity-50"
              >
                {cargandoPlanId ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    <span>Generando Orden con Flow...</span>
                  </>
                ) : (
                  <>
                    <CreditCard className="w-4 h-4" />
                    <span>Continuar a Webpay Plus</span>
                  </>
                )}
              </button>

              <button
                type="button"
                onClick={() => setModalCheckout(null)}
                className="w-full py-2.5 px-4 rounded-xl text-xs font-semibold text-gray-500 hover:text-gray-800 transition-colors"
              >
                Cancelar
              </button>
            </div>
          </div>
        </div>
      )}
    </Container>
  );
}
