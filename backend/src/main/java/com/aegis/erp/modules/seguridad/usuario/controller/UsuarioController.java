package com.aegis.erp.modules.seguridad.usuario.controller;

import com.aegis.erp.modules.seguridad.usuario.dto.*;
import com.aegis.erp.modules.seguridad.usuario.service.UsuarioMaintenanceService;

import jakarta.validation.Valid;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security/usuarios")
public class UsuarioController {
    private final UsuarioMaintenanceService service;

    public UsuarioController(UsuarioMaintenanceService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'usuario','CONSULTAR')")
    public List<UsuarioListResponse> listar() { return service.listar(); }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'usuario','CONSULTAR')")
    public UsuarioResponse obtener(@PathVariable String id) { return service.obtener(id); }

    @GetMapping("/options/empresas")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'usuario','CONSULTAR') or @permissionAuthorizationService.allowed(authentication.name,'usuario','ALTA') or @permissionAuthorizationService.allowed(authentication.name,'usuario','CAMBIO')")
    public List<UsuarioOptionResponse> empresas() { return service.opcionesEmpresa(); }

    @GetMapping("/options/sucursales")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'usuario','CONSULTAR') or @permissionAuthorizationService.allowed(authentication.name,'usuario','ALTA') or @permissionAuthorizationService.allowed(authentication.name,'usuario','CAMBIO')")
    public List<UsuarioOptionResponse> sucursales(@RequestParam Long idEmpresa) { return service.opcionesSucursal(idEmpresa); }

    @GetMapping("/options/generos")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'usuario','CONSULTAR') or @permissionAuthorizationService.allowed(authentication.name,'usuario','ALTA') or @permissionAuthorizationService.allowed(authentication.name,'usuario','CAMBIO')")
    public List<UsuarioOptionResponse> generos() { return service.opcionesGenero(); }

    @GetMapping("/options/status")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'usuario','CONSULTAR') or @permissionAuthorizationService.allowed(authentication.name,'usuario','ALTA') or @permissionAuthorizationService.allowed(authentication.name,'usuario','CAMBIO')")
    public List<UsuarioOptionResponse> statuses() { return service.opcionesStatus(); }

    @GetMapping("/options/roles")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'usuario','CONSULTAR') or @permissionAuthorizationService.allowed(authentication.name,'usuario','ALTA') or @permissionAuthorizationService.allowed(authentication.name,'usuario','CAMBIO')")
    public List<UsuarioOptionResponse> roles() { return service.opcionesRole(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'usuario','ALTA')")
    public UsuarioResponse crear(@Valid @RequestBody UsuarioCreateRequest request, JwtAuthenticationToken authentication) {
        return service.crear(request, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'usuario','CAMBIO')")
    public UsuarioResponse modificar(@PathVariable String id, @Valid @RequestBody UsuarioUpdateRequest request, JwtAuthenticationToken authentication) {
        return service.modificar(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'usuario','BAJA')")
    public void eliminar(@PathVariable String id) { service.eliminar(id); }

    @GetMapping("/print")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'usuario','IMPRIMIR')")
    public List<UsuarioListResponse> imprimir(@RequestParam(required = false) String search) { return service.imprimir(search); }

    @GetMapping(value = {"/export", "/export/csv"}, produces = "text/csv")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'usuario','EXPORTAR')")
    public ResponseEntity<byte[]> csv(@RequestParam(required = false) String search) { return download(service.exportarCsv(search), "text/csv;charset=UTF-8", "usuarios.csv"); }

    @GetMapping(value = "/export/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'usuario','EXPORTAR')")
    public ResponseEntity<byte[]> excel(@RequestParam(required = false) String search) { return download(service.exportarExcel(search), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "usuarios.xlsx"); }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'usuario','EXPORTAR')")
    public ResponseEntity<byte[]> pdf(@RequestParam(required = false) String search) { return download(service.exportarPdf(search), MediaType.APPLICATION_PDF_VALUE, "usuarios.pdf"); }

    private ResponseEntity<byte[]> download(byte[] content, String type, String filename) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(type)).body(content);
    }
}
