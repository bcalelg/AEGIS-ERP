package com.aegis.erp.modules.seguridad.auth.service;

import com.aegis.erp.common.exception.InvalidPasswordResetTokenException;
import com.aegis.erp.modules.seguridad.usuario.entity.Usuario;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Service
public class PasswordRecoveryTokenService {
    static final String AUDIENCE = "aegis-erp-password-reset";
    static final String PURPOSE = "password-recovery";
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final Clock clock;
    private final Duration expiration;
    private final String issuer;

    public PasswordRecoveryTokenService(
            Clock clock,
            @Value("${password-recovery.secret}") String configuredSecret,
            @Value("${password-recovery.expiration-minutes:15}") long expirationMinutes,
            @Value("${password-recovery.issuer:aegis-erp-password-recovery}") String issuer) {
        if (configuredSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "PASSWORD_RECOVERY_SECRET debe contener al menos 32 bytes.");
        }
        if (expirationMinutes <= 0 || expirationMinutes > 60) {
            throw new IllegalArgumentException(
                    "PASSWORD_RECOVERY_EXPIRATION_MINUTES debe estar entre 1 y 60.");
        }
        this.clock = clock;
        this.expiration = Duration.ofMinutes(expirationMinutes);
        this.issuer = issuer;
        SecretKey key = derivedKey(configuredSecret);
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        NimbusJwtDecoder jwtDecoder =
                NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        JwtTimestampValidator timestampValidator = new JwtTimestampValidator();
        timestampValidator.setClock(clock);
        jwtDecoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        timestampValidator,
                        new JwtIssuerValidator(issuer),
                        this::validatePurpose));
        this.decoder = jwtDecoder;
    }

    public String issue(Usuario usuario) {
        Instant now = clock.instant();
        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(issuer)
                        .subject(usuario.getIdUsuario())
                        .audience(List.of(AUDIENCE))
                        .issuedAt(now)
                        .expiresAt(now.plus(expiration))
                        .id(UUID.randomUUID().toString())
                        .claim("purpose", PURPOSE)
                        .claim("password_fingerprint", fingerprint(usuario.getPasswordHash()))
                        .build();
        return encoder
                .encode(
                        JwtEncoderParameters.from(
                                JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(), claims))
                .getTokenValue();
    }

    public RecoveryClaims decode(String token) {
        try {
            Jwt jwt = decoder.decode(token);
            String subject = jwt.getSubject();
            String fingerprint = jwt.getClaimAsString("password_fingerprint");
            if (subject == null || subject.isBlank() || fingerprint == null || fingerprint.isBlank()) {
                throw new InvalidPasswordResetTokenException();
            }
            return new RecoveryClaims(subject, fingerprint);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new InvalidPasswordResetTokenException();
        }
    }

    public void validateFingerprint(RecoveryClaims claims, Usuario usuario) {
        byte[] supplied = claims.passwordFingerprint().getBytes(StandardCharsets.US_ASCII);
        byte[] current = fingerprint(usuario.getPasswordHash()).getBytes(StandardCharsets.US_ASCII);
        if (!claims.subject().equals(usuario.getIdUsuario())
                || !MessageDigest.isEqual(supplied, current)) {
            throw new InvalidPasswordResetTokenException();
        }
    }

    private OAuth2TokenValidatorResult validatePurpose(Jwt jwt) {
        boolean valid =
                jwt.getAudience().contains(AUDIENCE)
                        && PURPOSE.equals(jwt.getClaimAsString("purpose"));
        return valid
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error(
                                "invalid_token",
                                "El token no corresponde a recuperación de contraseña.",
                                null));
    }

    private SecretKey derivedKey(String source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update("AEGIS-ERP:PASSWORD-RECOVERY\0".getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(
                    digest.digest(source.getBytes(StandardCharsets.UTF_8)), "HmacSHA256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no está disponible.", exception);
        }
    }

    private String fingerprint(String passwordHash) {
        try {
            byte[] value =
                    MessageDigest.getInstance("SHA-256")
                            .digest(passwordHash.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no está disponible.", exception);
        }
    }

    public record RecoveryClaims(String subject, String passwordFingerprint) {}
}
