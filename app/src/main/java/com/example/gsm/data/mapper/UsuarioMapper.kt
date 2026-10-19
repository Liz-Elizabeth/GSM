package com.example.gsm.data.mapper

import com.example.gsm.data.local.entity.UsuarioEntity
import com.example.gsm.data.model.RolUsuario
import com.example.gsm.data.model.Usuario

/**
 * Mapper exclusivo para Perfiles de Usuario y Autenticación.
 */
fun UsuarioEntity.toDomain() = Usuario(
    id = id,
    nombre = nombre,
    correo = correo,
    password = password,
    rol = runCatching { RolUsuario.valueOf(rol) }.getOrDefault(RolUsuario.CLIENTE),
    empresa = empresa,
    telefono = telefono,
    activo = activo,
    fechaCreacion = fechaCreacion,
    fechaActualizacion = fechaActualizacion
)

fun Usuario.toEntity() = UsuarioEntity(
    id = id,
    nombre = nombre,
    correo = correo,
    password = password,
    rol = rol.name,
    empresa = empresa,
    telefono = telefono,
    medioContacto = "WhatsApp",
    serviciosAsociados = null,
    activo = activo,
    fechaCreacion = fechaCreacion,
    fechaActualizacion = fechaActualizacion
)
