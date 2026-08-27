package com.aegis.erp.modules.seguridad.empresa.controller;

import com.aegis.erp.modules.seguridad.empresa.dto.*;
import com.aegis.erp.modules.seguridad.empresa.service.EmpresaService;

import jakarta.validation.Valid;

import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security/empresas")
public class EmpresaController {
    private final EmpresaService service;

    public EmpresaController(EmpresaService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'empresa','CONSULTAR')")
    public PageResponse<EmpresaResponse> listar(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        return service.listar(search, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'empresa','CONSULTAR')")
    public EmpresaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'empresa','ALTA')")
    public EmpresaResponse crear(
            @Valid @RequestBody EmpresaCreateRequest request, JwtAuthenticationToken auth) {
        return service.crear(request, auth.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'empresa','CAMBIO')")
    public EmpresaResponse modificar(
            @PathVariable Long id,
            @Valid @RequestBody EmpresaUpdateRequest request,
            JwtAuthenticationToken auth) {
        return service.modificar(id, request, auth.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'empresa','BAJA')")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    @GetMapping("/print")
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'empresa','IMPRIMIR')")
    public PageResponse<EmpresaResponse> imprimir(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 100, sort = "nombre") Pageable pageable) {
        return service.imprimir(search, pageable);
    }

    @GetMapping(value = {"/export", "/export/csv"}, produces = "text/csv")
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'empresa','EXPORTAR')")
    public ResponseEntity<byte[]> exportarCsv(@RequestParam(required = false) String search) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=empresas.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(service.exportarCsv(search));
    }

    @GetMapping(
            value = "/export/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'empresa','EXPORTAR')")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(required = false) String search) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=empresas.xlsx")
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(service.exportarExcel(search));
    }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'empresa','EXPORTAR')")
    public ResponseEntity<byte[]> exportarPdf(@RequestParam(required = false) String search) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=empresas.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(service.exportarPdf(search));
    }
}
