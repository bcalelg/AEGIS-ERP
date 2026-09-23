package com.aegis.erp.modules.planilla.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "LIQUIDACION")
public class Liquidacion extends CreationAuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_LIQUIDACION", nullable = false) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_EMPLEADO", nullable = false) private Empleado empleado;
    @Column(name = "FECHA_CONTRATACION", nullable = false) private LocalDate fechaContratacion;
    @Column(name = "FECHA_EGRESO", nullable = false) private LocalDate fechaEgreso;
    @Column(name = "FECHA_LIQUIDACION", nullable = false) private LocalDate fechaLiquidacion;
    @Column(name = "MOTIVO_EGRESO", length = 50) private String motivoEgreso;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PUESTO", nullable = false) private Puesto puesto;
    @Column(name = "INGRESO_SUELDO_BASE", nullable = false, precision = 10, scale = 2) private BigDecimal ingresoSueldoBase;
    @Column(name = "INGRESO_BONIFICACION_DECRETO", nullable = false, precision = 10, scale = 2) private BigDecimal ingresoBonificacionDecreto;
    @Column(name = "INGRESO_OTROS_INGRESOS", nullable = false, precision = 10, scale = 2) private BigDecimal ingresoOtrosIngresos;
    @Column(name = "DESCUENTO_IGSS", nullable = false, precision = 10, scale = 2) private BigDecimal descuentoIgss;
    @Column(name = "DESCUENTO_ISR", nullable = false, precision = 10, scale = 2) private BigDecimal descuentoIsr;
    @Column(name = "DESCUENTO_INASISTENCIAS", nullable = false, precision = 10, scale = 2) private BigDecimal descuentoInasistencias;
    @Column(name = "SALARIO_NETO", precision = 10, scale = 2) private BigDecimal salarioNeto;
    @Column(name = "TOTAL_INGRESOS", precision = 10, scale = 2) private BigDecimal totalIngresos;
    @Column(name = "TOTAL_DESCUENTOS", precision = 10, scale = 2) private BigDecimal totalDescuentos;
    @Column(name = "TOTAL_NETO", precision = 10, scale = 2) private BigDecimal totalNeto;
    protected Liquidacion() {}
    public Long getId() { return id; }
    public Empleado getEmpleado() { return empleado; }
    public Puesto getPuesto() { return puesto; }
}
