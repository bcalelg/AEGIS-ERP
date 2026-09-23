package com.aegis.erp.modules.planilla.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity @Table(name = "PERIODO_PLANILLA")
public class PeriodoPlanilla extends AuditableEntity {
    @EmbeddedId private PeriodoPlanillaId id;
    @Column(name = "FECHA_INICIO") private LocalDate fechaInicio;
    @Column(name = "FECHA_FIN") private LocalDate fechaFin;
    protected PeriodoPlanilla() {}
    public PeriodoPlanillaId getId() { return id; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
}
