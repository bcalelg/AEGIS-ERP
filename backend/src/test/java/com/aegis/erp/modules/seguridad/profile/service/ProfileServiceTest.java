package com.aegis.erp.modules.seguridad.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.InvalidProfilePhotoException;
import com.aegis.erp.modules.seguridad.empresa.entity.Empresa;
import com.aegis.erp.modules.seguridad.genero.entity.Genero;
import com.aegis.erp.modules.seguridad.profile.dto.ProfileResponse;
import com.aegis.erp.modules.seguridad.profile.dto.ProfileUpdateRequest;
import com.aegis.erp.modules.seguridad.usuario.entity.Role;
import com.aegis.erp.modules.seguridad.usuario.entity.StatusUsuario;
import com.aegis.erp.modules.seguridad.usuario.entity.Sucursal;
import com.aegis.erp.modules.seguridad.usuario.entity.Usuario;
import com.aegis.erp.modules.seguridad.usuario.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {
    @Mock private UsuarioRepository usuarios;
    @Mock private Genero genero;
    @Mock private StatusUsuario status;
    @Mock private Role role;
    @Mock private Sucursal sucursal;
    @Mock private Empresa empresa;
    private ProfileService service;
    private Usuario usuario;

    @BeforeEach
    void setup() {
        service =
                new ProfileService(
                        usuarios,
                        Clock.fixed(Instant.parse("2026-08-28T12:00:00Z"), ZoneOffset.UTC));
        lenient().when(genero.getNombre()).thenReturn("Masculino");
        lenient().when(status.getNombre()).thenReturn("Activo");
        lenient().when(role.getNombre()).thenReturn("Operador");
        lenient().when(sucursal.getNombre()).thenReturn("Central");
        lenient().when(sucursal.getEmpresa()).thenReturn(empresa);
        lenient().when(empresa.getNombre()).thenReturn("Software Inc.");
        usuario =
                Usuario.crear(
                        "propio",
                        "Marco",
                        "Lorenzana",
                        LocalDate.of(1990, 1, 1),
                        "hash-secreto",
                        "marco@example.com",
                        "555-1000",
                        "Pregunta privada",
                        "Respuesta privada",
                        genero,
                        status,
                        role,
                        sucursal,
                        "system",
                        LocalDateTime.of(2026, 1, 1, 0, 0));
        lenient().when(usuarios.findForMaintenance("propio")).thenReturn(Optional.of(usuario));
        lenient().when(usuarios.saveAndFlush(usuario)).thenReturn(usuario);
    }

    @Test
    void obtieneSoloPerfilDelUsuarioAutenticadoSinExponerSecretos() {
        ProfileResponse result = service.get("propio");

        verify(usuarios).findForMaintenance("propio");
        assertThat(result.idUsuario()).isEqualTo("propio");
        assertThat(result.empresa()).isEqualTo("Software Inc.");
        assertThat(result.fotografiaDisponible()).isFalse();
        assertThat(result.toString())
                .doesNotContain("hash-secreto", "Pregunta privada", "Respuesta privada");
    }

    @Test
    void actualizaSoloContactoYPreservaIdentidadYAsignacionAdministrativa() {
        LocalDate originalBirthDate = usuario.getFechaNacimiento();
        ProfileResponse result =
                service.update(
                        "propio",
                        new ProfileUpdateRequest(
                                "NUEVO@EXAMPLE.COM",
                                " +502 5555-1000 "));

        verify(usuarios)
                .existsByCorreoElectronicoAndIdUsuarioNot("nuevo@example.com", "propio");
        verify(usuarios)
                .existsByTelefonoMovilAndIdUsuarioNot("+502 5555-1000", "propio");
        assertThat(result.nombre()).isEqualTo("Marco");
        assertThat(result.apellido()).isEqualTo("Lorenzana");
        assertThat(result.fechaNacimiento()).isEqualTo(originalBirthDate);
        assertThat(result.correoElectronico()).isEqualTo("nuevo@example.com");
        assertThat(result.telefonoMovil()).isEqualTo("+502 5555-1000");
        assertThat(result.idUsuario()).isEqualTo("propio");
        assertThat(usuario.getGenero()).isSameAs(genero);
        assertThat(usuario.getRole()).isSameAs(role);
        assertThat(usuario.getSucursal()).isSameAs(sucursal);
        assertThat(usuario.getStatus()).isSameAs(status);
    }

    @Test
    void permiteConservarElMismoCorreoExcluyendoAlUsuarioAutenticado() {
        service.update(
                "propio", new ProfileUpdateRequest("marco@example.com", "555-1000"));

        verify(usuarios)
                .existsByCorreoElectronicoAndIdUsuarioNot("marco@example.com", "propio");
        verify(usuarios).saveAndFlush(usuario);
    }

    @Test
    void rechazaCorreoDuplicado() {
        when(usuarios.existsByCorreoElectronicoAndIdUsuarioNot("otro@example.com", "propio"))
                .thenReturn(true);

        assertThatThrownBy(
                        () ->
                                service.update(
                                        "propio",
                                        new ProfileUpdateRequest(
                                                "otro@example.com",
                                                "555")))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("correo electrónico");
    }

    @Test
    void aceptaJpegPngYWebpYRecuperaLaFotografia() {
        byte[][] images = {
            {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x01},
            {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x01},
            {0x52, 0x49, 0x46, 0x46, 0, 0, 0, 0, 0x57, 0x45, 0x42, 0x50, 0x01}
        };
        String[] types = {"image/jpeg", "image/png", "image/webp"};

        for (int index = 0; index < images.length; index++) {
            ProfilePhoto saved =
                    service.updatePhoto(
                            "propio",
                            new MockMultipartFile("file", "photo", types[index], images[index]));
            assertThat(saved.contentType()).isEqualTo(types[index]);
            assertThat(service.photo("propio")).get().extracting(ProfilePhoto::contentType)
                    .isEqualTo(types[index]);
        }
    }

    @Test
    void rechazaArchivoVacioGrandeMimeArbitrarioYContenidoFalso() {
        assertThatThrownBy(
                        () ->
                                service.updatePhoto(
                                        "propio",
                                        new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0])))
                .isInstanceOf(InvalidProfilePhotoException.class);
        assertThatThrownBy(
                        () ->
                                service.updatePhoto(
                                        "propio",
                                        new MockMultipartFile(
                                                "file",
                                                "large.jpg",
                                                "image/jpeg",
                                                new byte[(int) ProfileService.MAX_PHOTO_BYTES + 1])))
                .isInstanceOf(InvalidProfilePhotoException.class)
                .hasMessageContaining("2 MB");
        assertThatThrownBy(
                        () ->
                                service.updatePhoto(
                                        "propio",
                                        new MockMultipartFile("file", "x.svg", "image/svg+xml", "<svg>".getBytes())))
                .isInstanceOf(InvalidProfilePhotoException.class);
        assertThatThrownBy(
                        () ->
                                service.updatePhoto(
                                        "propio",
                                        new MockMultipartFile("file", "fake.jpg", "image/jpeg", "html".getBytes())))
                .isInstanceOf(InvalidProfilePhotoException.class);
    }

    @Test
    void eliminaSoloLaFotografiaDelUsuarioAutenticado() {
        service.updatePhoto(
                "propio",
                new MockMultipartFile(
                        "file",
                        "photo.jpg",
                        "image/jpeg",
                        new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff}));
        service.deletePhoto("propio");

        assertThat(service.photo("propio")).isEmpty();
        assertThat(usuario.getPasswordHash()).isEqualTo("hash-secreto");
    }
}
