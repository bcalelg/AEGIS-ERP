package com.aegis.erp.modules.seguridad.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank String passwordActual,
        @NotBlank String passwordNueva,
        @NotBlank String passwordConfirmacion) {}
