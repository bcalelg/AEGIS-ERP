package com.aegis.erp.modules.seguridad.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.aegis.erp.common.exception.InvalidPasswordChangeException;
import com.aegis.erp.modules.seguridad.auth.dto.*;
import com.aegis.erp.modules.seguridad.empresa.entity.Empresa;
import com.aegis.erp.modules.seguridad.usuario.entity.*;
import com.aegis.erp.modules.seguridad.usuario.repository.UsuarioRepository;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import java.time.*;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {
    private static final String SECRET = "test-only-recovery-secret-with-at-least-32-bytes";
    @Mock private UsuarioRepository usuarios;
    @Mock private PasswordRecoveryMailSender mail;
    @Mock private Empresa empresa;
    @Mock private Sucursal sucursal;
    @Mock private StatusUsuario status;
    @Mock private Role role;
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-19T12:00:00Z"), ZoneOffset.UTC);
    private PasswordRecoveryTokenService tokens;
    private PasswordRecoveryService service;
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setup() {
        tokens = new PasswordRecoveryTokenService(clock, SECRET, 15, "test-recovery");
        encoder = new BCryptPasswordEncoder();
        service = new PasswordRecoveryService(
                usuarios, tokens, mail, new PasswordRecoveryThrottle(clock, 60), encoder, clock);
        lenient().when(status.getNombre()).thenReturn("Activo");
        lenient().when(sucursal.getEmpresa()).thenReturn(empresa);
        lenient().when(empresa.getPasswordLargo()).thenReturn(8);
        lenient().when(empresa.getPasswordCantidadMayusculas()).thenReturn(1);
        lenient().when(empresa.getPasswordCantidadMinusculas()).thenReturn(1);
        lenient().when(empresa.getPasswordCantidadNumeros()).thenReturn(2);
        lenient().when(empresa.getPasswordCantidadCaracteresEspeciales()).thenReturn(1);
    }

    @Test
    void respuestaEsGenericaParaCuentaExistenteOInexistente() {
        Usuario usuario = usuario();
        when(usuarios.findForPasswordRecovery("TEST_LOGIN")).thenReturn(Optional.of(usuario));
        var existing = service.request(new ForgotPasswordRequest("TEST_LOGIN"));
        var missing = service.request(new ForgotPasswordRequest("NO_EXISTE"));

        assertThat(existing.message()).isEqualTo(missing.message());
        verify(mail).send(eq("test@example.com"), eq("Usuario"), anyString());
    }

    @Test
    void noEnviaParaUsuarioInactivoOSinCorreo() {
        Usuario inactivo = usuario("test@example.com");
        when(status.getNombre()).thenReturn("Inactivo");
        when(usuarios.findForPasswordRecovery("INACTIVO")).thenReturn(Optional.of(inactivo));

        var inactiveResponse = service.request(new ForgotPasswordRequest("INACTIVO"));

        when(status.getNombre()).thenReturn("Activo");
        when(usuarios.findForPasswordRecovery("SIN_CORREO"))
                .thenReturn(Optional.of(usuario(null)));
        var noMailResponse = service.request(new ForgotPasswordRequest("SIN_CORREO"));

        assertThat(inactiveResponse.message()).isEqualTo(noMailResponse.message());
        verifyNoInteractions(mail);
    }

    @Test
    void throttleMantieneRespuestaGenericaYSoloIntentaUnEnvio() {
        when(usuarios.findForPasswordRecovery("TEST_LOGIN"))
                .thenReturn(Optional.of(usuario()));

        var first = service.request(new ForgotPasswordRequest("TEST_LOGIN"));
        var throttled = service.request(new ForgotPasswordRequest("TEST_LOGIN"));

        assertThat(throttled.message()).isEqualTo(first.message());
        verify(mail, times(1)).send(anyString(), anyString(), anyString());
    }

    @Test
    void falloSmtpSeRegistraSinDatosSensiblesYConservaRespuestaGenerica() {
        var logger =
                (ch.qos.logback.classic.Logger)
                        LoggerFactory.getLogger(PasswordRecoveryService.class);
        var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        when(usuarios.findForPasswordRecovery("TEST_LOGIN"))
                .thenReturn(Optional.of(usuario()));
        doThrow(new RuntimeException("token-completo test@example.com"))
                .when(mail)
                .send(anyString(), anyString(), anyString());

        try {
            var response = service.request(new ForgotPasswordRequest("TEST_LOGIN"));

            assertThat(response.message()).isEqualTo(PasswordRecoveryService.GENERIC_MESSAGE);
            assertThat(appender.list).hasSize(1);
            assertThat(appender.list.getFirst().getFormattedMessage())
                    .contains("correlationId=", "cause=RuntimeException")
                    .doesNotContain("token-completo", "test@example.com");
            assertThat(appender.list.getFirst().getThrowableProxy()).isNull();
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    void restableceConBCryptPoliticaFechaYSesionNulaEInvalidaReuso() {
        Usuario usuario = usuario();
        usuario.registrarIngreso(LocalDateTime.now(clock), "session-before-reset");
        String token = tokens.issue(usuario);
        when(usuarios.findForPasswordRecoveryForUpdate("TEST_LOGIN"))
                .thenReturn(Optional.of(usuario));

        service.reset(new ResetPasswordRequest(token, "NuevaClave12!", "NuevaClave12!"));

        assertThat(encoder.matches("NuevaClave12!", usuario.getPasswordHash())).isTrue();
        assertThat(usuario.getUltimaFechaCambioPassword()).isEqualTo(LocalDateTime.now(clock));
        assertThat(usuario.getRequiereCambiarPassword()).isZero();
        assertThat(usuario.getSesionActual()).isNull();
        assertThatThrownBy(
                        () ->
                                service.reset(
                                        new ResetPasswordRequest(
                                                token, "OtraClave34!", "OtraClave34!")))
                .isInstanceOf(com.aegis.erp.common.exception.InvalidPasswordResetTokenException.class);
    }

    @Test
    void rechazaConfirmacionPoliticaYPasswordActual() {
        assertThatThrownBy(
                        () ->
                                service.reset(
                                        new ResetPasswordRequest(
                                                "token", "NuevaClave12!", "Diferente12!")))
                .isInstanceOf(InvalidPasswordChangeException.class);
        Usuario usuario = usuario();
        String token = tokens.issue(usuario);
        when(usuarios.findForPasswordRecoveryForUpdate("TEST_LOGIN"))
                .thenReturn(Optional.of(usuario));
        assertThatThrownBy(
                        () ->
                                service.reset(
                                        new ResetPasswordRequest(token, "debil", "debil")))
                .isInstanceOf(InvalidPasswordChangeException.class);
    }

    private Usuario usuario() {
        return usuario("test@example.com");
    }

    private Usuario usuario(String email) {
        return new Usuario(
                "TEST_LOGIN",
                "Usuario",
                "Pruebas",
                encoder.encode("Temporal12!"),
                0,
                1,
                status,
                role,
                sucursal) {
                    @Override
                    public String getCorreoElectronico() {
                        return email;
                    }
                };
    }
}
