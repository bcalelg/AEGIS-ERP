package com.aegis.erp.modules.seguridad.menu.repository;

import com.aegis.erp.modules.seguridad.menu.entity.Menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MenuMaintenanceRepository extends JpaRepository<Menu, Long> {
    @Query(
            "select m from Menu m join fetch m.modulo modulo "
                    + "order by modulo.ordenMenu, modulo.id, m.ordenMenu, m.id")
    List<Menu> findAllWithModuloOrdered();

    boolean existsByModuloIdAndNombreIgnoreCase(Long idModulo, String nombre);

    boolean existsByModuloIdAndNombreIgnoreCaseAndIdNot(
            Long idModulo,
            String nombre,
            Long id);

    boolean existsByModuloIdAndOrdenMenu(Long idModulo, Integer ordenMenu);

    boolean existsByModuloIdAndOrdenMenuAndIdNot(Long idModulo, Integer ordenMenu, Long id);
}
