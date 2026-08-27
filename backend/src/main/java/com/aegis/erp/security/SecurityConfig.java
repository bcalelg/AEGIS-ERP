package com.aegis.erp.security;

import org.springframework.context.annotation.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.web.csrf.*;

import java.text.Normalizer;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RestAuthenticationEntryPoint entry,
            RestAccessDeniedHandler denied,
            JwtCookieService cookie,
            ObjectProvider<SessionAuthorizationService> sessionServices)
            throws Exception {
        var repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repo.setCookiePath("/");
        repo.setHeaderName("X-XSRF-TOKEN");
        var handler = new CsrfTokenRequestAttributeHandler();
        handler.setCsrfRequestAttributeName(null);
        return http.csrf(c -> c.csrfTokenRepository(repo).csrfTokenRequestHandler(handler))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        a ->
                                a.requestMatchers(
                                                "/api/health",
                                                "/api/health/database",
                                                "/api/auth/login",
                                                "/api/auth/csrf",
                                                "/api/auth/forgot-password",
                                                "/api/auth/reset-password")
                                        .permitAll()
                                        .requestMatchers(
                                                "/api/auth/me",
                                                "/api/auth/logout",
                                                "/api/auth/change-password")
                                        .access(
                                                (authentication, context) ->
                                                        new AuthorizationDecision(
                                                                authenticatedWithValidSession(
                                                                        authentication.get(),
                                                                        sessionServices.getIfAvailable())))
                                        .anyRequest()
                                        .access(
                                                (authentication, context) -> {
                                                    var current = authentication.get();
                                                    boolean allowed =
                                                            authenticatedWithValidSession(
                                                                            current,
                                                                            sessionServices.getIfAvailable())
                                                                    && current.getAuthorities().stream()
                                                                            .noneMatch(
                                                                                    authority ->
                                                                                            authority.getAuthority()
                                                                                                    .equals("PASSWORD_CHANGE_REQUIRED"));
                                                    return new AuthorizationDecision(allowed);
                                                }))
                .oauth2ResourceServer(
                        r ->
                                r.bearerTokenResolver(
                                                new CookieBearerTokenResolver(cookie.cookieName()))
                                        .jwt(j -> j.jwtAuthenticationConverter(converter()))
                                        .authenticationEntryPoint(entry))
                .exceptionHandling(
                        e -> e.authenticationEntryPoint(entry).accessDeniedHandler(denied))
                .build();
    }

    private boolean authenticatedWithValidSession(
            org.springframework.security.core.Authentication authentication,
            SessionAuthorizationService sessions) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication
                        instanceof org.springframework.security.authentication.AnonymousAuthenticationToken
                || !(authentication instanceof JwtAuthenticationToken jwt)) {
            return false;
        }
        return sessions == null || sessions.valid(jwt);
    }

    private Converter<Jwt, AbstractAuthenticationToken> converter() {
        return jwt -> {
            String role = jwt.getClaimAsString("role");
            var authorities = new java.util.ArrayList<org.springframework.security.core.GrantedAuthority>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + normalize(role)));
            if (Boolean.TRUE.equals(jwt.getClaimAsBoolean("password_change_required"))) {
                authorities.add(new SimpleGrantedAuthority("PASSWORD_CHANGE_REQUIRED"));
            }
            return new JwtAuthenticationToken(
                    jwt,
                    authorities,
                    jwt.getSubject());
        };
    }

    private String normalize(String role) {
        if (role == null || role.isBlank()) return "UNASSIGNED";
        return Normalizer.normalize(role, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(java.util.Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
