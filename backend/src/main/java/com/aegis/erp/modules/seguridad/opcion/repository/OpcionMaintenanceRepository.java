package com.aegis.erp.modules.seguridad.opcion.repository;

import com.aegis.erp.modules.seguridad.menu.entity.Opcion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OpcionMaintenanceRepository extends JpaRepository<Opcion, Long> {
    @Query(
            "select o from Opcion o join fetch o.menu menu join fetch menu.modulo modulo "
                    + "order by modulo.ordenMenu, modulo.id, menu.ordenMenu, menu.id, "
                    + "o.ordenMenu, o.id")
    List<Opcion> findAllWithHierarchyOrdered();

    boolean existsByMenuIdAndNombreIgnoreCase(Long idMenu, String nombre);

    boolean existsByMenuIdAndNombreIgnoreCaseAndIdNot(Long idMenu, String nombre, Long id);

    boolean existsByMenuIdAndOrdenMenu(Long idMenu, Integer orden);

    boolean existsByMenuIdAndOrdenMenuAndIdNot(Long idMenu, Integer orden, Long id);

    boolean existsByPaginaIgnoreCase(String pagina);

    boolean existsByPaginaIgnoreCaseAndIdNot(String pagina, Long id);
}
