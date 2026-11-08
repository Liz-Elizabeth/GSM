# GSM - Plataforma Web de Gestión de Solicitudes y Soporte Técnico

Aplicación Web de producción construida con **React 19**, **Next.js 16 (App Router)**, **TypeScript**, **Tailwind CSS**, **Firebase Cloud Firestore** y **Appwrite Storage**. Comparte al 100% la misma base de datos Firestore y el mismo almacenamiento de evidencias con la aplicación móvil Android de GSM.

---

## 🚀 Arquitectura y Tecnologías

- **Framework**: Next.js 16 (App Router) + React 19 + TypeScript.
- **Estilos y UI**: Tailwind CSS basado en **Apple Human Interface Guidelines (HIG)** y **Teamwork Desk** (Fondos `#F2F2F7`, Azul corporativo `#0066FF`, tarjetas `rounded-2xl` y `rounded-3xl`).
- **Base de Datos en Tiempo Real**: **Firebase Cloud Firestore** v10+ con listeners reactivos bidireccionales (`onSnapshot`).
  - *Project ID*: `gsm-app-9a805`
  - *Colección principal*: `solicitudes` (ID de documento = `codigoSeguimiento`, ej: `SOL-202611-001`)
  - *Subcolección de Chat*: `solicitudes/{codigoSeguimiento}/historial`
  - *Subcolección de Archivos*: `solicitudes/{codigoSeguimiento}/evidencias`
- **Almacenamiento de Archivos y Evidencias**: **Appwrite Storage** (SDK Oficial).
  - *Endpoint*: `https://nyc.cloud.appwrite.io/v1`
  - *Project ID*: `6ae91595000d83e34686`
  - *Bucket ID*: `6ae915ad002bdc1515ec`
- **Códigos QR Dinámicos**: `qrcode.react` (Renderizado SVG en alta definición para pases de abordaje).
- **Iconografía**: `lucide-react`.

---

## 👥 Control de Acceso Basado en Roles (RBAC)

En el encabezado superior puedes alternar de forma inmediata entre 3 perfiles precargados:

1. **Administrador**: `admin@gsm.cl` (Rol: `ADMINISTRADOR`, Nombre: "Administrador GSM")
   - Acceso a métricas globales, consola de criticidad (cuadrícula 2x2 de SLAs), asignación de técnicos, cancelación con motivo y notas internas.
2. **Técnico Especialista**: `diego@gsm.cl` (Rol: `TECNICO`, ID: 3, Nombre: "Diego Torres")
   - Acceso a resolución de tickets con notas técnicas, chat con notas internas privadas y visualización de SLAs.
3. **Cliente Corporativo**: `camila@empresa.cl` (Rol: `CLIENTE`, ID: 1, Nombre: "Camila Rojas", Empresa: "Innovatech SpA")
   - Creación de tickets, carga de evidencias a Appwrite, visualización de pase QR de abordaje, chat público con soporte y confirmación de conformidad de cierre.

---

## 📦 Estructura del Proyecto Web (`web/src`)

```
src/
├── app/
│   ├── layout.tsx                    # Layout raíz con AuthProvider y Header
│   ├── globals.css                   # Variables Apple HIG, animaciones Shimmer
│   ├── page.tsx                      # Bandeja de Tickets & Billetera de Pases de Avión
│   ├── nuevo/
│   │   └── page.tsx                  # Crear Nueva Solicitud con carga Appwrite
│   └── tickets/
│       ├── [id]/
│       │   └── page.tsx              # Ficha de Detalle con control segmentado iOS
│       └── [id]/chat/
│           └── page.tsx              # Chat reactivo en tiempo real estilo WhatsApp
├── config/
│   ├── firebase.ts                   # Cliente Firestore Singleton (gsm-app-9a805)
│   └── appwrite.ts                   # Cliente Appwrite Storage (nyc.cloud.appwrite.io)
├── context/
│   └── AuthContext.tsx               # Contexto de autenticación y simulador de roles
├── types/
│   ├── solicitud.ts                  # Interfaces Solicitud, Categoria, Estado, SLA
│   └── usuario.ts                    # Usuarios preconfigurados y técnicos
├── services/
│   ├── firestoreService.ts           # Listeners onSnapshot, CRUD y bitácora
│   └── appwriteService.ts            # Carga de evidencias (<15MB) a bucket
└── components/
    ├── common/
    │   ├── Badges.tsx                # Badges de Estado, Prioridad SLA, Categoría y Rol
    │   └── Skeletons.tsx             # Shimmer skeletons estilo iOS
    ├── layout/
    │   └── Header.tsx                # Barra superior, status live y selector de perfil
    ├── tickets/
    │   ├── FilterBar.tsx             # Buscador reactivo y DropdownMenu de filtros
    │   ├── TicketCard.tsx            # Tarjeta de ticket para vista en lista
    │   ├── BoardingPassCard.tsx      # Pase de abordar CLI ✈️ GSM con QR desplegable
    │   └── TicketDetailView.tsx      # Detalle con pestañas iOS y Consola Admin
    ├── chat/
    │   └── TicketChat.tsx            # Sala de chat en vivo con notas internas
    └── evidencias/
        ├── FileUploadDropzone.tsx    # Drag & drop para Appwrite Storage
        └── VisorEvidenciaModal.tsx   # Modal visor de imágenes y descarga de archivos
```

---

## 💻 Instrucciones para Ejecutar

### 1. Desarrollo local
```bash
cd web
pnpm dev
```
La aplicación abrirá en `http://localhost:3000`.

### 2. Compilar para Producción
```bash
cd web
pnpm build
pnpm start
```
