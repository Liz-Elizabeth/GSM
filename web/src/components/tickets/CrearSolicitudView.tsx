"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import {
  CategoriaSolicitud,
  PrioridadSolicitud,
  Evidencia,
  CATEGORIAS_INFO,
  PRIORIDADES_INFO,
} from "@/types/solicitud";
import { crearSolicitudEnFirestore } from "@/services/firestoreService";
import { useAuth } from "@/context/AuthContext";
import { FileUploadDropzone } from "@/components/evidencias/FileUploadDropzone";
import { Container } from "@/components/common/Container";
import {
  ArrowLeft,
  Send,
  Bookmark,
  CheckCircle2,
  AlertCircle,
  HelpCircle,
  Loader2,
  Sparkles,
} from "lucide-react";

const LOCAL_STORAGE_BORRADOR = "gsm_borrador_nuevo_ticket";

export function CrearSolicitudView() {
  const router = useRouter();
  const { usuarioActual } = useAuth();

  const [titulo, setTitulo] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [categoria, setCategoria] = useState<CategoriaSolicitud>("INCIDENTE_TECNICO");
  const [prioridad, setPrioridad] = useState<PrioridadSolicitud>("MEDIA");
  const [servicioRelacionado, setServicioRelacionado] = useState("");
  const [activoRelacionado, setActivoRelacionado] = useState("");
  const [evidencias, setEvidencias] = useState<Evidencia[]>([]);

  const [enviando, setEnviando] = useState(false);
  const [borradorGuardado, setBorradorGuardado] = useState(false);
  const [errorValidacion, setErrorValidacion] = useState<string | null>(null);

  // Cargar borrador persistido
  useEffect(() => {
    try {
      const guardado = localStorage.getItem(LOCAL_STORAGE_BORRADOR);
      if (guardado) {
        const data = JSON.parse(guardado);
        if (data.titulo) setTitulo(data.titulo);
        if (data.descripcion) setDescripcion(data.descripcion);
        if (data.categoria) setCategoria(data.categoria);
        if (data.prioridad) setPrioridad(data.prioridad);
        if (data.servicioRelacionado) setServicioRelacionado(data.servicioRelacionado);
        if (data.activoRelacionado) setActivoRelacionado(data.activoRelacionado);
      }
    } catch {
      // Ignorar
    }
  }, []);

  const guardarBorrador = () => {
    try {
      const data = {
        titulo,
        descripcion,
        categoria,
        prioridad,
        servicioRelacionado,
        activoRelacionado,
      };
      localStorage.setItem(LOCAL_STORAGE_BORRADOR, JSON.stringify(data));
      setBorradorGuardado(true);
      setTimeout(() => setBorradorGuardado(false), 2500);
    } catch {
      // Ignorar
    }
  };

  const limpiarBorrador = () => {
    try {
      localStorage.removeItem(LOCAL_STORAGE_BORRADOR);
    } catch {
      // Ignorar
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorValidacion(null);

    if (titulo.trim().length < 5) {
      setErrorValidacion("El título debe tener al menos 5 caracteres descriptivos.");
      return;
    }
    if (descripcion.trim().length < 15) {
      setErrorValidacion(
        "Por favor explica detalladamente el problema o requerimiento (mínimo 15 caracteres)."
      );
      return;
    }

    setEnviando(true);

    try {
      const horasSla = PRIORIDADES_INFO[prioridad]?.slaHoras || 48;
      const solicitudCreada = await crearSolicitudEnFirestore({
        titulo: titulo.trim(),
        descripcion: descripcion.trim(),
        categoria,
        prioridad,
        clienteId: usuarioActual.id,
        clienteNombre: usuarioActual.nombre,
        tecnicoId: null,
        estado: "RECIBIDA",
        servicioRelacionado: servicioRelacionado.trim() || null,
        activoRelacionado: activoRelacionado.trim() || null,
        motivoCancelacion: null,
        conformidadCliente: null,
        fechaLimite: Date.now() + horasSla * 3600 * 1000,
        evidencias,
      });

      limpiarBorrador();
      router.push(`/tickets/${solicitudCreada.codigoSeguimiento}`);
    } catch (err) {
      console.error("Error al crear ticket:", err);
      setErrorValidacion("Ocurrió un error al guardar en Firestore. Inténtalo nuevamente.");
      setEnviando(false);
    }
  };

  return (
    <Container className="py-8 sm:py-12">
      <div className="max-w-4xl mx-auto">
        {/* Botón Volver */}
      <div className="mb-6">
        <Link
          href="/"
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#8E8E93] hover:text-[#0066FF] transition-colors"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          Volver a la Bandeja
        </Link>
      </div>

      {/* Tarjeta Formulario Apple HIG */}
      <div className="bg-white rounded-3xl border border-[#E5E5EA] shadow-sm overflow-hidden">
        {/* Cabecera */}
        <div className="px-6 sm:px-8 py-6 border-b border-[#F2F2F7] flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-gradient-to-b from-blue-50/40 to-transparent">
          <div>
            <span className="text-[11px] font-bold uppercase tracking-wider text-[#0066FF] flex items-center gap-1">
              <Sparkles className="w-3.5 h-3.5" />
              Mesa de Ayuda GSM
            </span>
            <h1 className="text-2xl font-bold text-[#1C1C1E] tracking-tight mt-0.5">
              Crear Nueva Solicitud
            </h1>
            <p className="text-xs text-[#8E8E93] mt-1">
              Solicitante activo: <strong className="text-[#1C1C1E]">{usuarioActual.nombre}</strong> (
              {usuarioActual.empresa || usuarioActual.rol})
            </p>
          </div>

          <button
            type="button"
            onClick={guardarBorrador}
            className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl border border-[#E5E5EA] bg-white hover:bg-[#F2F2F7] text-xs font-semibold text-[#1C1C1E] transition-colors self-start sm:self-auto"
          >
            {borradorGuardado ? (
              <>
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                <span className="text-emerald-700">¡Borrador guardado!</span>
              </>
            ) : (
              <>
                <Bookmark className="w-3.5 h-3.5 text-[#8E8E93]" />
                <span>Guardar borrador</span>
              </>
            )}
          </button>
        </div>

        {/* Formulario */}
        <form onSubmit={handleSubmit} className="p-6 sm:p-8 space-y-6">
          {errorValidacion && (
            <div className="p-3.5 rounded-2xl bg-rose-50 border border-rose-200 text-rose-800 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
              <span>{errorValidacion}</span>
            </div>
          )}

          {/* Título */}
          <div>
            <label className="block text-xs font-bold text-[#1C1C1E] mb-1.5">
              Título de la Solicitud <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              value={titulo}
              onChange={(e) => setTitulo(e.target.value)}
              placeholder="Ej: Caída de certificado SSL en servidor principal"
              className="w-full px-4 py-2.5 rounded-xl border border-[#E5E5EA] bg-[#F2F2F7]/50 text-sm font-medium text-[#1C1C1E] placeholder:text-[#8E8E93] focus:outline-hidden focus:ring-2 focus:ring-[#0066FF] focus:bg-white transition-all"
              required
            />
          </div>

          {/* Categoría (Select con Emojis) */}
          <div>
            <label className="block text-xs font-bold text-[#1C1C1E] mb-1.5">
              Categoría del Servicio <span className="text-rose-500">*</span>
            </label>
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-2">
              {Object.entries(CATEGORIAS_INFO).map(([catKey, info]) => {
                const seleccionada = categoria === catKey;
                return (
                  <button
                    key={catKey}
                    type="button"
                    onClick={() => setCategoria(catKey as CategoriaSolicitud)}
                    className={`p-3 rounded-2xl border text-left transition-all flex flex-col justify-between ${
                      seleccionada
                        ? "border-[#0066FF] bg-blue-50/70 ring-1 ring-[#0066FF]"
                        : "border-[#E5E5EA] bg-white hover:bg-[#F2F2F7]"
                    }`}
                  >
                    <span className="text-xl mb-1">{info.emoji}</span>
                    <span
                      className={`text-xs font-bold ${
                        seleccionada ? "text-[#0066FF]" : "text-[#1C1C1E]"
                      }`}
                    >
                      {info.label}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Prioridad y SLA */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="text-xs font-bold text-[#1C1C1E]">
                Nivel de Prioridad SLA <span className="text-rose-500">*</span>
              </label>
              <span className="text-[11px] text-[#8E8E93] flex items-center gap-1">
                <HelpCircle className="w-3 h-3" />
                Define el tiempo de atención garantizado
              </span>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
              {Object.entries(PRIORIDADES_INFO).map(([prioKey, info]) => {
                const seleccionada = prioridad === prioKey;
                return (
                  <button
                    key={prioKey}
                    type="button"
                    onClick={() => setPrioridad(prioKey as PrioridadSolicitud)}
                    className={`p-3 rounded-2xl border text-left transition-all ${
                      seleccionada
                        ? "border-[#0066FF] bg-blue-50/70 ring-1 ring-[#0066FF]"
                        : "border-[#E5E5EA] bg-white hover:bg-[#F2F2F7]"
                    }`}
                  >
                    <div className="flex items-center justify-between mb-1">
                      <span className="text-xs font-extrabold text-[#1C1C1E]">{info.label}</span>
                      <span>{info.iconEmoji}</span>
                    </div>
                    <p className="text-[11px] font-semibold text-[#0066FF]">
                      SLA: {info.slaHoras} hrs
                    </p>
                    <p className="text-[10px] text-[#8E8E93] line-clamp-1 mt-0.5">
                      {info.slaTexto}
                    </p>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Descripción Detallada */}
          <div>
            <label className="block text-xs font-bold text-[#1C1C1E] mb-1.5">
              Descripción Detallada del Requerimiento <span className="text-rose-500">*</span>
            </label>
            <textarea
              rows={4}
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
              placeholder="Indica qué sucede, mensajes de error observados, usuarios afectados o pasos para reproducir..."
              className="w-full px-4 py-2.5 rounded-xl border border-[#E5E5EA] bg-[#F2F2F7]/50 text-sm font-medium text-[#1C1C1E] placeholder:text-[#8E8E93] focus:outline-hidden focus:ring-2 focus:ring-[#0066FF] focus:bg-white transition-all resize-y"
              required
            />
            <span className="text-[10px] text-[#8E8E93] mt-1 block">
              Mínimo 15 caracteres. Sé lo más específico posible.
            </span>
          </div>

          {/* Campos Opcionales: Servicio y Activo */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
            <div>
              <label className="block text-xs font-bold text-[#1C1C1E] mb-1.5">
                Servicio Relacionado (Opcional)
              </label>
              <input
                type="text"
                value={servicioRelacionado}
                onChange={(e) => setServicioRelacionado(e.target.value)}
                placeholder="Ej: Pasarela Webpay Plus, Office 365, etc."
                className="w-full px-4 py-2 rounded-xl border border-[#E5E5EA] bg-[#F2F2F7]/40 text-xs font-medium text-[#1C1C1E] focus:outline-hidden focus:ring-2 focus:ring-[#0066FF] focus:bg-white"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-[#1C1C1E] mb-1.5">
                Activo / URL / Servidor (Opcional)
              </label>
              <input
                type="text"
                value={activoRelacionado}
                onChange={(e) => setActivoRelacionado(e.target.value)}
                placeholder="Ej: https://pagos.innovatech.cl o srv-prod-01"
                className="w-full px-4 py-2 rounded-xl border border-[#E5E5EA] bg-[#F2F2F7]/40 text-xs font-medium text-[#1C1C1E] focus:outline-hidden focus:ring-2 focus:ring-[#0066FF] focus:bg-white"
              />
            </div>
          </div>

          {/* Subida de Evidencias (Appwrite Storage) */}
          <div className="pt-2">
            <FileUploadDropzone
              evidencias={evidencias}
              onEvidenciasChange={setEvidencias}
            />
          </div>

          {/* Barra de Acciones Final */}
          <div className="pt-4 border-t border-[#F2F2F7] flex flex-col sm:flex-row items-center justify-between gap-3">
            <span className="text-xs text-[#8E8E93] order-2 sm:order-1">
              Al crear el ticket, se generará un código único y pase QR imprimible.
            </span>

            <div className="flex items-center gap-3 w-full sm:w-auto order-1 sm:order-2">
              <Link
                href="/"
                className="flex-1 sm:flex-initial px-4 py-2.5 rounded-xl border border-[#E5E5EA] hover:bg-[#F2F2F7] text-xs font-bold text-[#1C1C1E] text-center transition-colors"
              >
                Cancelar
              </Link>

              <button
                type="submit"
                disabled={enviando}
                className="flex-1 sm:flex-initial px-6 py-2.5 rounded-xl bg-[#0066FF] hover:bg-[#0052CC] text-white text-xs font-bold transition-all shadow-sm hover:shadow active:scale-95 flex items-center justify-center gap-2 disabled:opacity-50"
              >
                {enviando ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    <span>Creando Solicitud...</span>
                  </>
                ) : (
                  <>
                    <Send className="w-4 h-4" />
                    <span>Publicar Ticket</span>
                  </>
                )}
              </button>
            </div>
          </div>
        </form>
      </div>
      </div>
    </Container>
  );
}
