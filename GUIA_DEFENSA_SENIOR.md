# 🎓 Manual Maestro de Defensa Técnica: Proyecto GSM
### Cómo entender y defender cada línea del proyecto como un Ingeniero Senior siendo Estudiante de 1er Año

**Autores del Proyecto:** Elizabeth Reyes y Juan Varas  
**Asignatura:** DSY1105 - Desarrollo de Aplicaciones Móviles (Duoc UC)

---

## 🧭 1. El "Elevator Pitch" (Cómo presentar tu proyecto en los primeros 60 segundos)

Si el profesor dice: *"Cuéntenme de qué trata su proyecto y cómo lo construyeron"*, esta es tu respuesta ganadora:

> *"Profesor, desarrollamos **GSM (Gestión de Solicitudes Móviles)**, una aplicación Android nativa en **Kotlin** con **Jetpack Compose** y **Room (SQLite)** diseñada bajo arquitectura **MVVM**. El sistema resuelve el caos operativo de una empresa de servicios informáticos que recibía requerimientos por WhatsApp sin código único, sin prioridad ni trazabilidad. Nuestra app registra cada solicitud con un código único (`SOL-YYYY-XXXX`), calcula automáticamente su fecha límite según la prioridad (SLA), controla permisos estrictos para 3 roles (**Cliente, Administrador y Técnico**), audita cada transición de estado en un historial inmutable y genera el mensaje estructurado listo para derivar por WhatsApp, funcionando 100% offline."*

---

## 🏗️ 2. Arquitectura MVVM Explicada desde Cero (Hasta el más mínimo detalle)

### 2.1 ¿Qué significa la sigla MVVM?
* **M — Model (Modelo y Datos):** Son tus datos, la base de datos SQLite (`Room`), los traductores (`Mappers`) y las reglas del negocio (`Repositories`).
* **V — View (Vista / Pantalla):** Son las pantallas en Jetpack Compose (`LoginScreen`, `BandejaScreen`, etc.). **Regla de oro:** La Vista es "tonta"; solo dibuja lo que le dicen y avisa cuando el usuario toca un botón. No toma decisiones de negocio ni hace consultas SQL.
* **VM — ViewModel (Modelo de Vista):** Es el intermediario ("el mesero"). Recibe los toques de la pantalla, le pide al Repositorio que haga el trabajo pesado en segundo plano y guarda el resultado en variables observables para que la pantalla se redibuje sola.

### 2.2 Gráfico del Flujo Unidireccional de Datos (UDF)

```text
╔══════════════════════════════════════════════════════════════════════════════╗
║                 ARQUITECTURA MVVM + CLEAN BOUNDARIES (GSM)                   ║
╚══════════════════════════════════════════════════════════════════════════════╝

   ┌───────────────────────────┐
   │ 📱 1. VISTA (Compose UI)  │  <-- Solo dibuja y captura clics del usuario
   └──────┬─────────────▲──────┘
          │             │
   Llama a│             │ Lee variables con
   función│             │ `by mutableStateOf` (Se redibuja sola)
          ▼             │
   ┌──────┴─────────────┴──────┐
   │ 🧠 2. VIEWMODEL           │  <-- Abre `viewModelScope.launch` (2do plano)
   └──────┬─────────────▲──────┘      y guarda el estado de la pantalla
          │             │
   Pide   │             │ Devuelve `Result<T>` o
   acción │             │ tubería en vivo `Flow<List<T>>`
          ▼             │
   ┌──────┴─────────────┴──────┐
   │ 🛡️ 3. REPOSITORY          │  <-- Aplica reglas del negocio y seguridad OWASP
   │    (GsmRepository)        │      Coordina: Solicitud + Historial + Alerta
   └──────┬─────────────▲──────┘
          │             │
   Llama a│             │ Traduce de Caja SQL a
   .toEntity()          │ Modelo limpio con .toDomain()
          ▼             │
   ┌──────┴─────────────┴──────┐
   │ 🔄 4. MAPPER (Aduana)     │  <-- Aísla la base de datos de las pantallas
   └──────┬─────────────▲──────┘
          │             │
   Ejecuta│             │ Entrega filas de las
   SQL    │             │ tablas (`*Entity`)
          ▼             │
   ┌──────┴─────────────┴──────┐
   │ 💾 5. DAO + ROOM SQLITE   │  <-- Guarda los datos en el archivo físico
   │    (gsm_database.db)      │      del teléfono (Persistencia Offline)
   └───────────────────────────┘
```

---

## 🔬 3. Anatomía de Cada Capa: Qué hace cada anotación y cada palabra clave

Para que no exista ni una sola línea de código que no sepas explicar si el profesor abre un archivo al azar:

### Capa 1: `data/local/entity/` (Las Tablas de la Base de Datos)
Aquí están `UsuarioEntity`, `SolicitudEntity`, `HistorialEntity`, `EvidenciaEntity` y `NotificacionEntity`.

* **`@Entity(tableName = "solicitudes")`**: Le dice a Room *"crea una tabla en SQLite llamada `solicitudes` con estas columnas"*.
* **`@PrimaryKey(autoGenerate = true) val id: Int = 0`**: Es el identificador único de cada fila. `autoGenerate = true` hace que SQLite sume 1 automáticamente (`1, 2, 3...`).
* **`ForeignKey` (Llave Foránea)**: Conecta dos tablas para que no existan datos huérfanos:
  * **`onDelete = ForeignKey.CASCADE`** (Usado en `HistorialEntity`, `EvidenciaEntity`, `NotificacionEntity`): *"Si se borra una solicitud, borra automáticamente su historial, sus evidencias y sus notificaciones"*.
  * **`onDelete = ForeignKey.RESTRICT`** (Usado en `clienteId` de `SolicitudEntity`): *"Prohibido borrar a un usuario si tiene solicitudes registradas"*.
  * **`onDelete = ForeignKey.SET_NULL`** (Usado en `tecnicoId` de `SolicitudEntity`): *"Si un técnico deja de existir, la solicitud no se borra; su campo `tecnicoId` simplemente vuelve a `null` (sin asignar)"*.
* **`Index(value = ["codigoSeguimiento"], unique = true)`**:
  1. `Index`: Crea un índice de búsqueda rápida (como el índice de un libro) para que buscar un código por WhatsApp sea instantáneo.
  2. `unique = true`: Prohíbe que existan dos solicitudes con el mismo código `SOL-2026-XXXX` o dos usuarios con el mismo correo.

---

### Capa 2: `data/local/dao/` (Los Bodegueros SQL)
Aquí están las `interface` con anotaciones de Room:

* **`@Dao`**: Marca la interfaz como un *Data Access Object*. Tú solo escribes la firma de la función y el compilador **KSP** escribe el código Java/SQL real por debajo.
* **`@Insert(onConflict = OnConflictStrategy.ABORT)`**: Inserta una fila nueva. Si intenta meter un código repetido, aborta y avisa el error.
* **`@Query("SELECT * FROM solicitudes WHERE ...")`**: Ejecuta una consulta SQL verificada en tiempo de compilación (si escribes mal el nombre de una columna, Android Studio te avisa en rojo antes de ejecutar).
* **¿Por qué unas funciones del DAO llevan `suspend` y otras devuelven `Flow<...>`?**
  ```text
  ┌─────────────────────────────────────┬──────────────────────────────────────┐
  │ `suspend fun insertar(...)`         │ `fun listar(...): Flow<List<...>>`   │
  ├─────────────────────────────────────┼──────────────────────────────────────┤
  │ Es una acción de 1 sola vez         │ Es una cámara en vivo                │
  │ (guardar, actualizar o buscar 1 ID).│ Cada vez que cambia la tabla, emite  │
  │ Corre en segundo plano y termina.   │ la lista actualizada sola.           │
  └─────────────────────────────────────┴──────────────────────────────────────┘
  ```

---

### Capa 3: `model/` y `data/mapper/` (El Modelo de Dominio y la Aduana)
* **¿Por qué existen `SolicitudEntity` y `Solicitud` por separado?**
  * En SQLite (`SolicitudEntity`), los estados y categorías se guardan como texto plano (`val estado: String = "RECIBIDA"`).
  * En la lógica de tu app (`Solicitud`), sería peligroso usar `String` porque cualquiera podría escribir `"RECIVIDA"` con falta de ortografía. Por eso `Solicitud` usa **`enum class`** (`EstadoSolicitud.RECIBIDA`, `PrioridadSolicitud.ALTA`, `CategoriaSolicitud.HOSTING`).
* **¿Qué hace el Mapper (`SolicitudMapper.kt`)?**
  * `Entity.toDomain()`: Convierte el `String` de la base de datos al `enum` seguro usando `runCatching { EstadoSolicitud.valueOf(estado) }.getOrDefault(EstadoSolicitud.RECIBIDA)`. Si algún día en la base de datos hubiera un texto desconocido, no explota la app: usa un valor por defecto seguro.
  * `Model.toEntity()`: Convierte el `enum` de vuelta a `.name` (`String`) para guardarlo en SQLite.

---

### Capa 4: `data/repository/` (El Jefe de Cocina y Coordinador)
Aquí aplicamos el **Patrón Fachada (Facade Pattern)**:
* Tienes **6 repositorios pequeños especializados**:
  1. `UsuarioRepository`: Solo login y listar usuarios por rol.
  2. `SolicitudLocalRepository`: Solo guardar/filtrar solicitudes, generar el código `SOL-YYYY-XXXX` y calcular las horas límite (SLA).
  3. `HistorialRepository`: Solo guardar eventos en la bitácora y **filtrar comentarios internos** cuando consulta un cliente.
  4. `EvidenciaRepository`: Solo validar formato y peso máximo (5 MB) de archivos adjuntos.
  5. `NotificacionRepository`: Solo crear y leer alertas locales.
  6. `WhatsAppRepository`: Solo armar el texto con formato WhatsApp y simular el envío con `delay(1200L)`.
* Y tienes **1 Coordinador Principal (`GsmRepository`)**:
  * Cuando el Administrador asigna un técnico llamando a `gsmRepository.asignarTecnico(...)`, el coordinador ejecuta **3 cosas en orden**:
    1. Actualiza la solicitud a `ASIGNADA` en `SolicitudLocalRepository`.
    2. Registra quién hizo el cambio y a qué hora en `HistorialRepository`.
    3. Envía una alerta al técnico asignado en `NotificacionRepository`.

---

### Capa 5: `ui/viewmodel/` (Los 4 Cerebros de las Pantallas)
Separamos en **4 ViewModels** cortos y enfocados:
1. **`AuthViewModel`**: Tiene `usuarioActual`, `cargando`, `mensajeError`. Maneja quién está conectado.
2. **`SolicitudViewModel`**: Tiene `listaSolicitudes` y los filtros. Carga la bandeja según el rol y crea solicitudes nuevas.
3. **`DetalleSolicitudViewModel`**: Tiene `solicitud`, `historial`, `evidencias`, `mensajeWhatsApp` y los botones de acción de los 3 roles.
4. **`AdminDashboardViewModel`**: Tiene los 14 contadores en vivo para el panel del Administrador.

#### Detalle clave de defensa en los ViewModels:
```kotlin
var usuarioActual by mutableStateOf<Usuario?>(null)
    private set
```
* **`by mutableStateOf(...)`**: Convierte la variable en un estado observable de Compose. Cuando su valor cambia con `=`, cualquier pantalla que la esté leyendo se redibuja automáticamente.
* **`private set`**: Aplica **encapsulamiento**. La pantalla puede *leer* `authVm.usuarioActual`, pero tiene prohibido hacer `authVm.usuarioActual = otroUsuario`. Solo el propio `AuthViewModel` puede modificar sus variables internas.

---

## 🏛️ 4. Los 5 Principios SOLID en tu Proyecto (Con Ejemplos Reales de tu Código)

Si en la defensa te preguntan: *"¿Cómo aplicaron principios de diseño o SOLID?"*, aquí tienes qué significa cada letra y dónde está en tu código:

```text
╔══════╦══════════════════════════════╦════════════════════════════════════════╗
║ Letra║ Nombre del Principio         ║ Dónde está en tu proyecto GSM          ║
╠══════╬══════════════════════════════╬════════════════════════════════════════╣
║  S   ║ Single Responsibility        ║ • Cada repositorio y cada ViewModel    ║
║      ║ (Responsabilidad Única)      ║   tiene 1 sola responsabilidad.        ║
║      ║ "Una clase debe tener una    ║ • Ejemplo: `WhatsAppRepository` solo   ║
║      ║  sola razón para cambiar"    ║   sabe formatear el mensaje de WA; no  ║
║      ║                              ║   sabe nada de SQLite ni de usuarios.  ║
╠══════╬══════════════════════════════╬════════════════════════════════════════╣
║  O   ║ Open / Closed                ║ • Uso de `enum class` como             ║
║      ║ (Abierto/Cerrado)            ║   `CategoriaSolicitud(val descripcion)`║
║      ║ "Abierto a extensión,        ║ • Puedes agregar una categoría nueva   ║
║      ║  cerrado a modificación"     ║   al enum sin modificar las tablas ni  ║
║      ║                              ║   las consultas SQL de Room.           ║
╠══════╬══════════════════════════════╬════════════════════════════════════════╣
║  L   ║ Liskov Substitution          ║ • Tus ViewModels heredan de            ║
║      ║ (Sustitución de Liskov)      ║   `androidx.lifecycle.ViewModel()` y   ║
║      ║ "Las subclases deben poder   ║   respetan el ciclo de vida de Android ║
║      ║  usarse como su clase base"  ║   a través de `GsmViewModelFactory`.   ║
╠══════╬══════════════════════════════╬════════════════════════════════════════╣
║  I   ║ Interface Segregation        ║ • En vez de tener 1 solo DAO gigante   ║
║      ║ (Segregación de Interfaces)  ║   con 50 consultas mezcladas,          ║
║      ║ "Mejor varias interfaces     ║   separamos en 5 interfaces DAO:       ║
║      ║  pequeñas que una gigante"   ║   `UsuarioDao`, `SolicitudDao`,        ║
║      ║                              ║   `HistorialDao`, `EvidenciaDao`,      ║
║      ║                              ║   `NotificacionDao`.                   ║
╠══════╬══════════════════════════════╬════════════════════════════════════════╣
║  D   ║ Dependency Inversion         ║ • Ningún Repositorio crea su propio    ║
║      ║ (Inversión de Dependencias)  ║   DAO adentro con `new`, y ningún      ║
║      ║ "Recibir dependencias desde  ║   ViewModel crea su propio Repositorio.║
║      ║  afuera en vez de crearlas"  ║ • Todos reciben sus dependencias por   ║
║      ║                              ║   constructor desde `MainActivity` y   ║
║      ║                              ║   `GsmViewModelFactory`.               ║
╚══════╩══════════════════════════════╩════════════════════════════════════════╝
```

> 💡 **Tip de honestidad Senior si el profesor hila fino en la "D":**  
> *"Aplicamos Inyección de Dependencias por constructor mediante `GsmViewModelFactory`. Entre los Repositorios y los DAOs dependemos de interfaces (`@Dao interface`), cumpliendo DIP al 100%. Entre los ViewModels y los Repositorios inyectamos las clases concretas porque solo existe una fuente de datos local y crear interfaces duplicadas habría sido sobre-ingeniería (principio KISS/YAGNI)."* — **Esa respuesta es de nota 7.0.**

---

## 🛡️ 5. Seguridad OWASP Aplicada (Cómo blindamos la app)

OWASP (*Open Worldwide Application Security Project*) define las vulnerabilidades más críticas en aplicaciones móviles (**OWASP Mobile Top 10**). Así las prevenimos en GSM:

### 1. OWASP M1 / A01: Broken Access Control (Control de Acceso Roto)
* **¿Qué es?** Cuando una app solo esconde un botón en la pantalla, pero si un usuario sin permisos logra llamar a la función, el sistema ejecuta la acción igual.
* **Cómo lo protegimos en `GsmRepository.kt`:**
  1. **Guardias de Rol (`require`):**
     ```kotlin
     require(admin.rol == RolUsuario.ADMINISTRADOR) {
         "Acceso denegado: Solo el administrador puede asignar responsables técnicos."
     }
     ```
  2. **Propiedad del Recurso (Anti-IDOR):** Un cliente solo puede responder o cerrar solicitudes donde `solicitud.clienteId == cliente.id`.
  3. **Validación de Máquina de Estados:** En `confirmarCierre()`, exigimos `require(solicitud.estado == EstadoSolicitud.RESUELTA)`. Así cumplimos la regla del PDF de que nadie puede saltar directo de `RECIBIDA` a `CERRADA`.

### 2. OWASP M4: Insufficient Input/Output Validation (Validación de Entradas y Archivos)
* **¿Qué es?** Aceptar textos vacíos, archivos ejecutables maliciosos (`.exe`, `.apk`, `.sh`) o archivos gigantes que llenen la memoria del teléfono (Denegación de Servicio local).
* **Cómo lo protegimos en `EvidenciaRepository.kt` y `GsmRepository.kt`:**
  * Lista blanca (*Whitelist*) de formatos permitidos:
    ```kotlin
    val FORMATOS_PERMITIDOS = setOf("image/jpeg", "image/png", "application/pdf", "text/plain")
    ```
  * Límite estricto de tamaño máximo de **5 MB** (`5 * 1024 * 1024 bytes`).
  * Limpieza de espacios con `.trim()` y validación `.isNotBlank()` en títulos, descripciones, motivos y credenciales.

### 3. OWASP M6 / M9: Inadequate Privacy Controls (Control de Privacidad y Fuga de Datos)
* **¿Qué es?** Exponer información interna de la empresa al cliente o filtrar datos sensibles al compartir por redes externas.
* **Cómo lo protegimos:**
  1. **Filtro a nivel SQL en `HistorialRepository.kt`:**
     ```kotlin
     val flow = if (rolUsuario == RolUsuario.CLIENTE) {
         historialDao.obtenerHistorialVisibleCliente(solicitudId) // WHERE esComentarioInterno = 0
     } else {
         historialDao.obtenerHistorialCompleto(solicitudId)
     }
     ```
     Los comentarios internos entre técnicos y administradores jamás salen de SQLite cuando consulta un cliente.
  2. **Mensaje seguro en `WhatsAppRepository.kt`:** Solo incluye código de seguimiento, título, categoría, prioridad y enlace de referencia, sin exponer contraseñas ni datos privados.

### 4. Prevención de Inyección SQL (SQL Injection)
* Todas las consultas en los 5 DAOs usan **parámetros enlazados de Room** (`:correo`, `:codigo`, `:estado`). Room compila consultas parametrizadas (`PreparedStatement`), haciendo imposible una inyección SQL aunque alguien escriba `' OR '1'='1` en el buscador.

---

## 🎤 6. Preguntas Trampa del Profesor en la Defensa (y Cómo Responderlas)

### ❓ Pregunta 1: *"¿Cómo generan el código único de seguimiento y cómo evitan que se repita?"*
* **Tu respuesta:** *"En `SolicitudLocalRepository.generarCodigoUnico()` tomamos el año actual y un correlativo de 4 dígitos con el formato `SOL-2026-XXXX`. Además, en `SolicitudEntity` declaramos un índice único (`Index(value = ["codigoSeguimiento"], unique = true)`) a nivel de base de datos SQLite para garantizar integridad referencial."*

### ❓ Pregunta 2: *"¿Cómo sabe la aplicación cuándo una solicitud está atrasada?"*
* **Tu respuesta:** *"Al crear o reclasificar una solicitud, `calcularSlaFechaLimite()` suma las horas de SLA según la prioridad a la fecha actual en milisegundos: **6 horas** para Urgente, **24h** para Alta, **72h** para Media y **168h** para Baja. Luego, en `SolicitudDao.obtenerSolicitudesAtrasadas(ahora)`, consultamos todas las solicitudes cuya `fechaLimite < ahora` y que aún no estén en estado `RESUELTA`, `CERRADA` ni `CANCELADA`."*

### ❓ Pregunta 3: *"¿Por qué un Cliente no puede ver las notas internas que deja el Técnico?"*
* **Tu respuesta:** *"Porque en `HistorialEntity` tenemos el campo booleano `esComentarioInterno`. Cuando el ViewModel pide el historial a `HistorialRepository.obtenerHistorial(solicitudId, rolUsuario)`, el repositorio verifica el rol: si es `CLIENTE`, ejecuta la consulta SQL `WHERE solicitudId = :id AND esComentarioInterno = 0`. Así el dato nunca llega siquiera a la memoria de la interfaz."*

### ❓ Pregunta 4: *"¿Qué pasa si no hay conexión a internet durante la demostración?"*
* **Tu respuesta:** *"La aplicación es 100% **Offline-First**. Toda la persistencia vive localmente en SQLite mediante Room (`gsm_database.db`). Además, en `GsmDatabase.kt` implementamos una semilla automática (`sembrarDatosFicticiosSiEstaVacia`) que precarga 4 usuarios ficticios y 4 solicitudes en distintos estados la primera vez que se instala la app."*

### ❓ Pregunta 5: *"¿Por qué separaron en 4 ViewModels en vez de tener 1 solo?"*
* **Tu respuesta:** *"Por el Principio de Responsabilidad Única (la S de SOLID). Separamos la sesión (`AuthViewModel`), la bandeja y filtros (`SolicitudViewModel`), la transición de estados del detalle (`DetalleSolicitudViewModel`) y las métricas (`AdminDashboardViewModel`). Así cada archivo tiene menos de 150 líneas, es fácil de mantener y solo carga en memoria lo que necesita cada pantalla."*

### ❓ Pregunta 6: *"¿Cómo se comunican con WhatsApp sin usar la API pagada de Meta?"*
* **Tu respuesta:** *"Respetando las restricciones de la sección 3.4 y 5.1 del caso, `WhatsAppRepository` construye el mensaje estructurado oficial con el código único, título, categoría y enlace de seguimiento sin exponer datos sensibles, y provee tanto la simulación asíncrona con `delay` para feedback visual en la UI como el texto listo para compartir por `Intent` de Android."*
