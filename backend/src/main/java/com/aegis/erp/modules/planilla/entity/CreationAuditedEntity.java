package com.aegis.erp.modules.planilla.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class CreationAuditedEntity {
    @Column(name = "FECHA_CREACION", nullable = false)
    protected LocalDateTime fechaCreacion;
    @Column(name = "USUARIO_CREACION", nullable = false, length = 100)
    protected String usuarioCreacion;

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public String getUsuarioCreacion() { return usuarioCreacion; }
}
