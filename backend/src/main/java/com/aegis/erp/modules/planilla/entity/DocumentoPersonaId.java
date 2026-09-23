package com.aegis.erp.modules.planilla.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record DocumentoPersonaId(
        @Column(name = "ID_TIPO_DOCUMENTO", nullable = false) Long tipoDocumentoId,
        @Column(name = "ID_PERSONA", nullable = false) Long personaId)
        implements Serializable {
    public DocumentoPersonaId() { this(null, null); }
}
