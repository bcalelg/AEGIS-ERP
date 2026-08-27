package com.aegis.erp.modules.seguridad.auth.service;

import com.aegis.erp.common.exception.InvalidPasswordChangeException;
import com.aegis.erp.modules.seguridad.empresa.entity.Empresa;

public final class PasswordPolicyValidator {
    private PasswordPolicyValidator() {}

    public static void validate(Empresa empresa, String password) {
        validate(empresa, password, "La contraseña no cumple la política de seguridad de la empresa.");
    }

    public static void validateForChange(Empresa empresa, String password) {
        validate(empresa, password, "La nueva contraseña no cumple la política de seguridad de la empresa.");
    }

    private static void validate(Empresa empresa, String password, String message) {
        int largo = password.codePointCount(0, password.length());
        long mayusculas = password.codePoints().filter(Character::isUpperCase).count();
        long minusculas = password.codePoints().filter(Character::isLowerCase).count();
        long numeros = password.codePoints().filter(Character::isDigit).count();
        long especiales =
                password.codePoints().filter(value -> !Character.isLetterOrDigit(value)).count();

        if (largo < required(empresa.getPasswordLargo())
                || mayusculas < required(empresa.getPasswordCantidadMayusculas())
                || minusculas < required(empresa.getPasswordCantidadMinusculas())
                || numeros < required(empresa.getPasswordCantidadNumeros())
                || especiales < required(empresa.getPasswordCantidadCaracteresEspeciales())) {
            throw new InvalidPasswordChangeException(message);
        }
    }

    private static int required(Integer value) {
        if (value == null || value < 0) {
            throw new IllegalStateException("Política de contraseña incompleta.");
        }
        return value;
    }
}
