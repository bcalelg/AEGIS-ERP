package com.aegis.erp.modules.seguridad.opcion.controller;

import com.aegis.erp.modules.seguridad.opcion.dto.MenuOptionResponse;
import com.aegis.erp.modules.seguridad.opcion.dto.OpcionCreateRequest;
import com.aegis.erp.modules.seguridad.opcion.dto.OpcionMaintenanceResponse;
import com.aegis.erp.modules.seguridad.opcion.dto.OpcionUpdateRequest;
import com.aegis.erp.modules.seguridad.opcion.service.OpcionService;

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
@RequestMapping("/api/security/opciones")
public class OpcionController {
    private final OpcionService service;

    public OpcionController(OpcionService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'opcion','CONSULTAR')")
    public List<OpcionMaintenanceResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'opcion','CONSULTAR')")
    public OpcionMaintenanceResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/options/menus")
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'opcion','CONSULTAR') or "
                    + "@permissionAuthorizationService.allowed(authentication.name,'opcion','ALTA') or "
                    + "@permissionAuthorizationService.allowed(authentication.name,'opcion','CAMBIO')")
    public List<MenuOptionResponse> opcionesMenu() {
        return service.opcionesMenu();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'opcion','ALTA')")
    public OpcionMaintenanceResponse crear(
            @Valid @RequestBody OpcionCreateRequest request,
            JwtAuthenticationToken authentication) {
        return service.crear(request, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'opcion','CAMBIO')")
    public OpcionMaintenanceResponse modificar(
            @PathVariable Long id,
            @Valid @RequestBody OpcionUpdateRequest request,
            JwtAuthenticationToken authentication) {
        return service.modificar(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'opcion','BAJA')")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    @GetMapping("/print")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'opcion','IMPRIMIR')")
    public List<OpcionMaintenanceResponse> imprimir(@RequestParam(required = false) String search) {
        return service.imprimir(search);
    }

    @GetMapping(value = {"/export", "/export/csv"}, produces = "text/csv")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'opcion','EXPORTAR')")
    public ResponseEntity<byte[]> exportarCsv(@RequestParam(required = false) String search) {
        return download(service.exportarCsv(search), "text/csv;charset=UTF-8", "opciones.csv");
    }

    @GetMapping(
            value = "/export/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'opcion','EXPORTAR')")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(required = false) String search) {
        return download(
                service.exportarExcel(search),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "opciones.xlsx");
    }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'opcion','EXPORTAR')")
    public ResponseEntity<byte[]> exportarPdf(@RequestParam(required = false) String search) {
        return download(service.exportarPdf(search), MediaType.APPLICATION_PDF_VALUE, "opciones.pdf");
    }

    private ResponseEntity<byte[]> download(byte[] content, String type, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(type))
                .body(content);
    }
}
