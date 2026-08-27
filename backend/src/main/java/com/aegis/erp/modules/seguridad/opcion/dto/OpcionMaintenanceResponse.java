package com.aegis.erp.modules.seguridad.opcion.dto;

public record OpcionMaintenanceResponse(
        Long id,
        Long idMenu,
        String nombreMenu,
        String nombreModulo,
        String nombre,
        String pagina,
        Integer orden) {}
