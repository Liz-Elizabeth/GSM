export type RolUsuario = "ADMINISTRADOR" | "TECNICO" | "CLIENTE";

export type EstadoSuscripcion = "ACTIVA" | "PENDIENTE_PAGO" | "INACTIVA" | "CANCELADA";

export interface Usuario {
  id: number;
  nombre: string;
  correo: string;
  rol: RolUsuario;
  empresa?: string;
  telefono?: string;
  activo: boolean;
  plan?: "plus" | "pro" | "max";
  planDeseado?: "plus" | "pro" | "max";
  estadoSuscripcion?: EstadoSuscripcion;
  esUsuarioMock?: boolean;
  suscripcionFlowOrder?: number;
  fechaActivacionPlan?: number;
  password?: string;
  fechaRegistro?: number;
}

export const USUARIOS_PRECONFIGURADOS: Usuario[] = [
  {
    id: 99,
    nombre: "Administrador GSM",
    correo: "admin@gsm.cl",
    rol: "ADMINISTRADOR",
    empresa: "GSM Tech Solutions",
    telefono: "+56 9 1111 2222",
    activo: true,
    esUsuarioMock: true,
  },
  {
    id: 3,
    nombre: "Diego Torres",
    correo: "diego@gsm.cl",
    rol: "TECNICO",
    empresa: "GSM Tech Soporte",
    telefono: "+56 9 3333 4444",
    activo: true,
    esUsuarioMock: true,
  },
  {
    id: 1,
    nombre: "Camila Rojas (Demo)",
    correo: "camila@empresa.cl",
    rol: "CLIENTE",
    empresa: "Innovatech SpA",
    telefono: "+56 9 5555 6666",
    activo: true,
    plan: "pro",
    estadoSuscripcion: "ACTIVA",
    esUsuarioMock: true,
    fechaActivacionPlan: Date.now() - 86400000 * 5,
  },
];

export const TECNICOS_DISPONIBLES: { id: number; nombre: string; correo: string; especialidad: string }[] = [
  { id: 3, nombre: "Diego Torres", correo: "diego@gsm.cl", especialidad: "Soporte e Infraestructura" },
  { id: 4, nombre: "Valentina Silva", correo: "valentina@gsm.cl", especialidad: "Desarrollo Web & Cloud" },
  { id: 5, nombre: "Carlos Ramos", correo: "carlos@gsm.cl", especialidad: "Seguridad & Redes" },
];
