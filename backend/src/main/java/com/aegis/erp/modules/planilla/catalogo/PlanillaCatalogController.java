package com.aegis.erp.modules.planilla.catalogo;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planilla/catalogos/{catalogo}")
public class PlanillaCatalogController {
    private final PlanillaCatalogService service;
    public PlanillaCatalogController(PlanillaCatalogService service){this.service=service;}
    @GetMapping @PreAuthorize("@planillaCatalogAuthorization.allowed(authentication.name,#catalogo,'CONSULTAR')")
    public List<PlanillaCatalogResponse> list(@PathVariable String catalogo){return service.list(catalogo);}
    @GetMapping("/options") @PreAuthorize("@planillaCatalogAuthorization.allowed(authentication.name,#catalogo,'CONSULTAR') or #catalogo == 'empresas'")
    public List<PlanillaCatalogResponse> options(@PathVariable String catalogo){return service.options(catalogo);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("@planillaCatalogAuthorization.allowed(authentication.name,#catalogo,'ALTA')")
    public PlanillaCatalogResponse create(@PathVariable String catalogo,@Valid @RequestBody PlanillaCatalogRequest request,JwtAuthenticationToken auth){return service.create(catalogo,request,auth.getName());}
    @PutMapping("/{id}") @PreAuthorize("@planillaCatalogAuthorization.allowed(authentication.name,#catalogo,'CAMBIO')")
    public PlanillaCatalogResponse update(@PathVariable String catalogo,@PathVariable String id,@Valid @RequestBody PlanillaCatalogRequest request,JwtAuthenticationToken auth){return service.update(catalogo,id,request,auth.getName());}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("@planillaCatalogAuthorization.allowed(authentication.name,#catalogo,'BAJA')")
    public void delete(@PathVariable String catalogo,@PathVariable String id){service.delete(catalogo,id);}
    @GetMapping("/print") @PreAuthorize("@planillaCatalogAuthorization.allowed(authentication.name,#catalogo,'IMPRIMIR')")
    public List<PlanillaCatalogResponse> print(@PathVariable String catalogo){return service.list(catalogo);}
    @GetMapping(value="/export/excel",produces="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") @PreAuthorize("@planillaCatalogAuthorization.allowed(authentication.name,#catalogo,'EXPORTAR')")
    public ResponseEntity<byte[]> excel(@PathVariable String catalogo,@RequestParam(required=false) String search){return file(service.excel(catalogo,search),catalogo+".xlsx",MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));}
    @GetMapping(value="/export/pdf",produces=MediaType.APPLICATION_PDF_VALUE) @PreAuthorize("@planillaCatalogAuthorization.allowed(authentication.name,#catalogo,'EXPORTAR')")
    public ResponseEntity<byte[]> pdf(@PathVariable String catalogo,@RequestParam(required=false) String search){return file(service.pdf(catalogo,search),catalogo+".pdf",MediaType.APPLICATION_PDF);}
    private ResponseEntity<byte[]> file(byte[] body,String name,MediaType type){return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+name).contentType(type).body(body);}
}
