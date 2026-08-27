package com.aegis.erp.modules.seguridad.sucursal.repository;

import com.aegis.erp.modules.seguridad.empresa.entity.Empresa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SucursalEmpresaOptionRepository extends JpaRepository<Empresa, Long> {
    List<Empresa> findAllByOrderByNombreAscIdAsc();
}
