"use client";

import React from "react";
import { Evidencia } from "@/types/solicitud";
import { X, Download, FileText, Image as ImageIcon, Calendar, HardDrive, ExternalLink } from "lucide-react";

interface VisorEvidenciaModalProps {
  evidencia: Evidencia | null;
  onClose: () => void;
}

export function VisorEvidenciaModal({ evidencia, onClose }: VisorEvidenciaModalProps) {
  if (!evidencia) return null;

  const esImagen =
    evidencia.tipoArchivo.startsWith("image/") ||
    /\.(jpg|jpeg|png|webp|gif|svg)$/i.test(evidencia.nombreArchivo);

  const formatearTamano = (bytes: number) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
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
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in duration-200">
      <div
        className="fixed inset-0"
        onClick={onClose}
      />

      <div className="relative bg-white rounded-3xl max-w-4xl w-full max-h-[90dvh] overflow-hidden flex flex-col shadow-2xl z-10 border border-[#E5E5EA]">
        {/* Cabecera del Visor */}
        <div className="px-6 py-4 border-b border-[#E5E5EA] flex items-center justify-between bg-white">
          <div className="flex items-center gap-3 min-w-0">
            <div className="w-10 h-10 rounded-xl bg-blue-50 text-[#0066FF] flex items-center justify-center shrink-0">
              {esImagen ? <ImageIcon className="w-5 h-5" /> : <FileText className="w-5 h-5" />}
            </div>
            <div className="min-w-0">
              <h3 className="font-bold text-base text-[#1C1C1E] truncate">
                {evidencia.nombreArchivo}
              </h3>
              <div className="flex items-center gap-3 text-xs text-[#8E8E93]">
                <span className="flex items-center gap-1">
                  <HardDrive className="w-3 h-3" />
                  {formatearTamano(evidencia.tamanoBytes)}
                </span>
                <span className="flex items-center gap-1">
                  <Calendar className="w-3 h-3" />
                  {formatearFecha(evidencia.fechaIncorporacion)}
                </span>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <a
              href={evidencia.uriRemota}
              target="_blank"
              rel="noopener noreferrer"
              download={evidencia.nombreArchivo}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold text-white bg-[#0066FF] hover:bg-[#0052CC] transition-colors"
            >
              <Download className="w-3.5 h-3.5" />
              <span>Descargar</span>
            </a>

            <button
              onClick={onClose}
              className="p-1.5 rounded-xl text-[#8E8E93] hover:text-[#1C1C1E] hover:bg-[#F2F2F7] transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Cuerpo del Visor */}
        <div className="flex-1 overflow-auto p-6 bg-[#F2F2F7] flex items-center justify-center min-h-[300px]">
          {esImagen ? (
            <div className="relative max-h-[60vh] flex items-center justify-center">
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src={evidencia.uriRemota}
                alt={evidencia.nombreArchivo}
                className="max-h-[60vh] w-auto max-w-full rounded-2xl object-contain shadow-sm border border-[#E5E5EA]"
                onError={(e) => {
                  // Fallback si la imagen no carga
                  const target = e.currentTarget;
                  target.style.display = "none";
                  const fallback = target.nextElementSibling;
                  if (fallback) (fallback as HTMLElement).style.display = "flex";
                }}
              />
              <div
                style={{ display: "none" }}
                className="flex-col items-center justify-center p-8 text-center bg-white rounded-2xl border border-[#E5E5EA]"
              >
                <FileText className="w-12 h-12 text-[#8E8E93] mb-2" />
                <p className="text-sm font-semibold text-[#1C1C1E]">Vista previa no disponible</p>
                <p className="text-xs text-[#8E8E93] mt-1">El archivo está alojado en Appwrite Storage</p>
                <a
                  href={evidencia.uriRemota}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="mt-3 inline-flex items-center gap-1 text-xs font-bold text-[#0066FF]"
                >
                  Abrir enlace directo <ExternalLink className="w-3 h-3" />
                </a>
              </div>
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center p-10 bg-white rounded-2xl border border-[#E5E5EA] text-center max-w-md">
              <FileText className="w-16 h-16 text-[#0066FF] mb-3" />
              <p className="font-bold text-base text-[#1C1C1E] mb-1">{evidencia.nombreArchivo}</p>
              <p className="text-xs text-[#8E8E93] mb-4">
                Formato: {evidencia.tipoArchivo} ({formatearTamano(evidencia.tamanoBytes)})
              </p>
              <a
                href={evidencia.uriRemota}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold text-white bg-[#0066FF] hover:bg-[#0052CC] transition-colors"
              >
                <Download className="w-4 h-4" />
                Descargar desde Appwrite Storage
              </a>
            </div>
          )}
        </div>

        {/* Pie con descripción si existe */}
        {evidencia.descripcion && (
          <div className="px-6 py-3 bg-white border-t border-[#E5E5EA] text-xs text-[#1C1C1E]">
            <span className="font-bold text-[#8E8E93] mr-2">Nota adjunta:</span>
            {evidencia.descripcion}
          </div>
        )}
      </div>
    </div>
  );
}
