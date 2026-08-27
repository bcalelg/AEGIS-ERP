package com.aegis.erp.modules.seguridad.empresa.repository;

import com.aegis.erp.modules.seguridad.empresa.entity.Empresa;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    boolean existsByNit(String nit);

    boolean existsByNombre(String nombre);

    boolean existsByNitAndIdNot(String nit, Long id);

    boolean existsByNombreAndIdNot(String nombre, Long id);

    @Query(
            "select e from Empresa e where :search is null or lower(e.nombre) like"
                + " lower(concat('%',:search,'%')) or lower(e.nit) like"
                + " lower(concat('%',:search,'%'))")
    Page<Empresa> search(@Param("search") String search, Pageable pageable);

    @Query(
            "select e from Empresa e where :search is null or lower(e.nombre) like"
                + " lower(concat('%',:search,'%')) or lower(e.nit) like"
                + " lower(concat('%',:search,'%')) order by e.nombre")
    List<Empresa> export(@Param("search") String search);
}
