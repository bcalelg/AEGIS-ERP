package com.aegis.erp.modules.seguridad.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aegis.erp.common.exception.InvalidPasswordResetTokenException;
import com.aegis.erp.modules.seguridad.usuario.entity.Usuario;

import org.junit.jupiter.api.Test;

import java.time.*;

class PasswordRecoveryTokenServiceTest {
    private static final String SECRET = "test-only-recovery-secret-with-at-least-32-bytes";

    @Test
    void emiteTokenSeparadoConExpiracionPropositoYHuella() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-19T12:00:00Z"), ZoneOffset.UTC);
        PasswordRecoveryTokenService service =
                new PasswordRecoveryTokenService(clock, SECRET, 15, "test-recovery");
        Usuario usuario = mock(Usuario.class);
        when(usuario.getIdUsuario()).thenReturn("TEST_LOGIN");
        when(usuario.getPasswordHash()).thenReturn("$2a$12$hash-original");

        String token = service.issue(usuario);
        var claims = service.decode(token);

        assertThat(token).doesNotContain("TEST_LOGIN", "$2a$12$hash-original");
        assertThat(claims.subject()).isEqualTo("TEST_LOGIN");
        service.validateFingerprint(claims, usuario);
    }

    @Test
    void rechazaTokenAlteradoYHuellaDePasswordAnterior() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-19T12:00:00Z"), ZoneOffset.UTC);
        PasswordRecoveryTokenService service =
                new PasswordRecoveryTokenService(clock, SECRET, 15, "test-recovery");
        Usuario usuario = mock(Usuario.class);
        when(usuario.getIdUsuario()).thenReturn("TEST_LOGIN");
        when(usuario.getPasswordHash()).thenReturn("hash-original");
        String token = service.issue(usuario);

        assertThatThrownBy(() -> service.decode(token + "x"))
                .isInstanceOf(InvalidPasswordResetTokenException.class);

        var claims = service.decode(token);
        when(usuario.getPasswordHash()).thenReturn("hash-nuevo");
        assertThatThrownBy(() -> service.validateFingerprint(claims, usuario))
                .isInstanceOf(InvalidPasswordResetTokenException.class);
    }

    @Test
    void rechazaTokenExpirado() {
        Clock issuanceClock =
                Clock.fixed(Instant.parse("2026-08-19T12:00:00Z"), ZoneOffset.UTC);
        PasswordRecoveryTokenService issuer =
                new PasswordRecoveryTokenService(issuanceClock, SECRET, 15, "test-recovery");
        Usuario usuario = mock(Usuario.class);
        when(usuario.getIdUsuario()).thenReturn("TEST_LOGIN");
        when(usuario.getPasswordHash()).thenReturn("hash-original");
        String token = issuer.issue(usuario);
        Clock expiredClock =
                Clock.fixed(Instant.parse("2026-08-19T12:16:01Z"), ZoneOffset.UTC);
        PasswordRecoveryTokenService verifier =
                new PasswordRecoveryTokenService(expiredClock, SECRET, 15, "test-recovery");

        assertThatThrownBy(() -> verifier.decode(token))
                .isInstanceOf(InvalidPasswordResetTokenException.class);
    }
}
