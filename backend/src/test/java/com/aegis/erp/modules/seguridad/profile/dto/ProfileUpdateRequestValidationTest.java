package com.aegis.erp.modules.seguridad.profile.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

class ProfileUpdateRequestValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void aceptaCorreosConDominioSubdominioYTag() {
        for (String email :
                new String[] {
                    "nombre.apellido@empresa.com.gt",
                    "usuario@subdominio.organizacion.org",
                    "nombre+tag@dominio.com"
                }) {
            assertThat(validator.validate(new ProfileUpdateRequest(email, "+502 5555-5555")))
                    .isEmpty();
        }
    }

    @Test
    void aceptaTelefonosAprobadosYRechazaLetras() {
        for (String phone :
                new String[] {
                    "+502 5555-5555",
                    "(001) 402-584754",
                    "+1 (402) 584-7540",
                    "50255555555"
                }) {
            assertThat(validator.validate(new ProfileUpdateRequest("user@example.com", phone)))
                    .isEmpty();
        }
        assertThat(
                        validator.validate(
                                new ProfileUpdateRequest("user@example.com", "abc555")))
                .anyMatch(
                        violation ->
                                violation.getPropertyPath().toString().equals("telefonoMovil"));
    }
}
