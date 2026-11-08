"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import {
  Building2,
  Mail,
  User,
  Phone,
  Lock,
  Sparkles,
  ArrowRight,
  CheckCircle2,
  AlertCircle,
  Shield,
} from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { PLANES_SOPORTE } from "@/types/plan";

export function RegistroView() {
  const router = useRouter();
  const { registrarCliente } = useAuth();

  const [nombre, setNombre] = useState("");
  const [correo, setCorreo] = useState("");
  const [empresa, setEmpresa] = useState("");
  const [telefono, setTelefono] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const [planSeleccionado, setPlanSeleccionado] = useState<"plus" | "pro" | "max">("pro");
  const [terminosAceptados, setTerminosAceptados] = useState(true);

  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (password.length < 6) {
      setError("La contraseña debe tener al menos 6 caracteres.");
      return;
    }
    if (password !== passwordConfirm) {
      setError("Las contraseñas no coinciden.");
      return;
    }
    if (!terminosAceptados) {
      setError("Debes aceptar los términos del acuerdo de nivel de servicio (SLA).");
      return;
    }

    setCargando(true);

    const resultado = await registrarCliente({
      nombre,
      correo,
      empresa,
      telefono,
      password,
      plan: planSeleccionado,
    });

    if (resultado.success) {
      router.push("/tickets");
    } else {
      setError(resultado.error || "Error al registrar la cuenta");
      setCargando(false);
    }
  };

  return (
    <div className="w-full min-h-[calc(100dvh-4rem)] py-12 px-4 sm:px-6 flex flex-col justify-center items-center">
      <div className="w-full max-w-2xl">
        {/* Encabezado */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-blue-50 text-[#0066FF] border border-blue-100 mb-2">
            <Sparkles className="w-3.5 h-3.5" />
            Incorporación de Nuevos Clientes
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-gray-900 tracking-tight">
            Registra tu Empresa en GSM Soporte
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 mt-1">
            Obtén acceso inmediato a la mesa de ayuda con acuerdos de nivel de servicio (SLA) garantizados.
          </p>
        </div>

        {/* Tarjeta de Registro */}
        <div className="bg-white rounded-3xl shadow-xl border border-gray-200/80 p-6 sm:p-8">
          <form onSubmit={handleSubmit} className="space-y-5">
            {error && (
              <div className="p-3.5 rounded-2xl bg-rose-50 border border-rose-200 text-rose-800 text-xs flex items-center gap-2">
                <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            {/* Datos Personales y de Empresa */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-gray-700 mb-1.5">
                  Nombre Completo <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <input
                    type="text"
                    value={nombre}
                    onChange={(e) => setNombre(e.target.value)}
                    placeholder="Ej: Matías González"
                    className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-300 text-sm font-medium text-gray-900 placeholder:text-gray-400 focus:outline-hidden focus:ring-2 focus:ring-[#0066FF] bg-gray-50/40"
                    required
                  />
                  <User className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-700 mb-1.5">
                  Correo Electrónico Corporativo <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <input
                    type="email"
                    value={correo}
                    onChange={(e) => setCorreo(e.target.value)}
                    placeholder="contacto@miempresa.cl"
                    className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-300 text-sm font-medium text-gray-900 placeholder:text-gray-400 focus:outline-hidden focus:ring-2 focus:ring-[#0066FF] bg-gray-50/40"
                    required
                  />
                  <Mail className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-700 mb-1.5">
                  Empresa / Razón Social <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <input
                    type="text"
                    value={empresa}
                    onChange={(e) => setEmpresa(e.target.value)}
                    placeholder="Ej: Mi Empresa SpA"
                    className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-300 text-sm font-medium text-gray-900 placeholder:text-gray-400 focus:outline-hidden focus:ring-2 focus:ring-[#0066FF] bg-gray-50/40"
                    required
                  />
                  <Building2 className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-700 mb-1.5">
                  Teléfono de Contacto <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <input
                    type="tel"
                    value={telefono}
                    onChange={(e) => setTelefono(e.target.value)}
                    placeholder="+56 9 1234 5678"
                    className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-300 text-sm font-medium text-gray-900 placeholder:text-gray-400 focus:outline-hidden focus:ring-2 focus:ring-[#0066FF] bg-gray-50/40"
                    required
                  />
                  <Phone className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
                </div>
              </div>
            </div>

            {/* Contraseña */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-gray-700 mb-1.5">
                  Contraseña <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Mínimo 6 caracteres"
                    className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-300 text-sm font-medium text-gray-900 placeholder:text-gray-400 focus:outline-hidden focus:ring-2 focus:ring-[#0066FF] bg-gray-50/40"
                    required
                  />
                  <Lock className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-700 mb-1.5">
                  Confirmar Contraseña <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <input
                    type="password"
                    value={passwordConfirm}
                    onChange={(e) => setPasswordConfirm(e.target.value)}
                    placeholder="Repite la contraseña"
                    className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-300 text-sm font-medium text-gray-900 placeholder:text-gray-400 focus:outline-hidden focus:ring-2 focus:ring-[#0066FF] bg-gray-50/40"
                    required
                  />
                  <Lock className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
                </div>
              </div>
            </div>

            {/* Selector de Plan Inicial */}
            <div>
              <label className="block text-xs font-bold text-gray-700 mb-1.5">
                Selecciona tu Nivel de Servicio Inicial
              </label>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                {PLANES_SOPORTE.map((p) => {
                  const seleccionado = planSeleccionado === p.id;
                  return (
                    <button
                      key={p.id}
                      type="button"
                      onClick={() => setPlanSeleccionado(p.id)}
                      className={`p-3.5 rounded-2xl border text-left transition-all ${
                        seleccionado
                          ? "border-[#0066FF] bg-blue-50/70 ring-2 ring-[#0066FF]/20"
                          : "border-gray-200 bg-white hover:bg-gray-50"
                      }`}
                    >
                      <div className="flex justify-between items-center mb-1">
                        <span className="text-xs font-black text-gray-900">{p.nombre}</span>
                        {p.esRecomendado && (
                          <span className="px-1.5 py-0.5 rounded text-[9px] font-black bg-blue-100 text-[#0066FF]">
                            POPULAR
                          </span>
                        )}
                      </div>
                      <div className="text-sm font-black text-gray-900">{p.precioTexto}</div>
                      <div className="text-[11px] text-gray-500 mt-1">SLA: {p.slaRespuesta}</div>
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Checkbox de Términos */}
            <div className="flex items-start gap-2 pt-1">
              <input
                type="checkbox"
                id="terminos"
                checked={terminosAceptados}
                onChange={(e) => setTerminosAceptados(e.target.checked)}
                className="mt-0.5 rounded border-gray-300 text-[#0066FF] focus:ring-[#0066FF]"
              />
              <label htmlFor="terminos" className="text-xs text-gray-600 leading-tight">
                Acepto los términos de servicio, políticas de privacidad y los acuerdos de nivel de
                respuesta (SLA) de GSM Soporte Técnico.
              </label>
            </div>

            {/* Botón Registrar */}
            <div className="pt-2">
              <button
                type="submit"
                disabled={cargando}
                className="w-full py-3.5 px-4 rounded-xl font-bold text-sm text-white bg-[#0066FF] hover:bg-[#0052CC] active:scale-98 transition-all flex items-center justify-center gap-2 shadow-md hover:shadow-lg disabled:opacity-50"
              >
                {cargando ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    <span>Creando cuenta en Cloud Firestore...</span>
                  </>
                ) : (
                  <>
                    <span>Completar Registro & Entrar</span>
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            </div>
          </form>

          {/* Enlace a Login */}
          <div className="mt-6 text-center text-xs text-gray-500 border-t border-gray-100 pt-4">
            ¿Ya tienes una cuenta registrada?{" "}
            <Link href="/login" className="font-bold text-[#0066FF] hover:underline">
              Inicia sesión aquí
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
