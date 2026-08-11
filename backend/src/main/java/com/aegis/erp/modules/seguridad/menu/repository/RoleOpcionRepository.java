package com.aegis.erp.modules.seguridad.menu.repository;
import com.aegis.erp.modules.seguridad.menu.dto.MenuPermissionRow;
import com.aegis.erp.modules.seguridad.menu.entity.*;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface RoleOpcionRepository extends JpaRepository<RoleOpcion,RoleOpcionId>{
@Query("""
select new com.aegis.erp.modules.seguridad.menu.dto.MenuPermissionRow(
modulo.id,modulo.nombre,modulo.ordenMenu,menu.id,menu.nombre,menu.ordenMenu,
opcion.id,opcion.nombre,opcion.pagina,opcion.ordenMenu,
ro.consultar,ro.alta,ro.baja,ro.cambio,ro.imprimir,ro.exportar)
from RoleOpcion ro join ro.opcion opcion join opcion.menu menu join menu.modulo modulo
where ro.role.id=:idRole
order by modulo.ordenMenu,modulo.id,menu.ordenMenu,menu.id,opcion.ordenMenu,opcion.id
""") List<MenuPermissionRow> findMenuRowsByRoleId(@Param("idRole") Long idRole);}
