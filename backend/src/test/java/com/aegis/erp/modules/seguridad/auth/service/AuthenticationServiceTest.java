package com.aegis.erp.modules.seguridad.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.aegis.erp.common.exception.InvalidCredentialsException;
import com.aegis.erp.modules.seguridad.auth.dto.*;
import com.aegis.erp.modules.seguridad.auth.mapper.UsuarioAuthMapper;
import com.aegis.erp.modules.seguridad.usuario.entity.*;
import com.aegis.erp.modules.seguridad.usuario.repository.*;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
@Mock UsuarioRepository usuarios; @Mock StatusUsuarioRepository statuses; @Mock PasswordEncoder encoder; @Mock AccessAuditService audit;
@Mock JwtTokenService jwtTokens;
AuthenticationService service; UsuarioAuthMapper mapper=new UsuarioAuthMapper(); Clock clock=Clock.fixed(Instant.parse("2026-08-08T03:00:00Z"),ZoneOffset.UTC); LoginClientContext context=new LoginClientContext("JUnit","127.0.0.1",null);
StatusUsuario activo=new StatusUsuario(1L,AuthenticationService.ACTIVO),inactivo=new StatusUsuario(3L,AuthenticationService.INACTIVO),bloqueado=new StatusUsuario(2L,AuthenticationService.BLOQUEADO);
@BeforeEach void setup(){service=new AuthenticationService(usuarios,statuses,encoder,audit,mapper,clock,jwtTokens);}
@Test void loginExitoso(){Usuario u=usuario(activo,2,5);when(usuarios.findForAuthentication("Administrador")).thenReturn(Optional.of(u));when(encoder.matches("ITAdmin","hash")).thenReturn(true);when(jwtTokens.issue(u)).thenReturn(new JwtTokenService.IssuedToken("jwt",3600));assertThat(service.login(new LoginRequest("Administrador","ITAdmin"),context)).isEqualTo(new LoginResponse(true,"Bearer","jwt",3600,"Administrador","Administrador","IT","Administrador",true));assertThat(u.getIntentosAcceso()).isZero();assertThat(u.getUltimaFechaIngreso()).isEqualTo(LocalDateTime.now(clock));verify(audit).registrar("Administrador",AuthenticationService.CONCEDIDO,context);}
@Test void usuarioInexistente(){when(usuarios.findForAuthentication("desconocido")).thenReturn(Optional.empty());assertThatThrownBy(()->service.login(new LoginRequest("desconocido","clave"),context)).isInstanceOf(InvalidCredentialsException.class).hasMessage("Credenciales inválidas.");verify(audit).registrar("desconocido",AuthenticationService.USUARIO_NO_EXISTE,context);verifyNoInteractions(encoder);}
@Test void passwordIncorrecto(){Usuario u=usuario(activo,0,5);when(usuarios.findForAuthentication("Administrador")).thenReturn(Optional.of(u));when(encoder.matches("mala","hash")).thenReturn(false);assertThatThrownBy(()->service.login(new LoginRequest("Administrador","mala"),context)).isInstanceOf(InvalidCredentialsException.class);assertThat(u.getIntentosAcceso()).isEqualTo(1);verify(audit).registrar("Administrador",AuthenticationService.PASSWORD_INCORRECTO,context);}
@Test void usuarioInactivo(){Usuario u=usuario(inactivo,0,5);when(usuarios.findForAuthentication("Administrador")).thenReturn(Optional.of(u));assertThatThrownBy(()->service.login(new LoginRequest("Administrador","ITAdmin"),context)).isInstanceOf(InvalidCredentialsException.class);verify(audit).registrar("Administrador",AuthenticationService.USUARIO_INACTIVO,context);verifyNoInteractions(encoder);}
@Test void incrementaIntentosAntesDelLimite(){Usuario u=usuario(activo,3,5);when(usuarios.findForAuthentication("Administrador")).thenReturn(Optional.of(u));when(encoder.matches(anyString(),anyString())).thenReturn(false);assertThatThrownBy(()->service.login(new LoginRequest("Administrador","mala"),context)).isInstanceOf(InvalidCredentialsException.class);assertThat(u.getIntentosAcceso()).isEqualTo(4);assertThat(u.getStatus()).isSameAs(activo);verifyNoInteractions(statuses);}
@Test void bloqueaAlAlcanzarLimite(){Usuario u=usuario(activo,4,5);when(usuarios.findForAuthentication("Administrador")).thenReturn(Optional.of(u));when(encoder.matches(anyString(),anyString())).thenReturn(false);when(statuses.findByNombre(AuthenticationService.BLOQUEADO)).thenReturn(Optional.of(bloqueado));assertThatThrownBy(()->service.login(new LoginRequest("Administrador","mala"),context)).isInstanceOf(InvalidCredentialsException.class);assertThat(u.getIntentosAcceso()).isEqualTo(5);assertThat(u.getStatus()).isSameAs(bloqueado);verify(audit).registrar("Administrador",AuthenticationService.INTENTOS_EXCEDIDOS,context);}
private Usuario usuario(StatusUsuario s,int intentos,int limite){return new Usuario("Administrador","Administrador","IT","hash",intentos,1,s,new Role(1L,"Administrador"),new Sucursal(1L,new Empresa(1L,limite)));}}