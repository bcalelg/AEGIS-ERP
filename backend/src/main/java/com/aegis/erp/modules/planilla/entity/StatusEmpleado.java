package com.aegis.erp.modules.planilla.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "STATUS_EMPLEADO")
public class StatusEmpleado extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_STATUS_EMPLEADO", nullable = false)
    private Long id;
    @Column(name = "NOMBRE", length = 50)
    private String nombre;
    protected StatusEmpleado() {}
    public static StatusEmpleado crear(String nombre,String usuario,java.time.LocalDateTime fecha){var e=new StatusEmpleado();e.nombre=nombre;e.usuarioCreacion=usuario;e.fechaCreacion=fecha;return e;}
    public void modificar(String nombre,String usuario,java.time.LocalDateTime fecha){this.nombre=nombre;this.usuarioModificacion=usuario;this.fechaModificacion=fecha;}
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
}
