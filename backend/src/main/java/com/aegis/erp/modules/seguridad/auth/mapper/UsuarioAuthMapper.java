package com.aegis.erp.modules.seguridad.auth.mapper;

import com.aegis.erp.modules.seguridad.auth.dto.CurrentUserResponse;
import com.aegis.erp.modules.seguridad.auth.dto.LoginResponse;
import com.aegis.erp.modules.seguridad.usuario.entity.Usuario;

import org.springframework.stereotype.Component;

@Component
public class UsuarioAuthMapper {
    public LoginResponse toLoginResponse(Usuario u, long e) {
        return new LoginResponse(
                true,
                e,
                u.getIdUsuario(),
                u.getNombre(),
                u.getApellido(),
                u.getRole().getNombre(),
                Integer.valueOf(1).equals(u.getRequiereCambiarPassword()));
    }

    public CurrentUserResponse toCurrentUserResponse(Usuario u) {
        return new CurrentUserResponse(
                u.getIdUsuario(),
                u.getNombre(),
                u.getApellido(),
                u.getRole().getNombre(),
                Integer.valueOf(1).equals(u.getRequiereCambiarPassword()));
    }
}
