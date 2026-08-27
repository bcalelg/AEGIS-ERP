package com.aegis.erp.security;

import jakarta.servlet.http.*;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

public class CookieBearerTokenResolver implements BearerTokenResolver {
    private final String name;

    public CookieBearerTokenResolver(String name) {
        this.name = name;
    }

    public String resolve(HttpServletRequest r) {
        String path = r.getRequestURI();
        if (path.equals("/api/auth/login")
                || path.equals("/api/auth/csrf")
                || path.equals("/api/auth/forgot-password")
                || path.equals("/api/auth/reset-password")) {
            return null;
        }
        if (r.getCookies() != null)
            for (Cookie c : r.getCookies()) if (name.equals(c.getName())) return c.getValue();
        return null;
    }
}
