package com.aegis.erp.modules.seguridad.opcion.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OpcionCreateRequest(
        @NotNull Long idMenu,
        @NotBlank @Size(max = 50) String nombre,
        @NotBlank @Size(max = 100) String pagina,
        @NotNull @Min(1) Integer orden) {}
