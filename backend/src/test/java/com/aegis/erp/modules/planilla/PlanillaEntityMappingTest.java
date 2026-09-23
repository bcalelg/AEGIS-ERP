package com.aegis.erp.modules.planilla;

import static org.junit.jupiter.api.Assertions.*;

import com.aegis.erp.modules.planilla.entity.*;
import com.aegis.erp.modules.seguridad.empresa.entity.Empresa;
import com.aegis.erp.modules.seguridad.genero.entity.Genero;
import com.aegis.erp.modules.seguridad.usuario.entity.Sucursal;
import jakarta.persistence.*;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlanillaEntityMappingTest {
    private static final List<Class<?>> ENTITIES = List.of(
            StatusEmpleado.class, FlujoStatusEmpleado.class, Departamento.class,
            EstadoCivil.class, TipoDocumento.class, Persona.class, DocumentoPersona.class,
            Puesto.class, Banco.class, Empleado.class, CuentaBancariaEmpleado.class,
            Inasistencia.class, PeriodoPlanilla.class, PlanillaCabecera.class,
            PlanillaDetalle.class, Liquidacion.class);

    @Test
    void mapsTheSixteenPhaseTwoEntities() {
        assertEquals(16, ENTITIES.size());
        ENTITIES.forEach(type -> assertNotNull(type.getAnnotation(Entity.class), type.getName()));
    }

    @Test
    void mapsCompositeIdentifiersWithoutArtificialIds() {
        assertTrue(FlujoStatusEmpleadoId.class.isAnnotationPresent(Embeddable.class));
        assertTrue(DocumentoPersonaId.class.isAnnotationPresent(Embeddable.class));
        assertTrue(PeriodoPlanillaId.class.isAnnotationPresent(Embeddable.class));
        assertNotNull(field(FlujoStatusEmpleado.class, "id").getAnnotation(EmbeddedId.class));
        assertNotNull(field(DocumentoPersona.class, "id").getAnnotation(EmbeddedId.class));
        assertNotNull(field(PeriodoPlanilla.class, "id").getAnnotation(EmbeddedId.class));
        assertNotNull(field(PlanillaCabecera.class, "id").getAnnotation(EmbeddedId.class));
    }

    @Test
    void reusesPhaseOneEntitiesForExternalForeignKeys() {
        assertEquals(Empresa.class, field(Departamento.class, "empresa").getType());
        assertEquals(Genero.class, field(Persona.class, "genero").getType());
        assertEquals(Sucursal.class, field(Empleado.class, "sucursal").getType());
    }

    @Test
    void mapsSharedAndCompositeForeignKeys() {
        assertNotNull(field(FlujoStatusEmpleado.class, "statusActual").getAnnotation(MapsId.class));
        assertNotNull(field(FlujoStatusEmpleado.class, "statusNuevo").getAnnotation(MapsId.class));
        assertNotNull(field(DocumentoPersona.class, "tipoDocumento").getAnnotation(MapsId.class));
        assertNotNull(field(DocumentoPersona.class, "persona").getAnnotation(MapsId.class));
        assertNotNull(field(PlanillaCabecera.class, "periodo").getAnnotation(MapsId.class));
        assertEquals(2, field(PlanillaDetalle.class, "cabecera")
                .getAnnotation(JoinColumns.class).value().length);
    }

    private static Field field(Class<?> type, String name) {
        try {
            return type.getDeclaredField(name);
        } catch (NoSuchFieldException exception) {
            return fail("Missing field " + type.getSimpleName() + "." + name, exception);
        }
    }
}
