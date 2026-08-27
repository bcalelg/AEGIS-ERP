package com.aegis.erp.modules.seguridad.menu.controller;

import com.aegis.erp.modules.seguridad.menu.dto.MenuCreateRequest;
import com.aegis.erp.modules.seguridad.menu.dto.MenuMaintenanceResponse;
import com.aegis.erp.modules.seguridad.menu.dto.MenuUpdateRequest;
import com.aegis.erp.modules.seguridad.menu.dto.ModuloOptionResponse;
import com.aegis.erp.modules.seguridad.menu.service.MenuMaintenanceService;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security/menus")
public class MenuMaintenanceController {
    private final MenuMaintenanceService service;

    public MenuMaintenanceController(MenuMaintenanceService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'menu','CONSULTAR')")
    public List<MenuMaintenanceResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'menu','CONSULTAR')")
    public MenuMaintenanceResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/options/modulos")
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'menu','CONSULTAR') or "
                    + "@permissionAuthorizationService.allowed(authentication.name,'menu','ALTA') or "
                    + "@permissionAuthorizationService.allowed(authentication.name,'menu','CAMBIO')")
    public List<ModuloOptionResponse> opcionesModulo() {
        return service.opcionesModulo();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'menu','ALTA')")
    public MenuMaintenanceResponse crear(
            @Valid @RequestBody MenuCreateRequest request,
            JwtAuthenticationToken authentication) {
        return service.crear(request, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'menu','CAMBIO')")
    public MenuMaintenanceResponse modificar(
            @PathVariable Long id,
            @Valid @RequestBody MenuUpdateRequest request,
            JwtAuthenticationToken authentication) {
        return service.modificar(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'menu','BAJA')")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    @GetMapping("/print")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'menu','IMPRIMIR')")
    public List<MenuMaintenanceResponse> imprimir(@RequestParam(required = false) String search) {
        return service.imprimir(search);
    }

    @GetMapping(value = {"/export", "/export/csv"}, produces = "text/csv")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'menu','EXPORTAR')")
    public ResponseEntity<byte[]> exportarCsv(@RequestParam(required = false) String search) {
        return download(service.exportarCsv(search), "text/csv;charset=UTF-8", "menus.csv");
    }

    @GetMapping(
            value = "/export/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'menu','EXPORTAR')")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(required = false) String search) {
        return download(
                service.exportarExcel(search),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "menus.xlsx");
    }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'menu','EXPORTAR')")
    public ResponseEntity<byte[]> exportarPdf(@RequestParam(required = false) String search) {
        return download(service.exportarPdf(search), MediaType.APPLICATION_PDF_VALUE, "menus.pdf");
    }

    private ResponseEntity<byte[]> download(byte[] content, String type, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(type))
                .body(content);
    }
}
