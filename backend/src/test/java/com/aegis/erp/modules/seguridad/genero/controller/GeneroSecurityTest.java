package com.aegis.erp.modules.seguridad.genero.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aegis.erp.config.JwtConfig;
import com.aegis.erp.modules.seguridad.genero.service.GeneroService;
import com.aegis.erp.security.JwtCookieService;
import com.aegis.erp.security.PermissionAuthorizationService;
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
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

@WebMvcTest(GeneroController.class)
@Import({
    GeneroController.class,
    GeneroSecurityTest.Support.class,
    SecurityConfig.class,
    JwtConfig.class,
    RestAuthenticationEntryPoint.class,
    JwtCookieService.class,
    RestAccessDeniedHandler.class
})
@TestPropertySource(properties = "jwt.secret=test-only-secret-with-at-least-32-bytes-long")
class GeneroSecurityTest {
    @TestConfiguration
    @EnableWebSecurity
    static class Support {}

    @Autowired private MockMvc mvc;
    @Autowired private JwtEncoder encoder;
    @MockitoBean private GeneroService service;

    @MockitoBean(name = "permissionAuthorizationService")
    private PermissionAuthorizationService permission;

    @Test
    void listarSinConsultarDevuelve403() throws Exception {
        deny();
        mvc.perform(get("/api/security/generos").cookie(cookie()))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearSinAltaDevuelve403() throws Exception {
        deny();
        mvc.perform(
                        post("/api/security/generos")
                                .with(csrf())
                                .cookie(cookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"nombre\":\"Temporal\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void modificarSinCambioDevuelve403() throws Exception {
        deny();
        mvc.perform(
                        put("/api/security/generos/1")
                                .with(csrf())
                                .cookie(cookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"nombre\":\"Temporal\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void eliminarSinBajaDevuelve403() throws Exception {
        deny();
        mvc.perform(delete("/api/security/generos/1").with(csrf()).cookie(cookie()))
                .andExpect(status().isForbidden());
    }

    @Test
    void imprimirSinPermisoDevuelve403() throws Exception {
        deny();
        mvc.perform(get("/api/security/generos/print").cookie(cookie()))
                .andExpect(status().isForbidden());
    }

    @Test
    void exportarSinPermisoDevuelve403() throws Exception {
        deny();

        mvc.perform(get("/api/security/generos/export/csv").cookie(cookie()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/security/generos/export/excel").cookie(cookie()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/security/generos/export/pdf").cookie(cookie()))
                .andExpect(status().isForbidden());
    }

    @Test
    void csvTieneHeadersYContenidoCorrectos() throws Exception {
        allow();
        byte[] csv = "\uFEFFID,Nombre\r\n".getBytes();
        when(service.exportarCsv(org.mockito.ArgumentMatchers.any())).thenReturn(csv);

        mvc.perform(get("/api/security/generos/export/csv").cookie(cookie()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=generos.csv"))
                .andExpect(content().bytes(csv));
    }

    @Test
    void excelTieneHeadersYContenidoCorrectos() throws Exception {
        allow();
        byte[] xlsx = {0x50, 0x4B, 0x03, 0x04};
        when(service.exportarExcel(org.mockito.ArgumentMatchers.any())).thenReturn(xlsx);

        mvc.perform(get("/api/security/generos/export/excel").cookie(cookie()))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .contentType(
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=generos.xlsx"))
                .andExpect(content().bytes(xlsx));
    }

    @Test
    void pdfTieneHeadersYContenidoCorrectos() throws Exception {
        allow();
        byte[] pdf = "%PDF-".getBytes();
        when(service.exportarPdf(org.mockito.ArgumentMatchers.any())).thenReturn(pdf);

        mvc.perform(get("/api/security/generos/export/pdf").cookie(cookie()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=generos.pdf"))
                .andExpect(content().bytes(pdf));
    }

    private void deny() {
        when(permission.allowed(anyString(), anyString(), anyString())).thenReturn(false);
    }

    private void allow() {
        when(permission.allowed(anyString(), anyString(), anyString())).thenReturn(true);
    }

    private Cookie cookie() {
        Instant now = Instant.now();
        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .subject("sin-permiso")
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
