package com.aegis.erp.modules.planilla.calculo;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planilla/calculo/periodos")
public class PeriodoCalculoController {
    private final PeriodoCalculoService service;
    public PeriodoCalculoController(PeriodoCalculoService service) { this.service = service; }

    @GetMapping
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'calculo_planilla','CONSULTAR')")
    public List<PeriodoCalculoResponse> listar() { return service.listar(); }

    @GetMapping("/{anio}/{mes}")
    @PreAuthorize("@permissionAuthorizationService.allowed(authentication.name,'calculo_planilla','CONSULTAR')")
    public PeriodoCalculoResponse obtener(@PathVariable Integer anio, @PathVariable Integer mes) {
        return service.obtener(anio, mes);
    }
}
