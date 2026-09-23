package com.aegis.erp.modules.planilla.catalogo;

import com.aegis.erp.security.PermissionAuthorizationService;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("planillaCatalogAuthorization")
public class PlanillaCatalogAuthorization {
    private static final Map<String,String> PAGES = Map.of(
            "estados-civiles", "estado_civil",
            "status-empleados", "status_empleado",
            "flujos-status-empleado", "flujo_status_empleado",
            "tipos-documento", "tipos_documento",
            "departamentos", "departamento",
            "puestos", "puesto",
            "bancos", "banco");
    private final PermissionAuthorizationService permissions;
    public PlanillaCatalogAuthorization(PermissionAuthorizationService permissions){this.permissions=permissions;}
    public boolean allowed(String user,String catalog,String permission){
        String page=PAGES.get(catalog);
        return page!=null && permissions.allowed(user,page,permission);
    }
}
