"use client";

import React, { useState } from "react";
import Link from "next/link";
import { QRCodeSVG } from "qrcode.react";
import confetti from "canvas-confetti";
import {
  Solicitud,
  PrioridadSolicitud,
  Evidencia,
  PRIORIDADES_INFO,
  CATEGORIAS_INFO,
} from "@/types/solicitud";
import { TECNICOS_DISPONIBLES } from "@/types/usuario";
import { useAuth } from "@/context/AuthContext";
import { EstadoBadge, PrioridadBadge, CategoriaBadge } from "@/components/common/Badges";
import { VisorEvidenciaModal } from "@/components/evidencias/VisorEvidenciaModal";
import { FileUploadDropzone } from "@/components/evidencias/FileUploadDropzone";
import {
  actualizarSolicitud,
  cambiarPrioridadTicket,
  asignarTecnicoATicket,
  cancelarSolicitudEnFirestore,
  resolverSolicitudPorTecnico,
  confirmarConformidadCliente,
  agregarEvidenciaASolicitud,
} from "@/services/firestoreService";
import {
  FileText,
  QrCode,
  Shield,
  Edit3,
  Save,
  X,
  MessageSquare,
  CheckCircle2,
  AlertTriangle,
  Clock,
  Calendar,
  User,
  Paperclip,
  Share2,
  Copy,
  Check,
  MessageCircle,
  ExternalLink,
  Loader2,
  Plus,
} from "lucide-react";

interface TicketDetailViewProps {
  solicitud: Solicitud;
  initialTab?: "info" | "qr" | "admin";
}

export function TicketDetailView({ solicitud, initialTab = "info" }: TicketDetailViewProps) {
  const { usuarioActual, isAdmin, isTecnico, isCliente } = useAuth();
  const [tabActiva, setTabActiva] = useState<"info" | "qr" | "admin">(initialTab);

  // Estados de edición en caliente
  const [modoEdicion, setModoEdicion] = useState(false);
  const [tituloEditado, setTituloEditado] = useState(solicitud.titulo);
  const [descripcionEditada, setDescripcionEditada] = useState(solicitud.descripcion);
  const [guardandoEdicion, setGuardandoEdicion] = useState(false);

  // Estados de visor modal de evidencia
  const [evidenciaSeleccionada, setEvidenciaSeleccionada] = useState<Evidencia | null>(null);
  const [mostrarUploader, setMostrarUploader] = useState(false);
  const [evidenciasNuevas, setEvidenciasNuevas] = useState<Evidencia[]>([]);
  const [guardandoEvidencias, setGuardandoEvidencias] = useState(false);

  // Estados de gestión Admin
  const [prioridadSeleccionada, setPrioridadSeleccionada] = useState<PrioridadSolicitud>(
    solicitud.prioridad
  );
  const [tecnicoSeleccionado, setTecnicoSeleccionado] = useState<number | null>(
    solicitud.tecnicoId
  );
  const [guardandoAdmin, setGuardandoAdmin] = useState(false);
  const [modalCancelarAbierto, setModalCancelarAbierto] = useState(false);
  const [motivoCancelacion, setMotivoCancelacion] = useState("");
  const [cancelando, setCancelando] = useState(false);

  // Estados de resolución de técnico
  const [modalResolverAbierto, setModalResolverAbierto] = useState(false);
  const [notasResolucion, setNotasResolucion] = useState("");
  const [resolviendo, setResolviendo] = useState(false);

  // Copiado y compartir
  const [copiado, setCopiado] = useState(false);

  const urlSeguimiento =
    typeof window !== "undefined"
      ? `${window.location.origin}/tickets/${solicitud.codigoSeguimiento}`
      : `https://gsm.soporte.cl/tickets/${solicitud.codigoSeguimiento}`;

  const copiarEnlace = () => {
    navigator.clipboard.writeText(urlSeguimiento);
    setCopiado(true);
    setTimeout(() => setCopiado(false), 2500);
  };

  const compartirPorWhatsApp = () => {
    const texto = `Hola, este es mi pase digital de soporte GSM para el ticket ${solicitud.codigoSeguimiento}: "${solicitud.titulo}". Puedes revisarlo en: ${urlSeguimiento}`;
    window.open(`https://api.whatsapp.com/send?text=${encodeURIComponent(texto)}`, "_blank");
  };

  // Guardar edición de título y descripción
  const guardarEdicion = async () => {
    if (!tituloEditado.trim() || !descripcionEditada.trim()) return;
    setGuardandoEdicion(true);
    try {
      await actualizarSolicitud(solicitud.codigoSeguimiento, {
        titulo: tituloEditado.trim(),
        descripcion: descripcionEditada.trim(),
      });
      setModoEdicion(false);
    } catch (e) {
      console.error("Error al guardar cambios:", e);
    } finally {
      setGuardandoEdicion(false);
    }
  };

  // Guardar nuevas evidencias adjuntadas en caliente
  const guardarNuevasEvidencias = async () => {
    if (evidenciasNuevas.length === 0) return;
    setGuardandoEvidencias(true);
    try {
      for (const ev of evidenciasNuevas) {
        await agregarEvidenciaASolicitud(solicitud.codigoSeguimiento, ev);
      }
      setEvidenciasNuevas([]);
      setMostrarUploader(false);
    } catch (e) {
      console.error("Error al guardar evidencias:", e);
    } finally {
      setGuardandoEvidencias(false);
    }
  };

  // Guardar cambios de criticidad y técnico (Admin)
  const guardarGestionAdmin = async () => {
    setGuardandoAdmin(true);
    try {
      if (prioridadSeleccionada !== solicitud.prioridad) {
        await cambiarPrioridadTicket(
          solicitud.codigoSeguimiento,
          prioridadSeleccionada,
          usuarioActual.nombre
        );
      }
      if (tecnicoSeleccionado !== solicitud.tecnicoId) {
        await asignarTecnicoATicket(
          solicitud.codigoSeguimiento,
          tecnicoSeleccionado,
          usuarioActual.nombre
        );
      }
      alert("✅ Cambios de administración guardados con éxito.");
    } catch (e) {
      console.error("Error al actualizar gestión admin:", e);
    } finally {
      setGuardandoAdmin(false);
    }
  };

  // Confirmar cancelación por Admin
  const confirmarCancelacion = async () => {
    if (!motivoCancelacion.trim()) return;
    setCancelando(true);
    try {
      await cancelarSolicitudEnFirestore(
        solicitud.codigoSeguimiento,
        motivoCancelacion.trim(),
        usuarioActual.nombre
      );
      setModalCancelarAbierto(false);
      setMotivoCancelacion("");
    } catch (e) {
      console.error("Error cancelando solicitud:", e);
    } finally {
      setCancelando(false);
    }
  };

  // Marcar como resuelto por Técnico
  const confirmarResolucion = async () => {
    if (!notasResolucion.trim()) return;
    setResolviendo(true);
    try {
      await resolverSolicitudPorTecnico(
        solicitud.codigoSeguimiento,
        notasResolucion.trim(),
        usuarioActual.nombre,
        usuarioActual.id
      );
      setModalResolverAbierto(false);
      setNotasResolucion("");
    } catch (e) {
      console.error("Error al marcar resuelto:", e);
    } finally {
      setResolviendo(false);
    }
  };

  // Confirmar conformidad por Cliente
  const darConformidad = async () => {
    try {
      await confirmarConformidadCliente(
        solicitud.codigoSeguimiento,
        usuarioActual.nombre,
        usuarioActual.id
      );
      try {
        confetti({
          particleCount: 120,
          spread: 70,
          origin: { y: 0.6 },
        });
      } catch {
        // Ignorar si canvas no está disponible
      }
    } catch (e) {
      console.error("Error al confirmar conformidad:", e);
    }
  };

  const formatearFecha = (ms?: number | null) => {
    if (!ms) return "N/A";
    return new Intl.DateTimeFormat("es-CL", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    }).format(new Date(ms));
  };

  return (
    <div className="space-y-6">
      {/* 1. CONTROL DE NAVEGACIÓN SEGMENTADO SUPERIOR TIPO IOS */}
      <div className="flex justify-center">
        <div className="bg-[#E5E5EA]/80 p-1 rounded-2xl flex items-center gap-1 shadow-inner max-w-md w-full">
          <button
            onClick={() => setTabActiva("info")}
            className={`flex-1 py-2 px-3 rounded-xl text-xs sm:text-sm font-bold transition-all flex items-center justify-center gap-1.5 ${
              tabActiva === "info"
                ? "bg-white text-[#0066FF] shadow-xs"
                : "text-[#8E8E93] hover:text-[#1C1C1E]"
            }`}
          >
            <FileText className="w-4 h-4" />
            <span>Información</span>
          </button>

          <button
            onClick={() => setTabActiva("qr")}
            className={`flex-1 py-2 px-3 rounded-xl text-xs sm:text-sm font-bold transition-all flex items-center justify-center gap-1.5 ${
              tabActiva === "qr"
                ? "bg-white text-[#0066FF] shadow-xs"
                : "text-[#8E8E93] hover:text-[#1C1C1E]"
            }`}
          >
            <QrCode className="w-4 h-4" />
            <span>Pase QR</span>
          </button>

          {isAdmin && (
            <button
              onClick={() => setTabActiva("admin")}
              className={`flex-1 py-2 px-3 rounded-xl text-xs sm:text-sm font-bold transition-all flex items-center justify-center gap-1.5 ${
                tabActiva === "admin"
                  ? "bg-white text-purple-700 shadow-xs"
                  : "text-[#8E8E93] hover:text-[#1C1C1E]"
              }`}
            >
              <Shield className="w-4 h-4 text-purple-600" />
              <span>Gestión Admin</span>
            </button>
          )}
        </div>
      </div>

      {/* 2. PESTAÑA 0: INFORMACIÓN Y EVIDENCIAS */}
      {tabActiva === "info" && (
        <div className="space-y-6 animate-in fade-in duration-200">
          {/* Tarjeta Principal de Detalle y Edición en Caliente */}
          <div className="bg-white rounded-3xl p-6 sm:p-8 border border-[#E5E5EA] shadow-xs space-y-6">
            {/* Cabecera con Código, Badges y Botón de Edición */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-[#F2F2F7]">
              <div className="flex items-center gap-2.5">
                <span className="font-mono text-sm font-black text-[#0066FF] bg-blue-50 px-3 py-1.5 rounded-xl border border-blue-100">
                  {solicitud.codigoSeguimiento}
                </span>
                <EstadoBadge estado={solicitud.estado} />
                <PrioridadBadge prioridad={solicitud.prioridad} />
              </div>

              <div className="flex items-center gap-2">
                <Link
                  href={`/tickets/${solicitud.codigoSeguimiento}/chat`}
                  className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl text-xs font-bold text-white bg-[#0066FF] hover:bg-[#0052CC] shadow-xs transition-colors"
                >
                  <MessageSquare className="w-3.5 h-3.5" />
                  <span>Abrir Chat del Ticket</span>
                </Link>

                {!modoEdicion ? (
                  <button
                    onClick={() => {
                      setTituloEditado(solicitud.titulo);
                      setDescripcionEditada(solicitud.descripcion);
                      setModoEdicion(true);
                    }}
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold text-[#1C1C1E] bg-[#F2F2F7] hover:bg-[#E5E5EA] transition-colors"
                  >
                    <Edit3 className="w-3.5 h-3.5" />
                    <span>Editar</span>
                  </button>
                ) : (
                  <div className="flex items-center gap-1.5">
                    <button
                      onClick={guardarEdicion}
                      disabled={guardandoEdicion}
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 transition-colors disabled:opacity-50"
                    >
                      {guardandoEdicion ? (
                        <Loader2 className="w-3.5 h-3.5 animate-spin" />
                      ) : (
                        <Save className="w-3.5 h-3.5" />
                      )}
                      <span>Guardar</span>
                    </button>
                    <button
                      onClick={() => setModoEdicion(false)}
                      className="p-1.5 rounded-xl text-[#8E8E93] hover:text-[#1C1C1E] hover:bg-[#F2F2F7]"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  </div>
                )}
              </div>
            </div>

            {/* Título y Descripción (Modo Lectura o Modo Edición en Caliente) */}
            <div className="space-y-3">
              <div className="flex items-center gap-2">
                <CategoriaBadge categoria={solicitud.categoria} />
                <span className="text-xs text-[#8E8E93]">
                  {CATEGORIAS_INFO[solicitud.categoria]?.descripcion}
                </span>
              </div>

              {!modoEdicion ? (
                <>
                  <h1 className="text-2xl sm:text-3xl font-black text-[#1C1C1E] tracking-tight">
                    {solicitud.titulo}
                  </h1>
                  <p className="text-sm sm:text-base text-[#1C1C1E]/80 whitespace-pre-wrap leading-relaxed">
                    {solicitud.descripcion}
                  </p>
                </>
              ) : (
                <div className="space-y-3">
                  <div>
                    <label className="block text-xs font-bold text-[#8E8E93] mb-1">
                      Título de la Solicitud
                    </label>
                    <input
                      type="text"
                      value={tituloEditado}
                      onChange={(e) => setTituloEditado(e.target.value)}
                      className="w-full px-3.5 py-2.5 rounded-xl border border-[#0066FF] bg-white text-sm font-bold text-[#1C1C1E] outline-hidden"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-[#8E8E93] mb-1">
                      Descripción Detallada
                    </label>
                    <textarea
                      rows={5}
                      value={descripcionEditada}
                      onChange={(e) => setDescripcionEditada(e.target.value)}
                      className="w-full px-3.5 py-2.5 rounded-xl border border-[#0066FF] bg-white text-sm text-[#1C1C1E] outline-hidden leading-relaxed"
                    />
                  </div>
                </div>
              )}
            </div>

            {/* Metadatos en Cuadrícula Apple HIG */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 p-4 rounded-2xl bg-[#F2F2F7]/70 border border-[#E5E5EA]">
              <div>
                <span className="text-[10px] font-bold text-[#8E8E93] uppercase block">
                  CLIENTE
                </span>
                <span className="text-xs font-bold text-[#1C1C1E] truncate block mt-0.5">
                  {solicitud.clienteNombre || "Cliente #1"}
                </span>
              </div>

              <div>
                <span className="text-[10px] font-bold text-[#8E8E93] uppercase block">
                  TÉCNICO A CARGO
                </span>
                <span className="text-xs font-bold text-[#0066FF] truncate block mt-0.5">
                  {solicitud.tecnicoNombre || "Sin asignar"}
                </span>
              </div>

              <div>
                <span className="text-[10px] font-bold text-[#8E8E93] uppercase block">
                  FECHA INGRESO
                </span>
                <span className="text-xs font-semibold text-[#1C1C1E] block mt-0.5">
                  {formatearFecha(solicitud.fechaCreacion)}
                </span>
              </div>

              <div>
                <span className="text-[10px] font-bold text-[#8E8E93] uppercase block">
                  LÍMITE SLA
                </span>
                <span className="text-xs font-semibold text-[#1C1C1E] block mt-0.5">
                  {formatearFecha(solicitud.fechaLimite)}
                </span>
              </div>
            </div>

            {/* Servicio y Activo Relacionado si existen */}
            {(solicitud.servicioRelacionado || solicitud.activoRelacionado) && (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
                {solicitud.servicioRelacionado && (
                  <div className="p-3 rounded-xl bg-gray-50 border border-gray-200">
                    <span className="font-bold text-[#8E8E93] block mb-0.5">Servicio Relacionado:</span>
                    <span className="font-semibold text-[#1C1C1E]">{solicitud.servicioRelacionado}</span>
                  </div>
                )}
                {solicitud.activoRelacionado && (
                  <div className="p-3 rounded-xl bg-gray-50 border border-gray-200">
                    <span className="font-bold text-[#8E8E93] block mb-0.5">Activo / URL de Servidor:</span>
                    <span className="font-semibold text-[#0066FF]">{solicitud.activoRelacionado}</span>
                  </div>
                )}
              </div>
            )}

            {/* Banner de Resolución si ya está resuelta */}
            {solicitud.resolucion && (
              <div className="p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-xs space-y-1">
                <div className="flex items-center gap-2 font-bold text-emerald-800">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                  <span>Resolución del Equipo Técnico:</span>
                </div>
                <p className="text-emerald-950 whitespace-pre-wrap pl-6">
                  {solicitud.resolucion}
                </p>
                {solicitud.fechaResolucion && (
                  <p className="text-[11px] text-emerald-700 pl-6">
                    Resuelto el {formatearFecha(solicitud.fechaResolucion)}
                  </p>
                )}
              </div>
            )}

            {/* Banner de Cancelación si fue cancelada */}
            {solicitud.estado === "CANCELADA" && solicitud.motivoCancelacion && (
              <div className="p-4 rounded-2xl bg-rose-50 border border-rose-200 text-xs space-y-1">
                <div className="flex items-center gap-2 font-bold text-rose-800">
                  <AlertTriangle className="w-4 h-4 text-rose-600" />
                  <span>Motivo de Cancelación:</span>
                </div>
                <p className="text-rose-950 pl-6">{solicitud.motivoCancelacion}</p>
              </div>
            )}

            {/* Botones de Acción de Roles (KISS Workflow) */}
            <div className="pt-2 flex flex-wrap items-center gap-3">
              {/* Acción Técnico: Marcar como Resuelto */}
              {isTecnico && solicitud.estado !== "RESUELTA" && solicitud.estado !== "CERRADA" && solicitud.estado !== "CANCELADA" && (
                <button
                  onClick={() => setModalResolverAbierto(true)}
                  className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 shadow-xs transition-colors"
                >
                  <CheckCircle2 className="w-4 h-4" />
                  <span>Marcar como Resuelto</span>
                </button>
              )}

              {/* Acción Cliente: Confirmar Conformidad y Cerrar */}
              {isCliente && solicitud.estado === "RESUELTA" && !solicitud.conformidadCliente && (
                <button
                  onClick={darConformidad}
                  className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl text-xs font-bold text-white bg-gradient-to-r from-emerald-500 to-teal-600 hover:opacity-95 shadow-md transition-all active:scale-95 animate-bounce"
                >
                  <CheckCircle2 className="w-4 h-4" />
                  <span>Confirmar Plena Conformidad (Cerrar Ticket)</span>
                </button>
              )}

              {solicitud.conformidadCliente && (
                <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-emerald-100 text-emerald-800 text-xs font-bold">
                  <span>🌟 Conformidad confirmada por el cliente</span>
                </div>
              )}
            </div>
          </div>

          {/* Galería de Evidencias y Carga a Appwrite Storage */}
          <div className="bg-white rounded-3xl p-6 sm:p-8 border border-[#E5E5EA] shadow-xs space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-lg text-[#1C1C1E] flex items-center gap-2">
                  <Paperclip className="w-4 h-4 text-[#0066FF]" />
                  Evidencias y Archivos Adjuntos
                </h3>
                <p className="text-xs text-[#8E8E93]">
                  Almacenados en Appwrite Storage Bucket (nyc.cloud.appwrite.io)
                </p>
              </div>

              <button
                onClick={() => setMostrarUploader(!mostrarUploader)}
                className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl text-xs font-semibold text-[#0066FF] bg-blue-50 hover:bg-blue-100 transition-colors"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>{mostrarUploader ? "Ocultar Carga" : "Adjuntar Evidencia"}</span>
              </button>
            </div>

            {/* Desplegable de Uploader si se activa */}
            {mostrarUploader && (
              <div className="p-4 rounded-2xl bg-[#F2F2F7] border border-[#E5E5EA] space-y-3">
                <FileUploadDropzone
                  evidencias={evidenciasNuevas}
                  onEvidenciasChange={setEvidenciasNuevas}
                />
                {evidenciasNuevas.length > 0 && (
                  <div className="flex justify-end">
                    <button
                      onClick={guardarNuevasEvidencias}
                      disabled={guardandoEvidencias}
                      className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold text-white bg-[#0066FF] hover:bg-[#0052CC] shadow-xs disabled:opacity-50"
                    >
                      {guardandoEvidencias ? (
                        <Loader2 className="w-3.5 h-3.5 animate-spin" />
                      ) : (
                        <Save className="w-3.5 h-3.5" />
                      )}
                      <span>Vincular Evidencias al Ticket</span>
                    </button>
                  </div>
                )}
              </div>
            )}

            {/* Lista de Evidencias */}
            {(!solicitud.evidencias || solicitud.evidencias.length === 0) ? (
              <div className="py-8 text-center text-xs text-[#8E8E93] bg-[#F2F2F7]/50 rounded-2xl border border-dashed border-[#E5E5EA]">
                No hay evidencias adjuntas en esta solicitud.
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
                {solicitud.evidencias.map((ev) => {
                  const esImagen =
                    ev.tipoArchivo.startsWith("image/") ||
                    /\.(jpg|jpeg|png|webp|gif|svg)$/i.test(ev.nombreArchivo);

                  return (
                    <div
                      key={ev.id}
                      onClick={() => setEvidenciaSeleccionada(ev)}
                      className="group cursor-pointer p-3 rounded-2xl bg-[#F2F2F7]/70 hover:bg-white border border-[#E5E5EA] hover:border-[#0066FF] shadow-2xs hover:shadow-sm transition-all"
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-white border border-[#E5E5EA] flex items-center justify-center text-[#0066FF] shrink-0 group-hover:scale-105 transition-transform">
                          {esImagen ? (
                            // eslint-disable-next-line @next/next/no-img-element
                            <img
                              src={ev.uriRemota}
                              alt={ev.nombreArchivo}
                              className="w-10 h-10 object-cover rounded-xl"
                            />
                          ) : (
                            <FileText className="w-5 h-5" />
                          )}
                        </div>
                        <div className="min-w-0 flex-1">
                          <p className="text-xs font-bold text-[#1C1C1E] truncate group-hover:text-[#0066FF]">
                            {ev.nombreArchivo}
                          </p>
                          <p className="text-[10px] text-[#8E8E93]">
                            {(ev.tamanoBytes / 1024).toFixed(1)} KB • Appwrite
                          </p>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      )}

      {/* 3. PESTAÑA 1: PASE QR Y COMPARTIR */}
      {tabActiva === "qr" && (
        <div className="max-w-xl mx-auto space-y-6 animate-in fade-in duration-200">
          <div className="bg-white rounded-3xl overflow-hidden border border-[#E5E5EA] shadow-lg">
            {/* Cabecera Pase */}
            <div className="bg-[#0D253F] px-6 py-4 text-white flex items-center justify-between">
              <span className="font-mono text-xs font-bold tracking-wider">
                ✈️ BOARDING PASS • GSM DIGITAL
              </span>
              <span className="text-xs text-sky-200">
                {solicitud.categoria.replace("_", " ")}
              </span>
            </div>

            {/* Código QR en Alta Definición */}
            <div className="p-8 flex flex-col items-center gap-6">
              <div className="p-4 bg-white border-2 border-dashed border-[#0066FF]/40 rounded-3xl shadow-inner">
                <QRCodeSVG
                  value={urlSeguimiento}
                  size={240}
                  level="H"
                  includeMargin={true}
                />
              </div>

              <div className="text-center space-y-1">
                <span className="font-mono text-base font-black text-[#0066FF]">
                  {solicitud.codigoSeguimiento}
                </span>
                <p className="text-sm font-bold text-[#1C1C1E]">{solicitud.titulo}</p>
                <p className="text-xs text-[#8E8E93]">
                  Escanea para consultar estado, chatear y revisar avances en vivo
                </p>
              </div>

              {/* Botones de Compartir */}
              <div className="grid grid-cols-2 gap-3 w-full">
                <button
                  onClick={copiarEnlace}
                  className="flex items-center justify-center gap-2 py-3 px-4 rounded-xl text-xs font-bold bg-[#F2F2F7] hover:bg-[#E5E5EA] text-[#1C1C1E] transition-colors"
                >
                  {copiado ? (
                    <>
                      <Check className="w-4 h-4 text-emerald-600" />
                      <span className="text-emerald-700">¡Enlace Copiado!</span>
                    </>
                  ) : (
                    <>
                      <Copy className="w-4 h-4 text-[#8E8E93]" />
                      <span>Copiar Enlace</span>
                    </>
                  )}
                </button>

                <button
                  onClick={compartirPorWhatsApp}
                  className="flex items-center justify-center gap-2 py-3 px-4 rounded-xl text-xs font-bold bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs transition-colors"
                >
                  <MessageCircle className="w-4 h-4" />
                  <span>Compartir WhatsApp</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 4. PESTAÑA 2: CONSOLA DE GESTIÓN ADMIN (Solo Administrador) */}
      {tabActiva === "admin" && isAdmin && (
        <div className="bg-white rounded-3xl p-6 sm:p-8 border border-[#E5E5EA] shadow-xs space-y-8 animate-in fade-in duration-200">
          <div>
            <h2 className="text-xl font-black text-[#1C1C1E] flex items-center gap-2">
              <Shield className="w-5 h-5 text-purple-600" />
              Consola de Gestión y Asignación (Rol Administrador)
            </h2>
            <p className="text-xs text-[#8E8E93] mt-1">
              Control de Acuerdos de Nivel de Servicio (SLA), criticidad y técnico asignado.
            </p>
          </div>

          {/* Cuadrícula 2x2 de Nivel de Criticidad */}
          <div className="space-y-3">
            <label className="block text-xs font-bold text-[#8E8E93] uppercase tracking-wider">
              Nivel de Criticidad & SLA (Cuadrícula 2x2)
            </label>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {(Object.keys(PRIORIDADES_INFO) as PrioridadSolicitud[]).map((prio) => {
                const info = PRIORIDADES_INFO[prio];
                const seleccionada = prioridadSeleccionada === prio;
                return (
                  <button
                    key={prio}
                    type="button"
                    onClick={() => setPrioridadSeleccionada(prio)}
                    className={`p-4 rounded-2xl border text-left transition-all ${
                      seleccionada
                        ? "border-[#0066FF] bg-blue-50/70 shadow-xs ring-2 ring-[#0066FF]/20"
                        : "border-[#E5E5EA] hover:border-gray-300 bg-white"
                    }`}
                  >
                    <div className="flex items-center justify-between mb-1">
                      <span className="text-sm font-bold text-[#1C1C1E] flex items-center gap-2">
                        <span>{info.iconEmoji}</span>
                        <span>{info.label}</span>
                      </span>
                      {seleccionada && (
                        <span className="w-2.5 h-2.5 rounded-full bg-[#0066FF]" />
                      )}
                    </div>
                    <span className="text-xs text-[#8E8E93] block">
                      {info.slaTexto} ({info.slaHoras} horas hábiles)
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Selector de Asignación de Técnico */}
          <div className="space-y-3">
            <label className="block text-xs font-bold text-[#8E8E93] uppercase tracking-wider">
              Técnico Especialista a Cargo
            </label>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              {TECNICOS_DISPONIBLES.map((tec) => {
                const seleccionado = tecnicoSeleccionado === tec.id;
                return (
                  <button
                    key={tec.id}
                    type="button"
                    onClick={() => setTecnicoSeleccionado(tec.id)}
                    className={`p-4 rounded-2xl border text-left transition-all ${
                      seleccionado
                        ? "border-purple-600 bg-purple-50/70 shadow-xs ring-2 ring-purple-600/20"
                        : "border-[#E5E5EA] hover:border-gray-300 bg-white"
                    }`}
                  >
                    <div className="flex items-center justify-between mb-1">
                      <span className="text-xs font-bold text-[#1C1C1E]">{tec.nombre}</span>
                      {seleccionado && (
                        <span className="w-2.5 h-2.5 rounded-full bg-purple-600" />
                      )}
                    </div>
                    <span className="text-[11px] text-[#8E8E93] block">
                      {tec.especialidad}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Botón Guardar Cambios de Gestión */}
          <div className="flex justify-end pt-2 border-t border-[#F2F2F7]">
            <button
              onClick={guardarGestionAdmin}
              disabled={guardandoAdmin}
              className="inline-flex items-center gap-2 px-6 py-2.5 rounded-xl text-xs font-bold text-white bg-[#0066FF] hover:bg-[#0052CC] shadow-sm disabled:opacity-50"
            >
              {guardandoAdmin ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <Save className="w-4 h-4" />
              )}
              <span>Guardar Configuración SLA & Asignación</span>
            </button>
          </div>

          {/* Zona de Peligro / Cancelación en Rojo Suave */}
          <div className="p-6 rounded-2xl bg-rose-50/70 border border-rose-200 space-y-3">
            <div className="flex items-center gap-2 text-rose-800 font-bold text-sm">
              <AlertTriangle className="w-4 h-4 text-rose-600" />
              <span>Zona de Control y Cancelación</span>
            </div>
            <p className="text-xs text-rose-900/80 leading-relaxed">
              Cancelar esta solicitud notificará a las partes y registrará el motivo en la bitácora de auditoría inmutable de Firestore.
            </p>
            <div>
              <button
                onClick={() => setModalCancelarAbierto(true)}
                className="px-4 py-2 rounded-xl text-xs font-bold text-white bg-rose-600 hover:bg-rose-700 transition-colors shadow-2xs"
              >
                Cancelar Solicitud Definitivamente
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal para Motivo de Cancelación */}
      {modalCancelarAbierto && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl border border-[#E5E5EA] space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-rose-100 text-rose-600 flex items-center justify-center">
                <AlertTriangle className="w-5 h-5" />
              </div>
              <div>
                <h3 className="font-bold text-base text-[#1C1C1E]">
                  Cancelar Solicitud
                </h3>
                <p className="text-xs text-[#8E8E93]">
                  Ingresa el motivo obligatorio para la bitácora
                </p>
              </div>
            </div>

            <textarea
              rows={4}
              value={motivoCancelacion}
              onChange={(e) => setMotivoCancelacion(e.target.value)}
              placeholder="Ej: Solicitud duplicada o requerimiento desestimado por el cliente..."
              className="w-full px-3.5 py-2.5 rounded-xl border border-[#E5E5EA] text-xs text-[#1C1C1E] outline-hidden focus:border-rose-500"
            />

            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => setModalCancelarAbierto(false)}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-[#8E8E93] hover:text-[#1C1C1E]"
              >
                Volver
              </button>
              <button
                onClick={confirmarCancelacion}
                disabled={!motivoCancelacion.trim() || cancelando}
                className="px-4 py-2 rounded-xl text-xs font-bold text-white bg-rose-600 hover:bg-rose-700 disabled:opacity-50"
              >
                {cancelando ? "Cancelando..." : "Confirmar Cancelación"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal para Marcar como Resuelto (Técnico) */}
      {modalResolverAbierto && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl border border-[#E5E5EA] space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-600 flex items-center justify-center">
                <CheckCircle2 className="w-5 h-5" />
              </div>
              <div>
                <h3 className="font-bold text-base text-[#1C1C1E]">
                  Resolver Solicitud
                </h3>
                <p className="text-xs text-[#8E8E93]">
                  Describe la solución implementada para el cliente
                </p>
              </div>
            </div>

            <textarea
              rows={4}
              value={notasResolucion}
              onChange={(e) => setNotasResolucion(e.target.value)}
              placeholder="Detalla qué cambios o correcciones se realizaron..."
              className="w-full px-3.5 py-2.5 rounded-xl border border-[#E5E5EA] text-xs text-[#1C1C1E] outline-hidden focus:border-emerald-500"
            />

            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => setModalResolverAbierto(false)}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-[#8E8E93] hover:text-[#1C1C1E]"
              >
                Volver
              </button>
              <button
                onClick={confirmarResolucion}
                disabled={!notasResolucion.trim() || resolviendo}
                className="px-4 py-2 rounded-xl text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50"
              >
                {resolviendo ? "Guardando..." : "Confirmar Resolución"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal de Visor de Evidencia */}
      <VisorEvidenciaModal
        evidencia={evidenciaSeleccionada}
        onClose={() => setEvidenciaSeleccionada(null)}
      />
    </div>
  );
}
