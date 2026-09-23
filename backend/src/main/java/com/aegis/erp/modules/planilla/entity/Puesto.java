package com.aegis.erp.modules.planilla.entity;

import jakarta.persistence.*;

@Entity @Table(name = "PUESTO")
public class Puesto extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PUESTO", nullable = false) private Long id;
    @Column(name = "NOMBRE", nullable = false, length = 50) private String nombre;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_DEPARTAMENTO", nullable = false) private Departamento departamento;
    protected Puesto() {}
    public static Puesto crear(String nombre,Departamento departamento,String usuario,java.time.LocalDateTime fecha){var e=new Puesto();e.nombre=nombre;e.departamento=departamento;e.usuarioCreacion=usuario;e.fechaCreacion=fecha;return e;}
    public void modificar(String nombre,Departamento departamento,String usuario,java.time.LocalDateTime fecha){this.nombre=nombre;this.departamento=departamento;this.usuarioModificacion=usuario;this.fechaModificacion=fecha;}
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public Departamento getDepartamento() { return departamento; }
}
