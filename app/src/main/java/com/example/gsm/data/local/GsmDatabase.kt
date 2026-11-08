package com.example.gsm.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import com.example.gsm.security.SecurityKeyManager
import com.example.gsm.data.local.dao.EvidenciaDao
import com.example.gsm.data.local.dao.HistorialDao
import com.example.gsm.data.local.dao.NotificacionDao
import com.example.gsm.data.local.dao.SolicitudDao
import com.example.gsm.data.local.dao.UsuarioDao
import com.example.gsm.data.local.entity.EvidenciaEntity
import com.example.gsm.data.local.entity.HistorialEntity
import com.example.gsm.data.local.entity.NotificacionEntity
import com.example.gsm.data.local.entity.SolicitudEntity
import com.example.gsm.data.local.entity.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UsuarioEntity::class,
        SolicitudEntity::class,
        HistorialEntity::class,
        EvidenciaEntity::class,
        NotificacionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class GsmDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun solicitudDao(): SolicitudDao
    abstract fun historialDao(): HistorialDao
    abstract fun evidenciaDao(): EvidenciaDao
    abstract fun notificacionDao(): NotificacionDao

    companion object {
        init {
            try {
                System.loadLibrary("sqlcipher")
            } catch (t: Throwable) {
                android.util.Log.e("GsmDatabase", "Error inicializando libreria nativa sqlcipher", t)
            }
        }

        @Volatile
        private var INSTANCE: GsmDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE usuarios ADD COLUMN debeCambiarPassword INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): GsmDatabase {
            try {
                System.loadLibrary("sqlcipher")
            } catch (_: Throwable) {}
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext

                // Detección y migración transparente de bases de datos heredadas sin cifrado
                val dbFile = appContext.getDatabasePath("gsm_database.db")
                if (dbFile.exists()) {
                    try {
                        val header = ByteArray(16)
                        java.io.FileInputStream(dbFile).use { it.read(header) }
                        val headerStr = String(header, java.nio.charset.StandardCharsets.US_ASCII)
                        if (headerStr.startsWith("SQLite format 3")) {
                            android.util.Log.w("GsmDatabase", "Detectada base de datos previa sin cifrado. Recreando como base segura SQLCipher...")
                            appContext.deleteDatabase("gsm_database.db")
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("GsmDatabase", "No se pudo verificar cabecera de la base de datos previa", e)
                    }
                }

                val passphrase = SecurityKeyManager.obtenerPassphrase(appContext)
                val supportFactory = SupportOpenHelperFactory(passphrase)

                val instance = Room.databaseBuilder(
                    appContext,
                    GsmDatabase::class.java,
                    "gsm_database.db"
                )
                    .openHelperFactory(supportFactory)
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance

                // Cargamos los datos ficticios (mock) en segundo plano si la base está vacía
                CoroutineScope(Dispatchers.IO).launch {
                    sembrarDatosFicticiosSiEstaVacia(instance)
                }

                instance
            }
        }

        /**
         * Precarga usuarios y solicitudes ficticias (mock) exigidas por el PDF (Págs. 15 y 20)
         * para demostrar todos los flujos sin depender de internet.
         */
        suspend fun sembrarDatosFicticiosSiEstaVacia(db: GsmDatabase) {
            val usuarioDao = db.usuarioDao()
            // Si ya existe el usuario 1, no volvemos a insertar nada
            if (usuarioDao.obtenerUsuarioPorId(1) != null) return

            val ahora = System.currentTimeMillis()
            val horaMs = 3600 * 1000L

            // 1. USUARIOS FICTICIOS DE PRUEBA (Password para todos: "1234")
            val usuariosMock = listOf(
                UsuarioEntity(
                    id = 1,
                    nombre = "Camila Rojas (Cliente Demo)",
                    correo = "cliente@pyme.cl",
                    password = "1234",
                    rol = "CLIENTE",
                    empresa = "Pyme Soluciones Alfa Ltda.",
                    telefono = "+56900001111",
                    medioContacto = "WhatsApp",
                    serviciosAsociados = "Hosting, Desarrollo Web, Soporte de Correo"
                ),
                UsuarioEntity(
                    id = 2,
                    nombre = "Roberto Méndez (Coordinador Admin)",
                    correo = "admin@gsm.cl",
                    password = "1234",
                    rol = "ADMINISTRADOR",
                    empresa = "GSM Servicios Digitales",
                    telefono = "+56900002222",
                    medioContacto = "WhatsApp"
                ),
                UsuarioEntity(
                    id = 3,
                    nombre = "Diego Soto (Técnico Soporte)",
                    correo = "tecnico1@gsm.cl",
                    password = "1234",
                    rol = "TECNICO",
                    empresa = "GSM Área Infraestructura",
                    telefono = "+56900003333",
                    medioContacto = "WhatsApp"
                ),
                UsuarioEntity(
                    id = 4,
                    nombre = "Valentina Pino (Técnico Web)",
                    correo = "tecnico2@gsm.cl",
                    password = "1234",
                    rol = "TECNICO",
                    empresa = "GSM Área Desarrollo Web",
                    telefono = "+56900004444",
                    medioContacto = "WhatsApp"
                ),
                UsuarioEntity(
                    id = 5,
                    nombre = "Carlos Ramos (Técnico Redes)",
                    correo = "carlos@gsm.cl",
                    password = "1234",
                    rol = "TECNICO",
                    empresa = "GSM Área Infraestructura",
                    telefono = "+56900005555",
                    medioContacto = "WhatsApp"
                ),
                UsuarioEntity(
                    id = 99,
                    nombre = "Administrador GSM (Web)",
                    correo = "adminweb@gsm.cl",
                    password = "1234",
                    rol = "ADMINISTRADOR",
                    empresa = "GSM Servicios Digitales",
                    telefono = "+56900009999",
                    medioContacto = "WhatsApp"
                )
            )
            usuarioDao.insertarUsuarios(usuariosMock)

            val solicitudDao = db.solicitudDao()
            val historialDao = db.historialDao()
            val evidenciaDao = db.evidenciaDao()
            val notificacionDao = db.notificacionDao()

            // 2. SOLICITUD 1: RECIBIDA (Nueva, lista para clasificar y asignar)
            val idSol1 = solicitudDao.insertarSolicitud(
                SolicitudEntity(
                    codigoSeguimiento = "SOL-2026-1001",
                    titulo = "Error 500 al subir catálogo en sitio web",
                    descripcion = "Al intentar actualizar los productos desde el panel web aparece pantalla blanca con Error 500.",
                    categoria = "INCIDENTE_TECNICO",
                    clienteId = 1,
                    tecnicoId = null,
                    estado = "RECIBIDA",
                    prioridad = "ALTA",
                    servicioRelacionado = "Mantenimiento Web",
                    activoRelacionado = "https://tienda.pymealfa-ficticia.cl",
                    fechaLimite = ahora + (24 * horaMs),
                    fechaCreacion = ahora - (2 * horaMs),
                    fechaActualizacion = ahora - (2 * horaMs)
                )
            ).toInt()

            historialDao.insertarHistorial(
                HistorialEntity(
                    solicitudId = idSol1,
                    usuarioId = 1,
                    estadoAnterior = null,
                    estadoNuevo = "RECIBIDA",
                    accion = "CREACION",
                    comentario = "Solicitud registrada formalmente desde mensaje de WhatsApp.",
                    perfilUsuario = "CLIENTE",
                    evidenciaAdjunta = "captura_error_500_ficticia.png",
                    fechaCambio = ahora - (2 * horaMs)
                )
            )
            evidenciaDao.insertarEvidencia(
                EvidenciaEntity(
                    solicitudId = idSol1,
                    usuarioId = 1,
                    nombreArchivo = "captura_error_500_ficticia.png",
                    tipoArchivo = "image/png",
                    tamanoBytes = 320_000L,
                    uriLocal = "file:///ficticio/evidencias/captura_error_500_ficticia.png",
                    descripcion = "Captura de pantalla simulada del error en navegador",
                    estadoRevision = "PENDIENTE",
                    fechaIncorporacion = ahora - (2 * horaMs)
                )
            )

            // 3. SOLICITUD 2: EN_PROCESO y ATRASADA (Fecha límite vencida para probar Criterio 23)
            val idSol2 = solicitudDao.insertarSolicitud(
                SolicitudEntity(
                    codigoSeguimiento = "SOL-2026-1002",
                    titulo = "Caída de servicio de correo corporativo",
                    descripcion = "Las cuentas de ventas no pueden enviar ni recibir correos desde ayer.",
                    categoria = "SOPORTE_CORREO",
                    clienteId = 1,
                    tecnicoId = 3,
                    estado = "EN_PROCESO",
                    prioridad = "URGENTE",
                    servicioRelacionado = "Hosting y Correo",
                    activoRelacionado = "mail.pymealfa-ficticia.cl",
                    fechaLimite = ahora - (5 * horaMs), // Vencida hace 5 horas (ATRASADA)
                    fechaCreacion = ahora - (12 * horaMs),
                    fechaActualizacion = ahora - (3 * horaMs)
                )
            ).toInt()

            historialDao.insertarHistorial(
                HistorialEntity(
                    solicitudId = idSol2,
                    usuarioId = 2,
                    estadoAnterior = "RECIBIDA",
                    estadoNuevo = "ASIGNADA",
                    accion = "ASIGNACION",
                    comentario = "Asignado con prioridad URGENTE al técnico Diego Soto.",
                    perfilUsuario = "ADMINISTRADOR",
                    fechaCambio = ahora - (10 * horaMs)
                )
            )
            historialDao.insertarHistorial(
                HistorialEntity(
                    solicitudId = idSol2,
                    usuarioId = 3,
                    estadoAnterior = "ASIGNADA",
                    estadoNuevo = "EN_PROCESO",
                    accion = "AVANCE",
                    comentario = "Revisando registros DNS y certificados SSL del servidor de correo.",
                    perfilUsuario = "TECNICO",
                    fechaCambio = ahora - (3 * horaMs)
                )
            )

            // 4. SOLICITUD 3: PENDIENTE_INFORMACION (Para probar Criterio 22 y respuesta del Cliente)
            val idSol3 = solicitudDao.insertarSolicitud(
                SolicitudEntity(
                    codigoSeguimiento = "SOL-2026-1003",
                    titulo = "Actualización de banner campaña marketing",
                    descripcion = "Requerimos cambiar el banner principal de inicio por la campaña de invierno.",
                    categoria = "MARKETING_DIGITAL",
                    clienteId = 1,
                    tecnicoId = 4,
                    estado = "PENDIENTE_INFORMACION",
                    prioridad = "MEDIA",
                    servicioRelacionado = "Marketing Digital",
                    activoRelacionado = "https://pymealfa-ficticia.cl",
                    fechaLimite = ahora + (48 * horaMs),
                    fechaCreacion = ahora - (24 * horaMs),
                    fechaActualizacion = ahora - (4 * horaMs)
                )
            ).toInt()

            historialDao.insertarHistorial(
                HistorialEntity(
                    solicitudId = idSol3,
                    usuarioId = 4,
                    estadoAnterior = "EN_PROCESO",
                    estadoNuevo = "PENDIENTE_INFORMACION",
                    accion = "SOLICITUD_INFO",
                    comentario = "Por favor adjuntar el archivo gráfico en formato PNG y el texto legal de la promoción.",
                    perfilUsuario = "TECNICO",
                    fechaCambio = ahora - (4 * horaMs)
                )
            )

            // 5. SOLICITUD 4: RESUELTA (Lista para que el Cliente confirme cierre o pida reapertura)
            val idSol4 = solicitudDao.insertarSolicitud(
                SolicitudEntity(
                    codigoSeguimiento = "SOL-2026-1004",
                    titulo = "Creación de subdominio para entorno de pruebas",
                    descripcion = "Se solicita habilitar demo.pymealfa-ficticia.cl apuntando al servidor de test.",
                    categoria = "HOSTING",
                    clienteId = 1,
                    tecnicoId = 3,
                    estado = "RESUELTA",
                    prioridad = "BAJA",
                    servicioRelacionado = "Hosting",
                    activoRelacionado = "demo.pymealfa-ficticia.cl",
                    resolucion = "Se configuró el registro CNAME y el certificado SSL en el panel de hosting ficticio.",
                    fechaLimite = ahora + (72 * horaMs),
                    fechaCreacion = ahora - (48 * horaMs),
                    fechaActualizacion = ahora - (1 * horaMs),
                    fechaResolucion = ahora - (1 * horaMs)
                )
            ).toInt()

            historialDao.insertarHistorial(
                HistorialEntity(
                    solicitudId = idSol4,
                    usuarioId = 3,
                    estadoAnterior = "EN_PROCESO",
                    estadoNuevo = "RESUELTA",
                    accion = "RESOLUCION",
                    comentario = "Solución: Subdominio activo con SSL funcionando. Queda a la espera de conformidad del cliente.",
                    perfilUsuario = "TECNICO",
                    fechaCambio = ahora - (1 * horaMs)
                )
            )

            notificacionDao.insertar(
                NotificacionEntity(
                    usuarioDestinoId = 1,
                    solicitudId = idSol4,
                    titulo = "Solicitud Resuelta",
                    mensaje = "Tu solicitud #SOL-2026-1004 fue marcada como Resuelta. Confirma tu conformidad.",
                    tipoEvento = "RESUELTA",
                    leida = false,
                    fechaCreacion = ahora - (1 * horaMs)
                )
            )
        }
    }
}