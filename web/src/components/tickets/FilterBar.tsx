"use client";

import React, { useState } from "react";
import { EstadoSolicitud, PrioridadSolicitud, ESTADOS_INFO, PRIORIDADES_INFO } from "@/types/solicitud";
import { TECNICOS_DISPONIBLES } from "@/types/usuario";
import { useAuth } from "@/context/AuthContext";
import {
  Search,
  Filter,
  X,
  Clock,
  LayoutGrid,
  Ticket as TicketIcon,
  ChevronDown,
  UserCheck,
} from "lucide-react";

export interface FiltrosState {
  busqueda: string;
  estado: string; // "TODOS" | "ATRASADAS" | EstadoSolicitud
  prioridad: string; // "TODAS" | PrioridadSolicitud
  asignacion: string; // "TODOS" | "SIN_ASIGNAR" | string (id tecnico)
}

interface FilterBarProps {
  filtros: FiltrosState;
  onFiltrosChange: (nuevosFiltros: FiltrosState) => void;
  vistaActiva: "lista" | "wallet";
  onVistaChange: (vista: "lista" | "wallet") => void;
  totalResultados: number;
}

export function FilterBar({
  filtros,
  onFiltrosChange,
  vistaActiva,
  onVistaChange,
  totalResultados,
}: FilterBarProps) {
  const { isAdmin } = useAuth();
  const [dropdownEstadoAbierto, setDropdownEstadoAbierto] = useState(false);
  const [dropdownPrioridadAbierto, setDropdownPrioridadAbierto] = useState(false);
  const [dropdownAsignacionAbierto, setDropdownAsignacionAbierto] = useState(false);

  const hayFiltrosActivos =
    filtros.busqueda.trim() !== "" ||
    filtros.estado !== "TODOS" ||
    filtros.prioridad !== "TODAS" ||
    (isAdmin && filtros.asignacion !== "TODOS");

  const limpiarFiltros = () => {
    onFiltrosChange({
      busqueda: "",
      estado: "TODOS",
      prioridad: "TODAS",
      asignacion: "TODOS",
    });
  };

  return (
    <div className="bg-white rounded-2xl p-4 border border-[#E5E5EA] shadow-2xs space-y-3.5">
      {/* Fila Superior: Buscador Reactivo y Switcher de Vista */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
        {/* Input de Búsqueda */}
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-[#8E8E93] absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          <input
            type="text"
            value={filtros.busqueda}
            onChange={(e) =>
              onFiltrosChange({ ...filtros, busqueda: e.target.value })
            }
            placeholder="Buscar por código (ej: SOL-202611-001) o asunto..."
            className="w-full pl-10 pr-9 py-2.5 rounded-xl bg-[#F2F2F7] border border-transparent focus:border-[#0066FF] focus:bg-white text-sm text-[#1C1C1E] placeholder:text-[#8E8E93] outline-hidden transition-all"
          />
          {filtros.busqueda && (
            <button
              onClick={() => onFiltrosChange({ ...filtros, busqueda: "" })}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-[#8E8E93] hover:text-[#1C1C1E]"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>

        {/* Switcher de Vista: Tarjetas vs Boarding Passes */}
        <div className="flex items-center bg-[#F2F2F7] p-1 rounded-xl self-end sm:self-auto">
          <button
            onClick={() => onVistaChange("lista")}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all flex items-center gap-1.5 ${
              vistaActiva === "lista"
                ? "bg-white text-[#0066FF] shadow-2xs"
                : "text-[#8E8E93] hover:text-[#1C1C1E]"
            }`}
          >
            <LayoutGrid className="w-3.5 h-3.5" />
            Lista
          </button>
          <button
            onClick={() => onVistaChange("wallet")}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all flex items-center gap-1.5 ${
              vistaActiva === "wallet"
                ? "bg-white text-[#0066FF] shadow-2xs"
                : "text-[#8E8E93] hover:text-[#1C1C1E]"
            }`}
          >
            <TicketIcon className="w-3.5 h-3.5" />
            Boarding Passes
          </button>
        </div>
      </div>

      {/* Fila Inferior: Menús Desplegables de Filtros (DropdownMenu) */}
      <div className="flex flex-wrap items-center gap-2 pt-1 border-t border-[#F2F2F7]">
        <div className="flex items-center gap-1.5 text-xs font-bold text-[#8E8E93] mr-1">
          <Filter className="w-3.5 h-3.5 text-[#0066FF]" />
          <span>Filtros:</span>
        </div>

        {/* 1. Dropdown de Estado */}
        <div className="relative">
          <button
            onClick={() => {
              setDropdownEstadoAbierto(!dropdownEstadoAbierto);
              setDropdownPrioridadAbierto(false);
              setDropdownAsignacionAbierto(false);
            }}
            className={`px-3 py-1.5 rounded-xl text-xs font-semibold border flex items-center gap-1.5 transition-colors ${
              filtros.estado !== "TODOS"
                ? "bg-blue-50 border-blue-200 text-[#0066FF]"
                : "bg-white border-[#E5E5EA] text-[#1C1C1E] hover:bg-[#F2F2F7]"
            }`}
          >
            <span>
              {filtros.estado === "TODOS"
                ? "Estado: Todos"
                : filtros.estado === "ATRASADAS"
                ? "⚠️ Solo Atrasadas"
                : `Estado: ${ESTADOS_INFO[filtros.estado as EstadoSolicitud]?.label || filtros.estado}`}
            </span>
            <ChevronDown className="w-3 h-3 text-[#8E8E93]" />
          </button>

          {dropdownEstadoAbierto && (
            <>
              <div
                className="fixed inset-0 z-30"
                onClick={() => setDropdownEstadoAbierto(false)}
              />
              <div className="absolute left-0 mt-1.5 w-56 bg-white rounded-2xl shadow-xl border border-[#E5E5EA] z-40 p-1.5 animate-in fade-in zoom-in-95 duration-100 max-h-72 overflow-y-auto">
                <button
                  onClick={() => {
                    onFiltrosChange({ ...filtros, estado: "TODOS" });
                    setDropdownEstadoAbierto(false);
                  }}
                  className={`w-full text-left px-3 py-1.5 rounded-xl text-xs font-medium transition-colors ${
                    filtros.estado === "TODOS" ? "bg-blue-50 text-[#0066FF] font-bold" : "hover:bg-[#F2F2F7]"
                  }`}
                >
                  Todos los estados
                </button>

                <button
                  onClick={() => {
                    onFiltrosChange({ ...filtros, estado: "ATRASADAS" });
                    setDropdownEstadoAbierto(false);
                  }}
                  className={`w-full text-left px-3 py-1.5 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-colors ${
                    filtros.estado === "ATRASADAS"
                      ? "bg-rose-50 text-rose-700 font-bold"
                      : "text-rose-600 hover:bg-rose-50/50"
                  }`}
                >
                  <Clock className="w-3 h-3" />
                  ⚠️ Solo Atrasadas (SLA Vencido)
                </button>

                <div className="my-1 border-t border-[#F2F2F7]" />

                {(Object.keys(ESTADOS_INFO) as EstadoSolicitud[]).map((est) => (
                  <button
                    key={est}
                    onClick={() => {
                      onFiltrosChange({ ...filtros, estado: est });
                      setDropdownEstadoAbierto(false);
                    }}
                    className={`w-full text-left px-3 py-1.5 rounded-xl text-xs transition-colors flex items-center justify-between ${
                      filtros.estado === est
                        ? "bg-blue-50 text-[#0066FF] font-bold"
                        : "hover:bg-[#F2F2F7] text-[#1C1C1E]"
                    }`}
                  >
                    <span>{ESTADOS_INFO[est].label}</span>
                    <span className={`w-2 h-2 rounded-full ${ESTADOS_INFO[est].dotColor}`} />
                  </button>
                ))}
              </div>
            </>
          )}
        </div>

        {/* 2. Dropdown de Prioridad SLA */}
        <div className="relative">
          <button
            onClick={() => {
              setDropdownPrioridadAbierto(!dropdownPrioridadAbierto);
              setDropdownEstadoAbierto(false);
              setDropdownAsignacionAbierto(false);
            }}
            className={`px-3 py-1.5 rounded-xl text-xs font-semibold border flex items-center gap-1.5 transition-colors ${
              filtros.prioridad !== "TODAS"
                ? "bg-blue-50 border-blue-200 text-[#0066FF]"
                : "bg-white border-[#E5E5EA] text-[#1C1C1E] hover:bg-[#F2F2F7]"
            }`}
          >
            <span>
              {filtros.prioridad === "TODAS"
                ? "Prioridad SLA: Todas"
                : `${PRIORIDADES_INFO[filtros.prioridad as PrioridadSolicitud]?.iconEmoji} ${
                    PRIORIDADES_INFO[filtros.prioridad as PrioridadSolicitud]?.label
                  }`}
            </span>
            <ChevronDown className="w-3 h-3 text-[#8E8E93]" />
          </button>

          {dropdownPrioridadAbierto && (
            <>
              <div
                className="fixed inset-0 z-30"
                onClick={() => setDropdownPrioridadAbierto(false)}
              />
              <div className="absolute left-0 mt-1.5 w-60 bg-white rounded-2xl shadow-xl border border-[#E5E5EA] z-40 p-1.5 animate-in fade-in zoom-in-95 duration-100">
                <button
                  onClick={() => {
                    onFiltrosChange({ ...filtros, prioridad: "TODAS" });
                    setDropdownPrioridadAbierto(false);
                  }}
                  className={`w-full text-left px-3 py-1.5 rounded-xl text-xs font-medium transition-colors ${
                    filtros.prioridad === "TODAS"
                      ? "bg-blue-50 text-[#0066FF] font-bold"
                      : "hover:bg-[#F2F2F7]"
                  }`}
                >
                  Todas las prioridades
                </button>

                <div className="my-1 border-t border-[#F2F2F7]" />

                {(Object.keys(PRIORIDADES_INFO) as PrioridadSolicitud[]).map((prio) => (
                  <button
                    key={prio}
                    onClick={() => {
                      onFiltrosChange({ ...filtros, prioridad: prio });
                      setDropdownPrioridadAbierto(false);
                    }}
                    className={`w-full text-left px-3 py-1.5 rounded-xl text-xs transition-colors flex items-center justify-between ${
                      filtros.prioridad === prio
                        ? "bg-blue-50 text-[#0066FF] font-bold"
                        : "hover:bg-[#F2F2F7] text-[#1C1C1E]"
                    }`}
                  >
                    <span className="flex items-center gap-1.5">
                      <span>{PRIORIDADES_INFO[prio].iconEmoji}</span>
                      <span>{PRIORIDADES_INFO[prio].label}</span>
                    </span>
                    <span className="text-[11px] text-[#8E8E93]">
                      {PRIORIDADES_INFO[prio].slaTexto}
                    </span>
                  </button>
                ))}
              </div>
            </>
          )}
        </div>

        {/* 3. Dropdown de Asignación (Visible solo para Administrador) */}
        {isAdmin && (
          <div className="relative">
            <button
              onClick={() => {
                setDropdownAsignacionAbierto(!dropdownAsignacionAbierto);
                setDropdownEstadoAbierto(false);
                setDropdownPrioridadAbierto(false);
              }}
              className={`px-3 py-1.5 rounded-xl text-xs font-semibold border flex items-center gap-1.5 transition-colors ${
                filtros.asignacion !== "TODOS"
                  ? "bg-purple-50 border-purple-200 text-purple-700"
                  : "bg-white border-[#E5E5EA] text-[#1C1C1E] hover:bg-[#F2F2F7]"
              }`}
            >
              <UserCheck className="w-3 h-3 text-purple-600" />
              <span>
                {filtros.asignacion === "TODOS"
                  ? "Asignación: Todos"
                  : filtros.asignacion === "SIN_ASIGNAR"
                  ? "Sin Asignar"
                  : TECNICOS_DISPONIBLES.find((t) => String(t.id) === filtros.asignacion)?.nombre ||
                    "Técnico"}
              </span>
              <ChevronDown className="w-3 h-3 text-[#8E8E93]" />
            </button>

            {dropdownAsignacionAbierto && (
              <>
                <div
                  className="fixed inset-0 z-30"
                  onClick={() => setDropdownAsignacionAbierto(false)}
                />
                <div className="absolute left-0 mt-1.5 w-64 bg-white rounded-2xl shadow-xl border border-[#E5E5EA] z-40 p-1.5 animate-in fade-in zoom-in-95 duration-100">
                  <div className="px-3 py-1 text-[11px] font-bold text-[#8E8E93]">
                    Filtrar por Responsable
                  </div>
                  <button
                    onClick={() => {
                      onFiltrosChange({ ...filtros, asignacion: "TODOS" });
                      setDropdownAsignacionAbierto(false);
                    }}
                    className={`w-full text-left px-3 py-1.5 rounded-xl text-xs transition-colors ${
                      filtros.asignacion === "TODOS"
                        ? "bg-purple-50 text-purple-700 font-bold"
                        : "hover:bg-[#F2F2F7]"
                    }`}
                  >
                    Todos los técnicos y clientes
                  </button>

                  <button
                    onClick={() => {
                      onFiltrosChange({ ...filtros, asignacion: "SIN_ASIGNAR" });
                      setDropdownAsignacionAbierto(false);
                    }}
                    className={`w-full text-left px-3 py-1.5 rounded-xl text-xs transition-colors ${
                      filtros.asignacion === "SIN_ASIGNAR"
                        ? "bg-purple-50 text-purple-700 font-bold"
                        : "hover:bg-[#F2F2F7]"
                    }`}
                  >
                    🔴 Sin Asignar (Pendientes de Asignación)
                  </button>

                  <div className="my-1 border-t border-[#F2F2F7]" />

                  {TECNICOS_DISPONIBLES.map((tec) => (
                    <button
                      key={tec.id}
                      onClick={() => {
                        onFiltrosChange({ ...filtros, asignacion: String(tec.id) });
                        setDropdownAsignacionAbierto(false);
                      }}
                      className={`w-full text-left px-3 py-1.5 rounded-xl text-xs transition-colors flex items-center justify-between ${
                        filtros.asignacion === String(tec.id)
                          ? "bg-purple-50 text-purple-700 font-bold"
                          : "hover:bg-[#F2F2F7] text-[#1C1C1E]"
                      }`}
                    >
                      <span>{tec.nombre}</span>
                      <span className="text-[10px] text-[#8E8E93]">{tec.especialidad.split(" ")[0]}</span>
                    </button>
                  ))}
                </div>
              </>
            )}
          </div>
        )}

        {/* Botón Limpiar Filtros */}
        {hayFiltrosActivos && (
          <button
            onClick={limpiarFiltros}
            className="px-2.5 py-1.5 rounded-xl text-xs font-semibold text-rose-600 hover:bg-rose-50 border border-rose-200 transition-colors flex items-center gap-1 active:scale-95"
          >
            <X className="w-3.5 h-3.5" />
            Limpiar Filtros
          </button>
        )}

        {/* Conteo de Resultados */}
        <div className="ml-auto text-xs text-[#8E8E93] font-medium">
          Mostrando <span className="font-bold text-[#1C1C1E]">{totalResultados}</span> tickets
        </div>
      </div>
    </div>
  );
}
