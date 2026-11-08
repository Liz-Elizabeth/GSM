"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import {
  Lock,
  Mail,
  ArrowRight,
  Shield,
  Briefcase,
  User,
  AlertCircle,
  CheckCircle2,
  Sparkles,
} from "lucide-react";
import { useAuth } from "@/context/AuthContext";

export function LoginView() {
  const router = useRouter();
  const { iniciarSesion, cambiarUsuario } = useAuth();

  const [correo, setCorreo] = useState("");
  const [password, setPassword] = useState("");
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setCargando(true);

    const resultado = await iniciarSesion(correo, password);
    if (resultado.success) {
      router.push("/tickets");
    } else {
      setError(resultado.error || "Error al iniciar sesión");
      setCargando(false);
    }
  };

  const seleccionarPreset = (idUsuario: number, correoPreset: string) => {
    setCorreo(correoPreset);
    setPassword("gsm123456");
    cambiarUsuario(idUsuario);
    router.push("/tickets");
  };

  return (
    <div className="w-full min-h-[calc(100dvh-4rem)] flex flex-col justify-center items-center py-12 px-4 sm:px-6">
      <div className="w-full max-w-md">
        {/* Logo & Marca */}
        <div className="text-center mb-8">
          <div className="w-14 h-14 rounded-2xl bg-[#0066FF] flex items-center justify-center text-white font-black text-2xl shadow-md mx-auto mb-3">
            GSM
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-gray-900 tracking-tight">
            Iniciar Sesión en GSM
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 mt-1">
            Plataforma de Gestión de Solicitudes y Continuidad Operacional
          </p>
        </div>

        {/* Tarjeta de Formulario Apple HIG */}
        <div className="bg-white rounded-3xl shadow-xl border border-gray-200/80 p-7 sm:p-8">
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              <div className="p-3.5 rounded-2xl bg-rose-50 border border-rose-200 text-rose-800 text-xs flex items-center gap-2">
                <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <div>
              <label className="block text-xs font-bold text-gray-700 mb-1.5">
                Correo Electrónico
              </label>
              <div className="relative">
                <input
                  type="email"
                  value={correo}
                  onChange={(e) => setCorreo(e.target.value)}
                  placeholder="ejemplo@empresa.cl"
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-300 text-sm font-medium text-gray-900 placeholder:text-gray-400 focus:outline-hidden focus:ring-2 focus:ring-[#0066FF] bg-gray-50/40"
                  required
                />
                <Mail className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
              </div>
            </div>

            <div>
              <div className="flex justify-between items-center mb-1.5">
                <label className="text-xs font-bold text-gray-700">Contraseña</label>
                <span className="text-[11px] text-[#0066FF] hover:underline cursor-pointer">
                  ¿Olvidaste tu contraseña?
                </span>
              </div>
              <div className="relative">
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-300 text-sm font-medium text-gray-900 placeholder:text-gray-400 focus:outline-hidden focus:ring-2 focus:ring-[#0066FF] bg-gray-50/40"
                  required
                />
                <Lock className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
              </div>
            </div>

            <div className="pt-2">
              <button
                type="submit"
                disabled={cargando}
                className="w-full py-3.5 px-4 rounded-xl font-bold text-sm text-white bg-[#0066FF] hover:bg-[#0052CC] active:scale-98 transition-all flex items-center justify-center gap-2 shadow-md hover:shadow-lg disabled:opacity-50"
              >
                {cargando ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    <span>Verificando credenciales...</span>
                  </>
                ) : (
                  <>
                    <span>Ingresar al Portal</span>
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            </div>
          </form>

          {/* Selector Rápido de Roles (RBAC Demo de 1 Clic) */}
          <div className="mt-6 pt-6 border-t border-gray-100">
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-400 block text-center mb-3">
              Acceso Rápido por Rol (Demostración RBAC)
            </span>

            <div className="grid grid-cols-1 gap-2">
              <button
                type="button"
                onClick={() => seleccionarPreset(99, "admin@gsm.cl")}
                className="w-full p-2.5 rounded-xl border border-purple-100 bg-purple-50/50 hover:bg-purple-50 transition-colors flex items-center justify-between text-left group"
              >
                <div className="flex items-center gap-2.5">
                  <div className="w-7 h-7 rounded-lg bg-purple-100 text-purple-700 flex items-center justify-center">
                    <Shield className="w-3.5 h-3.5" />
                  </div>
                  <div>
                    <div className="text-xs font-bold text-gray-900">Administrador GSM</div>
                    <div className="text-[10px] text-gray-500">admin@gsm.cl (Gestión Total & SLA)</div>
                  </div>
                </div>
                <span className="text-[11px] font-bold text-purple-600 opacity-0 group-hover:opacity-100 transition-opacity">
                  Entrar →
                </span>
              </button>

              <button
                type="button"
                onClick={() => seleccionarPreset(3, "diego@gsm.cl")}
                className="w-full p-2.5 rounded-xl border border-blue-100 bg-blue-50/50 hover:bg-blue-50 transition-colors flex items-center justify-between text-left group"
              >
                <div className="flex items-center gap-2.5">
                  <div className="w-7 h-7 rounded-lg bg-blue-100 text-[#0066FF] flex items-center justify-center">
                    <Briefcase className="w-3.5 h-3.5" />
                  </div>
                  <div>
                    <div className="text-xs font-bold text-gray-900">Técnico Senior</div>
                    <div className="text-[10px] text-gray-500">diego@gsm.cl (Resolución & Chat Interno)</div>
                  </div>
                </div>
                <span className="text-[11px] font-bold text-[#0066FF] opacity-0 group-hover:opacity-100 transition-opacity">
                  Entrar →
                </span>
              </button>

              <button
                type="button"
                onClick={() => seleccionarPreset(1, "camila@empresa.cl")}
                className="w-full p-2.5 rounded-xl border border-emerald-100 bg-emerald-50/50 hover:bg-emerald-50 transition-colors flex items-center justify-between text-left group"
              >
                <div className="flex items-center gap-2.5">
                  <div className="w-7 h-7 rounded-lg bg-emerald-100 text-emerald-700 flex items-center justify-center">
                    <User className="w-3.5 h-3.5" />
                  </div>
                  <div>
                    <div className="text-xs font-bold text-gray-900">Cliente (Innovatech SpA)</div>
                    <div className="text-[10px] text-gray-500">camila@empresa.cl (Plan PRO & Pases QR)</div>
                  </div>
                </div>
                <span className="text-[11px] font-bold text-emerald-600 opacity-0 group-hover:opacity-100 transition-opacity">
                  Entrar →
                </span>
              </button>
            </div>
          </div>

          {/* Enlace al Registro */}
          <div className="mt-6 text-center text-xs text-gray-500">
            ¿Tu empresa aún no cuenta con soporte GSM?{" "}
            <Link href="/registro" className="font-bold text-[#0066FF] hover:underline">
              Regístrate aquí
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
