package com.aegis.erp.modules.seguridad.menu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MenuUpdateRequest(
        @NotNull Long idModulo,
        @NotBlank @Size(max = 50) String nombre,
        @NotNull @Min(1) Integer orden) {}
