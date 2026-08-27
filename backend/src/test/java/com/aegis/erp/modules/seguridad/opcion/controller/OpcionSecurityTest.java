package com.aegis.erp.modules.seguridad.opcion.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aegis.erp.config.JwtConfig;
import com.aegis.erp.modules.seguridad.opcion.service.OpcionService;
import com.aegis.erp.modules.seguridad.opcion.dto.OpcionUpdateRequest;
import com.aegis.erp.security.JwtCookieService;
import com.aegis.erp.security.PermissionAuthorizationService;
import com.aegis.erp.security.RestAccessDeniedHandler;
import com.aegis.erp.security.RestAuthenticationEntryPoint;
import com.aegis.erp.security.SecurityConfig;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

@WebMvcTest(OpcionController.class)
@Import({
    OpcionController.class,
    OpcionSecurityTest.Support.class,
    SecurityConfig.class,
    JwtConfig.class,
    RestAuthenticationEntryPoint.class,
    JwtCookieService.class,
    RestAccessDeniedHandler.class
})
@TestPropertySource(properties = "jwt.secret=test-only-secret-with-at-least-32-bytes-long")
class OpcionSecurityTest {
    @TestConfiguration
    @EnableWebSecurity
    static class Support {}

    @Autowired private MockMvc mvc;
    @Autowired private JwtEncoder encoder;
    @MockitoBean private OpcionService service;

    @MockitoBean(name = "permissionAuthorizationService")
    private PermissionAuthorizationService permission;

    @Test
    void consultaAltaSelectorImpresionYExportacionSinPermisoDevuelven403() throws Exception {
        when(permission.allowed(anyString(), anyString(), anyString())).thenReturn(false);
        mvc.perform(get("/api/security/opciones").cookie(cookie())).andExpect(status().isForbidden());
        mvc.perform(get("/api/security/opciones/options/menus").cookie(cookie()))
                .andExpect(status().isForbidden());
        mvc.perform(
                        post("/api/security/opciones")
                                .with(csrf())
                                .cookie(cookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"idMenu\":1,\"nombre\":\"Nueva\",\"pagina\":\"nueva.php\",\"orden\":9}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/security/opciones/print").cookie(cookie()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/security/opciones/export/csv").cookie(cookie()))
                .andExpect(status().isForbidden());
    }

    @Test
    void selectorYExportacionesPermitidosUsanNombresCorrectos() throws Exception {
        when(permission.allowed(anyString(), anyString(), anyString())).thenReturn(true);
        when(service.exportarCsv(any())).thenReturn(new byte[0]);
        when(service.exportarExcel(any())).thenReturn(new byte[0]);
        when(service.exportarPdf(any())).thenReturn(new byte[0]);
        mvc.perform(get("/api/security/opciones/options/menus").cookie(cookie()))
                .andExpect(status().isOk());
        mvc.perform(get("/api/security/opciones/export/csv").cookie(cookie()))
                .andExpect(header().string("Content-Disposition", "attachment; filename=opciones.csv"));
        mvc.perform(get("/api/security/opciones/export/excel").cookie(cookie()))
                .andExpect(header().string("Content-Disposition", "attachment; filename=opciones.xlsx"));
        mvc.perform(get("/api/security/opciones/export/pdf").cookie(cookie()))
                .andExpect(header().string("Content-Disposition", "attachment; filename=opciones.pdf"));
    }

    @Test
    void actualizacionNoIncorporaPaginaAunqueElClienteLaEnvie() throws Exception {
        when(permission.allowed(anyString(), anyString(), anyString())).thenReturn(true);
        mvc.perform(
                        put("/api/security/opciones/7")
                                .with(csrf())
                                .cookie(cookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"idMenu\":2,\"nombre\":\"Administración de Empresas\",\"pagina\":\"ruta_inventada\",\"orden\":3}"))
                .andExpect(status().isOk());

        ArgumentCaptor<OpcionUpdateRequest> request = ArgumentCaptor.forClass(OpcionUpdateRequest.class);
        verify(service).modificar(org.mockito.ArgumentMatchers.eq(7L), request.capture(), anyString());
        org.assertj.core.api.Assertions.assertThat(request.getValue())
                .isEqualTo(new OpcionUpdateRequest(2L, "Administración de Empresas", 3));
    }

    private Cookie cookie() {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("prueba-opcion")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("role", "Sin Opciones")
                .build();
        String token = encoder.encode(
                        JwtEncoderParameters.from(
                                JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new Cookie("AEGIS_ACCESS_TOKEN", token);
    }
}
