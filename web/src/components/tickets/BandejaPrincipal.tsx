"use client";

import React, { useState, useEffect, useMemo } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Solicitud } from "@/types/solicitud";
import {
  suscribirSolicitudes,
  sembrarTicketsDemostracionSiVacio,
  obtenerCacheLocalTickets,
} from "@/services/firestoreService";
import { useAuth } from "@/context/AuthContext";
import { RolBadge } from "@/components/common/Badges";
import { FilterBar, FiltrosState } from "@/components/tickets/FilterBar";
import { TicketCard } from "@/components/tickets/TicketCard";
import { BoardingPassCard } from "@/components/tickets/BoardingPassCard";
import { TicketSkeletonList } from "@/components/common/Skeletons";
import { Container } from "@/components/common/Container";
import {
  PlusCircle,
  Inbox,
  Clock,
  CheckCircle2,
  Flame,
  CloudOff,
  ExternalLink,
  Radio,
} from "lucide-react";

export function BandejaPrincipal() {
  const searchParams = useSearchParams();
  const initialView = searchParams.get("view") === "wallet" ? "wallet" : "lista";

  const { usuarioActual, isAdmin, isTecnico, isCliente } = useAuth();
  const [solicitudes, setSolicitudes] = useState<Solicitud[]>(() => obtenerCacheLocalTickets());
  const [cargando, setCargando] = useState(false);
  const [errorFirestore, setErrorFirestore] = useState<string | null>(null);
  const [vistaActiva, setVistaActiva] = useState<"lista" | "wallet">(initialView);

  const [filtros, setFiltros] = useState<FiltrosState>({
    busqueda: "",
    estado: "TODOS",
    prioridad: "TODAS",
    asignacion: "TODOS",
  });

  // Inicializar Firestore listener y sembrar datos iniciales si está vacío
  useEffect(() => {
    sembrarTicketsDemostracionSiVacio().catch(console.warn);

    const unsubscribe = suscribirSolicitudes(
      (datos) => {
        if (datos && datos.length > 0) {
          setSolicitudes(datos);
        }
        setCargando(false);
      },
      (error) => {
        console.warn("Aviso de conexión Firestore:", error.message);
        if (
          error.message.includes("permission-denied") ||
          error.message.includes("PERMISSION_DENIED") ||
          error.message.includes("not-found") ||
          error.message.includes("NOT_FOUND")
        ) {
          setErrorFirestore(
            "La base de datos Cloud Firestore aún no ha sido creada en Firebase Console para el proyecto gsm-app-9a805. La plataforma está operando en modo Offline-First con persistencia local."
          );
        }
        setCargando(false);
      }
    );

    return () => unsubscribe();
  }, []);

  // Actualizar vista activa si cambia la URL
  useEffect(() => {
    const v = searchParams.get("view");
    if (v === "wallet") {
      setVistaActiva("wallet");
    } else if (v === "lista") {
      setVistaActiva("lista");
    }
  }, [searchParams]);

  // Cálculo de Métricas SLA y Estados
  const metricas = useMemo(() => {
    const ahora = Date.now();
    const total = solicitudes.length;
    const abiertas = solicitudes.filter(
      (s) => s.estado !== "CERRADA" && s.estado !== "CANCELADA"
    ).length;
    const resueltas = solicitudes.filter((s) => s.estado === "RESUELTA").length;
    const atrasadas = solicitudes.filter(
      (s) =>
        s.fechaLimite != null &&
        s.fechaLimite < ahora &&
        s.estado !== "RESUELTA" &&
        s.estado !== "CERRADA" &&
        s.estado !== "CANCELADA"
    ).length;
    const urgentes = solicitudes.filter(
      (s) =>
        s.prioridad === "URGENTE" &&
        s.estado !== "RESUELTA" &&
        s.estado !== "CERRADA"
    ).length;

    return { total, abiertas, resueltas, atrasadas, urgentes };
  }, [solicitudes]);

  // Filtrado reactivo de tickets según búsqueda, estado, prioridad y rol
  const solicitudesFiltradas = useMemo(() => {
    const ahora = Date.now();

    return solicitudes.filter((ticket) => {
      // 1. Filtrado de rol:
      // Para cliente: si hay tickets con su clienteId o clienteNombre, o si no hay filtros específicos
      if (isCliente && ticket.clienteId !== usuarioActual.id && ticket.clienteId !== 0) {
        // En modo cliente mostrar solo sus tickets si coinciden, de lo contrario permitir ver para testing
        const tieneMisTickets = solicitudes.some((s) => s.clienteId === usuarioActual.id);
        if (tieneMisTickets && ticket.clienteId !== usuarioActual.id) {
          return false;
        }
      }

      // 2. Filtro de búsqueda por texto o código
      if (filtros.busqueda.trim() !== "") {
        const query = filtros.busqueda.toLowerCase();
        const coincideCodigo = ticket.codigoSeguimiento.toLowerCase().includes(query);
        const coincideTitulo = ticket.titulo.toLowerCase().includes(query);
        const coincideDesc = ticket.descripcion.toLowerCase().includes(query);
        if (!coincideCodigo && !coincideTitulo && !coincideDesc) {
          return false;
        }
      }

      // 3. Filtro de Estado
      if (filtros.estado === "ATRASADAS") {
        const estaAtrasada =
          ticket.fechaLimite != null &&
          ticket.fechaLimite < ahora &&
          ticket.estado !== "RESUELTA" &&
          ticket.estado !== "CERRADA" &&
          ticket.estado !== "CANCELADA";
        if (!estaAtrasada) return false;
      } else if (filtros.estado !== "TODOS") {
        if (ticket.estado !== filtros.estado) return false;
      }

      // 4. Filtro de Prioridad SLA
      if (filtros.prioridad !== "TODAS") {
        if (ticket.prioridad !== filtros.prioridad) return false;
      }

      // 5. Filtro de Asignación (Solo para Administrador)
      if (isAdmin && filtros.asignacion !== "TODOS") {
        if (filtros.asignacion === "SIN_ASIGNAR") {
          if (ticket.tecnicoId != null) return false;
        } else {
          if (String(ticket.tecnicoId) !== filtros.asignacion) return false;
        }
      }

      return true;
    });
  }, [solicitudes, filtros, usuarioActual, isCliente, isAdmin]);

  return (
    <Container className="space-y-6 py-6 sm:py-8">
      {/* Banner de Estado de Conexión si Firestore remoto aún no está habilitado */}
      {errorFirestore && (
        <div className="bg-amber-50 border border-amber-300 rounded-2xl p-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 shadow-2xs">
          <div className="flex items-start gap-3">
            <div className="w-8 h-8 rounded-xl bg-amber-100 text-amber-800 flex items-center justify-center shrink-0 mt-0.5">
              <CloudOff className="w-4 h-4" />
            </div>
            <div>
              <p className="text-xs font-bold text-amber-900">
                Modo Offline-First Activo (Caché Local Persistente)
              </p>
              <p className="text-[11px] text-amber-800 leading-relaxed mt-0.5">
                La API de Cloud Firestore aún no ha sido creada o habilitada en Firebase Console para el proyecto{" "}
                <span className="font-mono font-bold">gsm-app-9a805</span>. Toda la plataforma está funcionando con almacenamiento local.
              </p>
            </div>
          </div>
          <a
            href="https://console.firebase.google.com/project/gsm-app-9a805/firestore"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold bg-amber-600 hover:bg-amber-700 text-white shadow-2xs shrink-0 transition-colors"
          >
            <span>Habilitar Firestore</span>
            <ExternalLink className="w-3.5 h-3.5" />
          </a>
        </div>
      )}

      {/* 1. ENCABEZADO DE BIENVENIDA Y ACCIÓN PRINCIPAL */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <h1 className="text-2xl sm:text-3xl font-black text-[#1C1C1E] tracking-tight">
              Hola, {usuarioActual.nombre.split(" ")[0]}
            </h1>
            <RolBadge rol={usuarioActual.rol} />
          </div>
          <p className="text-xs sm:text-sm text-[#8E8E93]">
            {isCliente
              ? `Panel de autoservicio y seguimiento de tickets para ${usuarioActual.empresa || "Innovatech SpA"}`
              : isTecnico
              ? "Cola técnica de soporte y resolución de requerimientos asignados"
              : "Panel global de control, monitoreo SLA y gestión de solicitudes GSM"}
          </p>
        </div>

        <Link
          href="/nuevo"
          className="inline-flex items-center justify-center gap-2 px-5 py-2.5 rounded-2xl text-xs sm:text-sm font-bold text-white bg-[#0066FF] hover:bg-[#0052CC] shadow-sm hover:shadow-md transition-all active:scale-95"
        >
          <PlusCircle className="w-4 h-4" />
          <span>+ Nuevo Ticket</span>
        </Link>
      </div>

      {/* 2. TARJETAS DE MÉTRICAS KPI (Apple HIG Rounded-2xl) */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
        <div className="bg-white p-4 sm:p-5 rounded-2xl border border-[#E5E5EA] shadow-2xs flex items-center justify-between">
          <div>
            <span className="text-[11px] font-bold text-[#8E8E93] uppercase tracking-wider block">
              Tickets Activos
            </span>
            <span className="text-2xl sm:text-3xl font-black text-[#1C1C1E] mt-1 block">
              {metricas.abiertas}
            </span>
          </div>
          <div className="w-10 h-10 rounded-xl bg-blue-50 text-[#0066FF] flex items-center justify-center">
            <Inbox className="w-5 h-5" />
          </div>
        </div>

        <div className="bg-white p-4 sm:p-5 rounded-2xl border border-[#E5E5EA] shadow-2xs flex items-center justify-between">
          <div>
            <span className="text-[11px] font-bold text-[#8E8E93] uppercase tracking-wider block">
              SLA Vencido
            </span>
            <span className="text-2xl sm:text-3xl font-black text-rose-600 mt-1 block">
              {metricas.atrasadas}
            </span>
          </div>
          <div className="w-10 h-10 rounded-xl bg-rose-50 text-rose-600 flex items-center justify-center">
            <Clock className="w-5 h-5" />
          </div>
        </div>

        <div className="bg-white p-4 sm:p-5 rounded-2xl border border-[#E5E5EA] shadow-2xs flex items-center justify-between">
          <div>
            <span className="text-[11px] font-bold text-[#8E8E93] uppercase tracking-wider block">
              Urgentes
            </span>
            <span className="text-2xl sm:text-3xl font-black text-orange-600 mt-1 block">
              {metricas.urgentes}
            </span>
          </div>
          <div className="w-10 h-10 rounded-xl bg-orange-50 text-orange-600 flex items-center justify-center">
            <Flame className="w-5 h-5" />
          </div>
        </div>

        <div className="bg-white p-4 sm:p-5 rounded-2xl border border-[#E5E5EA] shadow-2xs flex items-center justify-between">
          <div>
            <span className="text-[11px] font-bold text-[#8E8E93] uppercase tracking-wider block">
              Resueltas
            </span>
            <span className="text-2xl sm:text-3xl font-black text-emerald-600 mt-1 block">
              {metricas.resueltas}
            </span>
          </div>
          <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
            <CheckCircle2 className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* 3. BARRA DE BÚSQUEDA Y MENÚS DESPLEGABLES DE FILTROS */}
      <FilterBar
        filtros={filtros}
        onFiltrosChange={setFiltros}
        vistaActiva={vistaActiva}
        onVistaChange={setVistaActiva}
        totalResultados={solicitudesFiltradas.length}
      />

      {/* 4. CONTENIDO: LISTA O BILLETERA DE PASES */}
      {cargando ? (
        <TicketSkeletonList count={4} />
      ) : solicitudesFiltradas.length === 0 ? (
        <div className="bg-white rounded-3xl p-12 text-center border border-[#E5E5EA] shadow-xs space-y-3">
          <div className="w-12 h-12 rounded-2xl bg-blue-50 text-[#0066FF] flex items-center justify-center mx-auto">
            <Inbox className="w-6 h-6" />
          </div>
          <h3 className="font-bold text-lg text-[#1C1C1E]">
            No se encontraron tickets con estos criterios
          </h3>
          <p className="text-xs text-[#8E8E93] max-w-sm mx-auto">
            Prueba ajustando los filtros de estado o prioridad, o crea un nuevo ticket de soporte.
          </p>
          <div className="pt-2">
            <Link
              href="/nuevo"
              className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold text-white bg-[#0066FF] hover:bg-[#0052CC] transition-colors"
            >
              <PlusCircle className="w-3.5 h-3.5" />
              <span>Crear Ticket Ahora</span>
            </Link>
          </div>
        </div>
      ) : vistaActiva === "lista" ? (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {solicitudesFiltradas.map((ticket) => (
            <TicketCard key={ticket.codigoSeguimiento} solicitud={ticket} />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
          {solicitudesFiltradas.map((ticket) => (
            <BoardingPassCard
              key={ticket.codigoSeguimiento}
              solicitud={ticket}
            />
          ))}
        </div>
      )}
    </Container>
  );
}
