package com.aegis.erp.modules.seguridad.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.aegis.erp.modules.seguridad.usuario.entity.StatusUsuario;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StatusUsuarioPolicyTest {
    @ParameterizedTest
    @ValueSource(strings = {"Activo", "ACTIVO", "activo", " Activo "})
    void reconoceActivoSinDependerDeMayusculasOEspacios(String nombre) {
        assertThat(StatusUsuarioPolicy.isActivo(new StatusUsuario(1L, nombre))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "Activo", "ACTIVO", "Inactivo", "INACTIVO",
                "Bloqueado por intentos de acceso", "BLOQUEADO POR INTENTOS DE ACCESO"
            })
    void reconoceEstadosTecnicos(String nombre) {
        assertThat(StatusUsuarioPolicy.isSystemStatus(nombre)).isTrue();
    }
}
