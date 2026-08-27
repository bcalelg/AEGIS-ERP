package com.aegis.erp.modules.seguridad.statususuario.repository;

import com.aegis.erp.modules.seguridad.usuario.entity.StatusUsuario;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface UsuarioStatusDependencyRepository extends Repository<StatusUsuario, Long> {
    @Query(
            value = "select count(*) from USUARIO where ID_STATUS_USUARIO = :idStatusUsuario",
            nativeQuery = true)
    long countUsuariosByStatusId(@Param("idStatusUsuario") Long idStatusUsuario);
}
