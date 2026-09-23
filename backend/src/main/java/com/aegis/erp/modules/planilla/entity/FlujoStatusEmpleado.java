package com.aegis.erp.modules.planilla.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "FLUJO_STATUS_EMPLEADO")
public class FlujoStatusEmpleado extends AuditableEntity {
    @EmbeddedId private FlujoStatusEmpleadoId id;
    @MapsId("statusActualId") @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_STATUS_ACTUAL", nullable = false)
    private StatusEmpleado statusActual;
    @MapsId("statusNuevoId") @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_STATUS_NUEVO", nullable = false)
    private StatusEmpleado statusNuevo;
    @Column(name = "NOMBRE_EVENTO", length = 50)
    private String nombreEvento;
    protected FlujoStatusEmpleado() {}
    public static FlujoStatusEmpleado crear(StatusEmpleado actual,StatusEmpleado nuevo,String evento,String usuario,java.time.LocalDateTime fecha){var e=new FlujoStatusEmpleado();e.id=new FlujoStatusEmpleadoId(actual.getId(),nuevo.getId());e.statusActual=actual;e.statusNuevo=nuevo;e.nombreEvento=evento;e.usuarioCreacion=usuario;e.fechaCreacion=fecha;return e;}
    public void modificar(String evento,String usuario,java.time.LocalDateTime fecha){this.nombreEvento=evento;this.usuarioModificacion=usuario;this.fechaModificacion=fecha;}
    public FlujoStatusEmpleadoId getId() { return id; }
    public StatusEmpleado getStatusActual() { return statusActual; }
    public StatusEmpleado getStatusNuevo() { return statusNuevo; }
    public String getNombreEvento() { return nombreEvento; }
}
