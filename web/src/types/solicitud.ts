import { RolUsuario } from "./usuario";

export type CategoriaSolicitud =
  | "INCIDENTE_TECNICO"
  | "DESARROLLO_WEB"
  | "MANTENIMIENTO_WEB"
  | "HOSTING"
  | "SOPORTE_CORREO"
  | "MARKETING_DIGITAL"
  | "SOLICITUD_CAMBIO"
  | "CONSULTA"
  | "ACCESO_CONFIGURACION"
  | "OTRO";

export type EstadoSolicitud =
  | "RECIBIDA"
  | "EN_ASIGNACION"
  | "ASIGNADA"
  | "EN_PROCESO"
  | "PENDIENTE_INFORMACION"
  | "RESUELTA"
  | "CERRADA"
  | "CANCELADA";

export type PrioridadSolicitud = "BAJA" | "MEDIA" | "ALTA" | "URGENTE";

export interface Evidencia {
  id: string;
  nombreArchivo: string;
  tipoArchivo: string;
  tamanoBytes: number;
  uriRemota: string;
  fechaIncorporacion: number;
  descripcion?: string;
}

export interface MensajeHistorial {
  id: string;
  solicitudId: number;
  autorId: number;
  autorNombre: string;
  autorRol: RolUsuario;
  mensaje: string;
  fecha: number;
  esInterno: boolean;
  evidenciaAdjunta?: string;
}

export interface Solicitud {
  id: number;
  codigoSeguimiento: string; // Ej: "SOL-202611-001"
  titulo: string;
  descripcion: string;
  categoria: CategoriaSolicitud;
  clienteId: number;
  clienteNombre?: string;
  tecnicoId: number | null;
  tecnicoNombre?: string;
  estado: EstadoSolicitud;
  prioridad: PrioridadSolicitud;
  servicioRelacionado: string | null;
  activoRelacionado: string | null;
  resolucion?: string | null;
  motivoCancelacion: string | null;
  motivoReapertura?: string | null;
  conformidadCliente: boolean | null;
  fechaLimite: number | null;
  fechaCreacion: number;
  fechaActualizacion: number;
  fechaResolucion?: number | null;
  fechaCierre?: number | null;
  evidencias?: Evidencia[];
}

export const CATEGORIAS_INFO: Record<
  CategoriaSolicitud,
  { label: string; emoji: string; descripcion: string; color: string }
> = {
  INCIDENTE_TECNICO: {
    label: "Incidente Técnico",
    emoji: "⚠️",
    descripcion: "Fallo crítico en sistema, corte de servicio o error inesperado",
    color: "bg-red-50 text-red-700 border-red-200",
  },
  DESARROLLO_WEB: {
    label: "Desarrollo Web",
    emoji: "💻",
    descripcion: "Nuevas funcionalidades, desarrollo frontend/backend o integraciones",
    color: "bg-blue-50 text-blue-700 border-blue-200",
  },
  MANTENIMIENTO_WEB: {
    label: "Mantenimiento Web",
    emoji: "🛠️",
    descripcion: "Actualizaciones de librerías, correcciones menores y optimizaciones",
    color: "bg-indigo-50 text-indigo-700 border-indigo-200",
  },
  HOSTING: {
    label: "Servidores & Hosting",
    emoji: "☁️",
    descripcion: "Problemas de VPS, certificados SSL, caídas de servidor o DNS",
    color: "bg-cyan-50 text-cyan-700 border-cyan-200",
  },
  SOPORTE_CORREO: {
    label: "Soporte de Correo",
    emoji: "✉️",
    descripcion: "Configuración de cuentas corporativas, SMTP, IMAP o SPAM",
    color: "bg-purple-50 text-purple-700 border-purple-200",
  },
  MARKETING_DIGITAL: {
    label: "Marketing Digital",
    emoji: "📈",
    descripcion: "Campañas, analítica web, píxeles de seguimiento y SEO",
    color: "bg-pink-50 text-pink-700 border-pink-200",
  },
  SOLICITUD_CAMBIO: {
    label: "Solicitud de Cambio",
    emoji: "🔄",
    descripcion: "Modificaciones a requerimientos aprobados o alcance de proyecto",
    color: "bg-amber-50 text-amber-700 border-amber-200",
  },
  CONSULTA: {
    label: "Consulta General",
    emoji: "❓",
    descripcion: "Dudas técnicas, asesoría de arquitectura o información comercial",
    color: "bg-emerald-50 text-emerald-700 border-emerald-200",
  },
  ACCESO_CONFIGURACION: {
    label: "Acceso y Configuración",
    emoji: "🔑",
    descripcion: "Creación de credenciales, roles, permisos y llaves API",
    color: "bg-teal-50 text-teal-700 border-teal-200",
  },
  OTRO: {
    label: "Otro Requerimiento",
    emoji: "📋",
    descripcion: "Cualquier otra solicitud que no encaje en las categorías anteriores",
    color: "bg-slate-50 text-slate-700 border-slate-200",
  },
};

export const ESTADOS_INFO: Record<
  EstadoSolicitud,
  { label: string; colorBg: string; colorText: string; borderColor: string; dotColor: string }
> = {
  RECIBIDA: {
    label: "Recibida",
    colorBg: "bg-blue-50",
    colorText: "text-blue-700",
    borderColor: "border-blue-200",
    dotColor: "bg-blue-500",
  },
  EN_ASIGNACION: {
    label: "En Asignación",
    colorBg: "bg-purple-50",
    colorText: "text-purple-700",
    borderColor: "border-purple-200",
    dotColor: "bg-purple-500",
  },
  ASIGNADA: {
    label: "Asignada",
    colorBg: "bg-sky-50",
    colorText: "text-sky-700",
    borderColor: "border-sky-200",
    dotColor: "bg-sky-500",
  },
  EN_PROCESO: {
    label: "En Proceso",
    colorBg: "bg-indigo-50",
    colorText: "text-indigo-700",
    borderColor: "border-indigo-200",
    dotColor: "bg-indigo-500",
  },
  PENDIENTE_INFORMACION: {
    label: "Pendiente Info",
    colorBg: "bg-amber-50",
    colorText: "text-amber-800",
    borderColor: "border-amber-200",
    dotColor: "bg-amber-500",
  },
  RESUELTA: {
    label: "Resuelta",
    colorBg: "bg-emerald-50",
    colorText: "text-emerald-700",
    borderColor: "border-emerald-200",
    dotColor: "bg-emerald-500",
  },
  CERRADA: {
    label: "Cerrada",
    colorBg: "bg-zinc-100",
    colorText: "text-zinc-700",
    borderColor: "border-zinc-300",
    dotColor: "bg-zinc-500",
  },
  CANCELADA: {
    label: "Cancelada",
    colorBg: "bg-rose-50",
    colorText: "text-rose-700",
    borderColor: "border-rose-200",
    dotColor: "bg-rose-500",
  },
};

export const PRIORIDADES_INFO: Record<
  PrioridadSolicitud,
  { label: string; slaHoras: number; slaTexto: string; badgeClass: string; iconEmoji: string }
> = {
  BAJA: {
    label: "Baja",
    slaHoras: 72,
    slaTexto: "SLA: 72 Horas",
    badgeClass: "bg-emerald-50 text-emerald-700 border-emerald-200",
    iconEmoji: "🟢",
  },
  MEDIA: {
    label: "Media",
    slaHoras: 48,
    slaTexto: "SLA: 48 Horas",
    badgeClass: "bg-blue-50 text-blue-700 border-blue-200",
    iconEmoji: "🟡",
  },
  ALTA: {
    label: "Alta",
    slaHoras: 24,
    slaTexto: "SLA: 24 Horas",
    badgeClass: "bg-orange-50 text-orange-700 border-orange-200",
    iconEmoji: "🟠",
  },
  URGENTE: {
    label: "Urgente",
    slaHoras: 4,
    slaTexto: "SLA: Inmediato (4h)",
    badgeClass: "bg-rose-50 text-rose-700 border-rose-200 animate-pulse",
    iconEmoji: "🔴",
  },
};
