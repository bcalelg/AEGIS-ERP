package com.aegis.erp.common.exception;

public class DatabaseUnavailableException extends RuntimeException {
    public DatabaseUnavailableException(Throwable cause) {
        super("No fue posible verificar la conexión con Oracle.", cause);
    }
}
