package com.aegis.erp.modules.seguridad.roleopcion.repository;

import com.aegis.erp.modules.seguridad.menu.entity.RoleOpcion;
import com.aegis.erp.modules.seguridad.menu.entity.RoleOpcionId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoleOpcionAssignmentRepository
        extends JpaRepository<RoleOpcion, RoleOpcionId> {
    @Query(
            "select ro from RoleOpcion ro join fetch ro.opcion o join o.menu m "
                    + "where ro.role.id=:idRole and m.modulo.id=:idModulo")
    List<RoleOpcion> findByRoleAndModulo(
            @Param("idRole") Long idRole,
            @Param("idModulo") Long idModulo);
}
