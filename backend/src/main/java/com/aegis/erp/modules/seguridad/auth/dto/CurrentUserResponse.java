package com.aegis.erp.modules.seguridad.auth.dto;

public record CurrentUserResponse(
        String idUsuario,
        String nombre,
        String apellido,
        String role,
        boolean requiereCambiarPassword) {}
