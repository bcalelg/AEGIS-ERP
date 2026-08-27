package com.aegis.erp.modules.seguridad.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aegis.erp.common.exception.InvalidPasswordChangeException;
import com.aegis.erp.modules.seguridad.auth.dto.ChangePasswordRequest;
import com.aegis.erp.modules.seguridad.auth.dto.LoginClientContext;
import com.aegis.erp.modules.seguridad.auth.dto.LoginRequest;
import com.aegis.erp.modules.seguridad.auth.mapper.UsuarioAuthMapper;
import com.aegis.erp.modules.seguridad.empresa.entity.Empresa;
import com.aegis.erp.modules.seguridad.usuario.entity.Role;
import com.aegis.erp.modules.seguridad.usuario.entity.StatusUsuario;
import com.aegis.erp.modules.seguridad.usuario.entity.Sucursal;
import com.aegis.erp.modules.seguridad.usuario.entity.Usuario;
import com.aegis.erp.modules.seguridad.usuario.repository.StatusUsuarioRepository;
import com.aegis.erp.modules.seguridad.usuario.repository.UsuarioRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationLifecycleServiceTest {
    private static final String USER = "Administrador";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 14, 4, 0);

    @Mock private UsuarioRepository usuarios;
    @Mock private StatusUsuarioRepository statuses;
    @Mock private PasswordEncoder encoder;
    @Mock private AccessAuditService audit;
    @Mock private JwtTokenService jwtTokens;

    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-14T04:00:00Z"), ZoneOffset.UTC);
        service =
                new AuthenticationService(
                        usuarios,
                        statuses,
                        encoder,
                        audit,
                        new UsuarioAuthMapper(),
                        clock,
                        jwtTokens);
    }

    @Test
    void expiredPasswordRequiresChangeDuringSuccessfulLogin() {
        Usuario usuario = usuario(61, false);
        when(usuarios.findForAuthentication(USER)).thenReturn(Optional.of(usuario));
        when(encoder.matches("Actual1!", "old-hash")).thenReturn(true);
        when(jwtTokens.issue(eq(usuario), anyString()))
                .thenReturn(new JwtTokenService.IssuedToken("jwt", 3600));

        var login =
                service.login(
                        new LoginRequest(USER, "Actual1!"),
                        new LoginClientContext("JUnit", "127.0.0.1", null));

        assertThat(login.response().requiereCambiarPassword()).isTrue();
        assertThat(usuario.getSesionActual()).isNotBlank();
    }

    @Test
    void currentPasswordWithoutChangeDateRequiresChange() {
        Usuario usuario = usuarioWithoutPasswordDate();
        when(usuarios.findForAuthentication(USER)).thenReturn(Optional.of(usuario));
        when(encoder.matches("Actual1!", "old-hash")).thenReturn(true);
        when(jwtTokens.issue(eq(usuario), anyString()))
                .thenReturn(new JwtTokenService.IssuedToken("jwt", 3600));

        var login =
                service.login(
                        new LoginRequest(USER, "Actual1!"),
                        new LoginClientContext("JUnit", "127.0.0.1", null));

        assertThat(login.response().requiereCambiarPassword()).isTrue();
    }

    @Test
    void logoutClearsCurrentSessionAndAuditsExit() {
        Usuario usuario = usuario(1, false);
        when(usuarios.findForAuthentication(USER)).thenReturn(Optional.of(usuario));

        service.logout(
                USER,
                "current-session",
                new LoginClientContext("JUnit", "127.0.0.1", "current-session"));

        assertThat(usuario.getSesionActual()).isNull();
        verify(audit)
                .registrar(
                        eq(USER),
                        eq(AuthenticationService.SALIDA),
                        eq(new LoginClientContext("JUnit", "127.0.0.1", "current-session")));
    }

    @Test
    void successfulPasswordChangeUpdatesHashDateFlagAndSession() {
        Usuario usuario = usuario(1, true);
        when(usuarios.findForAuthentication(USER)).thenReturn(Optional.of(usuario));
        when(encoder.matches("Actual1!", "old-hash")).thenReturn(true);
        when(encoder.matches("Nueva2@Segura", "old-hash")).thenReturn(false);
        when(encoder.encode("Nueva2@Segura")).thenReturn("new-bcrypt-hash");
        when(jwtTokens.issue(eq(usuario), anyString()))
                .thenReturn(new JwtTokenService.IssuedToken("jwt", 3600));

        var result =
                service.changePassword(
                        USER,
                        new ChangePasswordRequest(
                                "Actual1!",
                                "Nueva2@Segura",
                                "Nueva2@Segura"),
                        new LoginClientContext("JUnit", "127.0.0.1", "current-session"));

        assertThat(usuario.getPasswordHash()).isEqualTo("new-bcrypt-hash");
        assertThat(usuario.getRequiereCambiarPassword()).isZero();
        assertThat(usuario.getUltimaFechaCambioPassword()).isEqualTo(NOW);
        assertThat(usuario.getSesionActual()).isNotEqualTo("current-session");
        assertThat(result.response().requiereCambiarPassword()).isFalse();
    }

    @Test
    void incorrectCurrentPasswordIsRejected() {
        Usuario usuario = usuario(1, true);
        when(usuarios.findForAuthentication(USER)).thenReturn(Optional.of(usuario));
        when(encoder.matches("Incorrecta", "old-hash")).thenReturn(false);

        assertThatThrownBy(
                        () ->
                                service.changePassword(
                                        USER,
                                        new ChangePasswordRequest(
                                                "Incorrecta",
                                                "Nueva2@Segura",
                                                "Nueva2@Segura"),
                                        new LoginClientContext(
                                                "JUnit",
                                                "127.0.0.1",
                                                "current-session")))
                .isInstanceOf(InvalidPasswordChangeException.class);
    }

    @Test
    void mismatchedConfirmationIsRejected() {
        Usuario usuario = usuario(1, true);
        when(usuarios.findForAuthentication(USER)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(
                        () ->
                                service.changePassword(
                                        USER,
                                        new ChangePasswordRequest(
                                                "Actual1!",
                                                "Nueva2@Segura",
                                                "Otra2@Segura"),
                                        new LoginClientContext(
                                                "JUnit",
                                                "127.0.0.1",
                                                "current-session")))
                .isInstanceOf(InvalidPasswordChangeException.class);
    }

    @Test
    void passwordOutsideCompanyPolicyIsRejected() {
        Usuario usuario = usuario(1, true);
        when(usuarios.findForAuthentication(USER)).thenReturn(Optional.of(usuario));
        when(encoder.matches("Actual1!", "old-hash")).thenReturn(true);
        when(encoder.matches("debil", "old-hash")).thenReturn(false);

        assertThatThrownBy(
                        () ->
                                service.changePassword(
                                        USER,
                                        new ChangePasswordRequest(
                                                "Actual1!",
                                                "debil",
                                                "debil"),
                                        new LoginClientContext(
                                                "JUnit",
                                                "127.0.0.1",
                                                "current-session")))
                .isInstanceOf(InvalidPasswordChangeException.class)
                .hasMessageContaining("política");
    }

    private Usuario usuario(int passwordAgeDays, boolean requiresChange) {
        Usuario usuario = baseUser(requiresChange ? 1 : 0);
        usuario.cambiarPassword(
                "old-hash",
                NOW.minusDays(passwordAgeDays),
                "current-session");
        if (requiresChange) {
            usuario.requerirCambioPassword();
        }
        return usuario;
    }

    private Usuario usuarioWithoutPasswordDate() {
        return baseUser(0);
    }

    private Usuario baseUser(int requiresChange) {
        Empresa empresa =
                Empresa.crear(
                        "Empresa",
                        "Dirección",
                        "NIT",
                        1,
                        1,
                        1,
                        60,
                        8,
                        5,
                        1,
                        1,
                        "system",
                        NOW.minusYears(1));
        return new Usuario(
                USER,
                "Administrador",
                "IT",
                "old-hash",
                0,
                requiresChange,
                new StatusUsuario(1L, AuthenticationService.ACTIVO),
                new Role(1L, "Administrador"),
                new Sucursal(1L, empresa));
    }
}
