package com.aegis.erp.modules.planilla.catalogo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.aegis.erp.security.PermissionAuthorizationService;
import org.junit.jupiter.api.Test;

class PlanillaCatalogAuthorizationTest {
    @Test void delegatesAcademicLogicalPagesAndRejectsUnknownCatalogs(){var permissions=mock(PermissionAuthorizationService.class);when(permissions.allowed("admin","estado_civil","CONSULTAR")).thenReturn(true);var authorization=new PlanillaCatalogAuthorization(permissions);assertTrue(authorization.allowed("admin","estados-civiles","CONSULTAR"));assertFalse(authorization.allowed("admin","otro","CONSULTAR"));verify(permissions).allowed("admin","estado_civil","CONSULTAR");}
}
