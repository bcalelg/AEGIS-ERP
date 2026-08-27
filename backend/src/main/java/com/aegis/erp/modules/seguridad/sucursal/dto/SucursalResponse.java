package com.aegis.erp.modules.seguridad.sucursal.dto;

public record SucursalResponse(
        Long id,
        Long idEmpresa,
        String nombreEmpresa,
        String nombre,
        String direccion) {}
