package com.aegis.erp.modules.seguridad.auth.repository;

import com.aegis.erp.modules.seguridad.auth.entity.TipoAcceso;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoAccesoRepository extends JpaRepository<TipoAcceso, Long> {
    Optional<TipoAcceso> findByNombre(String nombre);
}
