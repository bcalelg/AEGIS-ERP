package com.aegis.erp.modules.seguridad.statususuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StatusUsuarioCreateRequest(@NotBlank @Size(max = 100) String nombre) {}
