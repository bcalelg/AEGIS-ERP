package com.aegis.erp.modules.planilla.entity;

import com.aegis.erp.modules.seguridad.empresa.entity.Empresa;
import jakarta.persistence.*;

@Entity
@Table(name = "DEPARTAMENTO")
public class Departamento extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DEPARTAMENTO", nullable = false) private Long id;
    @Column(name = "NOMBRE", length = 50) private String nombre;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_EMPRESA") private Empresa empresa;
    protected Departamento() {}
    public static Departamento crear(String nombre,Empresa empresa,String usuario,java.time.LocalDateTime fecha){var e=new Departamento();e.nombre=nombre;e.empresa=empresa;e.usuarioCreacion=usuario;e.fechaCreacion=fecha;return e;}
    public void modificar(String nombre,Empresa empresa,String usuario,java.time.LocalDateTime fecha){this.nombre=nombre;this.empresa=empresa;this.usuarioModificacion=usuario;this.fechaModificacion=fecha;}
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public Empresa getEmpresa() { return empresa; }
}
