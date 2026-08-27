package com.aegis.erp.security;

import jakarta.servlet.http.*;

import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper m;

    public RestAccessDeniedHandler(ObjectMapper m) {
        this.m = m;
    }

    public void handle(HttpServletRequest q, HttpServletResponse r, AccessDeniedException e)
            throws IOException {
        var p =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.FORBIDDEN, "Token CSRF ausente o inválido.");
        p.setType(URI.create("urn:aegis-erp:problem:forbidden"));
        p.setTitle("Acceso denegado");
        r.setStatus(403);
        r.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        m.writeValue(r.getOutputStream(), p);
    }
}
