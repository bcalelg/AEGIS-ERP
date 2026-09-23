package com.aegis.erp.modules.planilla.catalogo;

public record PlanillaCatalogResponse(
        String id, String nombre, Long relacionId, String relacionNombre,
        Long statusNuevoId, String statusNuevoNombre) {}
