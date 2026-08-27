package com.aegis.erp.modules.seguridad.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SmtpPasswordRecoveryMailSender implements PasswordRecoveryMailSender {
    private final JavaMailSender mail;
    private final String frontendUrl;
    private final String from;
    private final long expirationMinutes;

    public SmtpPasswordRecoveryMailSender(
            JavaMailSender mail,
            @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl,
            @Value("${spring.mail.username:}") String from,
            @Value("${password-recovery.expiration-minutes:15}") long expirationMinutes) {
        this.mail = mail;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
        this.from = from;
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    public void send(String recipient, String userName, String token) {
        String link =
                UriComponentsBuilder.fromUriString(frontendUrl)
                        .path("/reset-password")
                        .queryParam("token", token)
                        .build()
                        .encode()
                        .toUriString();
        SimpleMailMessage message = new SimpleMailMessage();
        if (!from.isBlank()) message.setFrom(from);
        message.setTo(recipient);
        message.setSubject("AEGIS-ERP - Recuperación de contraseña");
        message.setText(
                "Hola "
                        + userName
                        + ",\n\nRecibimos una solicitud para restablecer tu contraseña de AEGIS-ERP.\n"
                        + "Utiliza el siguiente enlace temporal:\n\n"
                        + link
                        + "\n\nSi no solicitaste este cambio, ignora este mensaje."
                        + " El enlace expirará aproximadamente en "
                        + expirationMinutes
                        + " minutos.");
        mail.send(message);
    }
}
