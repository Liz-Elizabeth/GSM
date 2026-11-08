"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { Usuario, USUARIOS_PRECONFIGURADOS, RolUsuario } from "@/types/usuario";
import {
  obtenerUsuariosCache,
  guardarUsuarioEnFirestore,
  registrarClienteEnFirestore,
} from "@/services/usuarioService";

interface AuthContextType {
  usuarioActual: Usuario;
  estaAutenticado: boolean;
  iniciarSesion: (correo: string, password?: string) => Promise<{ success: boolean; error?: string }>;
  cerrarSesion: () => void;
  registrarCliente: (datos: {
    nombre: string;
    correo: string;
    empresa: string;
    telefono: string;
    password?: string;
    plan: "plus" | "pro" | "max";
  }) => Promise<{ success: boolean; error?: string; usuario?: Usuario }>;
  cambiarUsuario: (usuarioId: number) => void;
  cambiarRolDirecto: (rol: RolUsuario) => void;
  actualizarPlanUsuario: (nuevoPlan: "plus" | "pro" | "max", flowOrder?: number | null) => void;
  isAdmin: boolean;
  isTecnico: boolean;
  isCliente: boolean;
  usuariosDisponibles: Usuario[];
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const LOCAL_STORAGE_KEY = "gsm_usuario_activo_id";
const LOCAL_STORAGE_AUTH = "gsm_esta_autenticado";
const LOCAL_STORAGE_PLAN_PREFIX = "gsm_plan_usuario_";

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [listaUsuarios, setListaUsuarios] = useState<Usuario[]>(USUARIOS_PRECONFIGURADOS);
  const [usuarioActual, setUsuarioActual] = useState<Usuario>(USUARIOS_PRECONFIGURADOS[0]);
  const [estaAutenticado, setEstaAutenticado] = useState(true);
  const [cargado, setCargado] = useState(false);

  useEffect(() => {
    try {
      const usuariosLocales = obtenerUsuariosCache();
      setListaUsuarios(usuariosLocales);

      const authGuardado = localStorage.getItem(LOCAL_STORAGE_AUTH);
      if (authGuardado === "false") {
        setEstaAutenticado(false);
      }

      const guardadoId = localStorage.getItem(LOCAL_STORAGE_KEY);
      if (guardadoId) {
        const idNum = Number(guardadoId);
        const encontrado = usuariosLocales.find((u) => u.id === idNum);
        if (encontrado) {
          const planGuardado = localStorage.getItem(`${LOCAL_STORAGE_PLAN_PREFIX}${idNum}`) as
            | "plus"
            | "pro"
            | "max"
            | null;
          const estadoGuardado = localStorage.getItem(`gsm_estado_suscripcion_${idNum}`) as
            | "ACTIVA"
            | "PENDIENTE_PAGO"
            | "INACTIVA"
            | null;

          // Solo los usuarios mock o aquellos con pago de Flow confirmado tienen suscripción activa
          const esActiva =
            Boolean(encontrado.esUsuarioMock) ||
            estadoGuardado === "ACTIVA" ||
            encontrado.estadoSuscripcion === "ACTIVA";

          setUsuarioActual({
            ...encontrado,
            plan: esActiva ? (planGuardado || encontrado.plan) : undefined,
            estadoSuscripcion: esActiva
              ? "ACTIVA"
              : (estadoGuardado || encontrado.estadoSuscripcion || "PENDIENTE_PAGO"),
          });
        }
      }
    } catch {
      // Ignorar errores de localStorage en SSR
    }
    setCargado(true);
  }, []);

  const iniciarSesion = async (
    correo: string,
    password?: string
  ): Promise<{ success: boolean; error?: string }> => {
    const correoLimpio = correo.trim().toLowerCase();
    const encontrado = listaUsuarios.find((u) => u.correo.toLowerCase() === correoLimpio);

    if (!encontrado) {
      return {
        success: false,
        error: "No existe ninguna cuenta registrada con este correo electrónico.",
      };
    }

    if (password && encontrado.password && encontrado.password !== password) {
      return {
        success: false,
        error: "Contraseña incorrecta. Por favor verifica tus credenciales.",
      };
    }

    setUsuarioActual(encontrado);
    setEstaAutenticado(true);

    try {
      localStorage.setItem(LOCAL_STORAGE_KEY, String(encontrado.id));
      localStorage.setItem(LOCAL_STORAGE_AUTH, "true");
    } catch {
      // Ignorar
    }

    return { success: true };
  };

  const cerrarSesion = () => {
    setEstaAutenticado(false);
    try {
      localStorage.setItem(LOCAL_STORAGE_AUTH, "false");
    } catch {
      // Ignorar
    }
  };

  const registrarCliente = async (datos: {
    nombre: string;
    correo: string;
    empresa: string;
    telefono: string;
    password?: string;
    plan: "plus" | "pro" | "max";
  }): Promise<{ success: boolean; error?: string; usuario?: Usuario }> => {
    const correoLimpio = datos.correo.trim().toLowerCase();
    const existe = listaUsuarios.find((u) => u.correo.toLowerCase() === correoLimpio);

    if (existe) {
      return {
        success: false,
        error: "Ya existe una cuenta registrada con este correo electrónico. Inicia sesión.",
      };
    }

    try {
      const nuevoUsuario = await registrarClienteEnFirestore(datos);
      setListaUsuarios((prev) => [nuevoUsuario, ...prev]);
      setUsuarioActual(nuevoUsuario);
      setEstaAutenticado(true);

      try {
        localStorage.setItem(LOCAL_STORAGE_KEY, String(nuevoUsuario.id));
        localStorage.setItem(LOCAL_STORAGE_AUTH, "true");
      } catch {
        // Ignorar
      }

      return { success: true, usuario: nuevoUsuario };
    } catch (err) {
      console.error("Error registrando nuevo cliente:", err);
      return {
        success: false,
        error: "Ocurrió un error al registrar en Firestore. Inténtalo nuevamente.",
      };
    }
  };

  const cambiarUsuario = (usuarioId: number) => {
    const seleccionado = listaUsuarios.find((u) => u.id === usuarioId);
    if (seleccionado) {
      let planFinal = seleccionado.plan;
      try {
        const guardado = localStorage.getItem(`${LOCAL_STORAGE_PLAN_PREFIX}${usuarioId}`) as
          | "plus"
          | "pro"
          | "max"
          | null;
        if (guardado) planFinal = guardado;
      } catch {
        // Ignorar
      }
      setUsuarioActual({ ...seleccionado, plan: planFinal });
      setEstaAutenticado(true);
      try {
        localStorage.setItem(LOCAL_STORAGE_KEY, String(usuarioId));
        localStorage.setItem(LOCAL_STORAGE_AUTH, "true");
      } catch {
        // Ignorar
      }
    }
  };

  const actualizarPlanUsuario = (nuevoPlan: "plus" | "pro" | "max") => {
    setUsuarioActual((prev) => {
      const actualizado = { ...prev, plan: nuevoPlan };
      try {
        localStorage.setItem(`${LOCAL_STORAGE_PLAN_PREFIX}${prev.id}`, nuevoPlan);
      } catch {
        // Ignorar
      }
      return actualizado;
    });
  };

  const cambiarRolDirecto = (rol: RolUsuario) => {
    const seleccionado = listaUsuarios.find((u) => u.rol === rol);
    if (seleccionado) {
      cambiarUsuario(seleccionado.id);
    }
  };

  const isAdmin = estaAutenticado && usuarioActual.rol === "ADMINISTRADOR";
  const isTecnico = estaAutenticado && usuarioActual.rol === "TECNICO";
  const isCliente = estaAutenticado && usuarioActual.rol === "CLIENTE";

  if (!cargado) {
    return null;
  }

  return (
    <AuthContext.Provider
      value={{
        usuarioActual,
        estaAutenticado,
        iniciarSesion,
        cerrarSesion,
        registrarCliente,
        cambiarUsuario,
        cambiarRolDirecto,
        actualizarPlanUsuario,
        isAdmin,
        isTecnico,
        isCliente,
        usuariosDisponibles: listaUsuarios,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth debe ser utilizado dentro de un AuthProvider");
  }
  return context;
}
