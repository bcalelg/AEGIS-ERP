package com.aegis.erp.modules.seguridad.usuario.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioRequestValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validaEdadMinimaConAnoMesYDia() {
        LocalDate today = LocalDate.now();
        assertThat(validator.validate(request(today.minusYears(18)))).isEmpty();
        assertThat(validator.validate(request(today.minusYears(18).plusDays(1))))
                .anyMatch(value -> value.getPropertyPath().toString().equals("fechaNacimiento"));
        assertThat(validator.validate(request(today.plusDays(1))))
                .anyMatch(value -> value.getPropertyPath().toString().equals("fechaNacimiento"));
    }

    @Test
    void validaCorreosRazonablesYRechazaFormatosInvalidos() {
        assertThat(validator.validate(requestWithEmail("nombre.apellido@empresa.com.gt"))).isEmpty();
        assertThat(validator.validate(requestWithEmail("usuario@subdominio.organizacion.org"))).isEmpty();
        assertThat(validator.validate(requestWithEmail("nombre+tag@dominio.com"))).isEmpty();
        for (String invalid : new String[] {"usuario", "usuario@", "@dominio.com", "usuario dominio@gmail.com", "usuario@ dominio.com"}) {
            assertThat(validator.validate(requestWithEmail(invalid)))
                    .anyMatch(value -> value.getPropertyPath().toString().equals("correoElectronico"));
        }
    }

    @Test
    void validaFormatosTelefonicosSinAceptarLetrasNiExcederOracle() {
        for (String valid : new String[] {"+502 5555-5555", "(001) 402-584754", "+1 (402) 584-7540", "50255555555"}) {
            assertThat(validator.validate(requestWithPhone(valid))).isEmpty();
        }
        assertThat(validator.validate(requestWithPhone("abc555")))
                .anyMatch(value -> value.getPropertyPath().toString().equals("telefonoMovil"));
        assertThat(validator.validate(requestWithPhone("1".repeat(31))))
                .anyMatch(value -> value.getPropertyPath().toString().equals("telefonoMovil"));
    }

    private UsuarioCreateRequest request(LocalDate birthDate) {
        return new UsuarioCreateRequest("TEST", "José", "O'Connor", birthDate,
                "usuario@example.com", "+502 5555-5555", "Temporal12!", "Temporal12!",
                "Pregunta", "Respuesta", 1L, 2L, 3L, 4L, 5L);
    }

    private UsuarioCreateRequest requestWithEmail(String email) {
        UsuarioCreateRequest base = request(LocalDate.now().minusYears(30));
        return new UsuarioCreateRequest(base.idUsuario(), base.nombre(), base.apellido(), base.fechaNacimiento(),
                email, base.telefonoMovil(), base.password(), base.passwordConfirmacion(), base.pregunta(),
                base.respuesta(), base.idEmpresa(), base.idSucursal(), base.idGenero(), base.idStatusUsuario(), base.idRole());
    }

    private UsuarioCreateRequest requestWithPhone(String phone) {
        UsuarioCreateRequest base = request(LocalDate.now().minusYears(30));
        return new UsuarioCreateRequest(base.idUsuario(), base.nombre(), base.apellido(), base.fechaNacimiento(),
                base.correoElectronico(), phone, base.password(), base.passwordConfirmacion(), base.pregunta(),
                base.respuesta(), base.idEmpresa(), base.idSucursal(), base.idGenero(), base.idStatusUsuario(), base.idRole());
    }
}
