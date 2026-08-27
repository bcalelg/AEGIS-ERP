package com.aegis.erp.modules.seguridad.statususuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.seguridad.statususuario.dto.StatusUsuarioCreateRequest;
import com.aegis.erp.modules.seguridad.statususuario.dto.StatusUsuarioUpdateRequest;
import com.aegis.erp.modules.seguridad.statususuario.repository.StatusUsuarioMaintenanceRepository;
import com.aegis.erp.modules.seguridad.statususuario.repository.UsuarioStatusDependencyRepository;
import com.aegis.erp.modules.seguridad.usuario.entity.StatusUsuario;

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
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class StatusUsuarioServiceTest {
    @Mock private StatusUsuarioMaintenanceRepository statuses;
    @Mock private UsuarioStatusDependencyRepository usuarios;
    private StatusUsuarioService service;
    private final Clock clock =
            Clock.fixed(Instant.parse("2026-08-14T18:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setup() {
        service = new StatusUsuarioService(statuses, usuarios, clock, new DocumentExportService());
    }

    @Test
    void listaYObtieneEstatus() {
        StatusUsuario activo = status("Activo");
        when(statuses.findAll()).thenReturn(List.of(activo));
        when(statuses.findById(1L)).thenReturn(Optional.of(activo));

        assertThat(service.listar()).extracting("nombre").containsExactly("Activo");
        assertThat(service.obtener(1L).nombre()).isEqualTo("Activo");
    }

    @Test
    void obtenerInexistenteLanza404() {
        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Estatus de usuario no encontrado.");
    }

    @Test
    void creaYModificaConAuditoria() {
        when(statuses.saveAndFlush(any(StatusUsuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.crear(new StatusUsuarioCreateRequest(" Temporal "), "Administrador");
        ArgumentCaptor<StatusUsuario> captor = ArgumentCaptor.forClass(StatusUsuario.class);
        verify(statuses).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Temporal");
        assertThat(captor.getValue().getUsuarioCreacion()).isEqualTo("Administrador");
        assertThat(captor.getValue().getFechaCreacion())
                .isEqualTo(LocalDateTime.of(2026, 8, 14, 18, 0));

        StatusUsuario status = status("Anterior");
        when(statuses.findById(1L)).thenReturn(Optional.of(status));
        service.modificar(1L, new StatusUsuarioUpdateRequest(" Nuevo "), "Administrador");
        assertThat(status.getNombre()).isEqualTo("Nuevo");
        assertThat(status.getUsuarioModificacion()).isEqualTo("Administrador");
    }

    @Test
    void duplicadosYRestriccionOracleProducen409() {
        when(statuses.existsByNombreIgnoreCase("Activo")).thenReturn(true);
        assertThatThrownBy(
                        () ->
                                service.crear(
                                        new StatusUsuarioCreateRequest("Activo"), "Administrador"))
                .isInstanceOf(BusinessConflictException.class);

        when(statuses.existsByNombreIgnoreCase("Temporal")).thenReturn(false);
        when(statuses.saveAndFlush(any(StatusUsuario.class)))
                .thenThrow(new DataIntegrityViolationException("ORA-00001"));
        assertThatThrownBy(
                        () ->
                                service.crear(
                                        new StatusUsuarioCreateRequest("Temporal"),
                                        "Administrador"))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void eliminaSoloSinUsuariosAsociados() {
        StatusUsuario status = status("Temporal");
        when(statuses.findById(1L)).thenReturn(Optional.of(status));
        service.eliminar(1L);
        verify(statuses).delete(status);

        when(usuarios.countUsuariosByStatusId(1L)).thenReturn(1L);
        assertThatThrownBy(() -> service.eliminar(1L))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("usuarios asociados");
        verify(statuses, never()).deleteAll();
    }

    @Test
    void imprimeYExportaLosTresFormatosRespetandoFiltro() throws Exception {
        when(statuses.findAll()).thenReturn(List.of(status("Activo"), status("Inactivo")));

        assertThat(service.imprimir("inac")).extracting("nombre").containsExactly("Inactivo");
        String csv = new String(service.exportarCsv("act"), StandardCharsets.UTF_8);
        assertThat(csv).startsWith("\uFEFFID,Nombre\r\n").contains("Activo");
        try (XSSFWorkbook workbook =
                new XSSFWorkbook(new ByteArrayInputStream(service.exportarExcel("inac")))) {
            assertThat(workbook.getSheet("Estatus de usuario").getLastRowNum()).isEqualTo(1);
        }
        try (var pdf = Loader.loadPDF(service.exportarPdf("act"))) {
            assertThat(pdf.getNumberOfPages()).isEqualTo(1);
        }
    }

    private StatusUsuario status(String nombre) {
        return StatusUsuario.crear(nombre, "system", LocalDateTime.now(clock));
    }
}
