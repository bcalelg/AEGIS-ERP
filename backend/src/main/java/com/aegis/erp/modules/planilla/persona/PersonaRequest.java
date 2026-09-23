package com.aegis.erp.modules.planilla.persona;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PersonaRequest(
        @NotBlank @Size(max = 50) String nombre,
        @NotBlank @Size(max = 50) String apellido,
        @NotNull LocalDate fechaNacimiento,
        @NotNull Long generoId,
        @NotBlank @Size(max = 100) String direccion,
        @NotBlank @Size(max = 50) String telefono,
        @Email @Size(max = 50) String correoElectronico,
        @NotNull Long estadoCivilId) {}
