package com.aegis.erp.modules.seguridad.auth.service;
import com.aegis.erp.common.exception.InvalidCredentialsException;
import com.aegis.erp.modules.seguridad.auth.dto.*;
import com.aegis.erp.modules.seguridad.auth.mapper.UsuarioAuthMapper;
import com.aegis.erp.modules.seguridad.usuario.entity.*;
import com.aegis.erp.modules.seguridad.usuario.repository.*;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class AuthenticationService {
static final String ACTIVO="Activo",INACTIVO="Inactivo",BLOQUEADO="Bloqueado por intentos de acceso";
static final String CONCEDIDO="Acceso Concedido",PASSWORD_INCORRECTO="Bloqueado - Password incorrecto",INTENTOS_EXCEDIDOS="Bloqueado - Numero de intentos exedidos",USUARIO_INACTIVO="Usuario Inactivo",USUARIO_NO_EXISTE="Usuario ingresado no existe";
private final UsuarioRepository usuarios;private final StatusUsuarioRepository statuses;private final PasswordEncoder encoder;private final AccessAuditService audit;private final UsuarioAuthMapper mapper;private final Clock clock;private final JwtTokenService jwtTokens;
public AuthenticationService(UsuarioRepository usuarios,StatusUsuarioRepository statuses,PasswordEncoder encoder,AccessAuditService audit,UsuarioAuthMapper mapper,Clock clock,JwtTokenService jwtTokens){this.usuarios=usuarios;this.statuses=statuses;this.encoder=encoder;this.audit=audit;this.mapper=mapper;this.clock=clock;this.jwtTokens=jwtTokens;}
@Transactional(noRollbackFor=InvalidCredentialsException.class)
public LoginResponse login(LoginRequest request,LoginClientContext context){
Usuario usuario=usuarios.findForAuthentication(request.idUsuario()).orElse(null);
if(usuario==null){audit.registrar(request.idUsuario(),USUARIO_NO_EXISTE,context);throw new InvalidCredentialsException();}
String estado=usuario.getStatus().getNombre();
if(INACTIVO.equals(estado)){audit.registrar(request.idUsuario(),USUARIO_INACTIVO,context);throw new InvalidCredentialsException();}
if(BLOQUEADO.equals(estado)){audit.registrar(request.idUsuario(),INTENTOS_EXCEDIDOS,context);throw new InvalidCredentialsException();}
if(!ACTIVO.equals(estado)){audit.registrar(request.idUsuario(),USUARIO_INACTIVO,context);throw new InvalidCredentialsException();}
if(!encoder.matches(request.password(),usuario.getPasswordHash())){registrarPasswordIncorrecto(usuario,context);throw new InvalidCredentialsException();}
usuario.registrarIngreso(LocalDateTime.now(clock));audit.registrar(usuario.getIdUsuario(),CONCEDIDO,context);var token=jwtTokens.issue(usuario);return mapper.toLoginResponse(usuario,token.value(),token.expiresInSeconds());}
private void registrarPasswordIncorrecto(Usuario usuario,LoginClientContext context){usuario.registrarIntentoFallido();Integer max=usuario.getSucursal().getEmpresa().getIntentosAntesDeBloquear();if(max==null||max<=0)throw new IllegalStateException("Política de intentos de acceso no configurada.");if(usuario.getIntentosAcceso()>=max){StatusUsuario bloqueado=statuses.findByNombre(BLOQUEADO).orElseThrow(()->new IllegalStateException("Catálogo STATUS_USUARIO incompleto."));usuario.bloquear(bloqueado);audit.registrar(usuario.getIdUsuario(),INTENTOS_EXCEDIDOS,context);}else{audit.registrar(usuario.getIdUsuario(),PASSWORD_INCORRECTO,context);}}}