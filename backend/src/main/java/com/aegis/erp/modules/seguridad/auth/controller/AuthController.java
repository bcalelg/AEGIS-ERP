package com.aegis.erp.modules.seguridad.auth.controller;
import com.aegis.erp.modules.seguridad.auth.dto.*;
import com.aegis.erp.modules.seguridad.auth.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth") public class AuthController {
private final AuthenticationService authentication;public AuthController(AuthenticationService authentication){this.authentication=authentication;}
@PostMapping("/login") public LoginResponse login(@Valid @RequestBody LoginRequest request,HttpServletRequest http){var session=http.getSession(false);var context=new LoginClientContext(http.getHeader("User-Agent"),http.getRemoteAddr(),session==null?null:session.getId());return authentication.login(request,context);}}