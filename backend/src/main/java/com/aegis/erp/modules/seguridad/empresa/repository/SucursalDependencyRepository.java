package com.aegis.erp.modules.seguridad.empresa.repository;

import com.aegis.erp.modules.seguridad.usuario.entity.Sucursal;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SucursalDependencyRepository extends JpaRepository<Sucursal, Long> {
    boolean existsByEmpresaId(Long idEmpresa);
}
