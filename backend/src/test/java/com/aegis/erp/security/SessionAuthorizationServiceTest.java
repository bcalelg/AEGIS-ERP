package com.aegis.erp.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.aegis.erp.modules.seguridad.usuario.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Map;

class SessionAuthorizationServiceTest {
    @Test
    void aceptaSoloJtiQueCoincideConSesionActual() {
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        SessionAuthorizationService service = new SessionAuthorizationService(usuarios);
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "HS256"),
                Map.of("sub", "TEST_LOGIN", "jti", "session-1"));
        when(usuarios.existsByIdUsuarioAndSesionActual("TEST_LOGIN", "session-1"))
                .thenReturn(true);
        assertThat(service.valid(new JwtAuthenticationToken(jwt))).isTrue();
        when(usuarios.existsByIdUsuarioAndSesionActual("TEST_LOGIN", "session-1"))
                .thenReturn(false);
        assertThat(service.valid(new JwtAuthenticationToken(jwt))).isFalse();
    }
}
