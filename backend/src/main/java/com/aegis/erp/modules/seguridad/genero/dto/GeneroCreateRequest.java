package com.aegis.erp.modules.seguridad.genero.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GeneroCreateRequest(@NotBlank @Size(max = 100) String nombre) {}
