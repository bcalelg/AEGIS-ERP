package com.aegis.erp.modules.planilla.empleado;
import java.math.BigDecimal;
import java.time.LocalDate;
public record EmpleadoResponse(Long id,Long personaId,String personaNombre,Long sucursalId,String sucursalNombre,
 LocalDate fechaContratacion,Long puestoId,String puestoNombre,String departamentoNombre,Long statusId,String statusNombre,
 BigDecimal ingresoSueldoBase,BigDecimal ingresoBonificacionDecreto,BigDecimal ingresoOtrosIngresos,
 BigDecimal descuentoIgss,BigDecimal descuentoIsr,BigDecimal descuentoInasistencias) {}
