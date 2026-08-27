package com.aegis.erp.common.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail invalidCredentials(InvalidCredentialsException e) {
        return problem(
                HttpStatus.UNAUTHORIZED,
                "Credenciales inválidas.",
                "Autenticación fallida",
                "invalid-credentials");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException e) {
        ProblemDetail p =
                problem(
                        HttpStatus.BAD_REQUEST,
                        "La solicitud contiene datos inválidos.",
                        "Solicitud inválida",
                        "validation");
        return p;
    }

    @ExceptionHandler(InvalidPasswordChangeException.class)
    ProblemDetail invalidPasswordChange(InvalidPasswordChangeException e) {
        return problem(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                "Cambio de contraseña inválido",
                "invalid-password-change");
    }

    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    ProblemDetail invalidPasswordResetToken(InvalidPasswordResetTokenException e) {
        return problem(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                "Enlace de recuperación inválido",
                "invalid-password-reset-token");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail notFound(ResourceNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, e.getMessage(), "Recurso no encontrado", "not-found");
    }

    @ExceptionHandler(BusinessConflictException.class)
    ProblemDetail conflict(BusinessConflictException e) {
        return problem(HttpStatus.CONFLICT, e.getMessage(), "Conflicto de negocio", "conflict");
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    ProblemDetail forbidden(org.springframework.security.access.AccessDeniedException e) {
        return problem(
                HttpStatus.FORBIDDEN,
                "No posee permisos para realizar esta operación.",
                "Acceso denegado",
                "forbidden");
    }

    @ExceptionHandler(DatabaseUnavailableException.class)
    ProblemDetail database(DatabaseUnavailableException e) {
        return problem(
                HttpStatus.SERVICE_UNAVAILABLE,
                "La base de datos Oracle no está disponible en este momento.",
                "Base de datos no disponible",
                "database-unavailable");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception e) {
        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno inesperado.",
                "Error interno",
                "internal-error");
    }

    private ProblemDetail problem(HttpStatus status, String detail, String title, String type) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detail);
        p.setType(URI.create("urn:aegis-erp:problem:" + type));
        p.setTitle(title);
        return p;
    }
}
