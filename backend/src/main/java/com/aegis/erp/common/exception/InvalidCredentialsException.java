package com.aegis.erp.common.exception;

public class InvalidCredentialsException extends RuntimeException {
    public static final String GENERIC_MESSAGE =
            "Usuario o contraseña incorrectos. Si el problema persiste, contacte al administrador del sistema.";

    public InvalidCredentialsException() {
        super(GENERIC_MESSAGE);
    }
}
