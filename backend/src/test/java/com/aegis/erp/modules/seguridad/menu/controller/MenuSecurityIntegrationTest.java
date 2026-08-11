package com.aegis.erp.modules.seguridad.menu.controller;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.aegis.erp.config.JwtConfig;
import com.aegis.erp.modules.seguridad.menu.dto.*;
import com.aegis.erp.modules.seguridad.menu.service.MenuService;
import com.aegis.erp.security.*;
import java.time.Instant;import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
@WebMvcTest(MenuController.class)
@Import({MenuController.class,MenuSecurityIntegrationTest.TestSecuritySupport.class,SecurityConfig.class,JwtConfig.class,RestAuthenticationEntryPoint.class})
@TestPropertySource(properties="jwt.secret=test-only-secret-with-at-least-32-bytes-long")
class MenuSecurityIntegrationTest {
@TestConfiguration @EnableWebSecurity static class TestSecuritySupport{}
@Autowired MockMvc mvc;@Autowired JwtEncoder encoder;@MockitoBean MenuService menuService;
@Test void menuWithoutTokenReturns401()throws Exception{mvc.perform(get("/api/security/menu")).andExpect(status().isUnauthorized());}
@Test void validJwtUsesSubjectAndReturnsMenu()throws Exception{var response=new ModuloMenuResponse(1L,"Seguridad",1,List.of(new MenuResponse(1L,"Parametros Generales",1,List.of(new OpcionResponse(1L,"Empresas","empresa.php",1,new PermisosResponse(true,true,true,true,true,true))))));when(menuService.findMenuForUser("Administrador")).thenReturn(List.of(response));mvc.perform(get("/api/security/menu").header("Authorization","Bearer "+token("Administrador"))).andExpect(status().isOk()).andExpect(jsonPath("$[0].menus[0].opciones[0].nombre").value("Empresas")).andExpect(jsonPath("$[0].menus[0].opciones[0].permisos.consultar").value(true));}
private String token(String subject){Instant now=Instant.now();var claims=JwtClaimsSet.builder().subject(subject).issuedAt(now).expiresAt(now.plusSeconds(3600)).claim("role","Administrador").build();return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();}}
