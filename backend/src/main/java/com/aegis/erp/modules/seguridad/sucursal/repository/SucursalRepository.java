package com.aegis.erp.modules.seguridad.sucursal.repository;

import com.aegis.erp.modules.seguridad.usuario.entity.Sucursal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
    @Query("select s from Sucursal s join fetch s.empresa e order by e.nombre, s.nombre, s.id")
    List<Sucursal> findAllWithEmpresaOrdered();

    List<Sucursal> findAllByEmpresaIdOrderByNombreAscIdAsc(Long idEmpresa);

    boolean existsByEmpresaIdAndNombreIgnoreCase(Long idEmpresa, String nombre);

    boolean existsByEmpresaIdAndNombreIgnoreCaseAndIdNot(
            Long idEmpresa,
            String nombre,
            Long id);
}
