package com.aegis.erp.modules.seguridad.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class SmtpPasswordRecoveryMailSenderTest {
    @Test
    void construyeLinkConfigurableSinDobleSlashEInformaExpiracion() {
        JavaMailSender mail = org.mockito.Mockito.mock(JavaMailSender.class);
        var sender =
                new SmtpPasswordRecoveryMailSender(
                        mail, "https://app.example.com///", "sender@example.com", 15);

        sender.send("recipient@example.com", "Usuario", "signed.token.value");

        var message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail).send(message.capture());
        assertThat(message.getValue().getText())
                .contains("https://app.example.com/reset-password?token=")
                .contains("15 minutos")
                .contains("ignora este mensaje")
                .doesNotContain("app.example.com//reset-password");
    }
}
