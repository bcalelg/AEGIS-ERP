package com.aegis.erp.modules.planilla.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record FlujoStatusEmpleadoId(
        @Column(name = "ID_STATUS_ACTUAL", nullable = false) Long statusActualId,
        @Column(name = "ID_STATUS_NUEVO", nullable = false) Long statusNuevoId)
        implements Serializable {
    public FlujoStatusEmpleadoId() { this(null, null); }
}
