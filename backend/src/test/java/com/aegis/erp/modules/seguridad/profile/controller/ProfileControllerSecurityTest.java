package com.aegis.erp.modules.seguridad.profile.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.aegis.erp.config.JwtConfig;
import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.modules.seguridad.profile.dto.ProfileResponse;
import com.aegis.erp.modules.seguridad.profile.service.ProfilePhoto;
import com.aegis.erp.modules.seguridad.profile.service.ProfileService;
import com.aegis.erp.security.JwtCookieService;
import com.aegis.erp.security.RestAccessDeniedHandler;
import com.aegis.erp.security.RestAuthenticationEntryPoint;
import com.aegis.erp.security.SecurityConfig;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

@WebMvcTest(ProfileController.class)
@Import({
    ProfileController.class,
    ProfileControllerSecurityTest.Support.class,
    SecurityConfig.class,
    JwtConfig.class,
    RestAuthenticationEntryPoint.class,
    JwtCookieService.class,
    RestAccessDeniedHandler.class
})
@TestPropertySource(properties = "jwt.secret=test-only-secret-with-at-least-32-bytes-long")
class ProfileControllerSecurityTest {
    @TestConfiguration
    @EnableWebSecurity
    static class Support {}

    @Autowired private MockMvc mvc;
    @Autowired private JwtEncoder encoder;
    @MockitoBean private ProfileService profiles;

    @Test
    void noAutenticadoRecibe401() throws Exception {
        mvc.perform(get("/api/security/profile")).andExpect(status().isUnauthorized());
        verifyNoInteractions(profiles);
    }

    @Test
    void obtienePerfilPropioDesdeJwtSinPermisoAdministrativo() throws Exception {
        when(profiles.get("usuario-actual")).thenReturn(response());

        mvc.perform(get("/api/security/profile").cookie(cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value("usuario-actual"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.respuesta").doesNotExist());
        verify(profiles).get("usuario-actual");
    }

    @Test
    void noExisteRutaParaSeleccionarOtroUsuario() throws Exception {
        mvc.perform(get("/api/security/profile/otro").cookie(cookie()))
                .andExpect(status().isNotFound());
        verifyNoInteractions(profiles);
    }

    @Test
    void actualizaYSubeFotoUsandoIdentidadAutenticada() throws Exception {
        when(profiles.update(eq("usuario-actual"), any())).thenReturn(response());
        mvc.perform(
                        put("/api/security/profile")
                                .with(csrf())
                                .cookie(cookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(contactJson()))
                .andExpect(status().isOk());

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "photo.png",
                        "image/png",
                        new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47});
        when(profiles.updatePhoto(eq("usuario-actual"), any()))
                .thenReturn(new ProfilePhoto(file.getBytes(), "image/png"));
        mvc.perform(multipart("/api/security/profile/photo").file(file).with(csrf()).cookie(cookie()).with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));
    }

    @Test
    void rechazaCorreoInvalidoYTelefonoConLetrasAntesDelServicio() throws Exception {
        mvc.perform(
                        put("/api/security/profile")
                                .with(csrf())
                                .cookie(cookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"correoElectronico":"correo-invalido","telefonoMovil":"abc555"}
                                        """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(profiles);
    }

    @Test
    void rechazaOverPostingDeIdentidadOrganizacionYSeguridad() throws Exception {
        mvc.perform(
                        put("/api/security/profile")
                                .with(csrf())
                                .cookie(cookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "correoElectronico":"nuevo@example.com",
                                          "telefonoMovil":"+502 5555-5555",
                                          "idUsuario":"otro",
                                          "nombre":"Administrador",
                                          "apellido":"Manipulado",
                                          "fechaNacimiento":"2000-01-01",
                                          "idGenero":9,
                                          "idEmpresa":9,
                                          "idSucursal":99,
                                          "idRole":1,
                                          "idStatusUsuario":1,
                                          "password":"alterada",
                                          "pregunta":"alterada",
                                          "respuesta":"alterada",
                                          "sesionActual":"alterada"
                                        }
                                        """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(profiles);
    }

    @Test
    void correoDeOtroUsuarioResponde409() throws Exception {
        when(profiles.update(eq("usuario-actual"), any()))
                .thenThrow(
                        new BusinessConflictException(
                                "Ya existe un usuario con ese correo electrónico."));

        mvc.perform(
                        put("/api/security/profile")
                                .with(csrf())
                                .cookie(cookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(contactJson()))
                .andExpect(status().isConflict());
    }

    @Test
    void recuperaYEliminaFotografiaPropia() throws Exception {
        when(profiles.photo("usuario-actual"))
                .thenReturn(Optional.of(new ProfilePhoto(new byte[] {1, 2}, "image/jpeg")));
        mvc.perform(get("/api/security/profile/photo").cookie(cookie()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"));
        mvc.perform(delete("/api/security/profile/photo").with(csrf()).cookie(cookie()))
                .andExpect(status().isNoContent());
        verify(profiles).deletePhoto("usuario-actual");
    }

    private ProfileResponse response() {
        return new ProfileResponse(
                "usuario-actual",
                "Marco",
                "Lorenzana",
                "marco@example.com",
                "555-1000",
                LocalDate.of(1990, 1, 1),
                "Masculino",
                "Activo",
                "Software Inc.",
                "Central",
                "Operador",
                false,
                null);
    }

    private String contactJson() {
        return """
                {"correoElectronico":"marco@example.com","telefonoMovil":"555-1000"}
                """;
    }

    private Cookie cookie() {
        Instant now = Instant.now();
        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .subject("usuario-actual")
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(3600))
                        .claim("role", "Sin Opciones")
                        .build();
        String token =
                encoder.encode(
                                JwtEncoderParameters.from(
                                        JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                        .getTokenValue();
        return new Cookie("AEGIS_ACCESS_TOKEN", token);
    }
}
