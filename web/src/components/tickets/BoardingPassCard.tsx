"use client";

import React, { useState } from "react";
import Link from "next/link";
import { QRCodeSVG } from "qrcode.react";
import { Solicitud } from "@/types/solicitud";
import { EstadoBadge } from "@/components/common/Badges";
import {
  ShieldCheck,
  Server,
  QrCode,
  ChevronDown,
  ChevronUp,
  Share2,
  Copy,
  Check,
  ArrowRight,
  MessageCircle,
  Clock,
  Lock,
} from "lucide-react";

export function BoardingPassCard({ solicitud }: { solicitud: Solicitud }) {
  const [abierto, setAbierto] = useState(false);
  const [copiado, setCopiado] = useState(false);

  // Mapeo corporativo de Tier y SLA según prioridad
  const planInfo = (() => {
    switch (solicitud.prioridad) {
      case "URGENTE":
        return {
          tier: "TIER 3 MAX",
          sla: "< 15 MIN",
          badgeColor: "bg-emerald-500/20 text-emerald-400 border-emerald-500/40",
          accentColor: "#10B981",
        };
      case "ALTA":
      case "MEDIA":
        return {
          tier: "TIER 2 PRO",
          sla: "< 4 HORAS",
          badgeColor: "bg-blue-500/20 text-blue-400 border-blue-500/40",
          accentColor: "#0066FF",
        };
      default:
        return {
          tier: "TIER 1 PLUS",
          sla: "< 24 HORAS",
          badgeColor: "bg-slate-500/20 text-slate-300 border-slate-500/40",
          accentColor: "#64748B",
        };
    }
  })();

  const urlSeguimiento = typeof window !== "undefined"
    ? `${window.location.origin}/tickets/${solicitud.codigoSeguimiento}`
    : `https://gsm.soporte.cl/tickets/${solicitud.codigoSeguimiento}`;

  const copiarEnlace = () => {
    navigator.clipboard.writeText(urlSeguimiento);
    setCopiado(true);
    setTimeout(() => setCopiado(false), 2500);
  };

  const compartirPorWhatsApp = () => {
    const texto = `🔐 *PASE DE SERVICIO ENTERPRISE - GSM*\n\n` +
      `📋 *Pase:* ${solicitud.codigoSeguimiento}\n` +
      `🏢 *Nivel:* ${planInfo.tier} (SLA ${planInfo.sla})\n` +
      `💻 *Activo:* ${solicitud.activoRelacionado || "Infraestructura Central"}\n` +
      `📌 *Asunto:* ${solicitud.titulo}\n` +
      `🏷️ *Categoría:* ${solicitud.categoria.replace("_", " ")}\n` +
      `📊 *Estado:* ${solicitud.estado}\n\n` +
      `🔗 *Validación en Línea:* ${urlSeguimiento}`;
    window.open(`https://api.whatsapp.com/send?text=${encodeURIComponent(texto)}`, "_blank");
  };

  const formatearFecha = (ms: number) => {
    return new Intl.DateTimeFormat("es-CL", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    }).format(new Date(ms));
  };

  return (
    <div className="bg-white rounded-3xl overflow-hidden border border-[#E2E8F0] shadow-sm hover:shadow-md transition-all duration-300 flex flex-col">
      {/* 1. CABECERA ENTERPRISE OBSIDIAN */}
      <div className="bg-[#0F172A] px-5 py-3 text-white flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded-lg bg-blue-500/20 flex items-center justify-center border border-blue-500/30">
            <ShieldCheck className="w-3.5 h-3.5 text-blue-400" />
          </div>
          <div>
            <span className="font-extrabold text-[11px] tracking-wider uppercase font-mono block text-white leading-tight">
              GSM ENTERPRISE PASS
            </span>
            <span className="text-[9px] font-bold tracking-widest text-slate-400 uppercase">
              CREDENTIAL & CUSTODY
            </span>
          </div>
        </div>
        <div
          className={`px-2.5 py-1 rounded-full text-[10px] font-bold border ${planInfo.badgeColor}`}
        >
          {planInfo.tier} • SLA {planInfo.sla}
        </div>
      </div>

      {/* 2. CUERPO CORPORATIVO */}
      <div className="p-5 space-y-3.5">
        {/* Código y Estado */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="font-mono text-xs font-black text-[#0F172A] bg-slate-100 px-2.5 py-1 rounded-md border border-slate-200">
              {solicitud.codigoSeguimiento}
            </span>
            <span className="text-xs text-slate-500 font-medium">
              • {solicitud.categoria.replace("_", " ")}
            </span>
          </div>
          <EstadoBadge estado={solicitud.estado} />
        </div>

        {/* Asunto */}
        <div>
          <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wide block">
            INCIDENCIA / REQUERIMIENTO
          </span>
          <h4 className="font-bold text-sm text-[#0F172A] leading-snug line-clamp-2 mt-0.5">
            {solicitud.titulo}
          </h4>
        </div>

        {/* Activo / Infraestructura Relacionada */}
        <div className="bg-slate-50 border border-slate-200/80 rounded-xl p-2.5 flex items-center gap-2.5">
          <Server className="w-4 h-4 text-[#0066FF] shrink-0" />
          <div className="min-w-0">
            <span className="text-[9px] font-bold text-slate-400 uppercase block">
              ACTIVO / INFRAESTRUCTURA RELACIONADA
            </span>
            <p className="text-xs font-semibold text-slate-700 truncate">
              {solicitud.activoRelacionado || "Infraestructura Cloud / Servidor On-Premise"}
            </p>
          </div>
        </div>

        {/* Metadata de Operación */}
        <div className="grid grid-cols-2 gap-3 pt-1 border-t border-slate-100">
          <div>
            <span className="text-[9px] font-bold text-slate-400 uppercase block">
              INGENIERO ASIGNADO
            </span>
            <span className="text-xs font-bold text-slate-800 flex items-center gap-1 mt-0.5">
              {solicitud.tecnicoNombre ? (
                <>
                  <span className="w-2 h-2 rounded-full bg-emerald-500 inline-block" />
                  {solicitud.tecnicoNombre}
                </>
              ) : (
                <>
                  <Clock className="w-3 h-3 text-amber-500" />
                  <span className="text-amber-700">En cola de asignación</span>
                </>
              )}
            </span>
          </div>
          <div className="text-right">
            <span className="text-[9px] font-bold text-slate-400 uppercase block">
              EMISIÓN DE PASE
            </span>
            <span className="text-xs font-semibold text-slate-600 mt-0.5 block">
              {formatearFecha(solicitud.fechaCreacion)}
            </span>
          </div>
        </div>
      </div>

      {/* 3. LÍNEA TROQUELADA DE PERFORACIÓN CON MUESCAS LATERALES */}
      <div className="relative flex items-center w-full py-1">
        <div className="absolute -left-3 w-6 h-6 rounded-full bg-[#F2F2F7] border border-[#E5E5EA]" />
        <div className="w-full border-t-2 border-dashed border-slate-300/80 mx-5" />
        <div className="absolute -right-3 w-6 h-6 rounded-full bg-[#F2F2F7] border border-[#E5E5EA]" />
      </div>

      {/* 4. TALÓN DEL PASE: EL QR SE VE AL ABRIRLO */}
      <div className="p-5 pt-3">
        {!abierto ? (
          <div className="flex flex-col gap-3">
            <button
              onClick={() => setAbierto(true)}
              className="w-full bg-slate-50 hover:bg-slate-100 border border-slate-200 transition-colors rounded-2xl p-3 flex items-center justify-between group active:scale-[0.99]"
            >
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-[#0F172A] flex items-center justify-center text-white shadow-2xs group-hover:scale-105 transition-transform">
                  <QrCode className="w-5 h-5" />
                </div>
                <div className="text-left">
                  <span className="block text-xs font-bold text-[#0F172A]">
                    Credencial QR & Validación en Terreno
                  </span>
                  <span className="block text-[11px] text-slate-500">
                    Clic para abrir pase y validar con el personal técnico
                  </span>
                </div>
              </div>
              <ChevronDown className="w-5 h-5 text-[#0066FF] group-hover:translate-y-0.5 transition-transform" />
            </button>

            <div className="flex justify-end">
              <Link
                href={`/tickets/${solicitud.codigoSeguimiento}`}
                className="inline-flex items-center gap-1 text-xs font-bold text-[#0066FF] hover:underline"
              >
                <span>Ver Detalle del Ticket</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>
          </div>
        ) : (
          <div className="flex flex-col items-center gap-3.5 animate-in fade-in zoom-in-95 duration-200">
            <div className="p-3 bg-slate-50 border border-slate-200 rounded-2xl shadow-inner">
              <QRCodeSVG
                value={urlSeguimiento}
                size={180}
                level="H"
                includeMargin={true}
              />
            </div>

            <div className="text-center">
              <p className="font-mono text-[11px] font-black text-slate-700">
                HASH: GSM-PASS-{solicitud.id}-{solicitud.codigoSeguimiento}
              </p>
              <p className="text-[11px] text-slate-500 mt-0.5 max-w-xs">
                Presenta este código al personal técnico para autorizar la custodia física y computar el SLA del plan contratado.
              </p>
            </div>

            {/* Botones de Acción */}
            <div className="grid grid-cols-2 gap-2 w-full">
              <button
                onClick={copiarEnlace}
                className="flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold bg-slate-100 hover:bg-slate-200 text-slate-800 transition-colors"
              >
                {copiado ? (
                  <>
                    <Check className="w-3.5 h-3.5 text-emerald-600" />
                    <span className="text-emerald-700">¡Copiado!</span>
                  </>
                ) : (
                  <>
                    <Copy className="w-3.5 h-3.5 text-slate-500" />
                    <span>Copiar Enlace</span>
                  </>
                )}
              </button>

              <button
                onClick={compartirPorWhatsApp}
                className="flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold bg-emerald-50 hover:bg-emerald-100 text-emerald-700 border border-emerald-200 transition-colors"
              >
                <MessageCircle className="w-3.5 h-3.5" />
                <span>WhatsApp</span>
              </button>
            </div>

            <div className="flex items-center justify-between w-full pt-1">
              <button
                onClick={() => setAbierto(false)}
                className="inline-flex items-center gap-1 text-xs font-semibold text-slate-500 hover:text-slate-900"
              >
                <ChevronUp className="w-3.5 h-3.5" />
                <span>Plegar Credencial</span>
              </button>

              <Link
                href={`/tickets/${solicitud.codigoSeguimiento}`}
                className="inline-flex items-center gap-1 text-xs font-bold text-[#0066FF] hover:underline"
              >
                <span>Ficha Ticket</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
