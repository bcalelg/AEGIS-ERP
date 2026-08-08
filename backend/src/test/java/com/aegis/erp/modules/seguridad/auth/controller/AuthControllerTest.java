package com.aegis.erp.modules.seguridad.auth.controller;
import static org.mockito.Mockito.*;
import com.aegis.erp.modules.seguridad.auth.dto.*;
import com.aegis.erp.modules.seguridad.auth.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
class AuthControllerTest {@Test void forwardsMetadataWithoutCreatingSession(){AuthenticationService s=mock(AuthenticationService.class);HttpServletRequest h=mock(HttpServletRequest.class);when(h.getHeader("User-Agent")).thenReturn("JUnit");when(h.getRemoteAddr()).thenReturn("127.0.0.1");LoginRequest r=new LoginRequest("Administrador","ITAdmin");new AuthController(s).login(r,h);verify(h).getSession(false);verify(s).login(r,new LoginClientContext("JUnit","127.0.0.1",null));}}