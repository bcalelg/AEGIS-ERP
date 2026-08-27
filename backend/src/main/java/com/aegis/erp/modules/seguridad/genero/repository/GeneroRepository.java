package com.aegis.erp.modules.seguridad.genero.repository;

import com.aegis.erp.modules.seguridad.genero.entity.Genero;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneroRepository extends JpaRepository<Genero, Long> {
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
