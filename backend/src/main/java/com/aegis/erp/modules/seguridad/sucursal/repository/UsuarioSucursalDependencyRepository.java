package com.aegis.erp.modules.seguridad.sucursal.repository;

import com.aegis.erp.modules.seguridad.usuario.entity.Sucursal;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface UsuarioSucursalDependencyRepository extends Repository<Sucursal, Long> {
    @Query(value = "select count(*) from USUARIO where ID_SUCURSAL = :idSucursal", nativeQuery = true)
    long countUsuariosBySucursalId(@Param("idSucursal") Long idSucursal);
}
