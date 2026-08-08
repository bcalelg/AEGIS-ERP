package com.aegis.erp.common.exception;
import java.net.URI; import org.springframework.http.HttpStatus; import org.springframework.http.ProblemDetail; import org.springframework.web.bind.annotation.ExceptionHandler; import org.springframework.web.bind.annotation.RestControllerAdvice;
@RestControllerAdvice public class GlobalExceptionHandler {
@ExceptionHandler(DatabaseUnavailableException.class) ProblemDetail database(DatabaseUnavailableException e){var p=ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,"La base de datos Oracle no está disponible en este momento.");p.setType(URI.create("urn:aegis-erp:problem:database-unavailable"));p.setTitle("Base de datos no disponible");return p;}
@ExceptionHandler(Exception.class) ProblemDetail unexpected(Exception e){var p=ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,"Ocurrió un error interno inesperado.");p.setType(URI.create("urn:aegis-erp:problem:internal-error"));p.setTitle("Error interno");return p;}}
