package com.aegis.erp.modules.planilla.persona;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planilla/personas")
public class PersonaController {
    private final PersonaService service;
    public PersonaController(PersonaService service) { this.service = service; }
    @GetMapping @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'personas','CONSULTAR')")
    public List<PersonaResponse> listar(@RequestParam(required=false) String search) { return service.listar(search); }
    @GetMapping("/{id}") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'personas','CONSULTAR')")
    public PersonaResponse obtener(@PathVariable Long id) { return service.obtener(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'personas','ALTA')")
    public PersonaResponse crear(@Valid @RequestBody PersonaRequest request, JwtAuthenticationToken auth) { return service.crear(request, auth.getName()); }
    @PutMapping("/{id}") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'personas','CAMBIO')")
    public PersonaResponse modificar(@PathVariable Long id,@Valid @RequestBody PersonaRequest request,JwtAuthenticationToken auth) { return service.modificar(id,request,auth.getName()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'personas','BAJA')")
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
    @GetMapping("/options/generos") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'personas','CONSULTAR')") public List<PersonaOptionResponse> generos(){return service.generos();}
    @GetMapping("/options/estados-civiles") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'personas','CONSULTAR')") public List<PersonaOptionResponse> estados(){return service.estadosCiviles();}
    @GetMapping("/print") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'personas','IMPRIMIR')") public List<PersonaResponse> imprimir(@RequestParam(required=false) String search){return service.listar(search);}
    @GetMapping(value="/export/excel",produces="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'personas','EXPORTAR')") public ResponseEntity<byte[]> excel(@RequestParam(required=false) String search){return file(service.excel(search),"personas.xlsx",MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));}
    @GetMapping(value="/export/pdf",produces=MediaType.APPLICATION_PDF_VALUE) @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'personas','EXPORTAR')") public ResponseEntity<byte[]> pdf(@RequestParam(required=false) String search){return file(service.pdf(search),"personas.pdf",MediaType.APPLICATION_PDF);}
    private ResponseEntity<byte[]> file(byte[] body,String name,MediaType type){return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+name).contentType(type).body(body);}
}
