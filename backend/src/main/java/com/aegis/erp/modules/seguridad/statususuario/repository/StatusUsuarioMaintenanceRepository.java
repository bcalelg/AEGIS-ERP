package com.aegis.erp.modules.seguridad.statususuario.repository;

import com.aegis.erp.modules.seguridad.usuario.entity.StatusUsuario;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusUsuarioMaintenanceRepository
        extends JpaRepository<StatusUsuario, Long> {
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
