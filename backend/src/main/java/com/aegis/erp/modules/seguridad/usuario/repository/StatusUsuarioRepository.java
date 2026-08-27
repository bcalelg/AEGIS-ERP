package com.aegis.erp.modules.seguridad.usuario.repository;

import com.aegis.erp.modules.seguridad.usuario.entity.StatusUsuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusUsuarioRepository extends JpaRepository<StatusUsuario, Long> {
    Optional<StatusUsuario> findByNombre(String nombre);
}
