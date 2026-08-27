package com.aegis.erp.modules.seguridad.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleUpdateRequest(@NotBlank @Size(max = 50) String nombre) {}
