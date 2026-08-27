package com.aegis.erp.modules.seguridad.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(max = 50) String idUsuario, @NotBlank @Size(max = 200) String password) {}
