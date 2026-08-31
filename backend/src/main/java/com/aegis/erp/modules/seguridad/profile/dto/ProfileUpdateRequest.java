package com.aegis.erp.modules.seguridad.profile.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank @Email @Size(max = 100) String correoElectronico,
        @Size(max = 30)
                @Pattern(
                        regexp = "^[0-9+()\\-\\s]+$",
                        message =
                                "El teléfono solo puede contener números, espacios, +, -, ( y ).")
                String telefonoMovil) {
    @JsonAnySetter
    public void rejectUnknownProperty(String property, Object value) {
        throw new IllegalArgumentException(
                "El campo '" + property + "' no puede modificarse desde Mi Perfil.");
    }
}
