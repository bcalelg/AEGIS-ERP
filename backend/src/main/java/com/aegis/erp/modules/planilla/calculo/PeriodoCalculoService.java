package com.aegis.erp.modules.planilla.calculo;

import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.modules.planilla.entity.PeriodoPlanilla;
import com.aegis.erp.modules.planilla.entity.PeriodoPlanillaId;
import com.aegis.erp.modules.planilla.repository.PeriodoPlanillaRepository;
import com.aegis.erp.modules.planilla.repository.PlanillaCabeceraRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PeriodoCalculoService {
    private final PeriodoPlanillaRepository periodos;
    private final PlanillaCabeceraRepository cabeceras;

    public PeriodoCalculoService(PeriodoPlanillaRepository periodos, PlanillaCabeceraRepository cabeceras) {
        this.periodos = periodos;
        this.cabeceras = cabeceras;
    }

    @Transactional(readOnly = true)
    public List<PeriodoCalculoResponse> listar() {
        return periodos.findAll().stream()
                .sorted(Comparator.comparing((PeriodoPlanilla p) -> p.getId().anio()).reversed()
                        .thenComparing(p -> p.getId().mes(), Comparator.reverseOrder()))
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public PeriodoCalculoResponse obtener(Integer anio, Integer mes) {
        PeriodoPlanillaId id = new PeriodoPlanillaId(anio, mes);
        return response(periodos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Período de planilla no encontrado.")));
    }

    private PeriodoCalculoResponse response(PeriodoPlanilla periodo) {
        PeriodoPlanillaId id = periodo.getId();
        return new PeriodoCalculoResponse(id.anio(), id.mes(), periodo.getFechaInicio(), periodo.getFechaFin(),
                periodo.getFechaCreacion(), periodo.getUsuarioCreacion(), periodo.getFechaModificacion(),
                periodo.getUsuarioModificacion(), cabeceras.existsById(id));
    }
}
