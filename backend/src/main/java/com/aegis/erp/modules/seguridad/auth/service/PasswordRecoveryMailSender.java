package com.aegis.erp.modules.seguridad.auth.service;

public interface PasswordRecoveryMailSender {
    void send(String recipient, String userName, String token);
}
