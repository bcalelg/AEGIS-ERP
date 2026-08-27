package com.aegis.erp.modules.seguridad.opcion.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OpcionUpdateRequest(
        @NotNull Long idMenu,
        @NotBlank @Size(max = 50) String nombre,
        @NotNull @Min(1) Integer orden) {}
