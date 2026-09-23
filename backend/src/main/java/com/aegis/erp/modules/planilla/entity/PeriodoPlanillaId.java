package com.aegis.erp.modules.planilla.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record PeriodoPlanillaId(
        @Column(name = "ANIO", nullable = false, precision = 10) Integer anio,
        @Column(name = "MES", nullable = false, precision = 10) Integer mes)
        implements Serializable {
    public PeriodoPlanillaId() { this(null, null); }
}
