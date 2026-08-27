package com.aegis.erp.modules.seguridad.auth.service;

import com.aegis.erp.common.exception.*;
import com.aegis.erp.modules.seguridad.auth.dto.*;
import com.aegis.erp.modules.seguridad.usuario.entity.Usuario;
import com.aegis.erp.modules.seguridad.usuario.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.UUID;

@Service
public class PasswordRecoveryService {
    private static final Logger log = LoggerFactory.getLogger(PasswordRecoveryService.class);
    static final String GENERIC_MESSAGE =
            "Si la cuenta existe y posee un correo registrado, recibirás instrucciones para restablecer la contraseña.";
    private final UsuarioRepository usuarios;
    private final PasswordRecoveryTokenService tokens;
    private final PasswordRecoveryMailSender mail;
    private final PasswordRecoveryThrottle throttle;
    private final PasswordEncoder encoder;
    private final Clock clock;

    public PasswordRecoveryService(
            UsuarioRepository usuarios,
            PasswordRecoveryTokenService tokens,
            PasswordRecoveryMailSender mail,
            PasswordRecoveryThrottle throttle,
            PasswordEncoder encoder,
            Clock clock) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.mail = mail;
        this.throttle = throttle;
        this.encoder = encoder;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ForgotPasswordResponse request(ForgotPasswordRequest request) {
        String identifier = request.identifier().trim();
        if (!throttle.allow(identifier)) return new ForgotPasswordResponse(GENERIC_MESSAGE);
        usuarios.findForPasswordRecovery(identifier)
                .filter(usuario -> "Activo".equals(usuario.getStatus().getNombre()))
                .filter(usuario -> usuario.getCorreoElectronico() != null && !usuario.getCorreoElectronico().isBlank())
                .ifPresent(this::sendSafely);
        return new ForgotPasswordResponse(GENERIC_MESSAGE);
    }

    @Transactional
    public void reset(ResetPasswordRequest request) {
        if (!request.passwordNueva().equals(request.passwordConfirmacion())) {
            throw new InvalidPasswordChangeException(
                    "La nueva contraseña y su confirmación no coinciden.");
        }
        PasswordRecoveryTokenService.RecoveryClaims claims = tokens.decode(request.token());
        Usuario usuario =
                usuarios.findForPasswordRecoveryForUpdate(claims.subject())
                        .orElseThrow(InvalidPasswordResetTokenException::new);
        tokens.validateFingerprint(claims, usuario);
        if (encoder.matches(request.passwordNueva(), usuario.getPasswordHash())) {
            throw new InvalidPasswordChangeException(
                    "La nueva contraseña debe ser diferente de la contraseña actual.");
        }
        PasswordPolicyValidator.validateForChange(
                usuario.getSucursal().getEmpresa(), request.passwordNueva());
        usuario.restablecerPassword(
                encoder.encode(request.passwordNueva()),
                LocalDateTime.now(clock),
                usuario.getIdUsuario());
    }

    private void sendSafely(Usuario usuario) {
        String correlationId = UUID.randomUUID().toString();
        try {
            mail.send(
                    usuario.getCorreoElectronico(),
                    usuario.getNombre(),
                    tokens.issue(usuario));
        } catch (RuntimeException exception) {
            log.error(
                    "No fue posible enviar el correo de recuperación. correlationId={} cause={}",
                    correlationId,
                    exception.getClass().getSimpleName());
        }
    }
}
