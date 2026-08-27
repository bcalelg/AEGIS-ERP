package com.aegis.erp.modules.seguridad.sucursal.controller;

import com.aegis.erp.modules.seguridad.sucursal.dto.EmpresaOptionResponse;
import com.aegis.erp.modules.seguridad.sucursal.dto.SucursalCreateRequest;
import com.aegis.erp.modules.seguridad.sucursal.dto.SucursalResponse;
import com.aegis.erp.modules.seguridad.sucursal.dto.SucursalUpdateRequest;
import com.aegis.erp.modules.seguridad.sucursal.service.SucursalService;

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
@RequestMapping("/api/security/sucursales")
public class SucursalController {
    private final SucursalService service;

    public SucursalController(SucursalService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'sucursal','CONSULTAR')")
    public List<SucursalResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'sucursal','CONSULTAR')")
    public SucursalResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/options/empresas")
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'sucursal','CONSULTAR') or "
                    + "@permissionAuthorizationService.allowed(authentication.name,'sucursal','ALTA') or "
                    + "@permissionAuthorizationService.allowed(authentication.name,'sucursal','CAMBIO')")
    public List<EmpresaOptionResponse> opcionesEmpresa() {
        return service.opcionesEmpresa();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'sucursal','ALTA')")
    public SucursalResponse crear(
            @Valid @RequestBody SucursalCreateRequest request,
            JwtAuthenticationToken authentication) {
        return service.crear(request, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'sucursal','CAMBIO')")
    public SucursalResponse modificar(
            @PathVariable Long id,
            @Valid @RequestBody SucursalUpdateRequest request,
            JwtAuthenticationToken authentication) {
        return service.modificar(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'sucursal','BAJA')")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    @GetMapping("/print")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'sucursal','IMPRIMIR')")
    public List<SucursalResponse> imprimir(@RequestParam(required = false) String search) {
        return service.imprimir(search);
    }

    @GetMapping(value = {"/export", "/export/csv"}, produces = "text/csv")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'sucursal','EXPORTAR')")
    public ResponseEntity<byte[]> exportarCsv(@RequestParam(required = false) String search) {
        return download(service.exportarCsv(search), "text/csv;charset=UTF-8", "sucursales.csv");
    }

    @GetMapping(
            value = "/export/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'sucursal','EXPORTAR')")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(required = false) String search) {
        return download(
                service.exportarExcel(search),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "sucursales.xlsx");
    }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'sucursal','EXPORTAR')")
    public ResponseEntity<byte[]> exportarPdf(@RequestParam(required = false) String search) {
        return download(service.exportarPdf(search), MediaType.APPLICATION_PDF_VALUE, "sucursales.pdf");
    }

    private ResponseEntity<byte[]> download(byte[] content, String type, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(type))
                .body(content);
    }
}
