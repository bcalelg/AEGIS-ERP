package com.aegis.erp.modules.seguridad.modulo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.seguridad.menu.entity.Modulo;
import com.aegis.erp.modules.seguridad.modulo.dto.ModuloCreateRequest;
import com.aegis.erp.modules.seguridad.modulo.dto.ModuloUpdateRequest;
import com.aegis.erp.modules.seguridad.modulo.repository.MenuModuloDependencyRepository;
import com.aegis.erp.modules.seguridad.modulo.repository.ModuloMaintenanceRepository;

import org.apache.pdfbox.Loader;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ModuloServiceTest {
    @Mock private ModuloMaintenanceRepository modulos;
    @Mock private MenuModuloDependencyRepository menus;
    private ModuloService service;
    private final Clock clock =
            Clock.fixed(Instant.parse("2026-08-14T18:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setup() {
        service = new ModuloService(modulos, menus, clock, new DocumentExportService());
    }

    @Test
    void listaEnOrdenYObtiene() {
        when(modulos.findAllByOrderByOrdenMenuAscIdAsc())
                .thenReturn(List.of(modulo("Primero", 1), modulo("Segundo", 2)));
        when(modulos.findById(1L)).thenReturn(Optional.of(modulo("Seguridad", 1)));
        assertThat(service.listar()).extracting("orden").containsExactly(1, 2);
        assertThat(service.obtener(1L).nombre()).isEqualTo("Seguridad");
    }

    @Test
    void obtenerInexistenteLanza404() {
        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void creaYModificaConAuditoria() {
        when(modulos.saveAndFlush(any(Modulo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        service.crear(new ModuloCreateRequest(" Inventario ", 2), "Administrador");
        ArgumentCaptor<Modulo> captor = ArgumentCaptor.forClass(Modulo.class);
        verify(modulos).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Inventario");
        assertThat(captor.getValue().getOrdenMenu()).isEqualTo(2);
        assertThat(captor.getValue().getUsuarioCreacion()).isEqualTo("Administrador");

        Modulo modulo = modulo("Anterior", 3);
        when(modulos.findById(1L)).thenReturn(Optional.of(modulo));
        service.modificar(1L, new ModuloUpdateRequest(" Nuevo ", 4), "Administrador");
        assertThat(modulo.getNombre()).isEqualTo("Nuevo");
        assertThat(modulo.getOrdenMenu()).isEqualTo(4);
        assertThat(modulo.getUsuarioModificacion()).isEqualTo("Administrador");
    }

    @Test
    void rechazaDuplicadosAlCrearYModificar() {
        when(modulos.existsByNombreIgnoreCase("Seguridad")).thenReturn(true);
        assertThatThrownBy(
                        () ->
                                service.crear(
                                        new ModuloCreateRequest("Seguridad", 1), "Administrador"))
                .isInstanceOf(BusinessConflictException.class);
        Modulo modulo = modulo("Anterior", 2);
        when(modulos.findById(1L)).thenReturn(Optional.of(modulo));
        when(modulos.existsByNombreIgnoreCaseAndIdNot("Seguridad", 1L)).thenReturn(true);
        assertThatThrownBy(
                        () ->
                                service.modificar(
                                        1L,
                                        new ModuloUpdateRequest("Seguridad", 3),
                                        "Administrador"))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void eliminaLibreYRechazaModuloConMenus() {
        Modulo modulo = modulo("Temporal", 2);
        when(modulos.findById(1L)).thenReturn(Optional.of(modulo));
        service.eliminar(1L);
        verify(modulos).delete(modulo);
        when(menus.countMenusByModuloId(1L)).thenReturn(1L);
        assertThatThrownBy(() -> service.eliminar(1L))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("menús asociados");
        verify(modulos, never()).deleteAll();
    }

    @Test
    void imprimeYExportaCsvExcelPdfConFiltro() throws Exception {
        when(modulos.findAllByOrderByOrdenMenuAscIdAsc())
                .thenReturn(List.of(modulo("Seguridad", 1), modulo("Inventario", 2)));
        assertThat(service.imprimir("inven"))
                .extracting("nombre")
                .containsExactly("Inventario");
        assertThat(new String(service.exportarCsv("seg"), StandardCharsets.UTF_8))
                .startsWith("\uFEFFID,Nombre,Orden\r\n")
                .contains("Seguridad");
        try (XSSFWorkbook workbook =
                new XSSFWorkbook(new ByteArrayInputStream(service.exportarExcel("inven")))) {
            assertThat(workbook.getSheet("Módulos").getLastRowNum()).isEqualTo(1);
        }
        try (var pdf = Loader.loadPDF(service.exportarPdf("seg"))) {
            assertThat(pdf.getNumberOfPages()).isEqualTo(1);
        }
    }

    private Modulo modulo(String nombre, int orden) {
        return Modulo.crear(nombre, orden, "system", LocalDateTime.now(clock));
    }
}
