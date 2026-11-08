"use client";

import React, { useState } from "react";
import Link from "next/link";
import {
  Clock,
  Shield,
  Zap,
  ArrowRight,
  Sparkles,
  CheckCircle2,
  AlertTriangle,
  Building2,
} from "lucide-react";

export function SlaCalculatorWidget() {
  const [criticidad, setCriticidad] = useState<"critica" | "alta" | "media" | "baja">("critica");
  const [tamano, setTamano] = useState<"pyme" | "crecimiento" | "enterprise">("crecimiento");

  // Cálculo de resultados dinámicos
  const obtenerCalculo = () => {
    if (criticidad === "critica" || tamano === "enterprise") {
      return {
        planRecomendado: "MAX",
        nivel: "TIER 3",
        tiempoRespuesta: "< 15 Minutos",
        cobertura: "24/7/365 Non-Stop Continuo",
        canal: "Línea VIP Telefónica + WhatsApp Dedicado",
        precioMensual: "$199.900 CLP / mes",
        colorBadge: "bg-emerald-100 text-emerald-800 border-emerald-300",
        destacado: "Máxima prioridad operacional para comercio electrónico y transacciones en vivo.",
      };
    }
    if (criticidad === "alta" || tamano === "crecimiento") {
      return {
        planRecomendado: "PRO",
        nivel: "TIER 2",
        tiempoRespuesta: "< 4 Horas",
        cobertura: "6x12 (Lunes a Sábado 08:00 - 20:00)",
        canal: "WhatsApp Corporativo + Mesa de Ayuda",
        precioMensual: "$79.900 CLP / mes",
        colorBadge: "bg-blue-100 text-[#0066FF] border-blue-300",
        destacado: "El balance perfecto entre velocidad de respuesta y costo para empresas en expansión.",
      };
    }
    return {
      planRecomendado: "PLUS",
      nivel: "TIER 1",
      tiempoRespuesta: "< 24 Horas",
      cobertura: "5x8 (Lunes a Viernes 09:00 - 18:00)",
      canal: "Portal Web & Correo Electrónico",
      precioMensual: "$29.900 CLP / mes",
      colorBadge: "bg-gray-100 text-gray-800 border-gray-300",
      destacado: "Ideal para pymes que necesitan soporte técnico garantizado en horario hábil.",
    };
  };

  const resultado = obtenerCalculo();

  return (
    <div className="bg-white rounded-3xl border border-gray-200/90 shadow-xl overflow-hidden p-6 sm:p-10 w-full max-w-5xl 2xl:max-w-6xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-gray-100 mb-8">
        <div>
          <span className="text-[11px] font-black uppercase tracking-wider text-[#0066FF] flex items-center gap-1.5 mb-1">
            <Sparkles className="w-3.5 h-3.5 text-[#0066FF]" />
            Simulador de Acuerdos de Nivel de Servicio
          </span>
          <h3 className="text-2xl font-black text-gray-900 tracking-tight">
            Calcula tu Garantía de SLA en Tiempo Real
          </h3>
          <p className="text-xs text-gray-500 mt-0.5">
            Configura los parámetros de tu negocio y descubre el tiempo de respuesta contractualmente asegurado.
          </p>
        </div>
        <div className="px-3.5 py-1.5 rounded-full bg-blue-50 border border-blue-200 text-xs font-bold text-[#0066FF] shrink-0 self-start sm:self-auto">
          Algoritmo SLA GSM v2.0
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
        {/* Controles Interactivos (Izquierda) */}
        <div className="lg:col-span-7 space-y-6">
          {/* Nivel de Criticidad */}
          <div>
            <label className="block text-xs font-bold text-gray-800 mb-2">
              1. Nivel de Urgencia habitual de tus requerimientos:
            </label>
            <div className="grid grid-cols-2 gap-2.5">
              <button
                type="button"
                onClick={() => setCriticidad("critica")}
                className={`p-3 rounded-2xl border text-left transition-all ${
                  criticidad === "critica"
                    ? "border-rose-500 bg-rose-50/70 ring-2 ring-rose-500/20 shadow-xs"
                    : "border-gray-200 bg-gray-50/50 hover:bg-gray-100"
                }`}
              >
                <div className="flex items-center gap-2 mb-1">
                  <span className="w-2.5 h-2.5 rounded-full bg-rose-500 animate-ping" />
                  <span className="text-xs font-black text-gray-900">Crítica / Caída</span>
                </div>
                <p className="text-[10px] text-gray-500">Servidores detenidos o checkout con fallas</p>
              </button>

              <button
                type="button"
                onClick={() => setCriticidad("alta")}
                className={`p-3 rounded-2xl border text-left transition-all ${
                  criticidad === "alta"
                    ? "border-amber-500 bg-amber-50/70 ring-2 ring-amber-500/20 shadow-xs"
                    : "border-gray-200 bg-gray-50/50 hover:bg-gray-100"
                }`}
              >
                <div className="flex items-center gap-2 mb-1">
                  <span className="w-2.5 h-2.5 rounded-full bg-amber-500" />
                  <span className="text-xs font-black text-gray-900">Alta / Rendimiento</span>
                </div>
                <p className="text-[10px] text-gray-500">Módulos lentos o errores en producción</p>
              </button>

              <button
                type="button"
                onClick={() => setCriticidad("media")}
                className={`p-3 rounded-2xl border text-left transition-all ${
                  criticidad === "media"
                    ? "border-blue-500 bg-blue-50/70 ring-2 ring-blue-500/20 shadow-xs"
                    : "border-gray-200 bg-gray-50/50 hover:bg-gray-100"
                }`}
              >
                <div className="flex items-center gap-2 mb-1">
                  <span className="w-2.5 h-2.5 rounded-full bg-blue-500" />
                  <span className="text-xs font-black text-gray-900">Media / Tareas</span>
                </div>
                <p className="text-[10px] text-gray-500">Nuevas configuraciones o cambios</p>
              </button>

              <button
                type="button"
                onClick={() => setCriticidad("baja")}
                className={`p-3 rounded-2xl border text-left transition-all ${
                  criticidad === "baja"
                    ? "border-emerald-500 bg-emerald-50/70 ring-2 ring-emerald-500/20 shadow-xs"
                    : "border-gray-200 bg-gray-50/50 hover:bg-gray-100"
                }`}
              >
                <div className="flex items-center gap-2 mb-1">
                  <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
                  <span className="text-xs font-black text-gray-900">Baja / Consultas</span>
                </div>
                <p className="text-[10px] text-gray-500">Asesorías técnicas o dudas menores</p>
              </button>
            </div>
          </div>

          {/* Tamaño de Organización */}
          <div>
            <label className="block text-xs font-bold text-gray-800 mb-2">
              2. Tamaño y volumen operacional de tu empresa:
            </label>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => setTamano("pyme")}
                className={`py-2.5 px-3 rounded-xl border text-xs font-bold transition-all ${
                  tamano === "pyme"
                    ? "border-[#0066FF] bg-blue-50 text-[#0066FF] ring-1 ring-[#0066FF]"
                    : "border-gray-200 text-gray-600 hover:bg-gray-50"
                }`}
              >
                1 - 10 Personas
              </button>
              <button
                type="button"
                onClick={() => setTamano("crecimiento")}
                className={`py-2.5 px-3 rounded-xl border text-xs font-bold transition-all ${
                  tamano === "crecimiento"
                    ? "border-[#0066FF] bg-blue-50 text-[#0066FF] ring-1 ring-[#0066FF]"
                    : "border-gray-200 text-gray-600 hover:bg-gray-50"
                }`}
              >
                11 - 50 Personas
              </button>
              <button
                type="button"
                onClick={() => setTamano("enterprise")}
                className={`py-2.5 px-3 rounded-xl border text-xs font-bold transition-all ${
                  tamano === "enterprise"
                    ? "border-[#0066FF] bg-blue-50 text-[#0066FF] ring-1 ring-[#0066FF]"
                    : "border-gray-200 text-gray-600 hover:bg-gray-50"
                }`}
              >
                +50 (Enterprise)
              </button>
            </div>
          </div>
        </div>

        {/* Tarjeta de Resultado Dinámico (Derecha) */}
        <div className="lg:col-span-5 bg-gradient-to-br from-gray-900 to-[#0A1A2F] text-white p-6 sm:p-7 rounded-3xl shadow-xl border border-gray-800 flex flex-col justify-between">
          <div>
            <div className="flex justify-between items-start mb-4">
              <div>
                <span className="text-[10px] font-mono text-blue-300 uppercase tracking-widest block">
                  GARANTÍA ASIGNADA
                </span>
                <h4 className="text-xl font-black text-white mt-0.5">
                  Plan {resultado.planRecomendado}
                </h4>
              </div>
              <span className="px-2.5 py-1 rounded-lg text-xs font-bold bg-white/10 text-white border border-white/10">
                {resultado.nivel}
              </span>
            </div>

            <div className="bg-white/10 rounded-2xl p-4 backdrop-blur-md border border-white/10 mb-4 space-y-2.5 text-xs">
              <div className="flex items-center justify-between">
                <span className="text-white/70 flex items-center gap-1.5">
                  <Clock className="w-3.5 h-3.5 text-emerald-400" />
                  Primera Respuesta:
                </span>
                <span className="font-mono font-bold text-emerald-300 text-sm">
                  {resultado.tiempoRespuesta}
                </span>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-white/70 flex items-center gap-1.5">
                  <Shield className="w-3.5 h-3.5 text-blue-300" />
                  Horario de Atención:
                </span>
                <span className="font-semibold text-white/90 text-[11px] text-right">
                  {resultado.cobertura}
                </span>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-white/70 flex items-center gap-1.5">
                  <Zap className="w-3.5 h-3.5 text-amber-400" />
                  Canal Dedicado:
                </span>
                <span className="font-semibold text-white/90 text-[11px] text-right">
                  {resultado.canal}
                </span>
              </div>
            </div>

            <div className="text-2xl font-black text-white tracking-tight mb-1">
              {resultado.precioMensual}
            </div>
            <p className="text-[11px] text-white/60 leading-relaxed mb-6">
              {resultado.destacado}
            </p>
          </div>

          <Link
            href="/planes"
            className="w-full py-3 px-4 rounded-xl font-bold text-xs bg-[#0066FF] hover:bg-[#0052CC] text-white transition-all flex items-center justify-center gap-2 shadow-lg active:scale-98"
          >
            <span>Contratar este Plan con Flow Webpay</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>
      </div>
    </div>
  );
}
