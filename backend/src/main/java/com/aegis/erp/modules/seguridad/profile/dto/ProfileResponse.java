package com.aegis.erp.modules.seguridad.profile.dto;

import java.time.LocalDate;

public record ProfileResponse(
        String idUsuario,
        String nombre,
        String apellido,
        String correoElectronico,
        String telefonoMovil,
        LocalDate fechaNacimiento,
        String genero,
        String estatus,
        String empresa,
        String sucursal,
        String role,
        boolean fotografiaDisponible,
        String fotografiaUrl) {}
