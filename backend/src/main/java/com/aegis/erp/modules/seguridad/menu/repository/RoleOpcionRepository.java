package com.aegis.erp.modules.seguridad.menu.repository;

import com.aegis.erp.modules.seguridad.menu.dto.MenuPermissionRow;
import com.aegis.erp.modules.seguridad.menu.entity.*;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface RoleOpcionRepository extends JpaRepository<RoleOpcion, RoleOpcionId> {
    @Query(
            "select ro from RoleOpcion ro join ro.role role join ro.opcion opcion join Usuario u on"
                + " u.role=role where u.idUsuario=:idUsuario and"
                + " replace(lower(opcion.pagina),'.php','')=lower(:pagina)")
    Optional<RoleOpcion> findForUserAndPage(
            @Param("idUsuario") String idUsuario, @Param("pagina") String pagina);

    @Query(
"""
select new com.aegis.erp.modules.seguridad.menu.dto.MenuPermissionRow(
modulo.id,modulo.nombre,modulo.ordenMenu,menu.id,menu.nombre,menu.ordenMenu,
opcion.id,opcion.nombre,opcion.pagina,opcion.ordenMenu,
ro.consultar,ro.alta,ro.baja,ro.cambio,ro.imprimir,ro.exportar)
from RoleOpcion ro join ro.opcion opcion join opcion.menu menu join menu.modulo modulo
where ro.role.id=:idRole
and (ro.consultar=1 or ro.alta=1 or ro.baja=1 or ro.cambio=1 or ro.imprimir=1 or ro.exportar=1)
order by modulo.ordenMenu,modulo.id,menu.ordenMenu,menu.id,opcion.ordenMenu,opcion.id
""")
    List<MenuPermissionRow> findMenuRowsByRoleId(@Param("idRole") Long idRole);
}
