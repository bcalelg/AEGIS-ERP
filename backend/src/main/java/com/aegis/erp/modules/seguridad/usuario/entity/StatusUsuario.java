package com.aegis.erp.modules.seguridad.usuario.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "STATUS_USUARIO")
public class StatusUsuario {
    @Id @Column(name = "ID_STATUS_USUARIO", nullable = false)
    private Long id;
    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;
    protected StatusUsuario() {}
    public StatusUsuario(Long id, String nombre) { this.id = id; this.nombre = nombre; }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
}