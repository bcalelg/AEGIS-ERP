package com.aegis.erp.modules.seguridad.roleopcion.repository;

import com.aegis.erp.modules.seguridad.usuario.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleOpcionRoleRepository extends JpaRepository<Role, Long> {
    List<Role> findAllByOrderByNombreAscIdAsc();
}
