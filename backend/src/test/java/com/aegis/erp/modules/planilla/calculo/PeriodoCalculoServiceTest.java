package com.aegis.erp.modules.planilla.calculo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.modules.planilla.entity.PeriodoPlanilla;
import com.aegis.erp.modules.planilla.entity.PeriodoPlanillaId;
import com.aegis.erp.modules.planilla.repository.PeriodoPlanillaRepository;
import com.aegis.erp.modules.planilla.repository.PlanillaCabeceraRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PeriodoCalculoServiceTest {
    @Mock PeriodoPlanillaRepository periodos;
    @Mock PlanillaCabeceraRepository cabeceras;

    @Test void ordenaPorAnioYMesDescendenteEIndicaCabecera() {
        PeriodoPlanilla a=periodo(2025,12),b=periodo(2026,1),c=periodo(2026,9);
        when(periodos.findAll()).thenReturn(List.of(a,b,c));
        when(cabeceras.existsById(new PeriodoPlanillaId(2026,9))).thenReturn(true);
        var result=new PeriodoCalculoService(periodos,cabeceras).listar();
        assertEquals(List.of(new PeriodoPlanillaId(2026,9),new PeriodoPlanillaId(2026,1),new PeriodoPlanillaId(2025,12)),result.stream().map(x->new PeriodoPlanillaId(x.anio(),x.mes())).toList());
        assertTrue(result.getFirst().planillaRegistrada());
        verify(cabeceras,times(3)).existsById(any());verify(cabeceras,never()).save(any());
    }
    @Test void consultaPkCompuestaEspecifica() {
        var id=new PeriodoPlanillaId(2026,9);var periodo=periodo(2026,9);when(periodos.findById(id)).thenReturn(Optional.of(periodo));
        var result=new PeriodoCalculoService(periodos,cabeceras).obtener(2026,9);
        assertEquals(2026,result.anio());assertEquals(9,result.mes());verify(periodos).findById(id);
    }
    @Test void rechazaPeriodoInexistente() {
        var id=new PeriodoPlanillaId(2030,1);when(periodos.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,()->new PeriodoCalculoService(periodos,cabeceras).obtener(2030,1));
        verifyNoInteractions(cabeceras);
    }
    private PeriodoPlanilla periodo(int anio,int mes){PeriodoPlanilla p=mock(PeriodoPlanilla.class);when(p.getId()).thenReturn(new PeriodoPlanillaId(anio,mes));lenient().when(p.getFechaInicio()).thenReturn(LocalDate.of(anio,mes,1));lenient().when(p.getFechaFin()).thenReturn(LocalDate.of(anio,mes,1).withDayOfMonth(LocalDate.of(anio,mes,1).lengthOfMonth()));return p;}
}
