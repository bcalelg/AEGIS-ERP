package com.aegis.erp.modules.seguridad.opcion.repository;

import com.aegis.erp.modules.seguridad.menu.entity.Menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OpcionMenuOptionRepository extends JpaRepository<Menu, Long> {
    @Query(
            "select m from Menu m join fetch m.modulo modulo "
                    + "order by modulo.ordenMenu, modulo.id, m.ordenMenu, m.id")
    List<Menu> findAllWithModuloOrdered();
}
