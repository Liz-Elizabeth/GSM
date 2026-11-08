"use client";

import React, { useState, useEffect } from "react";
import { QRCodeSVG } from "qrcode.react";
import {
  Plane,
  Clock,
  MessageSquare,
  Receipt,
  ShieldCheck,
  CheckCircle2,
  Sparkles,
  Lock,
  Radio,
  Activity,
  Send,
  Zap,
} from "lucide-react";

export function InteractiveHeroShowcase() {
  const [tabActiva, setTabActiva] = useState<"pase" | "radar" | "chat" | "pago">("pase");
  const [tiempoSla, setTiempoSla] = useState("03:41:28");

  // Reloj regresivo del SLA simulado en vivo
  useEffect(() => {
    const interval = setInterval(() => {
      const ahora = new Date();
      const segundos = 59 - ahora.getSeconds();
      const minutos = 41;
      const horas = 3;
      setTiempoSla(
        `0${horas}:${minutos}:${segundos < 10 ? "0" + segundos : segundos}`
      );
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="relative w-full max-w-5xl 2xl:max-w-6xl mx-auto mt-12 sm:mt-16">
      {/* Resplandores de fondo con gradientes mesh */}
      <div className="absolute -top-12 -left-12 w-72 h-72 bg-blue-500/20 rounded-full blur-3xl pointer-events-none animate-pulse-glow" />
      <div className="absolute -bottom-10 -right-10 w-80 h-80 bg-indigo-500/20 rounded-full blur-3xl pointer-events-none animate-pulse-glow" />
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-cyan-400/10 rounded-full blur-3xl pointer-events-none" />

      {/* Floating Chips de Telemetría (Efecto Wow de Profundidad) */}
      <div className="hidden lg:flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white/90 backdrop-blur-md border border-gray-200/80 shadow-lg text-xs font-bold text-gray-800 absolute -top-4 -left-6 z-20 animate-float">
        <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-ping" />
        <span className="text-emerald-700">Santiago DC: 0 Incidentes</span>
      </div>

      <div className="hidden lg:flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white/90 backdrop-blur-md border border-gray-200/80 shadow-lg text-xs font-bold text-gray-800 absolute -top-3 -right-6 z-20 animate-float-reverse">
        <Zap className="w-3.5 h-3.5 text-amber-500 fill-amber-500" />
        <span className="text-gray-700">Firestore Sync: <strong className="text-[#0066FF]">11ms</strong></span>
      </div>

      <div className="hidden lg:flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white/90 backdrop-blur-md border border-gray-200/80 shadow-lg text-xs font-bold text-gray-800 absolute -bottom-5 -left-4 z-20 animate-float-reverse">
        <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
        <span className="text-gray-700">Webpay Plus: <strong className="text-emerald-600">Aprobado</strong></span>
      </div>

      <div className="hidden lg:flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white/90 backdrop-blur-md border border-gray-200/80 shadow-lg text-xs font-bold text-gray-800 absolute -bottom-4 -right-4 z-20 animate-float">
        <Plane className="w-3.5 h-3.5 text-[#0066FF]" />
        <span className="text-gray-700">Ruta: <strong className="text-[#0066FF]">CLI ✈️ GSM</strong></span>
      </div>

      {/* Contenedor Principal Glassmorphic */}
      <div className="relative z-10 bg-white/85 backdrop-blur-xl rounded-3xl border border-white/80 shadow-2xl overflow-hidden ring-1 ring-gray-900/5">
        {/* Barra Superior con Selector de Pestañas Interactivas */}
        <div className="bg-gradient-to-r from-gray-900 via-[#0A1A2F] to-gray-900 p-4 sm:p-5 text-white flex flex-col sm:flex-row items-center justify-between gap-3 border-b border-white/10">
          <div className="flex items-center gap-2">
            <div className="flex gap-1.5">
              <span className="w-3 h-3 rounded-full bg-rose-500/80" />
              <span className="w-3 h-3 rounded-full bg-amber-500/80" />
              <span className="w-3 h-3 rounded-full bg-emerald-500/80" />
            </div>
            <div className="h-4 w-px bg-white/20 mx-2" />
            <span className="text-xs font-mono font-bold tracking-wider text-blue-300 flex items-center gap-1.5">
              <Radio className="w-3.5 h-3.5 text-emerald-400 animate-pulse" />
              MISSION CONTROL GSM v3.4
            </span>
          </div>

          {/* Selector de Pestañas Interactivas */}
          <div className="flex items-center p-1 rounded-2xl bg-white/10 backdrop-blur-md border border-white/10 text-xs">
            <button
              type="button"
              onClick={() => setTabActiva("pase")}
              className={`px-3 py-1.5 rounded-xl font-bold transition-all flex items-center gap-1.5 ${
                tabActiva === "pase"
                  ? "bg-[#0066FF] text-white shadow-md scale-102"
                  : "text-white/70 hover:text-white"
              }`}
            >
              <Plane className="w-3.5 h-3.5" />
              <span>Pase QR</span>
            </button>

            <button
              type="button"
              onClick={() => setTabActiva("radar")}
              className={`px-3 py-1.5 rounded-xl font-bold transition-all flex items-center gap-1.5 ${
                tabActiva === "radar"
                  ? "bg-[#0066FF] text-white shadow-md scale-102"
                  : "text-white/70 hover:text-white"
              }`}
            >
              <Activity className="w-3.5 h-3.5" />
              <span>Radar SLA</span>
            </button>

            <button
              type="button"
              onClick={() => setTabActiva("chat")}
              className={`px-3 py-1.5 rounded-xl font-bold transition-all flex items-center gap-1.5 ${
                tabActiva === "chat"
                  ? "bg-[#0066FF] text-white shadow-md scale-102"
                  : "text-white/70 hover:text-white"
              }`}
            >
              <MessageSquare className="w-3.5 h-3.5" />
              <span>Chat Vivo</span>
            </button>

            <button
              type="button"
              onClick={() => setTabActiva("pago")}
              className={`px-3 py-1.5 rounded-xl font-bold transition-all flex items-center gap-1.5 ${
                tabActiva === "pago"
                  ? "bg-[#0066FF] text-white shadow-md scale-102"
                  : "text-white/70 hover:text-white"
              }`}
            >
              <Receipt className="w-3.5 h-3.5" />
              <span>Flow Webpay</span>
            </button>
          </div>
        </div>

        {/* Contenido Dinámico de la Pestaña Activa */}
        <div className="p-6 sm:p-8 bg-gradient-to-b from-gray-50/60 to-white min-h-[380px] flex items-center justify-center">
          {/* 1. PASE DE AVIÓN BOARDING PASS */}
          {tabActiva === "pase" && (
            <div className="w-full max-w-xl bg-white rounded-3xl border border-gray-200/90 shadow-lg overflow-hidden animate-in fade-in zoom-in-95 duration-200">
              {/* Encabezado Azul Aviación */}
              <div className="bg-[#0D253F] text-white p-5 sm:p-6 relative overflow-hidden">
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <span className="text-[10px] font-extrabold uppercase tracking-widest text-blue-300">
                      TARJETA DE EMBARQUE TÉCNICO · BOARDING PASS
                    </span>
                    <h4 className="text-lg font-black tracking-tight text-white mt-0.5">
                      Caída Certificado SSL en Portal de Pagos
                    </h4>
                  </div>
                  <span className="px-2.5 py-1 rounded-full text-[10px] font-black bg-rose-500/20 text-rose-300 border border-rose-400/30 uppercase tracking-wider animate-pulse">
                    URGENTE SLA 4H
                  </span>
                </div>

                {/* Ruta Aérea CLI ✈️ GSM */}
                <div className="flex items-center justify-between pt-2">
                  <div className="text-left">
                    <span className="text-3xl font-black text-white tracking-wider block">CLI</span>
                    <span className="text-[11px] text-white/70">Innovatech SpA</span>
                  </div>

                  <div className="flex-1 flex flex-col items-center px-4">
                    <div className="w-full flex items-center gap-2">
                      <div className="h-0.5 flex-1 bg-white/20 border-b border-dashed border-white/40" />
                      <Plane className="w-5 h-5 text-blue-400 rotate-90" />
                      <div className="h-0.5 flex-1 bg-white/20 border-b border-dashed border-white/40" />
                    </div>
                    <span className="text-[10px] text-blue-300 font-mono mt-1">DIRECTO TIEMPO REAL</span>
                  </div>

                  <div className="text-right">
                    <span className="text-3xl font-black text-white tracking-wider block">GSM</span>
                    <span className="text-[11px] text-white/70">Centro de Soporte</span>
                  </div>
                </div>
              </div>

              {/* Perforación Punteada de Boleto Aéreo con Muescas Semicirculares */}
              <div className="relative py-2 bg-white flex items-center">
                <div className="w-4 h-6 bg-[#F2F2F7] rounded-r-full -ml-2 border-r border-gray-300/80" />
                <div className="flex-1 border-t-2 border-dashed border-gray-300 mx-2" />
                <div className="w-4 h-6 bg-[#F2F2F7] rounded-l-full -mr-2 border-l border-gray-300/80" />
              </div>

              {/* Detalle Inferior con QR y SLA Timer */}
              <div className="p-5 sm:p-6 bg-white flex flex-col sm:flex-row items-center justify-between gap-6">
                <div className="space-y-3 w-full sm:w-auto">
                  <div className="grid grid-cols-2 gap-4 text-xs">
                    <div>
                      <span className="text-[10px] text-gray-400 uppercase font-semibold block">Código Ticket</span>
                      <span className="font-mono font-bold text-gray-900 text-sm">SOL-202611-001</span>
                    </div>
                    <div>
                      <span className="text-[10px] text-gray-400 uppercase font-semibold block">Técnico Asignado</span>
                      <span className="font-bold text-gray-900">Diego Torres</span>
                    </div>
                  </div>

                  <div className="bg-blue-50/80 rounded-2xl p-3 border border-blue-100 flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Clock className="w-4 h-4 text-[#0066FF] animate-spin" />
                      <span className="text-xs font-semibold text-gray-700">Tiempo restante SLA:</span>
                    </div>
                    <span className="text-sm font-mono font-black text-[#0066FF]">{tiempoSla}</span>
                  </div>
                </div>

                {/* Código QR Generado con qrcode.react */}
                <div className="p-3 bg-gray-50 rounded-2xl border border-gray-200 text-center shrink-0 shadow-inner">
                  <QRCodeSVG
                    value="https://gsm-soporte.cl/tickets/SOL-202611-001"
                    size={88}
                    level="H"
                    includeMargin={false}
                  />
                  <span className="text-[9px] font-mono text-gray-500 block mt-1">Escanear Ticket</span>
                </div>
              </div>
            </div>
          )}

          {/* 2. RADAR SLA & TELEMETRÍA */}
          {tabActiva === "radar" && (
            <div className="w-full max-w-xl bg-white rounded-3xl border border-gray-200/90 p-6 shadow-lg animate-in fade-in zoom-in-95 duration-200">
              <div className="flex items-center justify-between pb-4 border-b border-gray-100 mb-6">
                <div className="flex items-center gap-2.5">
                  <div className="w-3 h-3 rounded-full bg-emerald-500 animate-ping" />
                  <span className="text-xs font-bold uppercase tracking-wider text-gray-800">
                    Telemetría en Vivo de Servidores
                  </span>
                </div>
                <span className="px-2.5 py-0.5 rounded-full text-xs font-mono font-bold bg-emerald-100 text-emerald-800">
                  99.98% OPERACIONAL
                </span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
                <div className="bg-gray-50 p-4 rounded-2xl border border-gray-100 text-center">
                  <span className="text-2xl font-black text-[#0066FF] block">8.4 min</span>
                  <span className="text-[11px] text-gray-500 font-semibold">Tiempo Medio de Respuesta</span>
                </div>

                <div className="bg-gray-50 p-4 rounded-2xl border border-gray-100 text-center">
                  <span className="text-2xl font-black text-emerald-600 block">100%</span>
                  <span className="text-[11px] text-gray-500 font-semibold">Cumplimiento SLA Hoy</span>
                </div>

                <div className="bg-gray-50 p-4 rounded-2xl border border-gray-100 text-center">
                  <span className="text-2xl font-black text-gray-900 block">24 / 24</span>
                  <span className="text-[11px] text-gray-500 font-semibold">Nodos Cloud Online</span>
                </div>
              </div>

              <div className="bg-gradient-to-r from-blue-50 to-indigo-50 p-4 rounded-2xl border border-blue-100/80 flex items-center justify-between text-xs">
                <div className="flex items-center gap-2">
                  <ShieldCheck className="w-4 h-4 text-[#0066FF]" />
                  <span className="font-semibold text-gray-800">Monitoreo Preventivo Activo</span>
                </div>
                <span className="text-[11px] font-mono text-gray-500">Última sonda: hace 3s</span>
              </div>
            </div>
          )}

          {/* 3. CHAT TÉCNICO EN VIVO */}
          {tabActiva === "chat" && (
            <div className="w-full max-w-xl bg-white rounded-3xl border border-gray-200/90 shadow-lg overflow-hidden animate-in fade-in zoom-in-95 duration-200">
              <div className="bg-gray-100 px-5 py-3 border-b border-gray-200 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
                  <span className="text-xs font-bold text-gray-900">Diego Torres (Técnico Senior)</span>
                </div>
                <span className="text-[11px] text-gray-500 font-mono">Chat Firestore</span>
              </div>

              <div className="p-4 space-y-3 bg-[#EFEAE2]/30 min-h-[220px] text-xs">
                {/* Mensaje Cliente */}
                <div className="flex justify-start">
                  <div className="bg-white p-3 rounded-2xl rounded-tl-xs shadow-xs max-w-xs border border-gray-200/60">
                    <span className="font-bold text-[#0066FF] block text-[10px] mb-0.5">Camila Rojas (Cliente)</span>
                    <p className="text-gray-800">Hola soporte, el checkout está arrojando error SSL en pagos.</p>
                    <span className="text-[9px] text-gray-400 text-right block mt-1">10:14 AM</span>
                  </div>
                </div>

                {/* Mensaje Técnico */}
                <div className="flex justify-end">
                  <div className="bg-[#0066FF] text-white p-3 rounded-2xl rounded-tr-xs shadow-xs max-w-xs">
                    <span className="font-bold text-blue-200 block text-[10px] mb-0.5">Diego Torres (Soporte)</span>
                    <p>Revisando servidor NGINX. Ya forzamos renovación certbot con DNS challenge.</p>
                    <span className="text-[9px] text-blue-200 text-right block mt-1">10:16 AM · Leído ✓✓</span>
                  </div>
                </div>

                {/* Nota Interna con Candado */}
                <div className="flex justify-center">
                  <div className="bg-amber-50 border border-amber-200 text-amber-900 px-3 py-1.5 rounded-xl text-[11px] flex items-center gap-1.5 shadow-2xs">
                    <Lock className="w-3 h-3 text-amber-600" />
                    <span>Nota Interna: Certificado renovado. Propagando en 10 min.</span>
                  </div>
                </div>
              </div>

              <div className="p-3 bg-white border-t border-gray-200 flex items-center gap-2">
                <input
                  type="text"
                  placeholder="Escribe un mensaje en vivo..."
                  disabled
                  className="flex-1 bg-gray-100 text-xs px-3 py-2 rounded-xl border border-gray-200 text-gray-500"
                  value="Escribiendo respuesta técnica..."
                />
                <button
                  type="button"
                  disabled
                  className="w-8 h-8 rounded-xl bg-[#0066FF] text-white flex items-center justify-center shrink-0"
                >
                  <Send className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          )}

          {/* 4. FACTURACIÓN FLOW WEBPAY */}
          {tabActiva === "pago" && (
            <div className="w-full max-w-md bg-white rounded-3xl border border-gray-200/90 p-6 shadow-lg animate-in fade-in zoom-in-95 duration-200">
              <div className="flex justify-between items-start mb-4">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-lg bg-[#003B95] text-white flex items-center justify-center font-black text-sm">
                    F
                  </div>
                  <div>
                    <span className="text-xs font-bold text-gray-900 block">Comprobante Flow Chile</span>
                    <span className="text-[10px] text-gray-500 font-mono">Orden #994821</span>
                  </div>
                </div>
                <span className="px-2.5 py-1 rounded-full text-xs font-black bg-emerald-100 text-emerald-800">
                  PAGADA
                </span>
              </div>

              <div className="bg-gray-50 rounded-2xl p-4 border border-gray-100 space-y-2 text-xs mb-4">
                <div className="flex justify-between">
                  <span className="text-gray-500">Plan Contratado:</span>
                  <span className="font-bold text-gray-900">Plan PRO (Tier 2)</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Medio de Pago:</span>
                  <span className="font-semibold text-gray-800">Webpay Plus (Débito)</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Comercio:</span>
                  <span className="font-semibold text-gray-800">GSM Soporte Técnico SpA</span>
                </div>
                <div className="pt-2 border-t border-gray-200 flex justify-between items-baseline">
                  <span className="font-bold text-gray-900 text-sm">Total Pagado:</span>
                  <span className="text-xl font-black text-gray-900">$79.900 CLP</span>
                </div>
              </div>

              <div className="flex items-center justify-between text-[11px] text-gray-500 bg-emerald-50 border border-emerald-200 rounded-xl p-2.5">
                <span className="flex items-center gap-1.5 font-semibold text-emerald-800">
                  <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" />
                  Firma Criptográfica HMAC-SHA256 Verificada
                </span>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
