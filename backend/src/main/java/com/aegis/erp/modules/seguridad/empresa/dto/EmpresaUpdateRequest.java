package com.aegis.erp.modules.seguridad.empresa.dto;

import jakarta.validation.constraints.*;

public record EmpresaUpdateRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 200) String direccion,
        @NotBlank @Size(max = 20) String nit,
        @NotNull @PositiveOrZero Integer passwordCantidadMayusculas,
        @NotNull @PositiveOrZero Integer passwordCantidadMinusculas,
        @NotNull @PositiveOrZero Integer passwordCantidadCaracteresEspeciales,
        @NotNull @Positive Integer passwordCantidadCaducidadDias,
        @NotNull @Positive Integer passwordLargo,
        @NotNull @Positive Integer passwordIntentosAntesDeBloquear,
        @NotNull @PositiveOrZero Integer passwordCantidadNumeros,
        @NotNull @PositiveOrZero Integer passwordCantidadPreguntasValidar) {
    @AssertTrue(message = "La longitud no puede ser menor que la suma de caracteres requeridos.")
    public boolean isPasswordPolicyValid() {
        return passwordLargo == null
                || passwordCantidadMayusculas == null
                || passwordCantidadMinusculas == null
                || passwordCantidadCaracteresEspeciales == null
                || passwordCantidadNumeros == null
                || passwordLargo
                        >= passwordCantidadMayusculas
                                + passwordCantidadMinusculas
                                + passwordCantidadCaracteresEspeciales
                                + passwordCantidadNumeros;
    }
}
