# Guía de Despliegue a Producción - GSM Web (Next.js)

Este proyecto está construido con **Next.js 16 (App Router)**, **TypeScript**, **Tailwind CSS v4** y conectado en tiempo real a **Google Cloud Firestore**, **Appwrite Storage** y la pasarela de pagos **Flow Chile (Webpay Plus)**.

---

## 1. Variables de Entorno Requeridas

Copia o configura estas variables en tu panel de despliegue (Vercel, Netlify, Firebase App Hosting, Railway, etc.):

```env
# Google Cloud Firestore (Base de Datos en Tiempo Real)
NEXT_PUBLIC_FIREBASE_API_KEY=AIzaSyBPiF8n3aflHGnm8aDtQOUM0r95DO57EKk
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=gsm-app-9a805.firebaseapp.com
NEXT_PUBLIC_FIREBASE_PROJECT_ID=gsm-app-9a805
NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET=gsm-app-9a805.firebasestorage.app
NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=459636129781
NEXT_PUBLIC_FIREBASE_APP_ID=1:459636129781:web:9b72db44d3067454026abc
NEXT_PUBLIC_FIREBASE_MEASUREMENT_ID=G-JL3KGYJ77G

# Appwrite Storage (Almacenamiento Seguro de Evidencias)
NEXT_PUBLIC_APPWRITE_ENDPOINT=https://nyc.cloud.appwrite.io/v1
NEXT_PUBLIC_APPWRITE_PROJECT_ID=6ae91595000d83e34686
NEXT_PUBLIC_APPWRITE_BUCKET_ID=6ae915ad002bdc1515ec

# Pasarela de Pagos Flow Chile (Webpay Plus / Tarjetas)
FLOW_API_KEY=tu_flow_api_key_aqui
FLOW_SECRET_KEY=tu_flow_secret_key_aqui
FLOW_ENVIRONMENT=sandbox # o 'production' al pasar a comercial
NEXT_PUBLIC_FLOW_ENVIRONMENT=sandbox
NEXT_PUBLIC_APP_URL=https://tu-dominio.com # En local: http://localhost:3000
```

> **Nota sobre Flow:** Si `FLOW_API_KEY` o `FLOW_SECRET_KEY` no están presentes o se encuentran en modo de prueba, el sistema activa automáticamente el **Simulador Sandbox interactivo**, permitiendo probar aprobaciones y rechazos de tarjetas bancarias sin fallar.

---

## 2. Comandos de Compilación y Ejecución

- **Modo Desarrollo:**
  ```bash
  cd web
  pnpm dev
  ```
- **Compilación de Producción:**
  ```bash
  cd web
  pnpm build
  ```
- **Iniciar Servidor de Producción:**
  ```bash
  cd web
  pnpm start
  ```

---

## 3. Despliegue en Vercel (Recomendado)

1. Conecta tu repositorio de GitHub a [Vercel](https://vercel.com).
2. Selecciona el directorio raíz del proyecto como `web`.
3. Framework Preset: **Next.js**.
4. Pega las variables de entorno de la sección 1.
5. Haz clic en **Deploy**.

---

## 4. Arquitectura y Rutas Clave

- **`/`**: Bandeja de Tickets en tiempo real con filtros SLA y Billetera de Pases de Avión QR.
- **`/nuevo`**: Creación guiada de incidencias con categorizador y evidencias en Appwrite.
- **`/tickets/[id]`**: Ficha técnica del ticket, SLA countdown, visor de imágenes y resolución.
- **`/tickets/[id]/chat`**: Mensajería en vivo sincronizada con Firestore y la App Android.
- **`/planes`**: Catálogo comercial de Planes GSM (PLUS, PRO, MAX) con Webpay Flow.
- **`/planes/retorno`**: Verificación criptográfica HMAC-SHA256 y emisión de voucher.
- **`/transacciones`**: Historial auditable de órdenes de compra, IDs de Flow y comprobantes.
- **`/api/flow/*`**: Endpoints seguros Server-Side para creación, validación y webhooks de Flow.
