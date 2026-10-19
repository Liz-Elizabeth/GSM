# 🧠 Contexto de Continuidad del Proyecto GSM (Guía para Retomar Mañana)

> **Propósito de este archivo:** Si abres una nueva sesión mañana, solo dile al asistente: *"Lee `CONTEXTO_PROYECTO.md` y sigamos donde quedamos"*. Aquí está todo el estado técnico, las decisiones de diseño tomadas y lo que falta por construir.

---

## 👥 1. Datos Generales del Proyecto y Equipo
* **Proyecto:** GSM - Gestión Móvil y Seguimiento de Solicitudes de Clientes.
* **Ramo:** DSY1105 - Desarrollo de Aplicaciones Móviles (Duoc UC, CITT Puente Alto).
* **Autores:** Elizabeth Reyes y Juan Varas.
* **Documento de Caso:** `Caso_DSY1105_005D_Gestión móvil y seguimiento de solicitudes de clientes.pdf` (40 criterios de éxito).
* **Defensa del Proyecto:** Viernes 23.

---

## 🎯 2. Reglas de Estilo y Trabajo Acordadas (¡MUY IMPORTANTE!)
1. **Código Simple, Pedagógico y Defendible (Nivel Estudiante de 1er Año):**
   * Evitar sobre-ingeniería y operadores reactivos complejos (`combine` anidados, `flatMapLatest`, `Channel` complejos).
   * En los **ViewModels** se usa `var ... by mutableStateOf(...)` con `private set`, corrutinas simples con `viewModelScope.launch { ... }`, condicionales `if (resultado.isSuccess) ... else ...` y cancelación de búsquedas previas con `Job?.cancel()`.
   * Todo el código debe ser tan claro que cualquier estudiante de primer año pueda leerlo de arriba a abajo y explicarlo frente al profesor en la defensa.
2. **Inyección de Dependencias Manual:**
   * Sin Hilt ni Koin. Se usa un único `GsmViewModelFactory(gsmRepo, usuarioRepo)` instanciado en `MainActivity`.
3. **Navegación Seleccionada:**
   * Usar **Navigation Compose oficial (`NavHost` y rutas)** agregando `implementation("androidx.navigation:navigation-compose:2.8.5")` en `app/build.gradle.kts`.
4. **Modo de Trabajo Preferido por el Usuario:**
   * Mostrar explicaciones claras en recuadros por terminal. Preguntar o respetar cuando el usuario pide ver primero el código en la terminal para aplicarlo a mano vs. aplicarlo directamente con herramientas.

---

## ✅ 3. Lo que YA ESTÁ 100% Hecho y Aplicado en Disco

### Fase 1: Persistencia Room (`data/local/`) — ✅ 100%
* **5 Entidades (`entity/`):** `UsuarioEntity`, `SolicitudEntity`, `HistorialEntity`, `EvidenciaEntity`, `NotificacionEntity`.
* **5 DAOs (`dao/`):** `UsuarioDao`, `SolicitudDao` (incluye `clasificarYPriorizar`), `HistorialDao`, `EvidenciaDao`, `NotificacionDao`.
* **Base de Datos (`GsmDatabase.kt`):**
  * Incluye `GsmDatabase.getInstance(context)` (Singleton).
  * Incluye `sembrarDatosFicticiosSiEstaVacia(db)` que precarga automáticamente al iniciar:
    * **4 Usuarios Mock** (password `"1234"`):
      1. `cliente@pyme.cl` (`CLIENTE` - Camila Rojas)
      2. `admin@gsm.cl` (`ADMINISTRADOR` - Roberto Méndez)
      3. `tecnico1@gsm.cl` (`TECNICO` - Diego Soto)
      4. `tecnico2@gsm.cl` (`TECNICO` - Valentina Pino)
    * **4 Solicitudes Mock** con Historial y Evidencia inicial:
      1. `SOL-2026-1001` (`RECIBIDA` - Error 500 catálogo web, con evidencia adjunta)
      2. `SOL-2026-1002` (`EN_PROCESO` y **ATRASADA** - Caída correo corporativo)
      3. `SOL-2026-1003` (`PENDIENTE_INFORMACION` - Banner campaña marketing)
      4. `SOL-2026-1004` (`RESUELTA` - Subdominio pruebas, lista para confirmar cierre o reabrir)

### Fase 2: Mappers (`data/mapper/`) — ✅ 100%
* `SolicitudMapper.kt`, `UsuarioMapper.kt`, `HistorialMapper.kt`.

### Fase 3: Repositorios y Seguridad OWASP (`data/repository/`) — ✅ 100%
* `SolicitudLocalRepository.kt`: CRUD, `clasificarYPriorizar`, generador `SOL-YYYY-XXXX` y cálculo de SLA (6h, 24h, 72h, 168h).
* `UsuarioRepository.kt`: `autenticar(correo, password)` con validación de entradas y consultas de clientes/técnicos.
* `HistorialRepository.kt`: Registro de auditoría y filtro de privacidad (oculta `esComentarioInterno = true` al rol `CLIENTE`).
* `EvidenciaRepository.kt`: Valida formatos permitidos (`image/jpeg`, `image/png`, `application/pdf`, `text/plain`) y tamaño máximo de 5 MB (OWASP M4 / Pág. 16 del PDF).
* `NotificacionRepository.kt`: Creación y lectura de notificaciones locales.
* `WhatsAppRepository.kt`: Genera mensaje estructurado con código, título, categoría y link ficticio `https://gsm-soporte.cl/seguimiento/SOL-YYYY-XXXX` + `simularEnvio()` con `delay(1200L)`.
* `GsmRepository.kt`: Fachada coordinadora con validaciones RBAC (`require(...)`) por rol y estado previo.

### Fase 4: Los 4 ViewModels (`ui/viewmodel/`) — ✅ 100%
Ya están creados en `app/src/main/java/com/example/gsm/ui/viewmodel/`:
1. `AuthViewModel.kt`: `usuarioActual`, `cargando`, `mensajeError`, `iniciarSesion()`, `iniciarSesionDemo(rol)`, `cerrarSesion()`.
2. `SolicitudViewModel.kt`: `listaSolicitudes`, filtros (`estadoFiltro`, `prioridadFiltro`, `soloAtrasadas`, `textoBusqueda`), `cargarSolicitudes(usuario)`, `crearSolicitud(...)`.
3. `DetalleSolicitudViewModel.kt`: `solicitud`, `historial`, `evidencias`, `tecnicosDisponibles`, `mensajeWhatsApp`, `enviandoWhatsApp`, `cargarDetalle(id, rol)` y las 9 acciones por rol usando `ejecutarCambio(...)` + `simularEnvioWhatsApp()`.
4. `AdminDashboardViewModel.kt`: Contadores reactivos por estado, prioridad, atrasadas y por técnico.

---

## 📍 4. Qué Falta Hacer Mañana (Paso a Paso)

### Paso Inmediato 1: Navegación y Fábrica (Fase 5 - Parte A)
Verificar si el usuario ya pegó o necesita aplicar estos 3 archivos que se mostraron al cierre de la sesión anterior:
1. **`app/build.gradle.kts`**: Agregar `implementation("androidx.navigation:navigation-compose:2.8.5")`.
2. **`ui/viewmodel/GsmViewModelFactory.kt`**: El `ViewModelProvider.Factory` que crea los 4 ViewModels.
3. **`ui/navigation/AppNavigation.kt`** y **`MainActivity.kt`**: El `NavHost` con las 5 rutas (`"login"`, `"bandeja"`, `"crear"`, `"detalle/{solicitudId}"`, `"dashboard"`).

### Paso Inmediato 2: Las 5 Pantallas Compose en `ui/view/` (Fase 5 - Parte B)
Construir las 5 pantallas Material 3 simples, bonitas y fáciles de defender, conectadas a `AppNavigation.kt`:
1. `LoginScreen.kt` (Login por correo/clave + 3 botones de acceso rápido Demo: Cliente, Admin, Técnico).
2. `BandejaScreen.kt` (Buscador por código, chips de filtro por estado/prioridad/atrasadas, tarjetas con colores según estado).
3. `CrearSolicitudScreen.kt` (Formulario de título, descripción, categoría, servicio/activo, evidencia ficticia, botones Guardar Borrador y Enviar).
4. `DetalleSolicitudScreen.kt` (Ficha completa, tarjeta de WhatsApp con vista previa del mensaje estructurado y animación de envío, panel de acciones según el Rol del usuario activo, lista de evidencias y línea de tiempo del historial).
5. `AdminDashboardScreen.kt` (Tarjetas de métricas por estado, prioridad, atrasadas y carga por técnico).

### Paso Final 3: Pruebas Unitarias (Fase 6)
* Actualizar `app/src/test/java/com/example/gsm/ExampleUnitTest.kt` con 4 pruebas unitarias simples (formato de código `SOL-YYYY-XXXX`, cálculo de horas SLA, formato de mensaje WhatsApp y validación de tamaño de evidencia).
