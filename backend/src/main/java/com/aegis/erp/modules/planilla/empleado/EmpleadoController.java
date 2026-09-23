package com.aegis.erp.modules.planilla.empleado;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/planilla/empleados") public class EmpleadoController {
 private final EmpleadoService service;public EmpleadoController(EmpleadoService service){this.service=service;}
 @GetMapping @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'empleado','CONSULTAR')") public List<EmpleadoResponse> listar(@RequestParam(required=false)String search){return service.listar(search);}
 @GetMapping("/{id}") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'empleado','CONSULTAR')") public EmpleadoResponse obtener(@PathVariable Long id){return service.obtener(id);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'empleado','ALTA')") public EmpleadoResponse crear(@Valid @RequestBody EmpleadoRequest request,JwtAuthenticationToken auth){return service.crear(request,auth.getName());}
 @PutMapping("/{id}") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'empleado','CAMBIO')") public EmpleadoResponse modificar(@PathVariable Long id,@Valid @RequestBody EmpleadoRequest request,JwtAuthenticationToken auth){return service.modificar(id,request,auth.getName());}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'empleado','BAJA')") public void eliminar(@PathVariable Long id){service.eliminar(id);}
 @GetMapping("/options/personas") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'empleado','CONSULTAR')") public List<EmpleadoOptionResponse> personas(@RequestParam(required=false)String search){return service.personas(search);}
 @GetMapping("/options/sucursales") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'empleado','CONSULTAR')") public List<EmpleadoOptionResponse> sucursales(){return service.sucursales();}
 @GetMapping("/options/puestos") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'empleado','CONSULTAR')") public List<EmpleadoOptionResponse> puestos(){return service.puestos();}
 @GetMapping("/options/status") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'empleado','CONSULTAR')") public List<EmpleadoOptionResponse> status(){return service.estados();}
 @GetMapping("/{id}/status-permitidos") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'empleado','CONSULTAR')") public List<EmpleadoOptionResponse> statusPermitidos(@PathVariable Long id){return service.statusPermitidos(id);}
 @GetMapping("/print") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'empleado','IMPRIMIR')") public List<EmpleadoResponse> imprimir(@RequestParam(required=false)String search){return service.listar(search);}
 @GetMapping(value="/export/excel",produces="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'empleado','EXPORTAR')") public ResponseEntity<byte[]> excel(@RequestParam(required=false)String search){return file(service.excel(search),"empleados.xlsx",MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));}
 @GetMapping(value="/export/pdf",produces=MediaType.APPLICATION_PDF_VALUE) @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'empleado','EXPORTAR')") public ResponseEntity<byte[]> pdf(@RequestParam(required=false)String search){return file(service.pdf(search),"empleados.pdf",MediaType.APPLICATION_PDF);}
 private ResponseEntity<byte[]> file(byte[] body,String name,MediaType type){return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+name).contentType(type).body(body);}
}
