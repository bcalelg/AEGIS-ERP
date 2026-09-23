package com.aegis.erp.modules.planilla.persona;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.planilla.entity.EstadoCivil;
import com.aegis.erp.modules.planilla.entity.Persona;
import com.aegis.erp.modules.planilla.repository.EstadoCivilRepository;
import com.aegis.erp.modules.planilla.repository.PersonaRepository;
import com.aegis.erp.modules.seguridad.genero.entity.Genero;
import com.aegis.erp.modules.seguridad.genero.repository.GeneroRepository;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class PersonaServiceTest {
 @Mock PersonaRepository personas;@Mock GeneroRepository generos;@Mock EstadoCivilRepository estados;@Mock DocumentExportService documents;PersonaService service;
 @BeforeEach void setup(){service=new PersonaService(personas,generos,estados,documents,Clock.fixed(Instant.parse("2026-09-21T12:00:00Z"),ZoneOffset.UTC));}
 @Test void creaConReferenciasTrimYAuditoria(){Genero g=mock(Genero.class);EstadoCivil e=mock(EstadoCivil.class);when(g.getId()).thenReturn(1L);when(g.getNombre()).thenReturn("Femenino");when(e.getId()).thenReturn(2L);when(e.getNombre()).thenReturn("Soltero");when(generos.findById(1L)).thenReturn(Optional.of(g));when(estados.findById(2L)).thenReturn(Optional.of(e));when(personas.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));var result=service.crear(new PersonaRequest(" Ana "," López ",LocalDate.of(1990,1,1),1L," Zona 1 "," 5555 "," ana@example.com ",2L),"admin");assertEquals("Ana",result.nombre());ArgumentCaptor<Persona> captor=ArgumentCaptor.forClass(Persona.class);verify(personas).saveAndFlush(captor.capture());assertEquals("admin",captor.getValue().getUsuarioCreacion());assertEquals("Zona 1",captor.getValue().getDireccion());}
 @Test void traduceConflictoFkAlEliminar(){Persona p=mock(Persona.class);when(personas.findById(1L)).thenReturn(Optional.of(p));doThrow(new DataIntegrityViolationException("FK")).when(personas).flush();assertThrows(BusinessConflictException.class,()->service.eliminar(1L));}
}
