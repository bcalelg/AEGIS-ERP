package com.aegis.erp.modules.seguridad.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank String passwordNueva,
        @NotBlank String passwordConfirmacion) {}
