package com.aegis.erp.security;

import com.aegis.erp.modules.seguridad.usuario.repository.UsuarioRepository;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionAuthorizationService {
    private final UsuarioRepository usuarios;

    public SessionAuthorizationService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public boolean valid(JwtAuthenticationToken authentication) {
        String sessionId = authentication.getToken().getId();
        return sessionId != null
                && !sessionId.isBlank()
                && usuarios.existsByIdUsuarioAndSesionActual(authentication.getName(), sessionId);
    }
}
