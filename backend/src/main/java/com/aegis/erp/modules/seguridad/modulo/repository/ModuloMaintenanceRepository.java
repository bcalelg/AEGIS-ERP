package com.aegis.erp.modules.seguridad.modulo.repository;

import com.aegis.erp.modules.seguridad.menu.entity.Modulo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuloMaintenanceRepository extends JpaRepository<Modulo, Long> {
    List<Modulo> findAllByOrderByOrdenMenuAscIdAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
