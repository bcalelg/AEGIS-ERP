package com.aegis.erp.modules.seguridad.auth.service;

import com.aegis.erp.modules.seguridad.auth.dto.LoginResponse;

public record AuthenticatedLogin(LoginResponse response, String accessToken) {}
