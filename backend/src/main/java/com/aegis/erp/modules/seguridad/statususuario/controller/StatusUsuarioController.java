package com.aegis.erp.modules.seguridad.statususuario.controller;

import com.aegis.erp.modules.seguridad.statususuario.dto.StatusUsuarioCreateRequest;
import com.aegis.erp.modules.seguridad.statususuario.dto.StatusUsuarioResponse;
import com.aegis.erp.modules.seguridad.statususuario.dto.StatusUsuarioUpdateRequest;
import com.aegis.erp.modules.seguridad.statususuario.service.StatusUsuarioService;

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
@RequestMapping("/api/security/status-usuarios")
public class StatusUsuarioController {
    private final StatusUsuarioService statusUsuarioService;

    public StatusUsuarioController(StatusUsuarioService statusUsuarioService) {
        this.statusUsuarioService = statusUsuarioService;
    }

    @GetMapping
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'status_usuario','CONSULTAR')")
    public List<StatusUsuarioResponse> listar() {
        return statusUsuarioService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'status_usuario','CONSULTAR')")
    public StatusUsuarioResponse obtener(@PathVariable Long id) {
        return statusUsuarioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'status_usuario','ALTA')")
    public StatusUsuarioResponse crear(
            @Valid @RequestBody StatusUsuarioCreateRequest request,
            JwtAuthenticationToken authentication) {
        return statusUsuarioService.crear(request, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'status_usuario','CAMBIO')")
    public StatusUsuarioResponse modificar(
            @PathVariable Long id,
            @Valid @RequestBody StatusUsuarioUpdateRequest request,
            JwtAuthenticationToken authentication) {
        return statusUsuarioService.modificar(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'status_usuario','BAJA')")
    public void eliminar(@PathVariable Long id) {
        statusUsuarioService.eliminar(id);
    }

    @GetMapping("/print")
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'status_usuario','IMPRIMIR')")
    public List<StatusUsuarioResponse> imprimir(@RequestParam(required = false) String search) {
        return statusUsuarioService.imprimir(search);
    }

    @GetMapping(value = {"/export", "/export/csv"}, produces = "text/csv")
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'status_usuario','EXPORTAR')")
    public ResponseEntity<byte[]> exportarCsv(@RequestParam(required = false) String search) {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=estatus-usuarios.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(statusUsuarioService.exportarCsv(search));
    }

    @GetMapping(
            value = "/export/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'status_usuario','EXPORTAR')")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(required = false) String search) {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=estatus-usuarios.xlsx")
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(statusUsuarioService.exportarExcel(search));
    }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize(
            "@permissionAuthorizationService.allowed(authentication.name,'status_usuario','EXPORTAR')")
    public ResponseEntity<byte[]> exportarPdf(@RequestParam(required = false) String search) {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=estatus-usuarios.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(statusUsuarioService.exportarPdf(search));
    }
}
