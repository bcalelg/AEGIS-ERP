package com.aegis.erp.modules.seguridad.auth.dto;

public record LoginClientContext(String userAgent, String direccionIp, String sesion) {
    public LoginClientContext withSession(String identificadorSesion) {
        return new LoginClientContext(userAgent, direccionIp, identificadorSesion);
    }
}
