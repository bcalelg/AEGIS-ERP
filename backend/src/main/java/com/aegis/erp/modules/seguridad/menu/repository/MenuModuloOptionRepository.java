package com.aegis.erp.modules.seguridad.menu.repository;

import com.aegis.erp.modules.seguridad.menu.entity.Modulo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuModuloOptionRepository extends JpaRepository<Modulo, Long> {
    List<Modulo> findAllByOrderByOrdenMenuAscIdAsc();
}
