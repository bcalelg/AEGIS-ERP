package com.aegis.erp.modules.seguridad.empresa.controller;

import static org.mockito.ArgumentMatchers.any;
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
import com.aegis.erp.modules.seguridad.empresa.service.EmpresaService;
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

@WebMvcTest(EmpresaController.class)
@Import({
    EmpresaController.class,
    EmpresaSecurityTest.Support.class,
    SecurityConfig.class,
    JwtConfig.class,
    RestAuthenticationEntryPoint.class,
    JwtCookieService.class,
    RestAccessDeniedHandler.class
})
@TestPropertySource(properties = "jwt.secret=test-only-secret-with-at-least-32-bytes-long")
class EmpresaSecurityTest {
    @TestConfiguration
    @EnableWebSecurity
    static class Support {}

    @Autowired private MockMvc mvc;
    @Autowired private JwtEncoder encoder;
    @MockitoBean private EmpresaService service;

    @MockitoBean(name = "permissionAuthorizationService")
    private PermissionAuthorizationService permission;

    @Test
    void getSinConsultar403() throws Exception {
        deny();
        mvc.perform(get("/api/security/empresas").cookie(cookie()))
                .andExpect(status().isForbidden());
    }

    @Test
    void postSinAlta403() throws Exception {
        deny();
        mvc.perform(
                        post("/api/security/empresas")
                                .with(csrf())
                                .cookie(cookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json()))
                .andExpect(status().isForbidden());
    }

    @Test
    void putSinCambio403() throws Exception {
        deny();
        mvc.perform(
                        put("/api/security/empresas/1")
                                .with(csrf())
                                .cookie(cookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteSinBaja403() throws Exception {
        deny();
        mvc.perform(delete("/api/security/empresas/1").with(csrf()).cookie(cookie()))
                .andExpect(status().isForbidden());
    }

    @Test
    void imprimirSinPermisoDevuelve403() throws Exception {
        deny();

        mvc.perform(get("/api/security/empresas/print").cookie(cookie()))
                .andExpect(status().isForbidden());
    }

    @Test
    void exportacionesSinPermisoDevuelven403() throws Exception {
        deny();

        mvc.perform(get("/api/security/empresas/export/csv").cookie(cookie()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/security/empresas/export/excel").cookie(cookie()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/security/empresas/export/pdf").cookie(cookie()))
                .andExpect(status().isForbidden());
    }

    @Test
    void csvTieneHeadersYContenidoCorrectos() throws Exception {
        allow();
        when(service.exportarCsv(any())).thenReturn("\uFEFFID,Nombre\r\n".getBytes());

        mvc.perform(get("/api/security/empresas/export/csv").cookie(cookie()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=empresas.csv"))
                .andExpect(content().bytes("\uFEFFID,Nombre\r\n".getBytes()));
    }

    @Test
    void excelTieneHeadersYContenidoCorrectos() throws Exception {
        allow();
        byte[] xlsx = {0x50, 0x4B, 0x03, 0x04};
        when(service.exportarExcel(any())).thenReturn(xlsx);

        mvc.perform(get("/api/security/empresas/export/excel").cookie(cookie()))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .contentType(
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=empresas.xlsx"))
                .andExpect(content().bytes(xlsx));
    }

    @Test
    void pdfTieneHeadersYContenidoCorrectos() throws Exception {
        allow();
        byte[] pdf = "%PDF-".getBytes();
        when(service.exportarPdf(any())).thenReturn(pdf);

        mvc.perform(get("/api/security/empresas/export/pdf").cookie(cookie()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=empresas.pdf"))
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
                        .subject("usuario-prueba")
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(3600))
                        .claim("role", "Rol prueba")
                        .build();
        String token =
                encoder.encode(
                                JwtEncoderParameters.from(
                                        JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                        .getTokenValue();
        return new Cookie("AEGIS_ACCESS_TOKEN", token);
    }

    private String json() {
        return "{\"nombre\":\"A\",\"direccion\":\"D\",\"nit\":\"N\","
                + "\"passwordCantidadMayusculas\":1,\"passwordCantidadMinusculas\":1,"
                + "\"passwordCantidadCaracteresEspeciales\":1,"
                + "\"passwordCantidadCaducidadDias\":60,\"passwordLargo\":8,"
                + "\"passwordIntentosAntesDeBloquear\":5,\"passwordCantidadNumeros\":1,"
                + "\"passwordCantidadPreguntasValidar\":1}";
    }
}
