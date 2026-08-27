package com.aegis.erp.modules.seguridad.modulo.repository;

import com.aegis.erp.modules.seguridad.menu.entity.Modulo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface MenuModuloDependencyRepository extends Repository<Modulo, Long> {
    @Query(value = "select count(*) from MENU where ID_MODULO = :idModulo", nativeQuery = true)
    long countMenusByModuloId(@Param("idModulo") Long idModulo);
}
