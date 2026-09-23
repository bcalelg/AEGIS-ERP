package com.aegis.erp.modules.planilla.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "PLANILLA_DETALLE")
public class PlanillaDetalle extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PLANILLA_DETALLE", nullable = false) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
        @JoinColumn(name = "ANIO", referencedColumnName = "ANIO", nullable = false),
        @JoinColumn(name = "MES", referencedColumnName = "MES", nullable = false)
    })
    private PlanillaCabecera cabecera;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_EMPLEADO", nullable = false) private Empleado empleado;
    @Column(name = "FECHA_CONTRATACION", nullable = false) private LocalDate fechaContratacion;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PUESTO", nullable = false) private Puesto puesto;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_STATUS_EMPLEADO", nullable = false) private StatusEmpleado status;
    @Column(name = "INGRESO_SUELDO_BASE", nullable = false, precision = 10, scale = 2) private BigDecimal ingresoSueldoBase;
    @Column(name = "INGRESO_BONIFICACION_DECRETO", nullable = false, precision = 10, scale = 2) private BigDecimal ingresoBonificacionDecreto;
    @Column(name = "INGRESO_OTROS_INGRESOS", nullable = false, precision = 10, scale = 2) private BigDecimal ingresoOtrosIngresos;
    @Column(name = "DESCUENTO_IGSS", nullable = false, precision = 10, scale = 2) private BigDecimal descuentoIgss;
    @Column(name = "DESCUENTO_ISR", nullable = false, precision = 10, scale = 2) private BigDecimal descuentoIsr;
    @Column(name = "DESCUENTO_INASISTENCIAS", nullable = false, precision = 10, scale = 2) private BigDecimal descuentoInasistencias;
    @Column(name = "SALARIO_NETO", precision = 10, scale = 2) private BigDecimal salarioNeto;
    protected PlanillaDetalle() {}
    public Long getId() { return id; }
    public PlanillaCabecera getCabecera() { return cabecera; }
    public Empleado getEmpleado() { return empleado; }
}
