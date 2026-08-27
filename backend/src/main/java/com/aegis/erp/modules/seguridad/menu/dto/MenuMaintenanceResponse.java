package com.aegis.erp.modules.seguridad.menu.dto;

public record MenuMaintenanceResponse(
        Long id,
        Long idModulo,
        String nombreModulo,
        String nombre,
        Integer orden) {}
