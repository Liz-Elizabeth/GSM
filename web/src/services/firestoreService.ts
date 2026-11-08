import {
  collection,
  doc,
  setDoc,
  getDoc,
  getDocs,
  updateDoc,
  onSnapshot,
  Unsubscribe,
  addDoc,
} from "firebase/firestore";
import { db } from "@/config/firebase";
import {
  Solicitud,
  MensajeHistorial,
  Evidencia,
  EstadoSolicitud,
  PrioridadSolicitud,
  PRIORIDADES_INFO,
} from "@/types/solicitud";
import { RolUsuario, TECNICOS_DISPONIBLES } from "@/types/usuario";

const COLECCION_SOLICITUDES = "solicitudes";
const LOCAL_STORAGE_TICKETS = "gsm_solicitudes_cache_v2";
const LOCAL_STORAGE_HISTORIAL_PREFIX = "gsm_historial_cache_";

// Helper robusto para parsear fechas de Firestore (Timestamp, Number o String)
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
  const parsed = Date.parse(String(val));
  if (!isNaN(parsed)) return parsed;
  return fallback;
}

// Tickets iniciales enriquecidos para cuando Firestore está en creación o sin conexión
export function obtenerTicketsInicialesDemo(): Solicitud[] {
  const ahora = Date.now();
  return [
    {
      id: 101,
      codigoSeguimiento: "SOL-202611-001",
      titulo: "Caída del certificado SSL en portal de pagos clientes",
      descripcion:
        "El portal de pagos de Innovatech muestra error 'NET::ERR_CERT_DATE_INVALID' desde las 08:30 AM. Los clientes no pueden concretar transacciones en línea.",
      categoria: "HOSTING",
      clienteId: 1,
      clienteNombre: "Camila Rojas",
      tecnicoId: 3,
      tecnicoNombre: "Diego Torres",
      estado: "EN_PROCESO",
      prioridad: "URGENTE",
      servicioRelacionado: "Plataforma eCommerce Web",
      activoRelacionado: "https://pagos.innovatech.cl",
      motivoCancelacion: null,
      conformidadCliente: null,
      fechaLimite: ahora + 4 * 3600 * 1000,
      fechaCreacion: ahora - 2 * 3600 * 1000,
      fechaActualizacion: ahora - 30 * 60 * 1000,
      evidencias: [
        {
          id: "ev-demo-ssl-01",
          nombreArchivo: "pantallazo_error_ssl.png",
          tipoArchivo: "image/png",
          tamanoBytes: 245000,
          uriRemota:
            "https://nyc.cloud.appwrite.io/v1/storage/buckets/6ae915ad002bdc1515ec/files/ev-demo-ssl-01/view?project=6ae91595000d83e34686",
          fechaIncorporacion: ahora - 2 * 3600 * 1000,
          descripcion: "Captura del navegador con advertencia SSL",
        },
      ],
    },
    {
      id: 102,
      codigoSeguimiento: "SOL-202611-002",
      titulo: "Integración de pasarela Transbank Webpay Plus",
      descripcion:
        "Requerimos añadir el nuevo flujo REST de Webpay Plus con soporte para cuotas y débito en el carrito de compras.",
      categoria: "DESARROLLO_WEB",
      clienteId: 1,
      clienteNombre: "Camila Rojas",
      tecnicoId: 4,
      tecnicoNombre: "Valentina Silva",
      estado: "ASIGNADA",
      prioridad: "ALTA",
      servicioRelacionado: "Checkout y Pasarela",
      activoRelacionado: "https://innovatech.cl/checkout",
      motivoCancelacion: null,
      conformidadCliente: null,
      fechaLimite: ahora + 24 * 3600 * 1000,
      fechaCreacion: ahora - 10 * 3600 * 1000,
      fechaActualizacion: ahora - 10 * 3600 * 1000,
      evidencias: [],
    },
    {
      id: 103,
      codigoSeguimiento: "SOL-202611-003",
      titulo: "Correos corporativos rebotan con código DMARC fail",
      descripcion:
        "Desde ayer, algunos correos enviados a clientes con dominio Gmail o Outlook rebotan con mensaje de falla en directiva SPF/DMARC.",
      categoria: "SOPORTE_CORREO",
      clienteId: 1,
      clienteNombre: "Camila Rojas",
      tecnicoId: null,
      tecnicoNombre: undefined,
      estado: "RECIBIDA",
      prioridad: "MEDIA",
      servicioRelacionado: "Google Workspace / DNS",
      activoRelacionado: "correo@empresa.cl",
      motivoCancelacion: null,
      conformidadCliente: null,
      fechaLimite: ahora + 48 * 3600 * 1000,
      fechaCreacion: ahora - 5 * 3600 * 1000,
      fechaActualizacion: ahora - 5 * 3600 * 1000,
      evidencias: [],
    },
    {
      id: 104,
      codigoSeguimiento: "SOL-202611-004",
      titulo: "Actualización de versión PHP 8.1 a 8.3 en entorno staging",
      descripcion:
        "Migración de staging para pruebas de compatibilidad previa a pase a producción.",
      categoria: "MANTENIMIENTO_WEB",
      clienteId: 1,
      clienteNombre: "Camila Rojas",
      tecnicoId: 3,
      tecnicoNombre: "Diego Torres",
      estado: "RESUELTA",
      prioridad: "BAJA",
      servicioRelacionado: "Servidor Staging",
      activoRelacionado: "https://staging.innovatech.cl",
      resolucion:
        "Se actualizó PHP-FPM a la versión 8.3.6 con módulos mbstring, curl y gd. Pruebas unitarias al 100%.",
      motivoCancelacion: null,
      conformidadCliente: null,
      fechaLimite: ahora - 5 * 3600 * 1000,
      fechaCreacion: ahora - 50 * 3600 * 1000,
      fechaActualizacion: ahora - 4 * 3600 * 1000,
      fechaResolucion: ahora - 4 * 3600 * 1000,
      evidencias: [],
    },
  ];
}

// Cargar caché local
export function obtenerCacheLocalTickets(): Solicitud[] {
  if (typeof window === "undefined") return obtenerTicketsInicialesDemo();
  try {
    const raw = localStorage.getItem(LOCAL_STORAGE_TICKETS);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed) && parsed.length > 0) return parsed;
    }
  } catch (e) {
    console.warn("Aviso leyendo caché local:", e);
  }
  const iniciales = obtenerTicketsInicialesDemo();
  guardarEnCacheLocal(iniciales);
  return iniciales;
}

function guardarEnCacheLocal(tickets: Solicitud[]) {
  if (typeof window === "undefined") return;
  try {
    localStorage.setItem(LOCAL_STORAGE_TICKETS, JSON.stringify(tickets));
  } catch (e) {
    console.warn("Aviso guardando en caché local:", e);
  }
}

/**
 * Escucha reactiva en tiempo real (onSnapshot) de todas las solicitudes.
 * Incluye fallback Offline-First transparente.
 */
export function suscribirSolicitudes(
  onData: (solicitudes: Solicitud[]) => void,
  onError?: (error: Error) => void
): Unsubscribe {
  // Primero emitir inmediatamente desde caché/demo para renderizado instantáneo
  const iniciales = obtenerCacheLocalTickets();
  onData(iniciales);

  const ref = collection(db, COLECCION_SOLICITUDES);

  // Escuchamos directamente la colección (sin orderBy para evitar omisiones de documentos)
  return onSnapshot(
    ref,
    (snapshot) => {
      if (snapshot.empty) {
        // Si Firestore está vacío, sembramos datos iniciales
        sembrarTicketsDemostracionSiVacio().catch(console.warn);
        onData(iniciales);
        return;
      }

      const tickets: Solicitud[] = [];
      snapshot.forEach((d) => {
        const data = d.data();
        tickets.push({
          id: Number(data.id || data.idLocal || 0),
          codigoSeguimiento: data.codigoSeguimiento || d.id,
          titulo: data.titulo || "Sin título",
          descripcion: data.descripcion || "",
          categoria: data.categoria || "OTRO",
          clienteId: Number(data.clienteId || 1),
          clienteNombre: data.clienteNombre || "Camila Rojas",
          tecnicoId: data.tecnicoId != null ? Number(data.tecnicoId) : null,
          tecnicoNombre:
            data.tecnicoNombre ||
            (data.tecnicoId ? obtenerNombreTecnico(data.tecnicoId) : undefined),
          estado: (data.estado || "RECIBIDA") as EstadoSolicitud,
          prioridad: (data.prioridad || "MEDIA") as PrioridadSolicitud,
          servicioRelacionado: data.servicioRelacionado || null,
          activoRelacionado: data.activoRelacionado || null,
          resolucion: data.resolucion || null,
          motivoCancelacion: data.motivoCancelacion || null,
          motivoReapertura: data.motivoReapertura || null,
          conformidadCliente: data.conformidadCliente ?? null,
          fechaLimite: data.fechaLimite ? parsearFechaEpoch(data.fechaLimite) : null,
          fechaCreacion: parsearFechaEpoch(data.fechaCreacion),
          fechaActualizacion: parsearFechaEpoch(data.fechaActualizacion),
          fechaResolucion: data.fechaResolucion ? parsearFechaEpoch(data.fechaResolucion) : null,
          fechaCierre: data.fechaCierre ? parsearFechaEpoch(data.fechaCierre) : null,
          evidencias: data.evidencias || [],
        });
      });

      // Ordenar por fecha de creación descendente
      tickets.sort((a, b) => b.fechaCreacion - a.fechaCreacion);

      guardarEnCacheLocal(tickets);
      onData(tickets);
    },
    (err) => {
      console.warn("Firestore onSnapshot en modo offline o permisos pendientes:", err.message);
      // Fallback: emitir los datos en caché para que la aplicación nunca se quede en blanco
      const fallback = obtenerCacheLocalTickets();
      onData(fallback);
      onError?.(err);
    }
  );
}

/**
 * Escucha en tiempo real de una única solicitud por su código de seguimiento.
 */
export function suscribirSolicitudPorCodigo(
  codigoSeguimiento: string,
  onData: (solicitud: Solicitud | null) => void,
  onError?: (error: Error) => void
): Unsubscribe {
  // Primero buscar en caché local
  const cached = obtenerCacheLocalTickets().find(
    (t) => t.codigoSeguimiento === codigoSeguimiento
  );
  if (cached) {
    onData(cached);
  }

  const docRef = doc(db, COLECCION_SOLICITUDES, codigoSeguimiento);

  return onSnapshot(
    docRef,
    (d) => {
      if (!d.exists()) {
        onData(cached || null);
        return;
      }
      const data = d.data();
      const ticket: Solicitud = {
        id: Number(data.id || data.idLocal || 0),
        codigoSeguimiento: data.codigoSeguimiento || d.id,
        titulo: data.titulo || "Sin título",
        descripcion: data.descripcion || "",
        categoria: data.categoria || "OTRO",
        clienteId: Number(data.clienteId || 1),
        clienteNombre: data.clienteNombre || "Camila Rojas",
        tecnicoId: data.tecnicoId != null ? Number(data.tecnicoId) : null,
        tecnicoNombre:
          data.tecnicoNombre ||
          (data.tecnicoId ? obtenerNombreTecnico(data.tecnicoId) : undefined),
        estado: (data.estado || "RECIBIDA") as EstadoSolicitud,
        prioridad: (data.prioridad || "MEDIA") as PrioridadSolicitud,
        servicioRelacionado: data.servicioRelacionado || null,
        activoRelacionado: data.activoRelacionado || null,
        resolucion: data.resolucion || null,
        motivoCancelacion: data.motivoCancelacion || null,
        motivoReapertura: data.motivoReapertura || null,
        conformidadCliente: data.conformidadCliente ?? null,
        fechaLimite: data.fechaLimite ? parsearFechaEpoch(data.fechaLimite) : null,
        fechaCreacion: parsearFechaEpoch(data.fechaCreacion),
        fechaActualizacion: parsearFechaEpoch(data.fechaActualizacion),
        fechaResolucion: data.fechaResolucion ? parsearFechaEpoch(data.fechaResolucion) : null,
        fechaCierre: data.fechaCierre ? parsearFechaEpoch(data.fechaCierre) : null,
        evidencias: data.evidencias || [],
      };
      onData(ticket);
    },
    (err) => {
      console.warn(`Firestore listener offline para ${codigoSeguimiento}:`, err.message);
      onData(cached || null);
      onError?.(err);
    }
  );
}

/**
 * Escucha reactiva en tiempo real del historial/chat de un ticket.
 */
export function suscribirHistorialTicket(
  codigoSeguimiento: string,
  onData: (mensajes: MensajeHistorial[]) => void,
  onError?: (error: Error) => void
): Unsubscribe {
  // Cargar de caché local si existe
  const cacheKey = `${LOCAL_STORAGE_HISTORIAL_PREFIX}${codigoSeguimiento}`;
  let mensajesLocales: MensajeHistorial[] = [];
  try {
    const raw = localStorage.getItem(cacheKey);
    if (raw) mensajesLocales = JSON.parse(raw);
  } catch (e) {
    console.warn("Aviso leyendo caché historial:", e);
  }

  if (mensajesLocales.length > 0) {
    onData(mensajesLocales);
  } else {
    // Mensaje inicial por defecto si no hay nada
    const ticket = obtenerCacheLocalTickets().find((t) => t.codigoSeguimiento === codigoSeguimiento);
    mensajesLocales = [
      {
        id: "msg-init-01",
        solicitudId: ticket?.id || 101,
        autorId: 1,
        autorNombre: ticket?.clienteNombre || "Camila Rojas",
        autorRol: "CLIENTE",
        mensaje: `Hola equipo de soporte. He generado el requerimiento "${ticket?.titulo || "Solicitud de Soporte"}". Quedo atenta a sus comentarios.`,
        fecha: ticket?.fechaCreacion || Date.now() - 3600000,
        esInterno: false,
      },
    ];
    onData(mensajesLocales);
  }

  const historialRef = collection(db, COLECCION_SOLICITUDES, codigoSeguimiento, "historial");

  return onSnapshot(
    historialRef,
    (snapshot) => {
      if (snapshot.empty) return;
      const mensajes: MensajeHistorial[] = [];
      snapshot.forEach((d) => {
        const data = d.data();
        mensajes.push({
          id: d.id,
          solicitudId: Number(data.solicitudId || 0),
          autorId: Number(data.autorId || 0),
          autorNombre: data.autorNombre || "Usuario",
          autorRol: (data.autorRol || "CLIENTE") as RolUsuario,
          mensaje: data.mensaje || "",
          fecha: parsearFechaEpoch(data.fecha),
          esInterno: Boolean(data.esInterno),
          evidenciaAdjunta: data.evidenciaAdjunta || undefined,
        });
      });

      mensajes.sort((a, b) => a.fecha - b.fecha);
      try {
        localStorage.setItem(cacheKey, JSON.stringify(mensajes));
      } catch {
        // Ignorar
      }
      onData(mensajes);
    },
    (err) => {
      console.warn(`Firestore historial offline para ${codigoSeguimiento}:`, err.message);
      onData(mensajesLocales);
      onError?.(err);
    }
  );
}

/**
 * Crea una nueva solicitud en Firestore y la guarda en la caché local.
 */
export async function crearSolicitudEnFirestore(
  datos: Omit<Solicitud, "id" | "codigoSeguimiento" | "fechaCreacion" | "fechaActualizacion">,
  codigoPersonalizado?: string
): Promise<Solicitud> {
  const ahora = Date.now();
  const idNumerico = Math.floor(1000 + Math.random() * 9000);
  const fechaStr = new Date(ahora).toISOString().slice(0, 7).replace("-", "");
  const consecutivo = Math.floor(100 + Math.random() * 900);
  const codigo = codigoPersonalizado || `SOL-${fechaStr}-${consecutivo}`;

  const horasSla = PRIORIDADES_INFO[datos.prioridad]?.slaHoras || 48;
  const fechaLimiteCalculada = datos.fechaLimite || ahora + horasSla * 3600 * 1000;

  const nuevaSolicitud: Solicitud = {
    ...datos,
    id: idNumerico,
    codigoSeguimiento: codigo,
    fechaLimite: fechaLimiteCalculada,
    fechaCreacion: ahora,
    fechaActualizacion: ahora,
    evidencias: datos.evidencias || [],
  };

  // Guardar inmediatamente en caché local
  const ticketsActuales = obtenerCacheLocalTickets();
  const actualizados = [nuevaSolicitud, ...ticketsActuales];
  guardarEnCacheLocal(actualizados);

  // Intentar guardar en Firestore (si la API de Firebase ya está habilitada)
  try {
    const docRef = doc(db, COLECCION_SOLICITUDES, codigo);
    await setDoc(docRef, {
      ...nuevaSolicitud,
      idLocal: idNumerico,
    });

    if (nuevaSolicitud.evidencias && nuevaSolicitud.evidencias.length > 0) {
      for (const ev of nuevaSolicitud.evidencias) {
        const evDocRef = doc(db, COLECCION_SOLICITUDES, codigo, "evidencias", ev.id);
        await setDoc(evDocRef, ev);
      }
    }

    const primerMensajeRef = collection(db, COLECCION_SOLICITUDES, codigo, "historial");
    await addDoc(primerMensajeRef, {
      solicitudId: idNumerico,
      autorId: datos.clienteId,
      autorNombre: datos.clienteNombre || "Cliente",
      autorRol: "CLIENTE",
      mensaje: `Solicitud creada: "${datos.titulo}"`,
      fecha: ahora,
      esInterno: false,
    });
  } catch (e) {
    console.warn("Aviso: Solicitud guardada localmente (esperando conexión remota Firestore):", e);
  }

  return nuevaSolicitud;
}

/**
 * Actualiza los campos de una solicitud.
 */
export async function actualizarSolicitud(
  codigoSeguimiento: string,
  cambios: Partial<Solicitud>
): Promise<void> {
  const tickets = obtenerCacheLocalTickets();
  const codigoLimpio = codigoSeguimiento.trim();
  const idx = tickets.findIndex(
    (t) => t.codigoSeguimiento.trim().toUpperCase() === codigoLimpio.toUpperCase()
  );
  if (idx !== -1) {
    tickets[idx] = { ...tickets[idx], ...cambios, fechaActualizacion: Date.now() };
    guardarEnCacheLocal(tickets);
  }

  try {
    const docRef = doc(db, COLECCION_SOLICITUDES, codigoLimpio);
    const payloadLimpio: Record<string, unknown> = {};
    for (const [k, v] of Object.entries(cambios)) {
      if (v !== undefined) {
        payloadLimpio[k] = v;
      }
    }
    payloadLimpio.fechaActualizacion = Date.now();
    await setDoc(docRef, payloadLimpio, { merge: true });
  } catch (e) {
    console.warn("Actualizado en caché local (Firestore pendiente):", e);
  }
}

/**
 * Envía un mensaje al chat.
 */
export async function enviarMensajeChat(
  codigoSeguimiento: string,
  mensaje: {
    solicitudId: number;
    autorId: number;
    autorNombre: string;
    autorRol: RolUsuario;
    mensaje: string;
    esInterno?: boolean;
    evidenciaAdjunta?: string;
  }
): Promise<void> {
  const cacheKey = `${LOCAL_STORAGE_HISTORIAL_PREFIX}${codigoSeguimiento}`;
  const ahora = Date.now();
  const nuevoMsg: MensajeHistorial = {
    id: `msg-${ahora}-${Math.random().toString(36).substring(2, 7)}`,
    solicitudId: mensaje.solicitudId,
    autorId: mensaje.autorId,
    autorNombre: mensaje.autorNombre,
    autorRol: mensaje.autorRol,
    mensaje: mensaje.mensaje.trim(),
    fecha: ahora,
    esInterno: Boolean(mensaje.esInterno),
    evidenciaAdjunta: mensaje.evidenciaAdjunta || undefined,
  };

  try {
    const raw = localStorage.getItem(cacheKey);
    const lista: MensajeHistorial[] = raw ? JSON.parse(raw) : [];
    lista.push(nuevoMsg);
    localStorage.setItem(cacheKey, JSON.stringify(lista));
  } catch {
    // Ignorar
  }

  try {
    const historialRef = collection(db, COLECCION_SOLICITUDES, codigoSeguimiento, "historial");
    await addDoc(historialRef, { ...nuevoMsg, evidenciaAdjunta: nuevoMsg.evidenciaAdjunta || null });
    const docRef = doc(db, COLECCION_SOLICITUDES, codigoSeguimiento);
    await updateDoc(docRef, { fechaActualizacion: ahora });
  } catch (e) {
    console.warn("Mensaje persistido localmente (Firestore pendiente):", e);
  }
}

/**
 * Asigna técnico.
 */
export async function asignarTecnicoATicket(
  codigoSeguimiento: string,
  tecnicoId: number | null,
  adminNombre: string
): Promise<void> {
  const tec = TECNICOS_DISPONIBLES.find((t) => t.id === tecnicoId);
  const nuevoEstado: EstadoSolicitud = tecnicoId ? "ASIGNADA" : "RECIBIDA";

  await actualizarSolicitud(codigoSeguimiento, {
    tecnicoId: tecnicoId,
    tecnicoNombre: tec ? tec.nombre : undefined,
    estado: nuevoEstado,
  });

  await enviarMensajeChat(codigoSeguimiento, {
    solicitudId: 0,
    autorId: 99,
    autorNombre: adminNombre,
    autorRol: "ADMINISTRADOR",
    mensaje: tec
      ? `Técnico ${tec.nombre} (${tec.especialidad}) ha sido asignado al ticket.`
      : "Se ha removido la asignación del técnico.",
    esInterno: false,
  });
}

/**
 * Cambia prioridad.
 */
export async function cambiarPrioridadTicket(
  codigoSeguimiento: string,
  nuevaPrioridad: PrioridadSolicitud,
  adminNombre: string
): Promise<void> {
  const info = PRIORIDADES_INFO[nuevaPrioridad];
  const ahora = Date.now();
  const limite = ahora + info.slaHoras * 3600 * 1000;

  await actualizarSolicitud(codigoSeguimiento, {
    prioridad: nuevaPrioridad,
    fechaLimite: limite,
  });

  await enviarMensajeChat(codigoSeguimiento, {
    solicitudId: 0,
    autorId: 99,
    autorNombre: adminNombre,
    autorRol: "ADMINISTRADOR",
    mensaje: `Prioridad actualizada a ${info.label}. ${info.slaTexto}.`,
    esInterno: false,
  });
}

/**
 * Cancela solicitud.
 */
export async function cancelarSolicitudEnFirestore(
  codigoSeguimiento: string,
  motivo: string,
  adminNombre: string
): Promise<void> {
  await actualizarSolicitud(codigoSeguimiento, {
    estado: "CANCELADA",
    motivoCancelacion: motivo.trim(),
  });

  await enviarMensajeChat(codigoSeguimiento, {
    solicitudId: 0,
    autorId: 99,
    autorNombre: adminNombre,
    autorRol: "ADMINISTRADOR",
    mensaje: `⚠️ Solicitud cancelada por ${adminNombre}. Motivo: ${motivo.trim()}`,
    esInterno: false,
  });
}

/**
 * Resuelve solicitud.
 */
export async function resolverSolicitudPorTecnico(
  codigoSeguimiento: string,
  resolucion: string,
  tecnicoNombre: string,
  tecnicoId: number
): Promise<void> {
  const ahora = Date.now();
  await actualizarSolicitud(codigoSeguimiento, {
    estado: "RESUELTA",
    resolucion: resolucion.trim(),
    fechaResolucion: ahora,
  });

  await enviarMensajeChat(codigoSeguimiento, {
    solicitudId: 0,
    autorId: tecnicoId,
    autorNombre: tecnicoNombre,
    autorRol: "TECNICO",
    mensaje: `✅ Solicitud resuelta: ${resolucion.trim()}`,
    esInterno: false,
  });
}

/**
 * Confirma conformidad del cliente.
 */
export async function confirmarConformidadCliente(
  codigoSeguimiento: string,
  clienteNombre: string,
  clienteId: number
): Promise<void> {
  const ahora = Date.now();
  await actualizarSolicitud(codigoSeguimiento, {
    estado: "CERRADA",
    conformidadCliente: true,
    fechaCierre: ahora,
  });

  await enviarMensajeChat(codigoSeguimiento, {
    solicitudId: 0,
    autorId: clienteId,
    autorNombre: clienteNombre,
    autorRol: "CLIENTE",
    mensaje: `🌟 El cliente confirmó conformidad. Ticket cerrado.`,
    esInterno: false,
  });
}

/**
 * Agrega evidencia a solicitud.
 */
export async function agregarEvidenciaASolicitud(
  codigoSeguimiento: string,
  evidencia: Evidencia
): Promise<void> {
  const tickets = obtenerCacheLocalTickets();
  const ticket = tickets.find((t) => t.codigoSeguimiento === codigoSeguimiento);
  if (ticket) {
    const evs = ticket.evidencias || [];
    evs.push(evidencia);
    ticket.evidencias = evs;
    guardarEnCacheLocal(tickets);
  }

  try {
    const docRef = doc(db, COLECCION_SOLICITUDES, codigoSeguimiento);
    const snap = await getDoc(docRef);
    if (snap.exists()) {
      const actual = snap.data().evidencias || [];
      actual.push(evidencia);
      await updateDoc(docRef, { evidencias: actual, fechaActualizacion: Date.now() });
    }
  } catch (e) {
    console.warn("Evidencia guardada localmente (Firestore pendiente):", e);
  }
}

function obtenerNombreTecnico(id: number): string {
  const tec = TECNICOS_DISPONIBLES.find((t) => t.id === id);
  return tec ? tec.nombre : `Técnico #${id}`;
}

export async function sembrarTicketsDemostracionSiVacio(): Promise<void> {
  try {
    const snap = await getDocs(collection(db, COLECCION_SOLICITUDES));
    if (!snap.empty) return;
    const iniciales = obtenerTicketsInicialesDemo();
    for (const t of iniciales) {
      const docRef = doc(db, COLECCION_SOLICITUDES, t.codigoSeguimiento);
      await setDoc(docRef, { ...t, idLocal: t.id });
    }
  } catch (e) {
    console.warn("Aviso sembrando tickets semilla en Firestore:", e);
  }
}
