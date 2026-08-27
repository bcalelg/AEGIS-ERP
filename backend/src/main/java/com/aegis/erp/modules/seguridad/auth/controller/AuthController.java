package com.aegis.erp.modules.seguridad.auth.controller;

import com.aegis.erp.modules.seguridad.auth.dto.*;
import com.aegis.erp.modules.seguridad.auth.service.AuthenticationService;
import com.aegis.erp.modules.seguridad.auth.service.PasswordRecoveryService;
import com.aegis.erp.common.exception.InvalidCredentialsException;
import com.aegis.erp.security.JwtCookieService;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService auth;
    private final JwtCookieService cookie;
    private final PasswordRecoveryService recovery;

    public AuthController(
            AuthenticationService a,
            JwtCookieService c,
            PasswordRecoveryService recovery) {
        auth = a;
        cookie = c;
        this.recovery = recovery;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest q, HttpServletRequest h, HttpServletResponse r) {
        var result = auth.login(q, clientContext(h, null));
        cookie.write(r, result.accessToken(), result.response().expiresIn());
        return result.response();
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken t) {
        return new CsrfResponse(t.getHeaderName(), t.getToken());
    }

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        return recovery.request(request);
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        recovery.reset(request);
    }

    @PostMapping("/logout")
    public void logout(
            JwtAuthenticationToken authentication,
            HttpServletRequest request,
            HttpServletResponse response) {
        String sessionId = requiredSessionId(authentication);
        auth.logout(
                authentication.getName(),
                sessionId,
                clientContext(request, sessionId));
        cookie.clear(response);
    }

    @PostMapping("/change-password")
    public LoginResponse changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            JwtAuthenticationToken authentication,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        var result =
                auth.changePassword(
                        authentication.getName(),
                        request,
                        clientContext(httpRequest, requiredSessionId(authentication)));
        cookie.write(response, result.accessToken(), result.response().expiresIn());
        return result.response();
    }

    @GetMapping("/me")
    public CurrentUserResponse me(JwtAuthenticationToken authentication) {
        return auth.currentUser(authentication.getName());
    }

    private LoginClientContext clientContext(
            HttpServletRequest request,
            String sessionId) {
        return new LoginClientContext(
                request.getHeader("User-Agent"),
                request.getRemoteAddr(),
                sessionId);
    }

    private String requiredSessionId(JwtAuthenticationToken authentication) {
        String sessionId = authentication.getToken().getId();
        if (sessionId == null || sessionId.isBlank()) {
            throw new InvalidCredentialsException();
        }
        return sessionId;
    }
}
