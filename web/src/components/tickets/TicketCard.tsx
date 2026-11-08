"use client";

import React from "react";
import Link from "next/link";
import { Solicitud } from "@/types/solicitud";
import { EstadoBadge, PrioridadBadge, CategoriaBadge } from "@/components/common/Badges";
import {
  MessageSquare,
  QrCode,
  ArrowRight,
  Clock,
  UserCheck,
  Calendar,
  AlertCircle,
  Paperclip,
} from "lucide-react";

export function TicketCard({ solicitud }: { solicitud: Solicitud }) {
  const ahora = Date.now();
  const estaAtrasada =
    solicitud.fechaLimite != null &&
    solicitud.fechaLimite < ahora &&
    solicitud.estado !== "RESUELTA" &&
    solicitud.estado !== "CERRADA" &&
    solicitud.estado !== "CANCELADA";

  const formatearFecha = (ms: number) => {
    return new Intl.DateTimeFormat("es-CL", {
      day: "2-digit",
      month: "short",
      hour: "2-digit",
      minute: "2-digit",
    }).format(new Date(ms));
  };

  return (
    <div
      className={`bg-white rounded-2xl p-5 border transition-all duration-200 hover:shadow-md flex flex-col justify-between ${
        estaAtrasada
          ? "border-rose-300 ring-1 ring-rose-200"
          : "border-[#E5E5EA] hover:border-[#C7C7CC]"
      }`}
    >
      <div>
        {/* Cabecera de la Tarjeta */}
        <div className="flex items-center justify-between gap-2 mb-3">
          <div className="flex items-center gap-2">
            <span className="font-mono text-xs font-black text-[#0066FF] bg-blue-50 px-2 py-1 rounded-md border border-blue-100">
              {solicitud.codigoSeguimiento}
            </span>
            {estaAtrasada && (
              <span className="inline-flex items-center gap-1 text-[11px] font-bold text-rose-700 bg-rose-50 px-2 py-0.5 rounded-full border border-rose-200 animate-pulse">
                <AlertCircle className="w-3 h-3 text-rose-600" />
                SLA Vencido
              </span>
            )}
          </div>
          <div className="flex items-center gap-1.5">
            <PrioridadBadge prioridad={solicitud.prioridad} />
            <EstadoBadge estado={solicitud.estado} />
          </div>
        </div>

        {/* Título y Categoría */}
        <div className="mb-2">
          <div className="mb-1.5">
            <CategoriaBadge categoria={solicitud.categoria} />
          </div>
          <Link
            href={`/tickets/${solicitud.codigoSeguimiento}`}
            className="group block"
          >
            <h3 className="font-bold text-base text-[#1C1C1E] group-hover:text-[#0066FF] transition-colors line-clamp-2">
              {solicitud.titulo}
            </h3>
          </Link>
        </div>

        {/* Descripción breve */}
        <p className="text-xs text-[#8E8E93] line-clamp-2 mb-4 leading-relaxed">
          {solicitud.descripcion}
        </p>
      </div>

      {/* Pie de la Tarjeta con Metadatos y Accesos */}
      <div className="pt-3 border-t border-[#F2F2F7] flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2.5">
        <div className="flex flex-wrap items-center gap-3 text-[11px] text-[#8E8E93]">
          <span className="flex items-center gap-1">
            <Calendar className="w-3 h-3 text-[#AEAEB2]" />
            {formatearFecha(solicitud.fechaCreacion)}
          </span>

          <span className="flex items-center gap-1">
            <UserCheck className="w-3 h-3 text-[#AEAEB2]" />
            {solicitud.tecnicoNombre || "Sin técnico asignado"}
          </span>

          {solicitud.evidencias && solicitud.evidencias.length > 0 && (
            <span className="flex items-center gap-1 text-[#0066FF] font-medium">
              <Paperclip className="w-3 h-3" />
              {solicitud.evidencias.length}
            </span>
          )}
        </div>

        {/* Acciones Rápidas */}
        <div className="flex items-center gap-1.5 self-end sm:self-auto">
          <Link
            href={`/tickets/${solicitud.codigoSeguimiento}/chat`}
            className="p-1.5 rounded-lg text-[#8E8E93] hover:text-[#0066FF] hover:bg-blue-50 transition-colors"
            title="Abrir Chat en Vivo"
          >
            <MessageSquare className="w-4 h-4" />
          </Link>

          <Link
            href={`/tickets/${solicitud.codigoSeguimiento}?tab=qr`}
            className="p-1.5 rounded-lg text-[#8E8E93] hover:text-[#0066FF] hover:bg-blue-50 transition-colors"
            title="Ver Pase de Abordaje QR"
          >
            <QrCode className="w-4 h-4" />
          </Link>

          <Link
            href={`/tickets/${solicitud.codigoSeguimiento}`}
            className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl text-xs font-semibold text-[#0066FF] bg-blue-50 hover:bg-blue-100 transition-colors"
          >
            <span>Detalle</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>
      </div>
    </div>
  );
}
