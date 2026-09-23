package com.aegis.erp.modules.planilla.catalogo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlanillaCatalogRequest(
        @NotBlank @Size(max = 50) String nombre,
        Long relacionId,
        Long statusNuevoId) {}
