package com.aegis.erp.modules.seguridad.genero.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.seguridad.genero.dto.GeneroCreateRequest;
import com.aegis.erp.modules.seguridad.genero.dto.GeneroUpdateRequest;
import com.aegis.erp.modules.seguridad.genero.entity.Genero;
import com.aegis.erp.modules.seguridad.genero.repository.GeneroRepository;
import com.aegis.erp.modules.seguridad.genero.repository.UsuarioGeneroDependencyRepository;

import org.apache.pdfbox.Loader;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class GeneroServiceTest {
    @Mock private GeneroRepository generos;
    @Mock private UsuarioGeneroDependencyRepository usuarios;

    private GeneroService service;
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-13T18:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setup() {
        service = new GeneroService(generos, usuarios, clock, new DocumentExportService());
    }

    @Test
    void listaGeneros() {
        when(generos.findAll())
                .thenReturn(List.of(Genero.crear("Masculino", "system", LocalDateTime.now(clock))));

        assertThat(service.listarGeneros()).extracting("nombre").containsExactly("Masculino");
    }

    @Test
    void obtieneGeneroExistente() {
        when(generos.findById(1L))
                .thenReturn(
                        Optional.of(Genero.crear("Femenino", "system", LocalDateTime.now(clock))));

        assertThat(service.obtenerGeneroPorId(1L).nombre()).isEqualTo("Femenino");
    }

    @Test
    void obtenerInexistenteLanza404() {
        assertThatThrownBy(() -> service.obtenerGeneroPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Género no encontrado.");
    }

    @Test
    void creaConAuditoria() {
        when(generos.saveAndFlush(any(Genero.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.crearGenero(new GeneroCreateRequest(" Temporal "), "Administrador");

        ArgumentCaptor<Genero> captor = ArgumentCaptor.forClass(Genero.class);
        verify(generos).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Temporal");
        assertThat(captor.getValue().getUsuarioCreacion()).isEqualTo("Administrador");
        assertThat(captor.getValue().getFechaCreacion())
                .isEqualTo(LocalDateTime.of(2026, 8, 13, 18, 0));
    }

    @Test
    void crearDuplicadoLanza409() {
        when(generos.existsByNombreIgnoreCase("Masculino")).thenReturn(true);

        assertThatThrownBy(
                        () ->
                                service.crearGenero(
                                        new GeneroCreateRequest("Masculino"), "Administrador"))
                .isInstanceOf(BusinessConflictException.class);
        verify(generos, never()).saveAndFlush(any());
    }

    @Test
    void integridadEnCreacionLanza409() {
        when(generos.saveAndFlush(any(Genero.class)))
                .thenThrow(new DataIntegrityViolationException("ORA-00001"));

        assertThatThrownBy(
                        () ->
                                service.crearGenero(
                                        new GeneroCreateRequest("Temporal"), "Administrador"))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessage("Ya existe un género con el nombre indicado.");
    }

    @Test
    void modificaConAuditoria() {
        Genero genero = Genero.crear("Anterior", "system", LocalDateTime.now(clock));
        when(generos.findById(1L)).thenReturn(Optional.of(genero));
        when(generos.saveAndFlush(genero)).thenReturn(genero);

        service.modificarGenero(1L, new GeneroUpdateRequest(" Nuevo "), "Administrador");

        assertThat(genero.getNombre()).isEqualTo("Nuevo");
        assertThat(genero.getUsuarioModificacion()).isEqualTo("Administrador");
        assertThat(genero.getFechaModificacion()).isEqualTo(LocalDateTime.of(2026, 8, 13, 18, 0));
    }

    @Test
    void modificarDuplicadoLanza409() {
        Genero genero = Genero.crear("Anterior", "system", LocalDateTime.now(clock));
        when(generos.findById(1L)).thenReturn(Optional.of(genero));
        when(generos.existsByNombreIgnoreCaseAndIdNot("Existente", 1L)).thenReturn(true);

        assertThatThrownBy(
                        () ->
                                service.modificarGenero(
                                        1L, new GeneroUpdateRequest("Existente"), "Administrador"))
                .isInstanceOf(BusinessConflictException.class);
        verify(generos, never()).saveAndFlush(any());
    }

    @Test
    void eliminaSinDependencias() {
        Genero genero = Genero.crear("Temporal", "system", LocalDateTime.now(clock));
        when(generos.findById(1L)).thenReturn(Optional.of(genero));

        service.eliminarGenero(1L);

        verify(generos).delete(genero);
    }

    @Test
    void eliminarConUsuariosLanza409() {
        Genero genero = Genero.crear("Masculino", "system", LocalDateTime.now(clock));
        when(generos.findById(1L)).thenReturn(Optional.of(genero));
        when(usuarios.countUsuariosByGeneroId(1L)).thenReturn(2L);

        assertThatThrownBy(() -> service.eliminarGenero(1L))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("usuarios asociados");
        verify(generos, never()).delete(any());
    }

    @Test
    void imprimirDevuelveListado() {
        when(generos.findAll())
                .thenReturn(List.of(Genero.crear("Masculino", "system", LocalDateTime.now(clock))));

        assertThat(service.imprimir()).extracting("nombre").containsExactly("Masculino");
    }

    @Test
    void exportaCsvUtf8Escapado() {
        when(generos.findAll())
                .thenReturn(
                        List.of(
                                Genero.crear(
                                        "No binario, \"otro\"",
                                        "system",
                                        LocalDateTime.now(clock))));

        String csv = new String(service.exportar(), StandardCharsets.UTF_8);

        assertThat(csv).startsWith("\uFEFFID,Nombre\r\n");
        assertThat(csv).contains("\"No binario, \"\"otro\"\"\"");
    }

    @Test
    void exportaExcelRealRespetandoFiltro() throws IOException {
        when(generos.findAll())
                .thenReturn(
                        List.of(
                                Genero.crear("Masculino", "system", LocalDateTime.now(clock)),
                                Genero.crear("Femenino", "system", LocalDateTime.now(clock))));

        byte[] excel = service.exportarExcel("fem");

        assertThat(excel).startsWith(0x50, 0x4B);
        assertThat(excel).isNotEmpty();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            assertThat(workbook.getSheet("Géneros").getLastRowNum()).isEqualTo(1);
            assertThat(workbook.getSheet("Géneros").getRow(1).getCell(1).getStringCellValue())
                    .isEqualTo("Femenino");
        }
    }

    @Test
    void exportaPdfReal() throws IOException {
        when(generos.findAll())
                .thenReturn(List.of(Genero.crear("Masculino", "system", LocalDateTime.now(clock))));

        byte[] pdf = service.exportarPdf("");

        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        try (var document = Loader.loadPDF(pdf)) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
        }
    }
}
