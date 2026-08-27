package com.aegis.erp.modules.seguridad.empresa.dto;

import java.time.LocalDateTime;

public record EmpresaResponse(
        Long idEmpresa,
        String nombre,
        String direccion,
        String nit,
        Integer passwordCantidadMayusculas,
        Integer passwordCantidadMinusculas,
        Integer passwordCantidadCaracteresEspeciales,
        Integer passwordCantidadCaducidadDias,
        Integer passwordLargo,
        Integer passwordIntentosAntesDeBloquear,
        Integer passwordCantidadNumeros,
        Integer passwordCantidadPreguntasValidar,
        LocalDateTime fechaCreacion,
        String usuarioCreacion,
        LocalDateTime fechaModificacion,
        String usuarioModificacion) {}
