package com.aegis.erp.modules.seguridad.controller;

import com.aegis.erp.common.response.ApplicationHealthResponse;
import com.aegis.erp.common.response.DatabaseHealthResponse;
import com.aegis.erp.modules.seguridad.service.DatabaseHealthService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    private final ObjectProvider<DatabaseHealthService> databaseHealthService;

    public HealthController(ObjectProvider<DatabaseHealthService> databaseHealthService) {
        this.databaseHealthService = databaseHealthService;
    }

    @GetMapping
    public ApplicationHealthResponse applicationHealth() {
        return new ApplicationHealthResponse("UP", "AEGIS-ERP");
    }

    @GetMapping("/database")
    public DatabaseHealthResponse databaseHealth() {
        return databaseHealthService.getObject().check();
    }
}