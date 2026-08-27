package com.aegis.erp.modules.seguridad.modulo.controller;

import com.aegis.erp.modules.seguridad.modulo.dto.ModuloCreateRequest;
import com.aegis.erp.modules.seguridad.modulo.dto.ModuloResponse;
import com.aegis.erp.modules.seguridad.modulo.dto.ModuloUpdateRequest;
import com.aegis.erp.modules.seguridad.modulo.service.ModuloService;

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
@RequestMapping("/api/security/modulos")
public class ModuloController {
    private final ModuloService moduloService;

    public ModuloController(ModuloService moduloService) {
        this.moduloService = moduloService;
    }

    @GetMapping
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'modulo','CONSULTAR')")
    public List<ModuloResponse> listar() {
        return moduloService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'modulo','CONSULTAR')")
    public ModuloResponse obtener(@PathVariable Long id) {
        return moduloService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'modulo','ALTA')")
    public ModuloResponse crear(
            @Valid @RequestBody ModuloCreateRequest request,
            JwtAuthenticationToken authentication) {
        return moduloService.crear(request, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'modulo','CAMBIO')")
    public ModuloResponse modificar(
            @PathVariable Long id,
            @Valid @RequestBody ModuloUpdateRequest request,
            JwtAuthenticationToken authentication) {
        return moduloService.modificar(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'modulo','BAJA')")
    public void eliminar(@PathVariable Long id) {
        moduloService.eliminar(id);
    }

    @GetMapping("/print")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'modulo','IMPRIMIR')")
    public List<ModuloResponse> imprimir(@RequestParam(required = false) String search) {
        return moduloService.imprimir(search);
    }

    @GetMapping(value = {"/export", "/export/csv"}, produces = "text/csv")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'modulo','EXPORTAR')")
    public ResponseEntity<byte[]> exportarCsv(@RequestParam(required = false) String search) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=modulos.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(moduloService.exportarCsv(search));
    }

    @GetMapping(
            value = "/export/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'modulo','EXPORTAR')")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(required = false) String search) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=modulos.xlsx")
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(moduloService.exportarExcel(search));
    }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'modulo','EXPORTAR')")
    public ResponseEntity<byte[]> exportarPdf(@RequestParam(required = false) String search) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=modulos.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(moduloService.exportarPdf(search));
    }
}
