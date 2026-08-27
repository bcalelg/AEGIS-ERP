package com.aegis.erp.modules.seguridad.genero.repository;

import com.aegis.erp.modules.seguridad.genero.entity.Genero;

import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioGeneroDependencyRepository extends Repository<Genero, Long> {
    @Query(value = "select count(*) from USUARIO where ID_GENERO = :idGenero", nativeQuery = true)
    long countUsuariosByGeneroId(@Param("idGenero") Long idGenero);
}
