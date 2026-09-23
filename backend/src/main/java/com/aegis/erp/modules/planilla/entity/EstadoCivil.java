package com.aegis.erp.modules.planilla.entity;

import jakarta.persistence.*;

@Entity @Table(name = "ESTADO_CIVIL")
public class EstadoCivil extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ESTADO_CIVIL", nullable = false) private Long id;
    @Column(name = "NOMBRE", nullable = false, length = 50) private String nombre;
    protected EstadoCivil() {}
    public static EstadoCivil crear(String nombre, String usuario, java.time.LocalDateTime fecha) { var e=new EstadoCivil(); e.nombre=nombre; e.usuarioCreacion=usuario; e.fechaCreacion=fecha; return e; }
    public void modificar(String nombre,String usuario,java.time.LocalDateTime fecha){this.nombre=nombre;this.usuarioModificacion=usuario;this.fechaModificacion=fecha;}
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
}
