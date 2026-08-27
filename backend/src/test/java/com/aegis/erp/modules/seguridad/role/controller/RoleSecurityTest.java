package com.aegis.erp.modules.seguridad.role.controller;

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
import com.aegis.erp.modules.seguridad.role.service.RoleService;
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

@WebMvcTest(RoleController.class)
@Import({
    RoleController.class,
    RoleSecurityTest.Support.class,
    SecurityConfig.class,
    JwtConfig.class,
    RestAuthenticationEntryPoint.class,
    JwtCookieService.class,
    RestAccessDeniedHandler.class
})
@TestPropertySource(properties = "jwt.secret=test-only-secret-with-at-least-32-bytes-long")
class RoleSecurityTest {
    @TestConfiguration
    @EnableWebSecurity
    static class Support {}

    @Autowired private MockMvc mvc;
    @Autowired private JwtEncoder encoder;
    @MockitoBean private RoleService service;

    @MockitoBean(name = "permissionAuthorizationService")
    private PermissionAuthorizationService permission;

    @Test
    void consultarAltaCambioBajaImprimirYExportarSinPermisoDevuelven403() throws Exception {
        when(permission.allowed(anyString(), anyString(), anyString())).thenReturn(false);
        mvc.perform(get("/api/security/roles").cookie(cookie())).andExpect(status().isForbidden());
        mvc.perform(
                        post("/api/security/roles")
                                .with(csrf())
                                .cookie(cookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"nombre\":\"Temporal\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(
                        put("/api/security/roles/1")
                                .with(csrf())
                                .cookie(cookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"nombre\":\"Temporal\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/security/roles/1").with(csrf()).cookie(cookie()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/security/roles/print").cookie(cookie()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/security/roles/export/csv").cookie(cookie()))
                .andExpect(status().isForbidden());
    }

    @Test
    void exportacionesPermitidasTienenFormatoYNombreCorrectos() throws Exception {
        when(permission.allowed(anyString(), anyString(), anyString())).thenReturn(true);
        when(service.exportarCsv(any())).thenReturn("\uFEFFID,Nombre\r\n".getBytes());
        when(service.exportarExcel(any())).thenReturn(new byte[] {0x50, 0x4B});
        when(service.exportarPdf(any())).thenReturn("%PDF-".getBytes());

        mvc.perform(get("/api/security/roles/export/csv").cookie(cookie()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=roles.csv"));
        mvc.perform(get("/api/security/roles/export/excel").cookie(cookie()))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .contentType(
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=roles.xlsx"));
        mvc.perform(get("/api/security/roles/export/pdf").cookie(cookie()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=roles.pdf"));
    }

    private Cookie cookie() {
        Instant now = Instant.now();
        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .subject("prueba-role")
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
