"use client";

import React, { useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { RolBadge } from "@/components/common/Badges";
import { Container } from "@/components/common/Container";
import {
  Inbox,
  Ticket,
  PlusCircle,
  ChevronDown,
  UserCheck,
  Radio,
  Menu,
  X,
  Shield,
  Briefcase,
  User,
  Sparkles,
  Receipt,
  LogOut,
  LogIn,
  UserPlus,
} from "lucide-react";

export function Header() {
  const pathname = usePathname();
  const router = useRouter();
  const {
    usuarioActual,
    estaAutenticado,
    cerrarSesion,
    cambiarUsuario,
    usuariosDisponibles,
  } = useAuth();

  const [menuPerfilAbierto, setMenuPerfilAbierto] = useState(false);
  const [menuMovilAbierto, setMenuMovilAbierto] = useState(false);

  const handleCerrarSesion = () => {
    cerrarSesion();
    setMenuPerfilAbierto(false);
    router.push("/login");
  };

  return (
    <header className="sticky top-0 z-40 bg-white/95 backdrop-blur-md border-b border-[#E5E5EA] shadow-2xs w-full">
      <Container>
        <div className="flex items-center justify-between h-16">
          {/* Logo y Nombre */}
          <div className="flex items-center gap-6">
            <Link href="/" className="flex items-center gap-2.5 group">
              <div className="w-10 h-10 rounded-xl bg-[#0066FF] flex items-center justify-center text-white font-black text-lg shadow-sm group-hover:scale-105 transition-transform">
                GSM
              </div>
              <div className="flex flex-col">
                <span className="font-bold text-base text-[#1C1C1E] tracking-tight flex items-center gap-1.5">
                  Soporte GSM
                  <span className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded text-[10px] font-semibold bg-blue-50 text-[#0066FF]">
                    <Radio className="w-2.5 h-2.5 animate-pulse text-[#0066FF]" />
                    LIVE
                  </span>
                </span>
                <span className="text-[11px] text-[#8E8E93] hidden sm:block">
                  Gestión de Solicitudes y Mesa de Ayuda
                </span>
              </div>
            </Link>

            {/* Navegación Desktop */}
            <nav className="hidden md:flex items-center gap-1">
              <Link
                href="/"
                className={`px-3 py-2 rounded-xl text-xs sm:text-sm font-semibold transition-colors ${
                  pathname === "/"
                    ? "bg-[#F2F2F7] text-[#0066FF]"
                    : "text-[#1C1C1E] hover:bg-[#F2F2F7] hover:text-[#0066FF]"
                }`}
              >
                Inicio
              </Link>

              <Link
                href="/#quienes-somos"
                className="px-3 py-2 rounded-xl text-xs sm:text-sm font-semibold text-[#1C1C1E] hover:bg-[#F2F2F7] hover:text-[#0066FF] transition-colors"
              >
                Quiénes Somos
              </Link>

              <Link
                href="/planes"
                className={`px-3 py-2 rounded-xl text-xs sm:text-sm font-semibold transition-colors flex items-center gap-1.5 ${
                  pathname.startsWith("/planes")
                    ? "bg-[#F2F2F7] text-[#0066FF]"
                    : "text-[#1C1C1E] hover:bg-[#F2F2F7] hover:text-[#0066FF]"
                }`}
              >
                <Sparkles className="w-4 h-4 text-[#0066FF]" />
                Planes & SLA
              </Link>

              {/* Enlaces exclusivos de plataforma para usuarios autenticados */}
              {estaAutenticado && (
                <>
                  <Link
                    href="/tickets"
                    className={`px-3 py-2 rounded-xl text-xs sm:text-sm font-semibold transition-colors flex items-center gap-1.5 ${
                      pathname === "/tickets"
                        ? "bg-[#F2F2F7] text-[#0066FF]"
                        : "text-[#1C1C1E] hover:bg-[#F2F2F7] hover:text-[#0066FF]"
                    }`}
                  >
                    <Inbox className="w-4 h-4" />
                    Bandeja
                  </Link>

                  <Link
                    href="/tickets?view=wallet"
                    className={`px-3 py-2 rounded-xl text-xs sm:text-sm font-semibold transition-colors flex items-center gap-1.5 ${
                      pathname === "/tickets" && typeof window !== "undefined" && window.location.search.includes("wallet")
                        ? "bg-[#F2F2F7] text-[#0066FF]"
                        : "text-[#1C1C1E] hover:bg-[#F2F2F7] hover:text-[#0066FF]"
                    }`}
                  >
                    <Ticket className="w-4 h-4" />
                    Pases QR
                  </Link>

                  <Link
                    href="/transacciones"
                    className={`px-3 py-2 rounded-xl text-xs sm:text-sm font-semibold transition-colors flex items-center gap-1.5 ${
                      pathname === "/transacciones"
                        ? "bg-[#F2F2F7] text-[#0066FF]"
                        : "text-[#1C1C1E] hover:bg-[#F2F2F7] hover:text-[#0066FF]"
                    }`}
                  >
                    <Receipt className="w-4 h-4" />
                    Pagos Flow
                  </Link>
                </>
              )}
            </nav>
          </div>

          {/* Acciones Derecha */}
          <div className="flex items-center gap-3">
            {estaAutenticado ? (
              <>
                <Link
                  href="/nuevo"
                  className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs sm:text-sm font-semibold text-white bg-[#0066FF] hover:bg-[#0052CC] shadow-sm transition-all hover:shadow active:scale-95"
                >
                  <PlusCircle className="w-4 h-4" />
                  <span className="hidden sm:inline">Nuevo Ticket</span>
                  <span className="sm:hidden">Nuevo</span>
                </Link>

                {/* Selector de Perfil / Rol Simulado (Apple HIG Dropdown) */}
                <div className="relative">
                  <button
                    onClick={() => setMenuPerfilAbierto(!menuPerfilAbierto)}
                    className="flex items-center gap-2.5 px-3 py-1.5 rounded-xl border border-[#E5E5EA] bg-[#F2F2F7]/80 hover:bg-[#E5E5EA] transition-colors text-left"
                    aria-expanded={menuPerfilAbierto}
                  >
                    <div className="w-8 h-8 rounded-full bg-white border border-[#E5E5EA] flex items-center justify-center text-xs font-bold text-[#0066FF] shadow-2xs">
                      {usuarioActual.nombre.charAt(0)}
                    </div>
                    <div className="hidden lg:flex flex-col">
                      <div className="flex items-center gap-1.5">
                        <span className="text-xs font-bold text-[#1C1C1E] leading-tight">
                          {usuarioActual.nombre}
                        </span>
                        {usuarioActual.rol === "CLIENTE" && usuarioActual.plan && (
                          <span className="px-1.5 py-0.2 rounded font-extrabold text-[9px] bg-blue-100 text-[#0066FF] uppercase">
                            {usuarioActual.plan}
                          </span>
                        )}
                      </div>
                      <span className="text-[10px] text-[#8E8E93]">
                        {usuarioActual.rol}
                      </span>
                    </div>
                    <ChevronDown className="w-3.5 h-3.5 text-[#8E8E93]" />
                  </button>

                  {menuPerfilAbierto && (
                    <>
                      <div
                        className="fixed inset-0 z-40"
                        onClick={() => setMenuPerfilAbierto(false)}
                      />
                      <div className="absolute right-0 mt-2 w-72 bg-white rounded-2xl shadow-xl border border-[#E5E5EA] z-50 p-2 animate-in fade-in zoom-in-95 duration-100">
                        <div className="px-3 py-2 border-b border-[#F2F2F7]">
                          <p className="text-[11px] font-bold uppercase tracking-wider text-[#8E8E93]">
                            Simular Perfil de Usuario (RBAC)
                          </p>
                          <p className="text-xs text-[#1C1C1E] mt-0.5">
                            Selecciona un rol para interactuar con sus permisos:
                          </p>
                        </div>

                        <div className="mt-1 space-y-1">
                          {usuariosDisponibles.map((user) => {
                            const activo = user.id === usuarioActual.id;
                            return (
                              <button
                                key={user.id}
                                onClick={() => {
                                  cambiarUsuario(user.id);
                                  setMenuPerfilAbierto(false);
                                }}
                                className={`w-full flex items-start gap-2.5 p-2.5 rounded-xl text-left transition-colors ${
                                  activo
                                    ? "bg-blue-50/80 border border-blue-200"
                                    : "hover:bg-[#F2F2F7]"
                                }`}
                              >
                                <div className="mt-0.5">
                                  {user.rol === "ADMINISTRADOR" && (
                                    <Shield className="w-4 h-4 text-purple-600" />
                                  )}
                                  {user.rol === "TECNICO" && (
                                    <Briefcase className="w-4 h-4 text-blue-600" />
                                  )}
                                  {user.rol === "CLIENTE" && (
                                    <User className="w-4 h-4 text-emerald-600" />
                                  )}
                                </div>
                                <div className="flex-1 min-w-0">
                                  <div className="flex items-center justify-between">
                                    <span className="text-xs font-bold text-[#1C1C1E] truncate">
                                      {user.nombre}
                                    </span>
                                    {activo && (
                                      <UserCheck className="w-3.5 h-3.5 text-[#0066FF]" />
                                    )}
                                  </div>
                                  <p className="text-[11px] text-[#8E8E93] truncate">
                                    {user.correo}
                                  </p>
                                  <div className="mt-1">
                                    <RolBadge rol={user.rol} />
                                  </div>
                                </div>
                              </button>
                            );
                          })}
                        </div>

                        {/* Botón Cerrar Sesión */}
                        <div className="mt-2 pt-2 border-t border-[#F2F2F7]">
                          <button
                            type="button"
                            onClick={handleCerrarSesion}
                            className="w-full px-3 py-2 rounded-xl text-xs font-semibold text-rose-600 hover:bg-rose-50 transition-colors flex items-center justify-between"
                          >
                            <span>Cerrar Sesión</span>
                            <LogOut className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>
                    </>
                  )}
                </div>
              </>
            ) : (
              <div className="flex items-center gap-2">
                <Link
                  href="/login"
                  className="px-4 py-2 rounded-xl text-xs sm:text-sm font-semibold text-gray-700 hover:text-gray-900 hover:bg-gray-100 transition-colors flex items-center gap-1.5"
                >
                  <LogIn className="w-4 h-4" />
                  <span>Ingresar</span>
                </Link>

                <Link
                  href="/registro"
                  className="px-4 py-2 rounded-xl text-xs sm:text-sm font-bold text-white bg-[#0066FF] hover:bg-[#0052CC] shadow-sm transition-all flex items-center gap-1.5"
                >
                  <UserPlus className="w-4 h-4" />
                  <span className="hidden sm:inline">Registrar Empresa</span>
                  <span className="sm:hidden">Registro</span>
                </Link>
              </div>
            )}

            {/* Botón Hamburguesa Móvil */}
            <button
              onClick={() => setMenuMovilAbierto(!menuMovilAbierto)}
              className="md:hidden p-2 rounded-xl text-[#1C1C1E] hover:bg-[#F2F2F7]"
            >
              {menuMovilAbierto ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>
        </div>

        {/* Menú Móvil Desplegable */}
        {menuMovilAbierto && (
          <div className="md:hidden py-3 border-t border-[#E5E5EA] space-y-1">
            <Link
              href="/"
              onClick={() => setMenuMovilAbierto(false)}
              className={`block px-3 py-2 rounded-xl text-sm font-semibold ${
                pathname === "/" ? "bg-[#F2F2F7] text-[#0066FF]" : "text-[#1C1C1E]"
              }`}
            >
              🏠 Inicio
            </Link>
            <Link
              href="/#quienes-somos"
              onClick={() => setMenuMovilAbierto(false)}
              className="block px-3 py-2 rounded-xl text-sm font-semibold text-[#1C1C1E]"
            >
              🏢 Quiénes Somos
            </Link>
            <Link
              href="/planes"
              onClick={() => setMenuMovilAbierto(false)}
              className={`block px-3 py-2 rounded-xl text-sm font-semibold ${
                pathname.startsWith("/planes") ? "bg-[#F2F2F7] text-[#0066FF]" : "text-[#1C1C1E]"
              }`}
            >
              ⭐ Planes de Soporte & SLA
            </Link>

            {estaAutenticado ? (
              <>
                <Link
                  href="/tickets"
                  onClick={() => setMenuMovilAbierto(false)}
                  className={`block px-3 py-2 rounded-xl text-sm font-semibold ${
                    pathname === "/tickets" ? "bg-[#F2F2F7] text-[#0066FF]" : "text-[#1C1C1E]"
                  }`}
                >
                  📋 Bandeja de Tickets
                </Link>
                <Link
                  href="/tickets?view=wallet"
                  onClick={() => setMenuMovilAbierto(false)}
                  className="block px-3 py-2 rounded-xl text-sm font-semibold text-[#1C1C1E]"
                >
                  ✈️ Billetera de Pases QR
                </Link>
                <Link
                  href="/transacciones"
                  onClick={() => setMenuMovilAbierto(false)}
                  className={`block px-3 py-2 rounded-xl text-sm font-semibold ${
                    pathname === "/transacciones" ? "bg-[#F2F2F7] text-[#0066FF]" : "text-[#1C1C1E]"
                  }`}
                >
                  💳 Historial de Pagos Flow
                </Link>
                <div className="pt-2 border-t border-gray-100">
                  <button
                    onClick={() => {
                      cerrarSesion();
                      setMenuMovilAbierto(false);
                      router.push("/login");
                    }}
                    className="w-full text-left px-3 py-2 rounded-xl text-sm font-bold text-rose-600 hover:bg-rose-50"
                  >
                    🚪 Cerrar Sesión
                  </button>
                </div>
              </>
            ) : (
              <div className="pt-2 border-t border-gray-100 flex flex-col gap-2">
                <Link
                  href="/login"
                  onClick={() => setMenuMovilAbierto(false)}
                  className="block px-3 py-2 rounded-xl text-sm font-semibold text-gray-800 bg-gray-100 text-center"
                >
                  Ingresar al Portal
                </Link>
                <Link
                  href="/registro"
                  onClick={() => setMenuMovilAbierto(false)}
                  className="block px-3 py-2 rounded-xl text-sm font-bold text-white bg-[#0066FF] text-center"
                >
                  Registrar mi Empresa
                </Link>
              </div>
            )}
          </div>
        )}
      </Container>
    </header>
  );
}
