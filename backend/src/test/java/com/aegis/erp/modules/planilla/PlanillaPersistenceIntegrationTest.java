package com.aegis.erp.modules.planilla;

import static org.junit.jupiter.api.Assertions.*;

import com.aegis.erp.modules.planilla.entity.*;
import com.aegis.erp.modules.planilla.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional(readOnly = true)
@EnabledIfEnvironmentVariable(named = "AEGIS_PLANILLA_INTEGRATION_TESTS", matches = "true")
class PlanillaPersistenceIntegrationTest {
    @Autowired StatusEmpleadoRepository status;
    @Autowired FlujoStatusEmpleadoRepository flujos;
    @Autowired DepartamentoRepository departamentos;
    @Autowired EstadoCivilRepository estadosCiviles;
    @Autowired TipoDocumentoRepository tiposDocumento;
    @Autowired PersonaRepository personas;
    @Autowired DocumentoPersonaRepository documentos;
    @Autowired PuestoRepository puestos;
    @Autowired BancoRepository bancos;
    @Autowired EmpleadoRepository empleados;
    @Autowired CuentaBancariaEmpleadoRepository cuentas;
    @Autowired InasistenciaRepository inasistencias;
    @Autowired PeriodoPlanillaRepository periodos;
    @Autowired PlanillaCabeceraRepository cabeceras;
    @Autowired PlanillaDetalleRepository detalles;
    @Autowired LiquidacionRepository liquidaciones;
    @Autowired EntityManager entityManager;

    @Test
    void readsInstalledAcademicDataWithoutMutatingIt() {
        assertAll(
                () -> assertEquals(5, status.count()),
                () -> assertEquals(9, flujos.count()),
                () -> assertEquals(13, departamentos.count()),
                () -> assertEquals(5, estadosCiviles.count()),
                () -> assertEquals(5, tiposDocumento.count()),
                () -> assertEquals(100, personas.count()),
                () -> assertEquals(100, documentos.count()),
                () -> assertEquals(29, puestos.count()),
                () -> assertEquals(4, bancos.count()),
                () -> assertEquals(100, empleados.count()),
                () -> assertEquals(100, cuentas.count()),
                () -> assertEquals(0, inasistencias.count()),
                () -> assertEquals(16, periodos.count()),
                () -> assertEquals(0, cabeceras.count()),
                () -> assertEquals(0, detalles.count()),
                () -> assertEquals(0, liquidaciones.count()));
    }

    @Test
    void resolvesSimpleCompositeAndPhaseOneRelationships() {
        Departamento departamento = departamentos.findById(1L).orElseThrow();
        Persona persona = personas.findById(1L).orElseThrow();
        Empleado empleado = empleados.findById(1L).orElseThrow();
        FlujoStatusEmpleado flujo = flujos.findAll().getFirst();
        DocumentoPersona documento = documentos.findAll().getFirst();

        assertAll(
                () -> assertNotNull(departamento.getEmpresa().getId()),
                () -> assertNotNull(persona.getGenero().getId()),
                () -> assertNotNull(empleado.getSucursal().getId()),
                () -> assertNotNull(flujo.getStatusActual().getId()),
                () -> assertNotNull(flujo.getStatusNuevo().getId()),
                () -> assertNotNull(documento.getTipoDocumento().getId()),
                () -> assertNotNull(documento.getPersona().getId()),
                () -> assertTrue(periodos.existsById(new PeriodoPlanillaId(2025, 9))));
    }

    @Test
    void verifiesInstalledDynamicNavigationAndAdministratorAssignments() {
        assertEquals(1L, count("SELECT COUNT(*) FROM MODULO WHERE ID_MODULO=2 AND NOMBRE='Planilla'"));
        assertEquals(4L, count("SELECT COUNT(*) FROM MENU WHERE ID_MODULO=2 AND ID_MENU BETWEEN 5 AND 8"));
        assertEquals(16L, count("SELECT COUNT(*) FROM OPCION WHERE ID_OPCION BETWEEN 11 AND 26"));
        assertEquals(16L, count("SELECT COUNT(*) FROM ROLE_OPCION ro JOIN ROLE r ON r.ID_ROLE=ro.ID_ROLE "
                + "WHERE UPPER(TRIM(r.NOMBRE))='ADMINISTRADOR' AND ro.ID_OPCION BETWEEN 11 AND 26"));
    }

    private long count(String sql) {
        return ((Number) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
    }
}
