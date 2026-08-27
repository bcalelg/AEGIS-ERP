package com.aegis.erp.modules.seguridad.role.controller;

import com.aegis.erp.modules.seguridad.role.dto.RoleCreateRequest;
import com.aegis.erp.modules.seguridad.role.dto.RoleResponse;
import com.aegis.erp.modules.seguridad.role.dto.RoleUpdateRequest;
import com.aegis.erp.modules.seguridad.role.service.RoleService;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/security/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'role','CONSULTAR')")
    public List<RoleResponse> listar() {
        return roleService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'role','CONSULTAR')")
    public RoleResponse obtener(@PathVariable Long id) {
        return roleService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'role','ALTA')")
    public RoleResponse crear(
            @Valid @RequestBody RoleCreateRequest request,
            JwtAuthenticationToken authentication) {
        return roleService.crear(request, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'role','CAMBIO')")
    public RoleResponse modificar(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request,
            JwtAuthenticationToken authentication) {
        return roleService.modificar(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'role','BAJA')")
    public void eliminar(@PathVariable Long id) {
        roleService.eliminar(id);
    }

    @GetMapping("/print")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'role','IMPRIMIR')")
    public List<RoleResponse> imprimir(@RequestParam(required = false) String search) {
        return roleService.imprimir(search);
    }

    @GetMapping(value = {"/export", "/export/csv"}, produces = "text/csv")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'role','EXPORTAR')")
    public ResponseEntity<byte[]> exportarCsv(@RequestParam(required = false) String search) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=roles.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(roleService.exportarCsv(search));
    }

    @GetMapping(
            value = "/export/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'role','EXPORTAR')")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(required = false) String search) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=roles.xlsx")
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(roleService.exportarExcel(search));
    }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'role','EXPORTAR')")
    public ResponseEntity<byte[]> exportarPdf(@RequestParam(required = false) String search) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=roles.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(roleService.exportarPdf(search));
    }
}
