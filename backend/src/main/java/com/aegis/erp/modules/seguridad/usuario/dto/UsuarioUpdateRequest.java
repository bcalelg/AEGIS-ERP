package com.aegis.erp.modules.seguridad.usuario.dto;

import com.aegis.erp.common.validation.Adult;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UsuarioUpdateRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 100) String apellido,
        @NotNull @Adult LocalDate fechaNacimiento,
        @NotBlank @Email @Size(max = 100) String correoElectronico,
        @Size(max = 30) @Pattern(regexp = "^[0-9+()\\-\\s]+$", message = "El teléfono solo puede contener números, espacios, +, -, ( y ).") String telefonoMovil,
        @NotBlank @Size(max = 200) String pregunta,
        @Size(max = 200) String respuesta,
        @NotNull Long idEmpresa,
        @NotNull Long idSucursal,
        @NotNull Long idGenero,
        @NotNull Long idStatusUsuario,
        @NotNull Long idRole) {}
