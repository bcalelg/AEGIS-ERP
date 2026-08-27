package com.aegis.erp.modules.seguridad.role.repository;

import com.aegis.erp.modules.seguridad.usuario.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleMaintenanceRepository extends JpaRepository<Role, Long> {
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
