package com.aegis.erp.modules.seguridad.roleopcion.dto;

import jakarta.validation.constraints.NotNull;

public record RoleOpcionItemRequest(
        @NotNull Long idOpcion,
        boolean consultar,
        boolean alta,
        boolean baja,
        boolean cambio,
        boolean imprimir,
        boolean exportar) {
    public boolean anyEnabled() {
        return consultar || alta || baja || cambio || imprimir || exportar;
    }
}
