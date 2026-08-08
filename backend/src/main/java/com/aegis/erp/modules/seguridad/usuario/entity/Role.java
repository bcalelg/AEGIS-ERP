package com.aegis.erp.modules.seguridad.usuario.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ROLE")
public class Role {
    @Id @Column(name = "ID_ROLE", nullable = false)
    private Long id;
    @Column(name = "NOMBRE", nullable = false, length = 50)
    private String nombre;
    protected Role() {}
    public Role(Long id, String nombre) { this.id = id; this.nombre = nombre; }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
}