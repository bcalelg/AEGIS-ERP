package com.aegis.erp.modules.planilla.persona;

import java.time.LocalDate;

public record PersonaResponse(Long id, String nombre, String apellido, LocalDate fechaNacimiento,
        Long generoId, String generoNombre, String direccion, String telefono,
        String correoElectronico, Long estadoCivilId, String estadoCivilNombre) {}
