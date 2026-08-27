package com.aegis.erp.modules.seguridad.opcion.repository;

import com.aegis.erp.modules.seguridad.menu.entity.RoleOpcion;
import com.aegis.erp.modules.seguridad.menu.entity.RoleOpcionId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleOpcionDependencyRepository
        extends JpaRepository<RoleOpcion, RoleOpcionId> {
    boolean existsByOpcionId(Long idOpcion);
}
