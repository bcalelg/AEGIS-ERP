package com.aegis.erp.modules.planilla.catalogo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.planilla.entity.*;
import com.aegis.erp.modules.planilla.repository.*;
import com.aegis.erp.modules.seguridad.empresa.repository.EmpresaRepository;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanillaCatalogServiceTest {
    @Mock EstadoCivilRepository estados; @Mock StatusEmpleadoRepository status;
    @Mock TipoDocumentoRepository tipos; @Mock BancoRepository bancos;
    @Mock DepartamentoRepository departamentos; @Mock FlujoStatusEmpleadoRepository flujos;
    @Mock PuestoRepository puestos; @Mock EmpresaRepository empresas; @Mock DocumentExportService documents;
    PlanillaCatalogService service;
    @BeforeEach void setup(){service=new PlanillaCatalogService(estados,status,tipos,bancos,departamentos,flujos,puestos,empresas,Clock.fixed(Instant.parse("2026-09-21T12:00:00Z"),ZoneOffset.UTC),documents);}
    @Test void createsStatusWithAuthenticatedAuditUser(){when(status.findAll()).thenReturn(List.of());StatusEmpleado saved=mock(StatusEmpleado.class);when(saved.getId()).thenReturn(6L);when(status.saveAndFlush(any())).thenReturn(saved);var result=service.create("status-empleados",new PlanillaCatalogRequest(" Vacaciones ",null,null),"admin.test");assertEquals("6",result.id());ArgumentCaptor<StatusEmpleado> captor=ArgumentCaptor.forClass(StatusEmpleado.class);verify(status).saveAndFlush(captor.capture());assertEquals("admin.test",captor.getValue().getUsuarioCreacion());}
    @Test void rejectsDuplicateCompositeTransition(){StatusEmpleado a=mock(StatusEmpleado.class),b=mock(StatusEmpleado.class);when(a.getId()).thenReturn(1L);when(b.getId()).thenReturn(2L);when(status.findById(1L)).thenReturn(Optional.of(a));when(status.findById(2L)).thenReturn(Optional.of(b));when(flujos.findAll()).thenReturn(List.of());when(flujos.existsById(new FlujoStatusEmpleadoId(1L,2L))).thenReturn(true);assertThrows(BusinessConflictException.class,()->service.create("flujos-status-empleado",new PlanillaCatalogRequest("Evento",1L,2L),"admin"));}
    @Test void translatesForeignKeyDeleteConflict(){doThrow(new DataIntegrityViolationException("FK")).when(departamentos).flush();assertThrows(BusinessConflictException.class,()->service.delete("departamentos","1"));}
}
