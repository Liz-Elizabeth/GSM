"use client";

import React, { useState, useEffect } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { Solicitud } from "@/types/solicitud";
import { suscribirSolicitudPorCodigo } from "@/services/firestoreService";
import { TicketChat } from "@/components/chat/TicketChat";
import { Container } from "@/components/common/Container";
import { Loader2, AlertCircle, ArrowLeft } from "lucide-react";

export default function TicketChatPage() {
  const params = useParams();
  const codigoSeguimiento = params.id as string;

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
          Conectando a sala de mensajería en tiempo real...
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
          Chat no disponible
        </h2>
        <p className="text-xs text-[#8E8E93]">
          No se encontró la solicitud {codigoSeguimiento}.
        </p>
        <div className="pt-2">
          <Link
            href="/"
            className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold text-[#0066FF] bg-blue-50"
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
      <TicketChat
        solicitud={solicitud}
        alVolverHref={`/tickets/${solicitud.codigoSeguimiento}`}
      />
    </Container>
  );
}
