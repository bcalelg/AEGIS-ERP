package com.aegis.erp.modules.seguridad.usuario.repository;

import com.aegis.erp.modules.seguridad.usuario.entity.Usuario;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    @Query(
            "select u from Usuario u join fetch u.genero join fetch u.status join fetch u.role"
                + " join fetch u.sucursal s join fetch s.empresa order by u.idUsuario")
    List<Usuario> findAllForMaintenance();

    @Query(
            "select u from Usuario u join fetch u.genero join fetch u.status join fetch u.role"
                + " join fetch u.sucursal s join fetch s.empresa where u.idUsuario = :idUsuario")
    Optional<Usuario> findForMaintenance(@Param("idUsuario") String idUsuario);

    boolean existsByCorreoElectronico(String correoElectronico);

    boolean existsByCorreoElectronicoAndIdUsuarioNot(String correoElectronico, String idUsuario);

    boolean existsByTelefonoMovil(String telefonoMovil);

    boolean existsByTelefonoMovilAndIdUsuarioNot(String telefonoMovil, String idUsuario);

    @Query(
            "select u from Usuario u join fetch u.status join fetch u.sucursal s join fetch s.empresa"
                + " where u.idUsuario = :identifier or lower(u.correoElectronico) = lower(:identifier)")
    Optional<Usuario> findForPasswordRecovery(@Param("identifier") String identifier);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "select u from Usuario u join fetch u.status join fetch u.sucursal s join fetch s.empresa"
                + " where u.idUsuario = :idUsuario")
    Optional<Usuario> findForPasswordRecoveryForUpdate(@Param("idUsuario") String idUsuario);

    boolean existsByIdUsuarioAndSesionActual(String idUsuario, String sesionActual);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u.role.id from Usuario u where u.idUsuario = :idUsuario")
    Optional<Long> findRoleIdByIdUsuario(@Param("idUsuario") String idUsuario);

    @Query(
            "select u from Usuario u join fetch u.status join fetch u.role join fetch u.sucursal s"
                + " join fetch s.empresa where u.idUsuario = :idUsuario")
    Optional<Usuario> findForAuthentication(@Param("idUsuario") String idUsuario);

    @Query(
            "select u from Usuario u join fetch u.status join fetch u.role join fetch u.sucursal s"
                + " join fetch s.empresa where u.idUsuario = :idUsuario")
    Optional<Usuario> findCurrentUser(@Param("idUsuario") String idUsuario);
}
