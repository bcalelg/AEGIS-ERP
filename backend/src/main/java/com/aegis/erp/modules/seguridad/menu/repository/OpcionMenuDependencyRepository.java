package com.aegis.erp.modules.seguridad.menu.repository;

import com.aegis.erp.modules.seguridad.menu.entity.Opcion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OpcionMenuDependencyRepository extends JpaRepository<Opcion, Long> {
    @Query("select count(o) from Opcion o where o.menu.id = :idMenu")
    long countOpcionesByMenuId(@Param("idMenu") Long idMenu);
}
