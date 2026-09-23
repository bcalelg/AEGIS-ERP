package com.aegis.erp.modules.planilla.documentopersona;

public record DocumentoPersonaResponse(String id, Long tipoDocumentoId, String tipoDocumentoNombre,
        Long personaId, String personaNombre, String numeroDocumento) {}
