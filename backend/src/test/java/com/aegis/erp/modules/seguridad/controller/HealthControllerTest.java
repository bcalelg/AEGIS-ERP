package com.aegis.erp.modules.seguridad.controller;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import com.aegis.erp.common.response.ApplicationHealthResponse;
import com.aegis.erp.modules.seguridad.service.DatabaseHealthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
class HealthControllerTest {
    @Test @SuppressWarnings("unchecked") void reportsUp() {
        var controller = new HealthController(mock(ObjectProvider.class));
        assertThat(controller.applicationHealth()).isEqualTo(new ApplicationHealthResponse("UP", "AEGIS-ERP"));
    }
}