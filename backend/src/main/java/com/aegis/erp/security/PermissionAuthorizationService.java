package com.aegis.erp.security;

import com.aegis.erp.modules.seguridad.menu.entity.RoleOpcion;
import com.aegis.erp.modules.seguridad.menu.repository.RoleOpcionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

@Service("permissionAuthorizationService")
public class PermissionAuthorizationService {
    private final RoleOpcionRepository repository;

    public PermissionAuthorizationService(RoleOpcionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public boolean allowed(String user, String page, String permission) {
        if (user == null || page == null || permission == null) return false;
        return repository
                .findForUserAndPage(user, page)
                .map(value -> enabled(value, permission))
                .orElse(false);
    }

    private boolean enabled(RoleOpcion value, String permission) {
        Function<RoleOpcion, Integer> getter =
                switch (permission) {
                    case "CONSULTAR" -> RoleOpcion::getConsultar;
                    case "ALTA" -> RoleOpcion::getAlta;
                    case "BAJA" -> RoleOpcion::getBaja;
                    case "CAMBIO" -> RoleOpcion::getCambio;
                    case "IMPRIMIR" -> RoleOpcion::getImprimir;
                    case "EXPORTAR" -> RoleOpcion::getExportar;
                    default -> ignored -> 0;
                };
        return Integer.valueOf(1).equals(getter.apply(value));
    }
}
