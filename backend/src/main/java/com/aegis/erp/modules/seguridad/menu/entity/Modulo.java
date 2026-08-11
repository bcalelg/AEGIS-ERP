package com.aegis.erp.modules.seguridad.menu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "MODULO")
public class Modulo {
    @Id @Column(name = "ID_MODULO", nullable = false) private Long id;
    @Column(name = "NOMBRE", nullable = false, length = 50) private String nombre;
    @Column(name = "ORDEN_MENU", nullable = false) private Integer ordenMenu;
    protected Modulo() {}
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public Integer getOrdenMenu() { return ordenMenu; }
}
