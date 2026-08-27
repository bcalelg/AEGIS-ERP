package com.aegis.erp.modules.seguridad.auth.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;import static org.mockito.ArgumentMatchers.any;import static org.mockito.ArgumentMatchers.eq;import static org.mockito.Mockito.verify;import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aegis.erp.config.JwtConfig;
import com.aegis.erp.modules.seguridad.auth.service.AuthenticationService;
import com.aegis.erp.modules.seguridad.auth.service.PasswordRecoveryService;
import com.aegis.erp.security.RestAuthenticationEntryPoint;
import com.aegis.erp.security.SecurityConfig;import com.aegis.erp.security.JwtCookieService;import com.aegis.erp.security.RestAccessDeniedHandler;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({AuthController.class, AuthSecurityIntegrationTest.TestSecuritySupport.class, SecurityConfig.class,
        JwtConfig.class, RestAuthenticationEntryPoint.class,JwtCookieService.class,RestAccessDeniedHandler.class})
@TestPropertySource(properties = "jwt.secret=test-only-secret-with-at-least-32-bytes-long")
class AuthSecurityIntegrationTest {
    @TestConfiguration
    @EnableWebSecurity
    static class TestSecuritySupport {}
    @Autowired MockMvc mvc;
    @Autowired JwtEncoder encoder;
    @MockitoBean AuthenticationService authenticationService;
    @MockitoBean PasswordRecoveryService passwordRecoveryService;

    @BeforeEach
    void currentUser() {
        when(authenticationService.currentUser("Administrador"))
                .thenReturn(
                        new com.aegis.erp.modules.seguridad.auth.dto.CurrentUserResponse(
                                "Administrador",
                                "Administrador",
                                "IT",
                                "Administrador",
                                false));
    }

    @Test
    void protectedEndpointWithoutTokenReturnsJson401() throws Exception {
        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("No autorizado"));
    }

    @Test
    void protectedEndpointWithInvalidTokenReturns401() throws Exception {
        mvc.perform(get("/api/auth/me").cookie(new jakarta.servlet.http.Cookie("AEGIS_ACCESS_TOKEN", "invalid-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Se requiere una cookie de autenticación válida."));
    }

    @Test
    void protectedEndpointWithValidTokenReturnsPrincipalClaims() throws Exception {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("Administrador")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .id("session-id")
                .claim("role", "Administrador")
                .claim("password_change_required", false)
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        mvc.perform(get("/api/auth/me").cookie(new jakarta.servlet.http.Cookie("AEGIS_ACCESS_TOKEN", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value("Administrador"))
                .andExpect(jsonPath("$.role").value("Administrador"));
    }
@Test void loginWritesHttpOnlyCookieWithoutJwtInBody()throws Exception{var response=new com.aegis.erp.modules.seguridad.auth.dto.LoginResponse(true,3600,"Administrador","Administrador","IT","Administrador",true);when(authenticationService.login(any(),any())).thenReturn(new com.aegis.erp.modules.seguridad.auth.service.AuthenticatedLogin(response,"signed.jwt.value"));mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"idUsuario\":\"Administrador\",\"password\":\"ITAdmin\"}")).andExpect(status().isOk()).andExpect(header().string("Set-Cookie",org.hamcrest.Matchers.allOf(org.hamcrest.Matchers.containsString("AEGIS_ACCESS_TOKEN="),org.hamcrest.Matchers.containsString("HttpOnly"),org.hamcrest.Matchers.containsString("SameSite=Lax")))).andExpect(jsonPath("$.accessToken").doesNotExist()).andExpect(jsonPath("$.tokenType").doesNotExist());}
@Test void loginWithoutCsrfReturns403()throws Exception{mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"idUsuario\":\"Administrador\",\"password\":\"ITAdmin\"}")).andExpect(status().isForbidden());}
@Test void logoutWithoutAuthenticationIsRejected()throws Exception{mvc.perform(post("/api/auth/logout").with(csrf())).andExpect(status().isUnauthorized());}
@Test void changePasswordIsAllowedWhileMandatoryChangeIsActive()throws Exception{Instant now=Instant.now();var claims=JwtClaimsSet.builder().subject("Administrador").issuedAt(now).expiresAt(now.plusSeconds(3600)).id("session-id").claim("role","Administrador").claim("password_change_required",true).build();String token=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();var response=new com.aegis.erp.modules.seguridad.auth.dto.LoginResponse(true,3600,"Administrador","Administrador","IT","Administrador",false);when(authenticationService.changePassword(eq("Administrador"),any(),any())).thenReturn(new com.aegis.erp.modules.seguridad.auth.service.AuthenticatedLogin(response,"renewed.jwt"));mvc.perform(post("/api/auth/change-password").with(csrf()).cookie(new jakarta.servlet.http.Cookie("AEGIS_ACCESS_TOKEN",token)).contentType(MediaType.APPLICATION_JSON).content("{\"passwordActual\":\"Actual1!\",\"passwordNueva\":\"Nueva2@Segura\",\"passwordConfirmacion\":\"Nueva2@Segura\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.requiereCambiarPassword").value(false)).andExpect(jsonPath("$.password").doesNotExist()).andExpect(jsonPath("$.accessToken").doesNotExist()).andExpect(header().string("Set-Cookie",org.hamcrest.Matchers.containsString("HttpOnly")));}
@Test void logoutWithCookieAndCsrfDeletesCookie()throws Exception{Instant now=Instant.now();var claims=JwtClaimsSet.builder().subject("Administrador").issuedAt(now).expiresAt(now.plusSeconds(3600)).id("session-id").claim("role","Administrador").claim("password_change_required",false).build();String token=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();mvc.perform(post("/api/auth/logout").with(csrf()).cookie(new jakarta.servlet.http.Cookie("AEGIS_ACCESS_TOKEN",token))).andExpect(status().isOk()).andExpect(header().string("Set-Cookie",org.hamcrest.Matchers.containsString("Max-Age=0")));verify(authenticationService).logout(eq("Administrador"),eq("session-id"),any());}
@Test void recoveryEndpointsArePublicButKeepCsrfAndIgnoreStaleCookie()throws Exception{when(passwordRecoveryService.request(any())).thenReturn(new com.aegis.erp.modules.seguridad.auth.dto.ForgotPasswordResponse("Respuesta genérica"));mvc.perform(post("/api/auth/forgot-password").with(csrf()).cookie(new jakarta.servlet.http.Cookie("AEGIS_ACCESS_TOKEN","stale-token")).contentType(MediaType.APPLICATION_JSON).content("{\"identifier\":\"usuario@example.com\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Respuesta genérica"));mvc.perform(post("/api/auth/reset-password").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"recovery-token\",\"passwordNueva\":\"NuevaClave12!\",\"passwordConfirmacion\":\"NuevaClave12!\"}")).andExpect(status().isOk());verify(passwordRecoveryService).reset(any());mvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content("{\"identifier\":\"usuario\"}")).andExpect(status().isForbidden());}
}
