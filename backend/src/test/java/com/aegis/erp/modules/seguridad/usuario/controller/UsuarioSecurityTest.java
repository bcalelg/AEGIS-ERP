package com.aegis.erp.modules.seguridad.usuario.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aegis.erp.config.JwtConfig;
import com.aegis.erp.modules.seguridad.usuario.service.UsuarioMaintenanceService;
import com.aegis.erp.security.*;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

@WebMvcTest(UsuarioController.class)
@Import({UsuarioController.class, UsuarioSecurityTest.Support.class, SecurityConfig.class, JwtConfig.class,
        RestAuthenticationEntryPoint.class, JwtCookieService.class, RestAccessDeniedHandler.class})
@TestPropertySource(properties = "jwt.secret=test-only-secret-with-at-least-32-bytes-long")
class UsuarioSecurityTest {
    @TestConfiguration @EnableWebSecurity static class Support {}
    @Autowired private MockMvc mvc;
    @Autowired private JwtEncoder encoder;
    @MockitoBean private UsuarioMaintenanceService service;
    @MockitoBean(name = "permissionAuthorizationService") private PermissionAuthorizationService permission;

    @Test
    void seisOperacionesYOptionsRequierenPermisoDeUsuario() throws Exception {
        when(permission.allowed(anyString(), anyString(), anyString())).thenReturn(false);
        mvc.perform(get("/api/security/usuarios").cookie(cookie())).andExpect(status().isForbidden());
        mvc.perform(get("/api/security/usuarios/options/empresas").cookie(cookie())).andExpect(status().isForbidden());
        mvc.perform(post("/api/security/usuarios").with(csrf()).cookie(cookie()).contentType(MediaType.APPLICATION_JSON).content(createJson()))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/security/usuarios/test").with(csrf()).cookie(cookie()).contentType(MediaType.APPLICATION_JSON).content(updateJson()))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/security/usuarios/test").with(csrf()).cookie(cookie())).andExpect(status().isForbidden());
        mvc.perform(get("/api/security/usuarios/print").cookie(cookie())).andExpect(status().isForbidden());
        mvc.perform(get("/api/security/usuarios/export/csv").cookie(cookie())).andExpect(status().isForbidden());
    }

    @Test
    void datosInvalidosSeRechazanCon400AntesDeInvocarServicio() throws Exception {
        when(permission.allowed(anyString(), eq("usuario"), eq("ALTA"))).thenReturn(true);
        mvc.perform(post("/api/security/usuarios").with(csrf()).cookie(cookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson().replace("1990-01-01", "2020-01-01")
                                .replace("test@example.com", "correo-invalido")
                                .replace("+502 5555-5555", "abc555")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    private Cookie cookie() {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().subject("prueba-usuario").issuedAt(now)
                .expiresAt(now.plusSeconds(3600)).claim("role", "Sin Opciones").build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new Cookie("AEGIS_ACCESS_TOKEN", token);
    }

    private String createJson() {
        return """
                {"idUsuario":"test","nombre":"Prueba","apellido":"Usuario","fechaNacimiento":"1990-01-01",
                 "correoElectronico":"test@example.com","telefonoMovil":"+502 5555-5555",
                 "password":"Temporal12!","passwordConfirmacion":"Temporal12!","pregunta":"Pregunta","respuesta":"Respuesta",
                 "idEmpresa":1,"idSucursal":1,"idGenero":1,"idStatusUsuario":1,"idRole":1}
                """;
    }

    private String updateJson() {
        return """
                {"nombre":"Prueba","apellido":"Usuario","fechaNacimiento":"1990-01-01","pregunta":"Pregunta",
                 "correoElectronico":"test@example.com","telefonoMovil":"+502 5555-5555",
                 "idEmpresa":1,"idSucursal":1,"idGenero":1,"idStatusUsuario":1,"idRole":1}
                """;
    }
}
