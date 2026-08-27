package com.aegis.erp.modules.seguridad.roleopcion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RoleOpcionSaveRequest(
        @NotNull Long idRole,
        @NotNull Long idModulo,
        @NotEmpty List<@Valid RoleOpcionItemRequest> opciones) {}
