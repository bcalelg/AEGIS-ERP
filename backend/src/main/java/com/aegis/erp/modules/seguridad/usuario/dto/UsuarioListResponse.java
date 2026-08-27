package com.aegis.erp.modules.seguridad.usuario.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UsuarioListResponse(
        String idUsuario,
        String nombre,
        String apellido,
        LocalDate fechaNacimiento,
        String correoElectronico,
        String telefonoMovil,
        Long idEmpresa,
        String nombreEmpresa,
        Long idSucursal,
        String nombreSucursal,
        Long idGenero,
        String nombreGenero,
        Long idStatusUsuario,
        String nombreStatusUsuario,
        Long idRole,
        String nombreRole,
        LocalDateTime ultimaFechaIngreso,
        boolean requiereCambiarPassword) {}
