package com.aegis.erp.modules.planilla.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class AuditableEntity {
    @Column(name = "FECHA_CREACION", nullable = false)
    protected LocalDateTime fechaCreacion;
    @Column(name = "USUARIO_CREACION", nullable = false, length = 100)
    protected String usuarioCreacion;
    @Column(name = "FECHA_MODIFICACION")
    protected LocalDateTime fechaModificacion;
    @Column(name = "USUARIO_MODIFICACION", length = 100)
    protected String usuarioModificacion;

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public String getUsuarioCreacion() { return usuarioCreacion; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public String getUsuarioModificacion() { return usuarioModificacion; }
}
