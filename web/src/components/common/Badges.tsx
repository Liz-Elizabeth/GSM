"use client";

import React from "react";
import {
  EstadoSolicitud,
  PrioridadSolicitud,
  CategoriaSolicitud,
  ESTADOS_INFO,
  PRIORIDADES_INFO,
  CATEGORIAS_INFO,
} from "@/types/solicitud";
import { RolUsuario } from "@/types/usuario";

export function EstadoBadge({ estado }: { estado: EstadoSolicitud }) {
  const info = ESTADOS_INFO[estado] || ESTADOS_INFO.RECIBIDA;
  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold border ${info.colorBg} ${info.colorText} ${info.borderColor}`}
    >
      <span className={`w-1.5 h-1.5 rounded-full ${info.dotColor}`} />
      {info.label}
    </span>
  );
}

export function PrioridadBadge({ prioridad }: { prioridad: PrioridadSolicitud }) {
  const info = PRIORIDADES_INFO[prioridad] || PRIORIDADES_INFO.MEDIA;
  return (
    <span
      className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold border ${info.badgeClass}`}
      title={info.slaTexto}
    >
      <span>{info.iconEmoji}</span>
      <span>{info.label}</span>
      <span className="text-[10px] opacity-75 font-normal">({info.slaHoras}h)</span>
    </span>
  );
}

export function CategoriaBadge({ categoria }: { categoria: CategoriaSolicitud }) {
  const info = CATEGORIAS_INFO[categoria] || CATEGORIAS_INFO.OTRO;
  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-md text-xs font-medium border ${info.color}`}
    >
      <span>{info.emoji}</span>
      <span>{info.label}</span>
    </span>
  );
}

export function RolBadge({ rol }: { rol: RolUsuario }) {
  switch (rol) {
    case "ADMINISTRADOR":
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-purple-100 text-purple-800 border border-purple-200">
          👑 Admin GSM
        </span>
      );
    case "TECNICO":
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-blue-100 text-blue-800 border border-blue-200">
          🔧 Técnico Especialista
        </span>
      );
    case "CLIENTE":
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800 border border-emerald-200">
          🏢 Cliente Corporativo
        </span>
      );
    default:
      return null;
  }
}
