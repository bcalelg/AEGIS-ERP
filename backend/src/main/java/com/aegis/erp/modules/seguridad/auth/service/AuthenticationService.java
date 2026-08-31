package com.aegis.erp.modules.seguridad.auth.service;

import com.aegis.erp.common.exception.InvalidCredentialsException;
import com.aegis.erp.common.exception.InvalidPasswordChangeException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.modules.seguridad.auth.dto.*;
import com.aegis.erp.modules.seguridad.auth.mapper.UsuarioAuthMapper;
import com.aegis.erp.modules.seguridad.usuario.entity.*;
import com.aegis.erp.modules.seguridad.usuario.repository.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    static final String ACTIVO = "Activo",
            INACTIVO = "Inactivo",
            BLOQUEADO = "Bloqueado por intentos de acceso";
    static final String CONCEDIDO = "Acceso Concedido",
            SALIDA = "Salida del Sistema",
            PASSWORD_INCORRECTO = "Bloqueado - Password incorrecto",
            INTENTOS_EXCEDIDOS = "Bloqueado - Numero de intentos exedidos",
            USUARIO_INACTIVO = "Usuario Inactivo",
            USUARIO_NO_EXISTE = "Usuario ingresado no existe";
    private final UsuarioRepository usuarios;
    private final StatusUsuarioRepository statuses;
    private final PasswordEncoder encoder;
    private final AccessAuditService audit;
    private final UsuarioAuthMapper mapper;
    private final Clock clock;
    private final JwtTokenService jwtTokens;

    public AuthenticationService(
            UsuarioRepository usuarios,
            StatusUsuarioRepository statuses,
            PasswordEncoder encoder,
            AccessAuditService audit,
            UsuarioAuthMapper mapper,
            Clock clock,
            JwtTokenService jwtTokens) {
        this.usuarios = usuarios;
        this.statuses = statuses;
        this.encoder = encoder;
        this.audit = audit;
        this.mapper = mapper;
        this.clock = clock;
        this.jwtTokens = jwtTokens;
    }

    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public AuthenticatedLogin login(LoginRequest request, LoginClientContext context) {
        Usuario usuario = usuarios.findForAuthentication(request.idUsuario()).orElse(null);
        if (usuario == null) {
            reject(request.idUsuario(), USUARIO_NO_EXISTE, context);
        }
        String estado = usuario.getStatus().getNombre();
        if (INACTIVO.equals(estado)) {
            reject(request.idUsuario(), USUARIO_INACTIVO, context);
        }
        if (BLOQUEADO.equals(estado)) {
            reject(request.idUsuario(), INTENTOS_EXCEDIDOS, context);
        }
        if (!ACTIVO.equals(estado)) {
            reject(request.idUsuario(), USUARIO_INACTIVO, context);
        }
        if (!encoder.matches(request.password(), usuario.getPasswordHash())) {
            registrarPasswordIncorrecto(usuario, context);
            throw new InvalidCredentialsException();
        }
        LocalDateTime now = LocalDateTime.now(clock);
        evaluarCaducidad(usuario, now);
        String identificadorSesion = UUID.randomUUID().toString();
        LoginClientContext sessionContext = context.withSession(identificadorSesion);
        usuario.registrarIngreso(now, identificadorSesion);
        audit.registrar(usuario.getIdUsuario(), CONCEDIDO, sessionContext);
        var token = jwtTokens.issue(usuario, identificadorSesion);
        return new AuthenticatedLogin(
                mapper.toLoginResponse(usuario, token.expiresInSeconds()), token.value());
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser(String idUsuario) {
        return usuarios.findCurrentUser(idUsuario)
                .map(mapper::toCurrentUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    }

    @Transactional
    public void logout(
            String idUsuario,
            String identificadorSesion,
            LoginClientContext context) {
        Usuario usuario = findForUpdate(idUsuario);
        if (!usuario.cerrarSesion(identificadorSesion)) {
            throw new InvalidCredentialsException();
        }
        audit.registrar(idUsuario, SALIDA, context.withSession(identificadorSesion));
    }

    @Transactional
    public AuthenticatedLogin changePassword(
            String idUsuario,
            ChangePasswordRequest request,
            LoginClientContext context) {
        Usuario usuario = findForUpdate(idUsuario);
        validarCambioPassword(usuario, request);

        LocalDateTime now = LocalDateTime.now(clock);
        String nuevaSesion = UUID.randomUUID().toString();
        usuario.cambiarPassword(encoder.encode(request.passwordNueva()), now, nuevaSesion);
        var token = jwtTokens.issue(usuario, nuevaSesion);
        return new AuthenticatedLogin(
                mapper.toLoginResponse(usuario, token.expiresInSeconds()), token.value());
    }

    private Usuario findForUpdate(String idUsuario) {
        return usuarios.findForAuthentication(idUsuario)
                .orElseThrow(InvalidCredentialsException::new);
    }

    private void evaluarCaducidad(Usuario usuario, LocalDateTime now) {
        Integer dias = usuario.getSucursal().getEmpresa().getPasswordCantidadCaducidadDias();
        if (dias == null || dias <= 0) {
            throw new IllegalStateException("Política de caducidad no configurada.");
        }
        LocalDateTime ultimaFecha = usuario.getUltimaFechaCambioPassword();
        if (ultimaFecha == null || !now.isBefore(ultimaFecha.plusDays(dias))) {
            usuario.requerirCambioPassword();
        }
    }

    private void validarCambioPassword(Usuario usuario, ChangePasswordRequest request) {
        if (!request.passwordNueva().equals(request.passwordConfirmacion())) {
            throw new InvalidPasswordChangeException(
                    "La nueva contraseña y su confirmación no coinciden.");
        }
        if (!encoder.matches(request.passwordActual(), usuario.getPasswordHash())) {
            throw new InvalidPasswordChangeException("La contraseña actual no es válida.");
        }
        if (encoder.matches(request.passwordNueva(), usuario.getPasswordHash())) {
            throw new InvalidPasswordChangeException(
                    "La nueva contraseña debe ser diferente de la actual.");
        }
        validarPolitica(usuario, request.passwordNueva());
    }

    private void validarPolitica(Usuario usuario, String password) {
        PasswordPolicyValidator.validateForChange(usuario.getSucursal().getEmpresa(), password);
    }

    private void registrarPasswordIncorrecto(Usuario usuario, LoginClientContext context) {
        usuario.registrarIntentoFallido();
        Integer max = usuario.getSucursal().getEmpresa().getIntentosAntesDeBloquear();
        if (max == null || max <= 0)
            throw new IllegalStateException("Política de intentos de acceso no configurada.");
        if (usuario.getIntentosAcceso() >= max) {
            StatusUsuario bloqueado =
                    statuses.findByNombre(BLOQUEADO)
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Catálogo STATUS_USUARIO incompleto."));
            usuario.bloquear(bloqueado);
            audit.registrar(usuario.getIdUsuario(), INTENTOS_EXCEDIDOS, context);
        } else {
            audit.registrar(usuario.getIdUsuario(), PASSWORD_INCORRECTO, context);
        }
    }

    private void reject(String idUsuario, String event, LoginClientContext context) {
        try {
            audit.registrar(idUsuario, event, context);
        } catch (RuntimeException exception) {
            // La auditoría no debe convertir credenciales inválidas en un error 500 ni revelar
            // el identificador proporcionado por el cliente en los logs.
            log.error(
                    "No fue posible registrar un acceso rechazado. event={} cause={}",
                    event,
                    exception.getClass().getSimpleName());
        }
        throw new InvalidCredentialsException();
    }
}
