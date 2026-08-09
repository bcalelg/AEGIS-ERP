package com.aegis.erp.modules.seguridad.auth.service;

import com.aegis.erp.modules.seguridad.usuario.entity.Usuario;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    private final JwtEncoder encoder;
    private final Clock clock;
    private final Duration expiration;

    public JwtTokenService(
            JwtEncoder encoder,
            Clock clock,
            @Value("${jwt.expiration-minutes:60}") long expirationMinutes) {
        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException("JWT_EXPIRATION_MINUTES debe ser mayor que cero.");
        }
        this.encoder = encoder;
        this.clock = clock;
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    public IssuedToken issue(Usuario usuario) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(expiration);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(usuario.getIdUsuario())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("role", usuario.getRole().getNombre())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String value = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(value, expiration.toSeconds());
    }

    public record IssuedToken(String value, long expiresInSeconds) {}
}
