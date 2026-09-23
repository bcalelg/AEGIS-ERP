package com.aegis.erp.modules.planilla.documentopersona;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DocumentoPersonaRequest(@NotNull Long tipoDocumentoId, @NotNull Long personaId,
        @Size(max = 50) String numeroDocumento) {}
