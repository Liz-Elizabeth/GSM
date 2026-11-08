"use client";

import React, { useState, useRef } from "react";
import { Evidencia } from "@/types/solicitud";
import { subirEvidenciaAAppwrite, TAMANO_MAXIMO_BYTES } from "@/services/appwriteService";
import {
  UploadCloud,
  FileCheck,
  X,
  Loader2,
  AlertCircle,
  HardDrive,
  Image as ImageIcon,
} from "lucide-react";

interface FileUploadDropzoneProps {
  evidencias: Evidencia[];
  onEvidenciasChange: (evidencias: Evidencia[]) => void;
  maxFiles?: number;
}

export function FileUploadDropzone({
  evidencias,
  onEvidenciasChange,
  maxFiles = 5,
}: FileUploadDropzoneProps) {
  const [arrastrando, setArrastrando] = useState(false);
  const [subiendo, setSubiendo] = useState(false);
  const [errorMensaje, setErrorMensaje] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const procesarArchivos = async (archivos: FileList | null) => {
    if (!archivos || archivos.length === 0) return;
    setErrorMensaje(null);

    if (evidencias.length + archivos.length > maxFiles) {
      setErrorMensaje(`Puedes adjuntar un máximo de ${maxFiles} archivos por ticket.`);
      return;
    }

    setSubiendo(true);
    const nuevasEvidencias = [...evidencias];

    try {
      for (let i = 0; i < archivos.length; i++) {
        const file = archivos[i];

        if (file.size > TAMANO_MAXIMO_BYTES) {
          throw new Error(`El archivo "${file.name}" supera el límite de 15 MB.`);
        }

        const evidenciaSubida = await subirEvidenciaAAppwrite(file);
        nuevasEvidencias.push(evidenciaSubida);
      }
      onEvidenciasChange(nuevasEvidencias);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Error al subir evidencia a Appwrite Storage";
      setErrorMensaje(msg);
    } finally {
      setSubiendo(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    }
  };

  const eliminarEvidencia = (id: string) => {
    onEvidenciasChange(evidencias.filter((e) => e.id !== id));
  };

  const formatearTamano = (bytes: number) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  };

  return (
    <div className="space-y-3">
      {/* Zona de Dropzone */}
      <div
        onDragOver={(e) => {
          e.preventDefault();
          setArrastrando(true);
        }}
        onDragLeave={() => setArrastrando(false)}
        onDrop={(e) => {
          e.preventDefault();
          setArrastrando(false);
          procesarArchivos(e.dataTransfer.files);
        }}
        onClick={() => fileInputRef.current?.click()}
        className={`border-2 border-dashed rounded-2xl p-6 text-center cursor-pointer transition-all ${
          arrastrando
            ? "border-[#0066FF] bg-blue-50/50"
            : "border-[#E5E5EA] hover:border-[#0066FF] bg-[#F2F2F7]/50 hover:bg-[#F2F2F7]"
        }`}
      >
        <input
          ref={fileInputRef}
          type="file"
          multiple
          className="hidden"
          onChange={(e) => procesarArchivos(e.target.files)}
          accept="image/*,.pdf,.doc,.docx,.txt,.zip,.log"
        />

        {subiendo ? (
          <div className="flex flex-col items-center justify-center py-2">
            <Loader2 className="w-8 h-8 text-[#0066FF] animate-spin mb-2" />
            <p className="text-xs font-bold text-[#1C1C1E]">
              Subiendo a Appwrite Storage...
            </p>
            <p className="text-[11px] text-[#8E8E93]">
              Cifrando y persistiendo en la nube
            </p>
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center">
            <div className="w-10 h-10 rounded-xl bg-white border border-[#E5E5EA] flex items-center justify-center text-[#0066FF] shadow-2xs mb-2">
              <UploadCloud className="w-5 h-5" />
            </div>
            <p className="text-xs font-bold text-[#1C1C1E]">
              Arrastra tus evidencias o haz clic para explorar
            </p>
            <p className="text-[11px] text-[#8E8E93] mt-1">
              Imágenes, capturas, logs o PDFs (Máximo 15 MB por archivo)
            </p>
          </div>
        )}
      </div>

      {/* Error de Validación */}
      {errorMensaje && (
        <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 flex items-center gap-2 text-xs font-semibold text-rose-700 animate-in fade-in">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{errorMensaje}</span>
        </div>
      )}

      {/* Lista de Evidencias Subidas */}
      {evidencias.length > 0 && (
        <div className="space-y-2">
          <div className="flex items-center justify-between text-xs text-[#8E8E93]">
            <span className="font-bold">Archivos adjuntos ({evidencias.length})</span>
            <span className="text-[11px]">Almacenados en Appwrite</span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
            {evidencias.map((ev) => (
              <div
                key={ev.id}
                className="flex items-center justify-between p-2.5 rounded-xl bg-white border border-[#E5E5EA] shadow-2xs"
              >
                <div className="flex items-center gap-2 min-w-0">
                  <div className="w-7 h-7 rounded-lg bg-blue-50 text-[#0066FF] flex items-center justify-center shrink-0">
                    <FileCheck className="w-4 h-4" />
                  </div>
                  <div className="min-w-0">
                    <p className="text-xs font-bold text-[#1C1C1E] truncate max-w-[160px]">
                      {ev.nombreArchivo}
                    </p>
                    <p className="text-[10px] text-[#8E8E93]">
                      {formatearTamano(ev.tamanoBytes)}
                    </p>
                  </div>
                </div>

                <button
                  type="button"
                  onClick={() => eliminarEvidencia(ev.id)}
                  className="p-1 rounded-lg text-[#8E8E93] hover:text-rose-600 hover:bg-rose-50 transition-colors"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
