"use client";

import React, { useState, useEffect, useRef } from "react";
import Link from "next/link";
import { Solicitud, MensajeHistorial } from "@/types/solicitud";
import { suscribirHistorialTicket, enviarMensajeChat } from "@/services/firestoreService";
import { useAuth } from "@/context/AuthContext";
import { EstadoBadge, RolBadge } from "@/components/common/Badges";
import {
  ArrowLeft,
  Send,
  Lock,
  LockOpen,
  Paperclip,
  CheckCheck,
  ShieldAlert,
  Loader2,
  Clock,
} from "lucide-react";

interface TicketChatProps {
  solicitud: Solicitud;
  alVolverHref?: string;
}

export function TicketChat({ solicitud, alVolverHref }: TicketChatProps) {
  const { usuarioActual, isCliente } = useAuth();
  const [mensajes, setMensajes] = useState<MensajeHistorial[]>([]);
  const [cargando, setCargando] = useState(true);
  const [textoMensaje, setTextoMensaje] = useState("");
  const [esInterno, setEsInterno] = useState(false);
  const [enviando, setEnviando] = useState(false);

  const finMensajesRef = useRef<HTMLDivElement>(null);

  // Escuchar historial en tiempo real
  useEffect(() => {
    setCargando(true);
    const unsubscribe = suscribirHistorialTicket(
      solicitud.codigoSeguimiento,
      (lista) => {
        setMensajes(lista);
        setCargando(false);
      },
      () => setCargando(false)
    );

    return () => unsubscribe();
  }, [solicitud.codigoSeguimiento]);

  // Auto-scroll al último mensaje
  useEffect(() => {
    finMensajesRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [mensajes]);

  // Filtrar notas internas si el usuario actual es CLIENTE (Regla de negocio RBAC)
  const mensajesVisibles = mensajes.filter((m) => {
    if (isCliente && m.esInterno) {
      return false;
    }
    return true;
  });

  const enviarMensaje = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!textoMensaje.trim() || enviando) return;

    setEnviando(true);
    try {
      await enviarMensajeChat(solicitud.codigoSeguimiento, {
        solicitudId: solicitud.id,
        autorId: usuarioActual.id,
        autorNombre: usuarioActual.nombre,
        autorRol: usuarioActual.rol,
        mensaje: textoMensaje.trim(),
        esInterno: isCliente ? false : esInterno,
      });
      setTextoMensaje("");
    } catch (err) {
      console.error("Error al enviar mensaje:", err);
    } finally {
      setEnviando(false);
    }
  };

  const formatearHora = (ms: number) => {
    return new Intl.DateTimeFormat("es-CL", {
      hour: "2-digit",
      minute: "2-digit",
    }).format(new Date(ms));
  };

  return (
    <div className="flex flex-col h-[calc(100dvh-7rem)] max-w-5xl 2xl:max-w-6xl mx-auto bg-[#EFEAE2] rounded-3xl overflow-hidden border border-[#E5E5EA] shadow-xl relative">
      {/* 1. BARRA SUPERIOR ESTILO WHATSAPP */}
      <div className="bg-white px-4 py-3 border-b border-[#E5E5EA] flex items-center justify-between z-10 shadow-xs">
        <div className="flex items-center gap-3">
          <Link
            href={alVolverHref || `/tickets/${solicitud.codigoSeguimiento}`}
            className="p-1.5 rounded-xl text-[#0066FF] hover:bg-blue-50 transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </Link>

          <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-[#0066FF] font-bold text-sm shadow-2xs">
            {solicitud.codigoSeguimiento.slice(-3)}
          </div>

          <div className="flex flex-col">
            <div className="flex items-center gap-2">
              <span className="font-mono text-xs font-black text-[#1C1C1E]">
                {solicitud.codigoSeguimiento}
              </span>
              <EstadoBadge estado={solicitud.estado} />
            </div>
            <span className="text-xs font-semibold text-[#8E8E93] truncate max-w-[240px] sm:max-w-md">
              {solicitud.titulo}
            </span>
          </div>
        </div>

        <div className="hidden sm:flex items-center gap-2">
          <span className="text-[11px] text-[#8E8E93]">Técnico:</span>
          <span className="text-xs font-bold text-[#1C1C1E]">
            {solicitud.tecnicoNombre || "En Asignación"}
          </span>
        </div>
      </div>

      {/* 2. ÁREA DE MENSAJES CON FONDO TEXTURIZADO Y AUTO-SCROLL */}
      <div className="flex-1 overflow-y-auto p-4 space-y-3">
        {/* Banner Informativo de Encriptación */}
        <div className="flex justify-center">
          <div className="bg-[#FCF5C7] text-[#856404] text-[11px] font-medium px-3.5 py-1.5 rounded-xl shadow-2xs max-w-md text-center border border-[#FFEEBA]">
            🔒 Canal de soporte cifrado y en tiempo real conectado a Firestore.
          </div>
        </div>

        {cargando ? (
          <div className="flex flex-col items-center justify-center py-12">
            <Loader2 className="w-8 h-8 text-[#0066FF] animate-spin mb-2" />
            <p className="text-xs text-[#8E8E93]">Cargando mensajes del ticket...</p>
          </div>
        ) : mensajesVisibles.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-center text-[#8E8E93]">
            <p className="text-sm font-semibold text-[#1C1C1E]">No hay mensajes aún</p>
            <p className="text-xs mt-1">Escribe el primer mensaje para iniciar la conversación con soporte.</p>
          </div>
        ) : (
          mensajesVisibles.map((msg) => {
            const esMio = msg.autorId === usuarioActual.id;

            if (msg.esInterno) {
              /* NOTA INTERNA (Solo para Soporte y Admin) */
              return (
                <div key={msg.id} className="flex justify-center my-2">
                  <div className="bg-amber-50 border border-amber-300 text-amber-900 rounded-2xl p-3 max-w-lg w-full shadow-2xs">
                    <div className="flex items-center justify-between mb-1 pb-1 border-b border-amber-200 text-[11px] font-bold">
                      <span className="flex items-center gap-1 text-amber-800">
                        <Lock className="w-3.5 h-3.5 text-amber-600" />
                        NOTA INTERNA (Oculta al cliente)
                      </span>
                      <span>{formatearHora(msg.fecha)}</span>
                    </div>
                    <p className="text-xs font-semibold text-amber-950 whitespace-pre-wrap">
                      {msg.mensaje}
                    </p>
                    <div className="mt-1 text-[10px] text-amber-700 font-medium text-right">
                      Por: {msg.autorNombre} ({msg.autorRol})
                    </div>
                  </div>
                </div>
              );
            }

            return (
              <div
                key={msg.id}
                className={`flex ${esMio ? "justify-end" : "justify-start"} animate-in fade-in duration-150`}
              >
                <div
                  className={`max-w-[85%] sm:max-w-md rounded-2xl px-4 py-2.5 shadow-xs flex flex-col ${
                    esMio
                      ? "bg-[#0066FF] text-white rounded-br-xs"
                      : "bg-white text-[#1C1C1E] border border-[#E5E5EA] rounded-bl-xs"
                  }`}
                >
                  {/* Nombre y Rol del Autor si no es mío */}
                  {!esMio && (
                    <div className="flex items-center justify-between gap-2 mb-1">
                      <span className="text-[11px] font-bold text-[#0066FF]">
                        {msg.autorNombre}
                      </span>
                      <span className="text-[9px] font-semibold px-1.5 py-0.2 rounded-full bg-gray-100 text-[#8E8E93]">
                        {msg.autorRol}
                      </span>
                    </div>
                  )}

                  {/* Cuerpo del Mensaje */}
                  <p className="text-xs leading-relaxed whitespace-pre-wrap break-words">
                    {msg.mensaje}
                  </p>

                  {/* Hora y Visto */}
                  <div
                    className={`flex items-center justify-end gap-1 mt-1 text-[10px] ${
                      esMio ? "text-blue-100" : "text-[#8E8E93]"
                    }`}
                  >
                    <span>{formatearHora(msg.fecha)}</span>
                    {esMio && <CheckCheck className="w-3 h-3 text-sky-200" />}
                  </div>
                </div>
              </div>
            );
          })
        )}
        <div ref={finMensajesRef} />
      </div>

      {/* 3. BARRA INFERIOR FIJA CON SELECTOR DE NOTA INTERNA Y INPUT */}
      <div className="bg-white p-3 border-t border-[#E5E5EA] space-y-2">
        {/* Toggle de Nota Interna (Visible solo para Técnicos y Administradores) */}
        {!isCliente && (
          <div className="flex items-center justify-between px-2">
            <button
              type="button"
              onClick={() => setEsInterno(!esInterno)}
              className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold transition-colors ${
                esInterno
                  ? "bg-amber-100 text-amber-900 border border-amber-300 font-bold"
                  : "bg-gray-100 text-[#8E8E93] hover:text-[#1C1C1E]"
              }`}
            >
              {esInterno ? (
                <>
                  <Lock className="w-3 h-3 text-amber-600" />
                  Nota Interna Activa (Solo Soporte)
                </>
              ) : (
                <>
                  <LockOpen className="w-3 h-3" />
                  Mensaje Público para Cliente
                </>
              )}
            </button>

            <span className="text-[10px] text-[#8E8E93]">
              Enviando como: <span className="font-bold text-[#1C1C1E]">{usuarioActual.nombre}</span>
            </span>
          </div>
        )}

        {/* Input y Botón de Envío */}
        <form onSubmit={enviarMensaje} className="flex items-center gap-2">
          <input
            type="text"
            value={textoMensaje}
            onChange={(e) => setTextoMensaje(e.target.value)}
            placeholder={
              esInterno
                ? "Escribe una nota interna para el equipo técnico..."
                : "Escribe un mensaje de respuesta..."
            }
            className={`flex-1 px-4 py-2.5 rounded-2xl text-xs sm:text-sm border outline-hidden transition-all ${
              esInterno
                ? "bg-amber-50/60 border-amber-300 focus:border-amber-500 text-amber-950 placeholder:text-amber-700/60"
                : "bg-[#F2F2F7] border-transparent focus:border-[#0066FF] focus:bg-white text-[#1C1C1E] placeholder:text-[#8E8E93]"
            }`}
          />

          <button
            type="submit"
            disabled={!textoMensaje.trim() || enviando}
            className={`p-2.5 rounded-2xl text-white transition-all shadow-sm active:scale-95 ${
              esInterno
                ? "bg-amber-600 hover:bg-amber-700 disabled:opacity-40"
                : "bg-[#0066FF] hover:bg-[#0052CC] disabled:opacity-40"
            }`}
          >
            {enviando ? (
              <Loader2 className="w-5 h-5 animate-spin" />
            ) : (
              <Send className="w-5 h-5" />
            )}
          </button>
        </form>
      </div>
    </div>
  );
}
