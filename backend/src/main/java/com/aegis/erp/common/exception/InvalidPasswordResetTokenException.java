package com.aegis.erp.common.exception;

public class InvalidPasswordResetTokenException extends RuntimeException {
    public InvalidPasswordResetTokenException() {
        super("El enlace de recuperación no es válido o ha expirado.");
    }
}
