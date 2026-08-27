package com.aegis.erp.modules.seguridad.sucursal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SucursalUpdateRequest(
        @NotNull Long idEmpresa,
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 200) String direccion) {}
