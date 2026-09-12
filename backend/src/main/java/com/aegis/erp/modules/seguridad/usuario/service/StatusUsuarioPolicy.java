package com.aegis.erp.modules.seguridad.usuario.service;

import com.aegis.erp.modules.seguridad.usuario.entity.StatusUsuario;

import java.util.Locale;
import java.util.Set;

/** Reglas centralizadas para interpretar los estados técnicos sin depender de mayúsculas. */
public final class StatusUsuarioPolicy {
    public static final String ACTIVO = "Activo";
    public static final String INACTIVO = "Inactivo";
    public static final String BLOQUEADO = "Bloqueado por intentos de acceso";

    private static final Set<String> SYSTEM_STATUSES =
            Set.of(normalize(ACTIVO), normalize(INACTIVO), normalize(BLOQUEADO));

    private StatusUsuarioPolicy() {}

    public static boolean isActivo(StatusUsuario status) {
        return status != null && matches(status.getNombre(), ACTIVO);
    }

    public static boolean isInactivo(StatusUsuario status) {
        return status != null && matches(status.getNombre(), INACTIVO);
    }

    public static boolean isBloqueado(StatusUsuario status) {
        return status != null && matches(status.getNombre(), BLOQUEADO);
    }

    public static boolean isSystemStatus(String nombre) {
        return nombre != null && SYSTEM_STATUSES.contains(normalize(nombre));
    }

    public static boolean matches(String actual, String expected) {
        return actual != null && expected != null && normalize(actual).equals(normalize(expected));
    }

    private static String normalize(String value) {
        return value.strip().toLowerCase(Locale.ROOT);
    }
}
