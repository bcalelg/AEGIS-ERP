package com.aegis.erp.modules.seguridad.auth.dto;

public record LoginClientContext(
        String userAgent,
        String direccionIp,
        String sesion) {
}
