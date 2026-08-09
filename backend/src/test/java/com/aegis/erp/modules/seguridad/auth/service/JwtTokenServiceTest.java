package com.aegis.erp.modules.seguridad.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aegis.erp.modules.seguridad.usuario.entity.Empresa;
import com.aegis.erp.modules.seguridad.usuario.entity.Role;
import com.aegis.erp.modules.seguridad.usuario.entity.StatusUsuario;
import com.aegis.erp.modules.seguridad.usuario.entity.Sucursal;
import com.aegis.erp.modules.seguridad.usuario.entity.Usuario;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtTokenServiceTest {
    private static final SecretKey KEY = new SecretKeySpec(
            "test-only-secret-with-at-least-32-bytes-long".getBytes(), "HmacSHA256");

    @Test
    void generatesExpectedClaimsAndExpiration() {
        Instant now = Instant.now().minusSeconds(5).truncatedTo(ChronoUnit.SECONDS);
        JwtTokenService service = service(Clock.fixed(now, ZoneOffset.UTC), 60);

        JwtTokenService.IssuedToken issued = service.issue(usuario());
        var jwt = decoder().decode(issued.value());

        assertThat(jwt.getSubject()).isEqualTo("Administrador");
        assertThat(jwt.getClaimAsString("role")).isEqualTo("Administrador");
        assertThat(jwt.getIssuedAt()).isEqualTo(now);
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(60));
        assertThat(issued.expiresInSeconds()).isEqualTo(3600);
        assertThat(jwt.getClaims()).containsOnlyKeys("sub", "role", "iat", "exp");
    }

    @Test
    void expiredTokenIsRejected() {
        Clock oldClock = Clock.fixed(Instant.now().minus(Duration.ofHours(2)), ZoneOffset.UTC);
        String token = service(oldClock, 60).issue(usuario()).value();

        assertThatThrownBy(() -> decoder().decode(token)).isInstanceOf(JwtValidationException.class);
    }

    private JwtTokenService service(Clock clock, long minutes) {
        return new JwtTokenService(new NimbusJwtEncoder(new ImmutableSecret<>(KEY)), clock, minutes);
    }

    private JwtDecoder decoder() {
        return NimbusJwtDecoder.withSecretKey(KEY).macAlgorithm(MacAlgorithm.HS256).build();
    }

    private Usuario usuario() {
        return new Usuario("Administrador", "Administrador", "IT", "hash", 0, 1,
                new StatusUsuario(1L, "Activo"), new Role(1L, "Administrador"),
                new Sucursal(1L, new Empresa(1L, 5)));
    }
}
