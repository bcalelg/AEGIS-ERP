package com.aegis.erp.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
@Configuration public class SecurityConfig {
/** Temporary development policy; replace when real authentication is implemented. */
@Bean SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception{return http.authorizeHttpRequests(a->a.requestMatchers("/api/health","/api/health/database").permitAll().anyRequest().denyAll()).build();}}
