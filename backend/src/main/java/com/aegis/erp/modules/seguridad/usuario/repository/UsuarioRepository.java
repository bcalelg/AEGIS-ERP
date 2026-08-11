package com.aegis.erp.modules.seguridad.usuario.repository;

import com.aegis.erp.modules.seguridad.usuario.entity.Usuario;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u.role.id from Usuario u where u.idUsuario = :idUsuario")
    Optional<Long> findRoleIdByIdUsuario(@Param("idUsuario") String idUsuario);

    @Query("select u from Usuario u join fetch u.status join fetch u.role join fetch u.sucursal s join fetch s.empresa where u.idUsuario = :idUsuario")
    Optional<Usuario> findForAuthentication(@Param("idUsuario") String idUsuario);
}