package com.aegis.erp.modules.seguridad.usuario.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "EMPRESA")
public class Empresa {
    @Id @Column(name = "ID_EMPRESA", nullable = false)
    private Long id;
    @Column(name = "PASSWORD_INTENTOS_ANTES_DE_BLOQUEAR")
    private Integer intentosAntesDeBloquear;
    protected Empresa() {}
    public Empresa(Long id, Integer intentosAntesDeBloquear) { this.id = id; this.intentosAntesDeBloquear = intentosAntesDeBloquear; }
    public Long getId() { return id; }
    public Integer getIntentosAntesDeBloquear() { return intentosAntesDeBloquear; }
}