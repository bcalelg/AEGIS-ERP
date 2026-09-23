package com.aegis.erp.modules.planilla.empleado;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
public record EmpleadoRequest(@NotNull Long personaId,@NotNull Long sucursalId,@NotNull LocalDate fechaContratacion,
 @NotNull Long puestoId,@NotNull Long statusId,
 @NotNull @Digits(integer=8,fraction=2) BigDecimal ingresoSueldoBase,
 @NotNull @Digits(integer=8,fraction=2) BigDecimal ingresoBonificacionDecreto,
 @NotNull @Digits(integer=8,fraction=2) BigDecimal ingresoOtrosIngresos,
 @NotNull @Digits(integer=8,fraction=2) BigDecimal descuentoIgss,
 @NotNull @Digits(integer=8,fraction=2) BigDecimal descuentoIsr,
 @NotNull @Digits(integer=8,fraction=2) BigDecimal descuentoInasistencias) {}
