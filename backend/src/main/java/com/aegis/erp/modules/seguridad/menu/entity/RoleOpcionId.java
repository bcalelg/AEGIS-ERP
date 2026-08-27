package com.aegis.erp.modules.seguridad.menu.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RoleOpcionId implements Serializable {
    @Column(name = "ID_ROLE", nullable = false)
    private Long idRole;

    @Column(name = "ID_OPCION", nullable = false)
    private Long idOpcion;

    protected RoleOpcionId() {}

    public RoleOpcionId(Long idRole, Long idOpcion) {
        this.idRole = idRole;
        this.idOpcion = idOpcion;
    }

    public Long getIdRole() {
        return idRole;
    }

    public Long getIdOpcion() {
        return idOpcion;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof RoleOpcionId that
                        && Objects.equals(idRole, that.idRole)
                        && Objects.equals(idOpcion, that.idOpcion));
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRole, idOpcion);
    }
}
