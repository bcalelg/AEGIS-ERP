package com.aegis.erp.modules.seguridad.auth.controller;
import static org.mockito.Mockito.*;
import com.aegis.erp.modules.seguridad.auth.dto.*;
import com.aegis.erp.modules.seguridad.auth.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;import jakarta.servlet.http.HttpServletResponse;import com.aegis.erp.security.JwtCookieService;
import org.junit.jupiter.api.Test;
class AuthControllerTest {@Test void forwardsMetadataWithoutCreatingSession(){AuthenticationService s=mock(AuthenticationService.class);HttpServletRequest h=mock(HttpServletRequest.class);when(h.getHeader("User-Agent")).thenReturn("JUnit");when(h.getRemoteAddr()).thenReturn("127.0.0.1");LoginRequest r=new LoginRequest("Administrador","ITAdmin");when(s.login(eq(r),any())).thenReturn(new com.aegis.erp.modules.seguridad.auth.service.AuthenticatedLogin(new LoginResponse(true,3600,"Administrador","Administrador","IT","Administrador",true),"jwt"));JwtCookieService c=mock(JwtCookieService.class);HttpServletResponse response=mock(HttpServletResponse.class);new AuthController(s,c,mock(com.aegis.erp.modules.seguridad.auth.service.PasswordRecoveryService.class)).login(r,h,response);verify(h,never()).getSession(anyBoolean());verify(s).login(r,new LoginClientContext("JUnit","127.0.0.1",null));}}
