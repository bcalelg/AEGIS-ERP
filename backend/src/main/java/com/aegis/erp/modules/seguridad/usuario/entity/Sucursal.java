package com.aegis.erp.modules.seguridad.usuario.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "SUCURSAL")
public class Sucursal {
    @Id @Column(name = "ID_SUCURSAL", nullable = false)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_EMPRESA", nullable = false)
    private Empresa empresa;
    protected Sucursal() {}
    public Sucursal(Long id, Empresa empresa) { this.id = id; this.empresa = empresa; }
    public Long getId() { return id; }
    public Empresa getEmpresa() { return empresa; }
}