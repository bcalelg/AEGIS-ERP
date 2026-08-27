package com.aegis.erp.modules.seguridad.roleopcion.controller;

import com.aegis.erp.modules.seguridad.roleopcion.dto.CatalogOptionResponse;
import com.aegis.erp.modules.seguridad.roleopcion.dto.RoleOpcionMatrixResponse;
import com.aegis.erp.modules.seguridad.roleopcion.dto.RoleOpcionSaveRequest;
import com.aegis.erp.modules.seguridad.roleopcion.service.RoleOpcionService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security/role-opciones")
public class RoleOpcionController {
    private final RoleOpcionService service;

    public RoleOpcionController(RoleOpcionService service) {
        this.service = service;
    }

    @GetMapping("/options/roles")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'asignacion_opcion_role','CONSULTAR')")
    public List<CatalogOptionResponse> roles() {
        return service.roles();
    }

    @GetMapping("/options/modulos")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'asignacion_opcion_role','CONSULTAR')")
    public List<CatalogOptionResponse> modulos() {
        return service.modulos();
    }

    @GetMapping
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'asignacion_opcion_role','CONSULTAR')")
    public List<RoleOpcionMatrixResponse> matriz(
            @RequestParam Long roleId,
            @RequestParam Long moduloId) {
        return service.matriz(roleId, moduloId);
    }

    @PutMapping
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'asignacion_opcion_role','CAMBIO')")
    public List<RoleOpcionMatrixResponse> guardar(
            @Valid @RequestBody RoleOpcionSaveRequest request,
            JwtAuthenticationToken authentication) {
        return service.guardar(request, authentication.getName());
    }
}
