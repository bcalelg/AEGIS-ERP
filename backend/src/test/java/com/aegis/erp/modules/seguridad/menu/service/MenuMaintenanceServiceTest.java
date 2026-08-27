package com.aegis.erp.modules.seguridad.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.seguridad.menu.dto.MenuCreateRequest;
import com.aegis.erp.modules.seguridad.menu.dto.MenuUpdateRequest;
import com.aegis.erp.modules.seguridad.menu.entity.Menu;
import com.aegis.erp.modules.seguridad.menu.entity.Modulo;
import com.aegis.erp.modules.seguridad.menu.repository.MenuMaintenanceRepository;
import com.aegis.erp.modules.seguridad.menu.repository.MenuModuloOptionRepository;
import com.aegis.erp.modules.seguridad.menu.repository.OpcionMenuDependencyRepository;

import org.apache.pdfbox.Loader;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class MenuMaintenanceServiceTest {
    @Mock private MenuMaintenanceRepository menus;
    @Mock private MenuModuloOptionRepository modulos;
    @Mock private OpcionMenuDependencyRepository opciones;
    private MenuMaintenanceService service;
    private final Clock clock =
            Clock.fixed(Instant.parse("2026-08-14T18:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setup() {
        service =
                new MenuMaintenanceService(
                        menus, modulos, opciones, clock, new DocumentExportService());
    }

    @Test
    void listaObtieneYOpcionesConNombreDeModulo() {
        Modulo seguridad = modulo("Seguridad", 1);
        Menu parametros = menu(seguridad, "Parámetros", 1);
        when(menus.findAllWithModuloOrdered()).thenReturn(List.of(parametros));
        when(menus.findById(1L)).thenReturn(Optional.of(parametros));
        when(modulos.findAllByOrderByOrdenMenuAscIdAsc()).thenReturn(List.of(seguridad));

        assertThat(service.listar()).extracting("nombreModulo").containsExactly("Seguridad");
        assertThat(service.obtener(1L).nombre()).isEqualTo("Parámetros");
        assertThat(service.opcionesModulo()).extracting("nombre").containsExactly("Seguridad");
    }

    @Test
    void rechazaMenuYModuloInexistentes() {
        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(
                        () ->
                                service.crear(
                                        new MenuCreateRequest(99L, "Parámetros", 1),
                                        "Administrador"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Módulo");
    }

    @Test
    void creaYModificaModuloNombreOrdenConAuditoria() {
        Modulo seguridad = modulo("Seguridad", 1);
        Modulo inventario = modulo("Inventario", 2);
        when(modulos.findById(1L)).thenReturn(Optional.of(seguridad));
        when(modulos.findById(2L)).thenReturn(Optional.of(inventario));
        when(menus.saveAndFlush(any(Menu.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.crear(new MenuCreateRequest(1L, " Parámetros ", 1), "Administrador");
        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(menus).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Parámetros");
        assertThat(captor.getValue().getUsuarioCreacion()).isEqualTo("Administrador");

        Menu menu = menu(seguridad, "Anterior", 2);
        when(menus.findById(7L)).thenReturn(Optional.of(menu));
        service.modificar(
                7L,
                new MenuUpdateRequest(2L, " Existencias ", 3),
                "Administrador");
        assertThat(menu.getModulo()).isSameAs(inventario);
        assertThat(menu.getNombre()).isEqualTo("Existencias");
        assertThat(menu.getOrdenMenu()).isEqualTo(3);
        assertThat(menu.getUsuarioModificacion()).isEqualTo("Administrador");
    }

    @Test
    void respetaUnicosPorModuloParaNombreYOrden() {
        Modulo seguridad = modulo("Seguridad", 1);
        when(modulos.findById(1L)).thenReturn(Optional.of(seguridad));
        when(menus.existsByModuloIdAndNombreIgnoreCase(1L, "Parámetros"))
                .thenReturn(true);
        assertThatThrownBy(
                        () ->
                                service.crear(
                                        new MenuCreateRequest(1L, "Parámetros", 1),
                                        "Administrador"))
                .isInstanceOf(BusinessConflictException.class);

        when(menus.existsByModuloIdAndNombreIgnoreCase(1L, "Acciones"))
                .thenReturn(false);
        when(menus.existsByModuloIdAndOrdenMenu(1L, 1)).thenReturn(true);
        assertThatThrownBy(
                        () ->
                                service.crear(
                                        new MenuCreateRequest(1L, "Acciones", 1),
                                        "Administrador"))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void eliminaLibreYProtegeOpcionesAsociadas() {
        Menu menu = menu(modulo("Seguridad", 1), "Temporal", 5);
        when(menus.findById(1L)).thenReturn(Optional.of(menu));
        service.eliminar(1L);
        verify(menus).delete(menu);

        when(opciones.countOpcionesByMenuId(1L)).thenReturn(1L);
        assertThatThrownBy(() -> service.eliminar(1L))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessage("No es posible eliminar el menú porque posee opciones asociadas.");
    }

    @Test
    void imprimeYExportaCsvExcelPdfConFiltroDescriptivo() throws Exception {
        when(menus.findAllWithModuloOrdered())
                .thenReturn(
                        List.of(
                                menu(modulo("Seguridad", 1), "Parámetros", 1),
                                menu(modulo("Inventario", 2), "Catálogos", 1)));
        assertThat(service.imprimir("inventario"))
                .extracting("nombre")
                .containsExactly("Catálogos");
        assertThat(new String(service.exportarCsv("pará"), StandardCharsets.UTF_8))
                .contains("Módulo,Menú,Orden")
                .contains("Seguridad")
                .contains("Parámetros");
        try (XSSFWorkbook workbook =
                new XSSFWorkbook(new ByteArrayInputStream(service.exportarExcel("catá")))) {
            assertThat(workbook.getSheet("Menús").getLastRowNum()).isEqualTo(1);
        }
        try (var pdf = Loader.loadPDF(service.exportarPdf("seguridad"))) {
            assertThat(pdf.getNumberOfPages()).isEqualTo(1);
        }
    }

    private Modulo modulo(String nombre, int orden) {
        Modulo modulo = Modulo.crear(nombre, orden, "system", LocalDateTime.now(clock));
        ReflectionTestUtils.setField(modulo, "id", (long) orden);
        return modulo;
    }

    private Menu menu(Modulo modulo, String nombre, int orden) {
        return Menu.crear(modulo, nombre, orden, "system", LocalDateTime.now(clock));
    }
}
