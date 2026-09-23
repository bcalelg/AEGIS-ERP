package com.aegis.erp.modules.planilla.entity;

import com.aegis.erp.modules.seguridad.usuario.entity.Sucursal;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "EMPLEADO")
public class Empleado extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_EMPLEADO", nullable = false) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PERSONA", nullable = false) private Persona persona;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SUCURSAL", nullable = false) private Sucursal sucursal;
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
    protected Empleado() {}
    public static Empleado crear(Persona persona, Sucursal sucursal, LocalDate fechaContratacion,
            Puesto puesto, StatusEmpleado status, BigDecimal sueldo, BigDecimal bonificacion,
            BigDecimal otros, BigDecimal igss, BigDecimal isr, BigDecimal inasistencias,
            String usuario, java.time.LocalDateTime ahora) {
        Empleado e = new Empleado();
        e.modificarDatos(persona,sucursal,fechaContratacion,puesto,status,sueldo,bonificacion,otros,igss,isr,inasistencias);
        e.usuarioCreacion=usuario;e.fechaCreacion=ahora;return e;
    }
    public void modificar(Persona persona, Sucursal sucursal, LocalDate fechaContratacion,
            Puesto puesto, StatusEmpleado status, BigDecimal sueldo, BigDecimal bonificacion,
            BigDecimal otros, BigDecimal igss, BigDecimal isr, BigDecimal inasistencias,
            String usuario, java.time.LocalDateTime ahora) {
        modificarDatos(persona,sucursal,fechaContratacion,puesto,status,sueldo,bonificacion,otros,igss,isr,inasistencias);
        usuarioModificacion=usuario;fechaModificacion=ahora;
    }
    private void modificarDatos(Persona persona,Sucursal sucursal,LocalDate fechaContratacion,Puesto puesto,
            StatusEmpleado status,BigDecimal sueldo,BigDecimal bonificacion,BigDecimal otros,
            BigDecimal igss,BigDecimal isr,BigDecimal inasistencias){this.persona=persona;this.sucursal=sucursal;
        this.fechaContratacion=fechaContratacion;this.puesto=puesto;this.status=status;this.ingresoSueldoBase=sueldo;
        this.ingresoBonificacionDecreto=bonificacion;this.ingresoOtrosIngresos=otros;this.descuentoIgss=igss;
        this.descuentoIsr=isr;this.descuentoInasistencias=inasistencias;}
    public Long getId() { return id; }
    public Persona getPersona() { return persona; }
    public Sucursal getSucursal() { return sucursal; }
    public LocalDate getFechaContratacion() { return fechaContratacion; }
    public Puesto getPuesto() { return puesto; }
    public StatusEmpleado getStatus() { return status; }
    public BigDecimal getIngresoSueldoBase() { return ingresoSueldoBase; }
    public BigDecimal getIngresoBonificacionDecreto() { return ingresoBonificacionDecreto; }
    public BigDecimal getIngresoOtrosIngresos() { return ingresoOtrosIngresos; }
    public BigDecimal getDescuentoIgss() { return descuentoIgss; }
    public BigDecimal getDescuentoIsr() { return descuentoIsr; }
    public BigDecimal getDescuentoInasistencias() { return descuentoInasistencias; }
}
