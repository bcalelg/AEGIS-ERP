package com.aegis.erp.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class JwtCookieService {
    private final String name;
    private final boolean secure;
    private final String sameSite;

    public JwtCookieService(
            @Value("${jwt.cookie.name:AEGIS_ACCESS_TOKEN}") String name,
            @Value("${jwt.cookie.secure:false}") boolean secure,
            @Value("${jwt.cookie.same-site:Lax}") String sameSite) {
        this.name = name;
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public String cookieName() {
        return name;
    }

    public void write(HttpServletResponse r, String token, long seconds) {
        add(r, token, Duration.ofSeconds(seconds));
    }

    public void clear(HttpServletResponse r) {
        add(r, "", Duration.ZERO);
    }

    private void add(HttpServletResponse r, String value, Duration age) {
        r.addHeader(
                HttpHeaders.SET_COOKIE,
                ResponseCookie.from(name, value)
                        .httpOnly(true)
                        .secure(secure)
                        .sameSite(sameSite)
                        .path("/")
                        .maxAge(age)
                        .build()
                        .toString());
    }
}
