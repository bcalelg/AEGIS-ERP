package com.aegis.erp.common.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail unreadableRequest(HttpMessageNotReadableException e) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "La solicitud contiene campos o contenido no permitido.",
                "Solicitud inválida",
                "invalid-request");
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

    @ExceptionHandler(InvalidProfilePhotoException.class)
    ProblemDetail invalidProfilePhoto(InvalidProfilePhotoException e) {
        return problem(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                "Fotografía inválida",
                "invalid-profile-photo");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ProblemDetail maxUploadSize(MaxUploadSizeExceededException e) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "La fotografía no puede exceder 2 MB.",
                "Fotografía inválida",
                "invalid-profile-photo");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail notFound(ResourceNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, e.getMessage(), "Recurso no encontrado", "not-found");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ProblemDetail noResource(NoResourceFoundException e) {
        return problem(
                HttpStatus.NOT_FOUND,
                "El recurso solicitado no existe.",
                "Recurso no encontrado",
                "not-found");
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
        String correlationId = UUID.randomUUID().toString();
        log.error(
                "Excepción no controlada. correlationId={} cause={}",
                correlationId,
                e.getClass().getName(),
                e);
        ProblemDetail response = problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno inesperado.",
                "Error interno",
                "internal-error");
        response.setProperty("correlationId", correlationId);
        return response;
    }

    private ProblemDetail problem(HttpStatus status, String detail, String title, String type) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detail);
        p.setType(URI.create("urn:aegis-erp:problem:" + type));
        p.setTitle(title);
        return p;
    }
}
