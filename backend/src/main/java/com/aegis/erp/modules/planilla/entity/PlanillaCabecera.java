package com.aegis.erp.modules.planilla.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "PLANILLA_CABECERA")
public class PlanillaCabecera extends AuditableEntity {
    @EmbeddedId private PeriodoPlanillaId id;
    @MapsId @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
        @JoinColumn(name = "ANIO", referencedColumnName = "ANIO", nullable = false),
        @JoinColumn(name = "MES", referencedColumnName = "MES", nullable = false)
    })
    private PeriodoPlanilla periodo;
    @Column(name = "TOTAL_INGRESOS", precision = 10, scale = 2) private BigDecimal totalIngresos;
    @Column(name = "TOTAL_DESCUENTOS", precision = 10, scale = 2) private BigDecimal totalDescuentos;
    @Column(name = "SALARIO_NETO", precision = 10, scale = 2) private BigDecimal salarioNeto;
    @Column(name = "FECHA_HORA_PROCESADA") private LocalDateTime fechaHoraProcesada;
    protected PlanillaCabecera() {}
    public PeriodoPlanillaId getId() { return id; }
    public PeriodoPlanilla getPeriodo() { return periodo; }
}
