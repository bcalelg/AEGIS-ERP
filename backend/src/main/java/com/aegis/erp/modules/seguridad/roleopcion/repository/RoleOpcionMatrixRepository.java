package com.aegis.erp.modules.seguridad.roleopcion.repository;

import com.aegis.erp.modules.seguridad.menu.entity.Opcion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoleOpcionMatrixRepository extends JpaRepository<Opcion, Long> {
    @Query(
            "select o from Opcion o join fetch o.menu menu "
                    + "where menu.modulo.id=:idModulo "
                    + "order by menu.ordenMenu,menu.id,o.ordenMenu,o.id")
    List<Opcion> findByModuloOrdered(@Param("idModulo") Long idModulo);
}
