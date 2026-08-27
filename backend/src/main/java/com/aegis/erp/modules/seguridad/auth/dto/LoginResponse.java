package com.aegis.erp.modules.seguridad.auth.dto;

public record LoginResponse(
        boolean authenticated,
        long expiresIn,
        String idUsuario,
        String nombre,
        String apellido,
        String role,
        boolean requiereCambiarPassword) {}
