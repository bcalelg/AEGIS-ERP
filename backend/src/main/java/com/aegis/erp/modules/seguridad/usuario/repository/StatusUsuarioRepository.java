package com.aegis.erp.modules.seguridad.usuario.repository;
import com.aegis.erp.modules.seguridad.usuario.entity.StatusUsuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface StatusUsuarioRepository extends JpaRepository<StatusUsuario, Long> { Optional<StatusUsuario> findByNombre(String nombre); }