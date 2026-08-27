package com.aegis.erp.modules.seguridad.opcion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.seguridad.menu.entity.Menu;
import com.aegis.erp.modules.seguridad.menu.entity.Modulo;
import com.aegis.erp.modules.seguridad.menu.entity.Opcion;
import com.aegis.erp.modules.seguridad.opcion.dto.OpcionCreateRequest;
import com.aegis.erp.modules.seguridad.opcion.dto.OpcionUpdateRequest;
import com.aegis.erp.modules.seguridad.opcion.repository.OpcionMaintenanceRepository;
import com.aegis.erp.modules.seguridad.opcion.repository.OpcionMenuOptionRepository;
import com.aegis.erp.modules.seguridad.opcion.repository.RoleOpcionDependencyRepository;

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
class OpcionServiceTest {
    @Mock private OpcionMaintenanceRepository opciones;
    @Mock private OpcionMenuOptionRepository menus;
    @Mock private RoleOpcionDependencyRepository roleOpciones;
    private OpcionService service;
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-17T18:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setup() {
        service = new OpcionService(opciones, menus, roleOpciones, clock, new DocumentExportService());
    }

    @Test
    void listaObtieneYOpcionesMenuEnOrden() {
        Menu menu = menu(1L, "Parámetros", "Seguridad", 1);
        Opcion opcion = opcion(1L, menu, "Empresas", "empresa.php", 1);
        when(opciones.findAllWithHierarchyOrdered()).thenReturn(List.of(opcion));
        when(opciones.findById(1L)).thenReturn(Optional.of(opcion));
        when(menus.findAllWithModuloOrdered()).thenReturn(List.of(menu));
        assertThat(service.listar()).extracting("nombre").containsExactly("Empresas");
        assertThat(service.obtener(1L).pagina()).isEqualTo("empresa.php");
        assertThat(service.opcionesMenu()).extracting("modulo").containsExactly("Seguridad");
    }

    @Test
    void devuelve404ParaOpcionYMenuInexistentes() {
        assertThatThrownBy(() -> service.obtener(99L)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.crear(new OpcionCreateRequest(99L, "Nueva", "nueva.php", 1), "admin"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Menú");
    }

    @Test
    void creaConPaginaYModificaNombreMenuYOrdenSinAlterarPaginaIdORoles() {
        Menu menu = menu(1L, "Parámetros", "Seguridad", 1);
        Menu acciones = menu(2L, "Acciones", "Seguridad", 2);
        when(menus.findById(1L)).thenReturn(Optional.of(menu));
        when(menus.findById(2L)).thenReturn(Optional.of(acciones));
        when(opciones.saveAndFlush(any(Opcion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.crear(new OpcionCreateRequest(1L, " Empresas ", " empresa.php ", 1), "admin");
        ArgumentCaptor<Opcion> captor = ArgumentCaptor.forClass(Opcion.class);
        verify(opciones).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getPagina()).isEqualTo("empresa.php");
        assertThat(captor.getValue().getUsuarioCreacion()).isEqualTo("admin");

        Opcion opcion = opcion(7L, menu, "Anterior", "anterior.php", 2);
        when(opciones.findById(7L)).thenReturn(Optional.of(opcion));
        service.modificar(7L, new OpcionUpdateRequest(2L, "Administración de Empresas", 3), "admin");
        assertThat(opcion.getId()).isEqualTo(7L);
        assertThat(opcion.getMenu()).isSameAs(acciones);
        assertThat(opcion.getNombre()).isEqualTo("Administración de Empresas");
        assertThat(opcion.getPagina()).isEqualTo("anterior.php");
        assertThat(opcion.getOrdenMenu()).isEqualTo(3);
        assertThat(opcion.getUsuarioModificacion()).isEqualTo("admin");
        verifyNoInteractions(roleOpciones);
        assertThat(service.obtener(7L).pagina()).isEqualTo("anterior.php");
    }

    @Test
    void updateRequestNoExponePagina() {
        assertThat(OpcionUpdateRequest.class.getRecordComponents())
                .extracting(component -> component.getName())
                .containsExactly("idMenu", "nombre", "orden");
    }

    @Test
    void rechazaNombreOrdenOPaginaDuplicadosReales() {
        Menu menu = menu(1L, "Parámetros", "Seguridad", 1);
        when(menus.findById(1L)).thenReturn(Optional.of(menu));
        when(opciones.existsByMenuIdAndNombreIgnoreCase(1L, "Empresas")).thenReturn(true);
        assertThatThrownBy(() -> service.crear(new OpcionCreateRequest(1L, "Empresas", "otra.php", 1), "admin"))
                .isInstanceOf(BusinessConflictException.class);
        when(opciones.existsByMenuIdAndNombreIgnoreCase(1L, "Otra")).thenReturn(false);
        when(opciones.existsByMenuIdAndOrdenMenu(1L, 2)).thenReturn(false);
        when(opciones.existsByPaginaIgnoreCase("empresa.php")).thenReturn(true);
        assertThatThrownBy(() -> service.crear(new OpcionCreateRequest(1L, "Otra", "empresa.php", 2), "admin"))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void rechazaNombreYOrdenDuplicadosAlEditar() {
        Menu menu = menu(1L, "Parámetros", "Seguridad", 1);
        Opcion opcion = opcion(7L, menu, "Empresas", "empresa.php", 1);
        when(opciones.findById(7L)).thenReturn(Optional.of(opcion));
        when(menus.findById(1L)).thenReturn(Optional.of(menu));
        when(opciones.existsByMenuIdAndNombreIgnoreCaseAndIdNot(1L, "Duplicada", 7L))
                .thenReturn(true);
        assertThatThrownBy(() -> service.modificar(7L, new OpcionUpdateRequest(1L, "Duplicada", 2), "admin"))
                .isInstanceOf(BusinessConflictException.class);

        when(opciones.existsByMenuIdAndNombreIgnoreCaseAndIdNot(1L, "Disponible", 7L))
                .thenReturn(false);
        when(opciones.existsByMenuIdAndOrdenMenuAndIdNot(1L, 2, 7L)).thenReturn(true);
        assertThatThrownBy(() -> service.modificar(7L, new OpcionUpdateRequest(1L, "Disponible", 2), "admin"))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void eliminaLibreYProtegeAsignacionesRoleOpcion() {
        Opcion opcion = opcion(1L, menu(1L, "Parámetros", "Seguridad", 1), "Temporal", "tmp.php", 9);
        when(opciones.findById(1L)).thenReturn(Optional.of(opcion));
        service.eliminar(1L);
        verify(opciones).delete(opcion);
        when(roleOpciones.existsByOpcionId(1L)).thenReturn(true);
        assertThatThrownBy(() -> service.eliminar(1L))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("permisos asignados a roles");
    }

    @Test
    void imprimeYExportaCsvExcelPdfConFiltro() throws Exception {
        when(opciones.findAllWithHierarchyOrdered()).thenReturn(List.of(
                opcion(1L, menu(1L, "Parámetros", "Seguridad", 1), "Empresas", "empresa.php", 1),
                opcion(2L, menu(2L, "Acciones", "Seguridad", 2), "Usuarios", "usuario.php", 1)));
        assertThat(service.imprimir("usuario.php")).extracting("nombre").containsExactly("Usuarios");
        assertThat(new String(service.exportarCsv("empresas"), StandardCharsets.UTF_8))
                .contains("Módulo,Menú,Opción,Página,Orden").contains("empresa.php");
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(service.exportarExcel("usuarios")))) {
            assertThat(workbook.getSheet("Opciones").getLastRowNum()).isEqualTo(1);
        }
        try (var pdf = Loader.loadPDF(service.exportarPdf("seguridad"))) {
            assertThat(pdf.getNumberOfPages()).isEqualTo(1);
        }
    }

    private Menu menu(long id, String nombre, String moduloNombre, int orden) {
        Modulo modulo = Modulo.crear(moduloNombre, 1, "system", LocalDateTime.now(clock));
        ReflectionTestUtils.setField(modulo, "id", 1L);
        Menu menu = Menu.crear(modulo, nombre, orden, "system", LocalDateTime.now(clock));
        ReflectionTestUtils.setField(menu, "id", id);
        return menu;
    }

    private Opcion opcion(long id, Menu menu, String nombre, String pagina, int orden) {
        Opcion opcion = Opcion.crear(menu, nombre, pagina, orden, "system", LocalDateTime.now(clock));
        ReflectionTestUtils.setField(opcion, "id", id);
        return opcion;
    }
}
