package com.aegis.erp.modules.planilla.documentopersona;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.planilla.entity.*;
import com.aegis.erp.modules.planilla.repository.*;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentoPersonaServiceTest {
 @Mock DocumentoPersonaRepository documentos;@Mock PersonaRepository personas;@Mock TipoDocumentoRepository tipos;@Mock DocumentExportService exports;DocumentoPersonaService service;
 @BeforeEach void setup(){service=new DocumentoPersonaService(documentos,personas,tipos,exports,Clock.fixed(Instant.parse("2026-09-21T12:00:00Z"),ZoneOffset.UTC));}
 @Test void rechazaCombinacionDuplicada(){when(documentos.existsById(new DocumentoPersonaId(1L,2L))).thenReturn(true);assertThrows(BusinessConflictException.class,()->service.crear(new DocumentoPersonaRequest(1L,2L,"123"),"admin"));}
 @Test void impideMutarClaveCompuesta(){DocumentoPersona entity=mock(DocumentoPersona.class);when(documentos.findById(new DocumentoPersonaId(1L,2L))).thenReturn(Optional.of(entity));assertThrows(BusinessConflictException.class,()->service.modificar(1L,2L,new DocumentoPersonaRequest(3L,2L,"123"),"admin"));verify(entity,never()).modificarNumero(any(),any(),any());}
}
