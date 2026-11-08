import {
  collection,
  doc,
  setDoc,
  getDoc,
  getDocs,
  query,
  where,
} from "firebase/firestore";
import { db } from "@/config/firebase";
import { Usuario, USUARIOS_PRECONFIGURADOS } from "@/types/usuario";

const COLECCION_USUARIOS = "usuarios";
const LOCAL_STORAGE_USUARIOS = "gsm_usuarios_db_cache_v1";

export function obtenerUsuariosCache(): Usuario[] {
  if (typeof window === "undefined") return USUARIOS_PRECONFIGURADOS;
  try {
    const raw = localStorage.getItem(LOCAL_STORAGE_USUARIOS);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed) && parsed.length > 0) return parsed;
    }
  } catch (e) {
    console.warn("Aviso leyendo usuarios en caché:", e);
  }
  guardarUsuariosCache(USUARIOS_PRECONFIGURADOS);
  return USUARIOS_PRECONFIGURADOS;
}

export function guardarUsuariosCache(usuarios: Usuario[]) {
  if (typeof window === "undefined") return;
  try {
    localStorage.setItem(LOCAL_STORAGE_USUARIOS, JSON.stringify(usuarios));
  } catch (e) {
    console.warn("Aviso guardando usuarios en caché:", e);
  }
}

/**
 * Guarda o actualiza un usuario en Firestore y en la caché local.
 */
export async function guardarUsuarioEnFirestore(usuario: Usuario): Promise<void> {
  const lista = obtenerUsuariosCache();
  const idx = lista.findIndex((u) => u.id === usuario.id || u.correo.toLowerCase() === usuario.correo.toLowerCase());
  if (idx >= 0) {
    lista[idx] = { ...lista[idx], ...usuario };
  } else {
    lista.push(usuario);
  }
  guardarUsuariosCache(lista);

  try {
    const docRef = doc(db, COLECCION_USUARIOS, String(usuario.id));
    await setDoc(docRef, usuario, { merge: true });
  } catch (e) {
    console.warn("Usuario guardado en caché local (Firestore offline):", e);
  }
}

/**
 * Busca un usuario por su correo electrónico en caché o en Firestore.
 */
export async function buscarUsuarioPorCorreo(correo: string): Promise<Usuario | null> {
  const correoLimpio = correo.trim().toLowerCase();
  const enCache = obtenerUsuariosCache().find((u) => u.correo.toLowerCase() === correoLimpio);

  try {
    const q = query(collection(db, COLECCION_USUARIOS), where("correo", "==", correoLimpio));
    const snap = await getDocs(q);
    if (!snap.empty) {
      const data = snap.docs[0].data() as Usuario;
      return data;
    }
  } catch (e) {
    console.warn("Fallo consultando Firestore por correo:", e);
  }

  return enCache || null;
}

/**
 * Registra un nuevo cliente corporativo en el sistema.
 */
export async function registrarClienteEnFirestore(datos: {
  nombre: string;
  correo: string;
  empresa: string;
  telefono: string;
  password?: string;
  plan: "plus" | "pro" | "max";
}): Promise<Usuario> {
  const ahora = Date.now();
  const nuevoId = Math.floor(100 + Math.random() * 900);

  // NUEVO CLIENTE: NO TIENE SUSCRIPCIÓN ACTIVA AUTOMÁTICA
  // Se guarda con estadoSuscripcion: "PENDIENTE_PAGO" y planDeseado.
  // Solo se activará cuando Flow confirme el pago en Webpay.
  const nuevoUsuario: Usuario = {
    id: nuevoId,
    nombre: datos.nombre.trim(),
    correo: datos.correo.trim().toLowerCase(),
    rol: "CLIENTE",
    empresa: datos.empresa.trim(),
    telefono: datos.telefono.trim(),
    activo: true,
    plan: undefined, // Sin suscripción activa automática
    planDeseado: datos.plan,
    estadoSuscripcion: "PENDIENTE_PAGO",
    esUsuarioMock: false,
    password: datos.password || "gsm123456",
    fechaRegistro: ahora,
  };

  await guardarUsuarioEnFirestore(nuevoUsuario);
  return nuevoUsuario;
}

/**
 * Activa la suscripción de un cliente en Firestore cuando Flow aprueba el pago.
 */
export async function activarSuscripcionUsuarioEnFirestore(params: {
  clienteId?: number;
  clienteEmail?: string;
  planId: "plus" | "pro" | "max";
  flowOrder?: number | null;
  fechaPago?: number | null;
}): Promise<Usuario | null> {
  const lista = obtenerUsuariosCache();
  const ahora = Date.now();
  const idx = lista.findIndex(
    (u) =>
      (params.clienteId && u.id === params.clienteId) ||
      (params.clienteEmail && u.correo.toLowerCase() === params.clienteEmail.toLowerCase())
  );

  if (idx >= 0) {
    lista[idx] = {
      ...lista[idx],
      plan: params.planId,
      estadoSuscripcion: "ACTIVA",
      suscripcionFlowOrder: params.flowOrder ?? undefined,
      fechaActivacionPlan: params.fechaPago || ahora,
    };
    guardarUsuariosCache(lista);

    try {
      const docRef = doc(db, COLECCION_USUARIOS, String(lista[idx].id));
      await setDoc(
        docRef,
        {
          plan: params.planId,
          estadoSuscripcion: "ACTIVA",
          suscripcionFlowOrder: params.flowOrder ?? null,
          fechaActivacionPlan: params.fechaPago || ahora,
        },
        { merge: true }
      );
    } catch (e) {
      console.warn("Aviso activando plan en Firestore:", e);
    }

    return lista[idx];
  }
  return null;
}
