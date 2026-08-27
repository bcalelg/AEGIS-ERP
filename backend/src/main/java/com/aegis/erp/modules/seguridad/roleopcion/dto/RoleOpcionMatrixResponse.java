package com.aegis.erp.modules.seguridad.roleopcion.dto;

public record RoleOpcionMatrixResponse(
        Long idOpcion,
        String nombreOpcion,
        String nombreMenu,
        Integer ordenMenu,
        Integer ordenOpcion,
        boolean consultar,
        boolean alta,
        boolean baja,
        boolean cambio,
        boolean imprimir,
        boolean exportar) {}
