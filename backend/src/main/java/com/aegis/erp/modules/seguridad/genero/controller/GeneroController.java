package com.aegis.erp.modules.seguridad.genero.controller;

import com.aegis.erp.modules.seguridad.genero.dto.GeneroCreateRequest;
import com.aegis.erp.modules.seguridad.genero.dto.GeneroResponse;
import com.aegis.erp.modules.seguridad.genero.dto.GeneroUpdateRequest;
import com.aegis.erp.modules.seguridad.genero.service.GeneroService;

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
@RequestMapping("/api/security/generos")
public class GeneroController {
    private final GeneroService generoService;

    public GeneroController(GeneroService generoService) {
        this.generoService = generoService;
    }

    @GetMapping
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'genero','CONSULTAR')")
    public List<GeneroResponse> listar() {
        return generoService.listarGeneros();
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'genero','CONSULTAR')")
    public GeneroResponse obtenerPorId(@PathVariable Long id) {
        return generoService.obtenerGeneroPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'genero','ALTA')")
    public GeneroResponse crear(
            @Valid @RequestBody GeneroCreateRequest request, JwtAuthenticationToken auth) {
        return generoService.crearGenero(request, auth.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'genero','CAMBIO')")
    public GeneroResponse modificar(
            @PathVariable Long id,
            @Valid @RequestBody GeneroUpdateRequest request,
            JwtAuthenticationToken auth) {
        return generoService.modificarGenero(id, request, auth.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'genero','BAJA')")
    public void eliminar(@PathVariable Long id) {
        generoService.eliminarGenero(id);
    }

    @GetMapping("/print")
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'genero','IMPRIMIR')")
    public List<GeneroResponse> imprimir() {
        return generoService.imprimir();
    }

    @GetMapping(value = {"/export", "/export/csv"}, produces = "text/csv")
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'genero','EXPORTAR')")
    public ResponseEntity<byte[]> exportarCsv(@RequestParam(required = false) String search) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generos.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(generoService.exportarCsv(search));
    }

    @GetMapping(
            value = "/export/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'genero','EXPORTAR')")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(required = false) String search) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generos.xlsx")
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(generoService.exportarExcel(search));
    }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'genero','EXPORTAR')")
    public ResponseEntity<byte[]> exportarPdf(@RequestParam(required = false) String search) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generos.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(generoService.exportarPdf(search));
    }
}
