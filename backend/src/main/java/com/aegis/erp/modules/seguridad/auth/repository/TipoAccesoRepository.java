package com.aegis.erp.modules.seguridad.auth.repository;
import com.aegis.erp.modules.seguridad.auth.entity.TipoAcceso;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TipoAccesoRepository extends JpaRepository<TipoAcceso,Long>{Optional<TipoAcceso> findByNombre(String nombre);}