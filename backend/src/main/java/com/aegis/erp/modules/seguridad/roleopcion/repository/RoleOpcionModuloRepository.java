package com.aegis.erp.modules.seguridad.roleopcion.repository;

import com.aegis.erp.modules.seguridad.menu.entity.Modulo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleOpcionModuloRepository extends JpaRepository<Modulo, Long> {
    List<Modulo> findAllByOrderByOrdenMenuAscIdAsc();
}
