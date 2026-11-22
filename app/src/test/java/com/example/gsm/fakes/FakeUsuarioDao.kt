package com.example.gsm.fakes

import com.example.gsm.data.local.dao.UsuarioDao
import com.example.gsm.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake en memoria para UsuarioDao, simulando SQLite/Room sin dependencias de dispositivo.
 */
class FakeUsuarioDao : UsuarioDao {

    val usuarios = mutableListOf<UsuarioEntity>()
    private val _usuariosFlow = MutableStateFlow<List<UsuarioEntity>>(emptyList())
    private var nextId = 1

    private fun notificarCambio() {
        _usuariosFlow.value = usuarios.toList()
    }

    override suspend fun insertarUsuario(usuario: UsuarioEntity): Long {
        val idAsignado = if (usuario.id > 0) usuario.id else nextId++
        val nuevo = usuario.copy(id = idAsignado)
        usuarios.add(nuevo)
        notificarCambio()
        return idAsignado.toLong()
    }

    override suspend fun insertarUsuarios(usuarios: List<UsuarioEntity>) {
        for (u in usuarios) {
            insertarUsuario(u)
        }
    }

    override suspend fun actualizarUsuario(usuario: UsuarioEntity) {
        val index = usuarios.indexOfFirst { it.id == usuario.id }
        if (index != -1) {
            usuarios[index] = usuario
            notificarCambio()
        }
    }

    override fun obtenerUsuarios(): Flow<List<UsuarioEntity>> =
        _usuariosFlow.asStateFlow().map { it.sortedBy { u -> u.nombre } }

    override suspend fun obtenerUsuarioPorId(id: Int): UsuarioEntity? =
        usuarios.firstOrNull { it.id == id }

    override suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity? =
        usuarios.firstOrNull { it.correo.equals(correo.trim(), ignoreCase = true) }

    override suspend fun autenticar(correo: String, password: String): UsuarioEntity? =
        usuarios.firstOrNull { 
            it.correo.equals(correo.trim(), ignoreCase = true) && 
            it.password == password && 
            it.activo 
        }

    override suspend fun cambiarEstadoActivo(id: Int, activo: Boolean, ts: Long) {
        val index = usuarios.indexOfFirst { it.id == id }
        if (index != -1) {
            usuarios[index] = usuarios[index].copy(activo = activo, fechaActualizacion = ts)
            notificarCambio()
        }
    }

    override suspend fun actualizarCredenciales(id: Int, password: String, debeCambiar: Boolean, ts: Long) {
        val index = usuarios.indexOfFirst { it.id == id }
        if (index != -1) {
            usuarios[index] = usuarios[index].copy(
                password = password,
                debeCambiarPassword = debeCambiar,
                fechaActualizacion = ts
            )
            notificarCambio()
        }
    }

    override suspend fun actualizarPlanSuscripcion(id: Int, plan: String, tickets: Int, estado: String, ts: Long) {
        val index = usuarios.indexOfFirst { it.id == id }
        if (index != -1) {
            usuarios[index] = usuarios[index].copy(
                plan = plan,
                ticketsDisponibles = tickets,
                estadoSuscripcion = estado,
                fechaActualizacion = ts
            )
            notificarCambio()
        }
    }

    override suspend fun descontarTicketDisponible(id: Int): Int {
        val index = usuarios.indexOfFirst { it.id == id }
        if (index == -1 || usuarios[index].ticketsDisponibles <= 0) return 0
        usuarios[index] = usuarios[index].copy(
            ticketsDisponibles = usuarios[index].ticketsDisponibles - 1,
            fechaActualizacion = System.currentTimeMillis()
        )
        notificarCambio()
        return 1
    }

    override suspend fun eliminarUsuario(id: Int) {
        usuarios.removeAll { it.id == id }
        notificarCambio()
    }

    override fun obtenerTecnicosDisponibles(): Flow<List<UsuarioEntity>> =
        _usuariosFlow.asStateFlow().map { lista ->
            lista.filter { it.rol == "TECNICO" && it.activo }.sortedBy { it.nombre }
        }

    override fun obtenerClientes(): Flow<List<UsuarioEntity>> =
        _usuariosFlow.asStateFlow().map { lista ->
            lista.filter { it.rol == "CLIENTE" && it.activo }.sortedBy { it.nombre }
        }

    override fun obtenerUsuariosPorRol(rol: String): Flow<List<UsuarioEntity>> =
        _usuariosFlow.asStateFlow().map { lista ->
            lista.filter { it.rol == rol && it.activo }.sortedBy { it.nombre }
        }

    override fun obtenerEquipoPorEmpresa(empresa: String): Flow<List<UsuarioEntity>> =
        _usuariosFlow.asStateFlow().map { lista ->
            lista.filter { it.rol == "CLIENTE" && it.empresa.orEmpty().trim().equals(empresa.trim(), ignoreCase = true) }
                .sortedBy { it.nombre }
        }

    override suspend fun obtenerIdsPorEmpresa(empresa: String): List<Int> =
        usuarios.filter { it.rol == "CLIENTE" && it.empresa.orEmpty().trim().equals(empresa.trim(), ignoreCase = true) }
            .map { it.id }

    override suspend fun actualizarPlanEmpresa(empresa: String, plan: String, tickets: Int, estado: String, ts: Long) {
        var changed = false
        for (i in usuarios.indices) {
            if (usuarios[i].rol == "CLIENTE" && usuarios[i].empresa.orEmpty().trim().equals(empresa.trim(), ignoreCase = true)) {
                usuarios[i] = usuarios[i].copy(plan = plan, ticketsDisponibles = tickets, estadoSuscripcion = estado, fechaActualizacion = ts)
                changed = true
            }
        }
        if (changed) notificarCambio()
    }

    override suspend fun descontarTicketEmpresa(empresa: String, ts: Long): Int {
        var count = 0
        for (i in usuarios.indices) {
            if (usuarios[i].rol == "CLIENTE" && usuarios[i].empresa.orEmpty().trim().equals(empresa.trim(), ignoreCase = true) && usuarios[i].ticketsDisponibles > 0) {
                usuarios[i] = usuarios[i].copy(ticketsDisponibles = usuarios[i].ticketsDisponibles - 1, fechaActualizacion = ts)
                count++
            }
        }
        if (count > 0) notificarCambio()
        return count
    }

    override fun contarSolicitudesCreadas(clienteId: Int): Flow<Int> =
        MutableStateFlow(0)

    override fun contarSolicitudesAsignadas(tecnicoId: Int): Flow<Int> =
        MutableStateFlow(0)
}
