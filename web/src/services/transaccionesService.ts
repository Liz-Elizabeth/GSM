import {
  collection,
  doc,
  setDoc,
  getDoc,
  updateDoc,
  onSnapshot,
  Unsubscribe,
} from "firebase/firestore";
import { db } from "@/config/firebase";
import { TransaccionPago } from "@/types/plan";

const COLECCION_TRANSACCIONES = "transacciones";
const LOCAL_STORAGE_TRANSACCIONES = "gsm_transacciones_cache_v1";

function parsearFechaEpoch(val: unknown, fallback: number = Date.now()): number {
  if (!val) return fallback;
  if (typeof val === "number") return val;
  if (typeof (val as { toMillis?: () => number }).toMillis === "function") {
    return (val as { toMillis: () => number }).toMillis();
  }
  if (typeof (val as { toDate?: () => Date }).toDate === "function") {
    return (val as { toDate: () => Date }).toDate().getTime();
  }
  if (typeof val === "object" && val !== null && "seconds" in val) {
    return Number((val as { seconds: number }).seconds) * 1000;
  }
  const num = Number(val);
  if (!isNaN(num) && num > 0) return num;
  return fallback;
}

export function obtenerTransaccionesDemo(): TransaccionPago[] {
  const ahora = Date.now();
  return [
    {
      id: "FLOW-TX-DEMO-001",
      commerceOrder: "GSM-PRO-849201",
      flowOrder: 994821,
      token: "DEMO_TOKEN_849201",
      planId: "pro",
      planNombre: "Plan PRO (Tier 2)",
      montoClp: 79900,
      esAnual: false,
      estado: "PAGADA",
      clienteId: 1,
      clienteNombre: "Camila Rojas",
      clienteEmail: "camila@empresa.cl",
      medioPago: "Webpay Plus Débito",
      fechaCreacion: ahora - 7 * 24 * 3600 * 1000,
      fechaPago: ahora - 7 * 24 * 3600 * 1000 + 45000,
      urlRetorno: "https://gsm-soporte.cl/planes/retorno",
    },
    {
      id: "FLOW-TX-DEMO-002",
      commerceOrder: "GSM-PLUS-312940",
      flowOrder: 991204,
      token: "DEMO_TOKEN_312940",
      planId: "plus",
      planNombre: "Plan PLUS (Tier 1)",
      montoClp: 287040, // Anual con 20%
      esAnual: true,
      estado: "PAGADA",
      clienteId: 1,
      clienteNombre: "Camila Rojas",
      clienteEmail: "camila@empresa.cl",
      medioPago: "Webpay Plus Crédito (12 cuotas)",
      fechaCreacion: ahora - 38 * 24 * 3600 * 1000,
      fechaPago: ahora - 38 * 24 * 3600 * 1000 + 60000,
      urlRetorno: "https://gsm-soporte.cl/planes/retorno",
    },
  ];
}

export function obtenerCacheLocalTransacciones(): TransaccionPago[] {
  if (typeof window === "undefined") return obtenerTransaccionesDemo();
  try {
    const raw = localStorage.getItem(LOCAL_STORAGE_TRANSACCIONES);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed) && parsed.length > 0) return parsed;
    }
  } catch (e) {
    console.warn("Aviso leyendo transacciones en caché:", e);
  }
  const demo = obtenerTransaccionesDemo();
  guardarEnCacheLocalTransacciones(demo);
  return demo;
}

export function guardarEnCacheLocalTransacciones(transacciones: TransaccionPago[]) {
  if (typeof window === "undefined") return;
  try {
    localStorage.setItem(LOCAL_STORAGE_TRANSACCIONES, JSON.stringify(transacciones));
  } catch (e) {
    console.warn("Aviso guardando transacciones en caché:", e);
  }
}

/**
 * Guarda o actualiza una transacción en Firestore (patrón dual offline-first).
 */
export async function guardarTransaccion(tx: TransaccionPago): Promise<void> {
  const lista = obtenerCacheLocalTransacciones();
  const idx = lista.findIndex((t) => t.token === tx.token || t.commerceOrder === tx.commerceOrder);
  if (idx >= 0) {
    lista[idx] = { ...lista[idx], ...tx };
  } else {
    lista.unshift(tx);
  }
  guardarEnCacheLocalTransacciones(lista);

  try {
    const docId = tx.token || tx.commerceOrder;
    const docRef = doc(db, COLECCION_TRANSACCIONES, docId);
    await setDoc(docRef, tx, { merge: true });
  } catch (e) {
    console.warn("Transacción guardada en caché local (Firestore offline):", e);
  }
}

/**
 * Actualiza el estado de una transacción por token o orden.
 */
export async function actualizarEstadoTransaccion(
  tokenOOrden: string,
  datos: Partial<TransaccionPago>
): Promise<void> {
  const lista = obtenerCacheLocalTransacciones();
  const idx = lista.findIndex(
    (t) => t.token === tokenOOrden || t.commerceOrder === tokenOOrden || t.id === tokenOOrden
  );
  if (idx >= 0) {
    lista[idx] = { ...lista[idx], ...datos };
    guardarEnCacheLocalTransacciones(lista);
  }

  try {
    const docRef = doc(db, COLECCION_TRANSACCIONES, tokenOOrden);
    await updateDoc(docRef, datos);
  } catch (e) {
    console.warn("Actualización guardada en caché local:", e);
  }
}

/**
 * Consulta una transacción por token desde Firestore o caché.
 */
export async function obtenerTransaccionPorToken(token: string): Promise<TransaccionPago | null> {
  const enCache = obtenerCacheLocalTransacciones().find((t) => t.token === token);
  try {
    const docRef = doc(db, COLECCION_TRANSACCIONES, token);
    const snap = await getDoc(docRef);
    if (snap.exists()) {
      const data = snap.data();
      return {
        id: data.id || snap.id,
        commerceOrder: data.commerceOrder || "",
        flowOrder: data.flowOrder || null,
        token: data.token || snap.id,
        planId: data.planId || "pro",
        planNombre: data.planNombre || "Plan PRO",
        montoClp: Number(data.montoClp || 0),
        esAnual: Boolean(data.esAnual),
        estado: (data.estado || "PENDIENTE") as TransaccionPago["estado"],
        clienteId: Number(data.clienteId || 1),
        clienteNombre: data.clienteNombre || "Cliente GSM",
        clienteEmail: data.clienteEmail || "",
        medioPago: data.medioPago || null,
        fechaCreacion: parsearFechaEpoch(data.fechaCreacion),
        fechaPago: data.fechaPago ? parsearFechaEpoch(data.fechaPago) : null,
        urlRetorno: data.urlRetorno,
        rawResponse: data.rawResponse || null,
      };
    }
  } catch (e) {
    console.warn("Fallo al consultar Firestore por token, usando caché:", e);
  }
  return enCache || null;
}

/**
 * Suscripción en tiempo real a todas las transacciones de pago.
 */
export function suscribirTransacciones(
  onData: (transacciones: TransaccionPago[]) => void,
  onError?: (error: Error) => void
): Unsubscribe {
  const cached = obtenerCacheLocalTransacciones();
  onData(cached);

  const ref = collection(db, COLECCION_TRANSACCIONES);
  return onSnapshot(
    ref,
    (snapshot) => {
      if (snapshot.empty) {
        onData(cached);
        return;
      }
      const transacciones: TransaccionPago[] = [];
      snapshot.forEach((d) => {
        const data = d.data();
        transacciones.push({
          id: data.id || d.id,
          commerceOrder: data.commerceOrder || "",
          flowOrder: data.flowOrder || null,
          token: data.token || d.id,
          planId: data.planId || "pro",
          planNombre: data.planNombre || "Plan PRO",
          montoClp: Number(data.montoClp || 0),
          esAnual: Boolean(data.esAnual),
          estado: (data.estado || "PENDIENTE") as TransaccionPago["estado"],
          clienteId: Number(data.clienteId || 1),
          clienteNombre: data.clienteNombre || "Cliente GSM",
          clienteEmail: data.clienteEmail || "",
          medioPago: data.medioPago || null,
          fechaCreacion: parsearFechaEpoch(data.fechaCreacion),
          fechaPago: data.fechaPago ? parsearFechaEpoch(data.fechaPago) : null,
          urlRetorno: data.urlRetorno,
          rawResponse: data.rawResponse || null,
        });
      });

      transacciones.sort((a, b) => b.fechaCreacion - a.fechaCreacion);
      guardarEnCacheLocalTransacciones(transacciones);
      onData(transacciones);
    },
    (err) => {
      console.warn("Firestore transacciones offline listener:", err.message);
      onData(cached);
      onError?.(err);
    }
  );
}
