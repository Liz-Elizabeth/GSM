package com.example.gsm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gsm.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarUsuario(usuario: UsuarioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuarios(usuarios: List<UsuarioEntity>)

    @Update
    suspend fun actualizarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    fun obtenerUsuarios(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtenerUsuarioPorId(id: Int): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity?

    // Autenticación ficticia requerida por el caso de uso
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND password = :password AND activo = 1 LIMIT 1")
    suspend fun autenticar(correo: String, password: String): UsuarioEntity?

    @Query("UPDATE usuarios SET activo = :activo, fechaActualizacion = :ts WHERE id = :id")
    suspend fun cambiarEstadoActivo(id: Int, activo: Boolean, ts: Long = System.currentTimeMillis())

    @Query("UPDATE usuarios SET password = :password, debeCambiarPassword = :debeCambiar, fechaActualizacion = :ts WHERE id = :id")
    suspend fun actualizarCredenciales(id: Int, password: String, debeCambiar: Boolean, ts: Long = System.currentTimeMillis())

    @Query("UPDATE usuarios SET plan = :plan, ticketsDisponibles = :tickets, estadoSuscripcion = :estado, fechaActualizacion = :ts WHERE id = :id")
    suspend fun actualizarPlanSuscripcion(id: Int, plan: String, tickets: Int, estado: String, ts: Long = System.currentTimeMillis())

    @Query("UPDATE usuarios SET ticketsDisponibles = ticketsDisponibles - 1 WHERE id = :id AND ticketsDisponibles > 0")
    suspend fun descontarTicketDisponible(id: Int): Int

    @Query("DELETE FROM usuarios WHERE id = :id")
    suspend fun eliminarUsuario(id: Int)

    // Listado de técnicos disponibles para asignación
    @Query("SELECT * FROM usuarios WHERE rol = 'TECNICO' AND activo = 1 ORDER BY nombre ASC")
    fun obtenerTecnicosDisponibles(): Flow<List<UsuarioEntity>>

    // Listado de clientes para creación o filtrado administrativo
    @Query("SELECT * FROM usuarios WHERE rol = 'CLIENTE' AND activo = 1 ORDER BY nombre ASC")
    fun obtenerClientes(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE rol = :rol AND activo = 1 ORDER BY nombre ASC")
    fun obtenerUsuariosPorRol(rol: String): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE rol = 'CLIENTE' AND LOWER(TRIM(empresa)) = LOWER(TRIM(:empresa)) ORDER BY nombre ASC")
    fun obtenerEquipoPorEmpresa(empresa: String): Flow<List<UsuarioEntity>>

    @Query("SELECT id FROM usuarios WHERE rol = 'CLIENTE' AND LOWER(TRIM(empresa)) = LOWER(TRIM(:empresa))")
    suspend fun obtenerIdsPorEmpresa(empresa: String): List<Int>

    @Query("UPDATE usuarios SET plan = :plan, ticketsDisponibles = :tickets, estadoSuscripcion = :estado, fechaActualizacion = :ts WHERE rol = 'CLIENTE' AND LOWER(TRIM(empresa)) = LOWER(TRIM(:empresa))")
    suspend fun actualizarPlanEmpresa(empresa: String, plan: String, tickets: Int, estado: String, ts: Long = System.currentTimeMillis())

    @Query("UPDATE usuarios SET ticketsDisponibles = ticketsDisponibles - 1, fechaActualizacion = :ts WHERE rol = 'CLIENTE' AND LOWER(TRIM(empresa)) = LOWER(TRIM(:empresa)) AND ticketsDisponibles > 0")
    suspend fun descontarTicketEmpresa(empresa: String, ts: Long = System.currentTimeMillis()): Int

    // Contadores requeridos en "Información de usuarios y perfiles" (PDF Pág. 8)
    @Query("SELECT COUNT(*) FROM solicitudes WHERE clienteId = :clienteId AND esBorrador = 0")
    fun contarSolicitudesCreadas(clienteId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM solicitudes WHERE tecnicoId = :tecnicoId")
    fun contarSolicitudesAsignadas(tecnicoId: Int): Flow<Int>
}
