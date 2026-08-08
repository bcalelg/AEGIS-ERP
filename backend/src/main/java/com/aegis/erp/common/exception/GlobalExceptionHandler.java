package com.aegis.erp.common.exception;

import java.net.URI;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail invalidCredentials(InvalidCredentialsException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Credenciales inválidas.");
        problem.setType(URI.create("urn:aegis-erp:problem:invalid-credentials"));
        problem.setTitle("Autenticación fallida");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "La solicitud contiene datos inválidos.");
        problem.setType(URI.create("urn:aegis-erp:problem:validation"));
        problem.setTitle("Solicitud inválida");
        return problem;
    }

    @ExceptionHandler(DatabaseUnavailableException.class)
    ProblemDetail database(DatabaseUnavailableException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "La base de datos Oracle no está disponible en este momento.");
        problem.setType(URI.create("urn:aegis-erp:problem:database-unavailable"));
        problem.setTitle("Base de datos no disponible");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno inesperado.");
        problem.setType(URI.create("urn:aegis-erp:problem:internal-error"));
        problem.setTitle("Error interno");
        return problem;
    }
}