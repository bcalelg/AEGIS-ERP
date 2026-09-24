package com.aegis.erp.modules.planilla.calculo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PeriodoCalculoResponse(
        Integer anio,
        Integer mes,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalDateTime fechaCreacion,
        String usuarioCreacion,
        LocalDateTime fechaModificacion,
        String usuarioModificacion,
        boolean planillaRegistrada) {}
