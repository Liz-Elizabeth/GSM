"use client";

import React, { useState, useEffect } from "react";
import { useParams, useSearchParams } from "next/navigation";
import Link from "next/link";
import { Solicitud } from "@/types/solicitud";
import { suscribirSolicitudPorCodigo } from "@/services/firestoreService";
import { TicketDetailView } from "@/components/tickets/TicketDetailView";
import { Container } from "@/components/common/Container";
import { ArrowLeft, Loader2, AlertCircle } from "lucide-react";

export default function TicketDetailPage() {
  const params = useParams();
  const searchParams = useSearchParams();
  const codigoSeguimiento = params.id as string;
  const initialTab = (searchParams.get("tab") as "info" | "qr" | "admin") || "info";

  const [solicitud, setSolicitud] = useState<Solicitud | null>(null);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    if (!codigoSeguimiento) return;

    setCargando(true);
    const unsubscribe = suscribirSolicitudPorCodigo(
      codigoSeguimiento,
      (ticket) => {
        setSolicitud(ticket);
        setCargando(false);
      },
      (error) => {
        console.error("Error al escuchar ticket:", error);
        setCargando(false);
      }
    );

    return () => unsubscribe();
  }, [codigoSeguimiento]);

  if (cargando) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[50vh] space-y-3">
        <Loader2 className="w-8 h-8 text-[#0066FF] animate-spin" />
        <p className="text-xs font-semibold text-[#8E8E93]">
          Cargando solicitud en tiempo real desde Firestore...
        </p>
      </div>
    );
  }

  if (!solicitud) {
    return (
      <div className="bg-white rounded-3xl p-12 text-center border border-[#E5E5EA] shadow-xs space-y-4 max-w-lg mx-auto">
        <div className="w-12 h-12 rounded-2xl bg-rose-50 text-rose-600 flex items-center justify-center mx-auto">
          <AlertCircle className="w-6 h-6" />
        </div>
        <h2 className="text-xl font-bold text-[#1C1C1E]">
          Solicitud no encontrada
        </h2>
        <p className="text-xs text-[#8E8E93]">
          No pudimos encontrar una solicitud con el código{" "}
          <span className="font-mono font-bold text-[#1C1C1E]">{codigoSeguimiento}</span> en Firestore.
        </p>
        <div className="pt-2">
          <Link
            href="/"
            className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold text-[#0066FF] bg-blue-50 hover:bg-blue-100 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Volver a la Bandeja</span>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <Container className="py-6 sm:py-8 space-y-4">
      {/* Navegación de Regreso */}
      <div>
        <Link
          href="/"
          className="inline-flex items-center gap-1.5 text-xs font-bold text-[#8E8E93] hover:text-[#0066FF] transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Volver a la Bandeja de Tickets</span>
        </Link>
      </div>

      {/* Vista de Detalle con Pestañas iOS */}
      <TicketDetailView solicitud={solicitud} initialTab={initialTab} />
    </Container>
  );
}
