package com.aegis.erp.modules.planilla.documentopersona;

import com.aegis.erp.modules.planilla.persona.PersonaOptionResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planilla/documentos-persona")
public class DocumentoPersonaController {
    private final DocumentoPersonaService service;
    public DocumentoPersonaController(DocumentoPersonaService service){this.service=service;}
    @GetMapping @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'documento_persona','CONSULTAR')") public List<DocumentoPersonaResponse> listar(@RequestParam(required=false) String search){return service.listar(search);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'documento_persona','ALTA')") public DocumentoPersonaResponse crear(@Valid @RequestBody DocumentoPersonaRequest request,JwtAuthenticationToken auth){return service.crear(request,auth.getName());}
    @PutMapping("/{tipoId}/{personaId}") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'documento_persona','CAMBIO')") public DocumentoPersonaResponse modificar(@PathVariable Long tipoId,@PathVariable Long personaId,@Valid @RequestBody DocumentoPersonaRequest request,JwtAuthenticationToken auth){return service.modificar(tipoId,personaId,request,auth.getName());}
    @DeleteMapping("/{tipoId}/{personaId}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'documento_persona','BAJA')") public void eliminar(@PathVariable Long tipoId,@PathVariable Long personaId){service.eliminar(tipoId,personaId);}
    @GetMapping("/options/personas") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'documento_persona','CONSULTAR')") public List<PersonaOptionResponse> personas(@RequestParam(required=false) String search){return service.personas(search);}
    @GetMapping("/options/tipos-documento") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'documento_persona','CONSULTAR')") public List<PersonaOptionResponse> tipos(){return service.tipos();}
    @GetMapping("/print") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'documento_persona','IMPRIMIR')") public List<DocumentoPersonaResponse> imprimir(@RequestParam(required=false) String search){return service.listar(search);}
    @GetMapping(value="/export/excel",produces="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'documento_persona','EXPORTAR')") public ResponseEntity<byte[]> excel(@RequestParam(required=false) String search){return file(service.excel(search),"documentos-persona.xlsx",MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));}
    @GetMapping(value="/export/pdf",produces=MediaType.APPLICATION_PDF_VALUE) @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'documento_persona','EXPORTAR')") public ResponseEntity<byte[]> pdf(@RequestParam(required=false) String search){return file(service.pdf(search),"documentos-persona.pdf",MediaType.APPLICATION_PDF);}
    private ResponseEntity<byte[]> file(byte[] body,String name,MediaType type){return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+name).contentType(type).body(body);}
}
