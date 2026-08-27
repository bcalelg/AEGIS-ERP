package com.aegis.erp.modules.seguridad.role.repository;

import com.aegis.erp.modules.seguridad.usuario.entity.Role;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface RoleDependencyRepository extends Repository<Role, Long> {
    @Query(value = "select count(*) from USUARIO where ID_ROLE = :idRole", nativeQuery = true)
    long countUsuariosByRoleId(@Param("idRole") Long idRole);

    @Query(value = "select count(*) from ROLE_OPCION where ID_ROLE = :idRole", nativeQuery = true)
    long countOpcionesByRoleId(@Param("idRole") Long idRole);
}
