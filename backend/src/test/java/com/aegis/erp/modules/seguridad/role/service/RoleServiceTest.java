package com.aegis.erp.modules.seguridad.role.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.seguridad.role.dto.RoleCreateRequest;
import com.aegis.erp.modules.seguridad.role.dto.RoleUpdateRequest;
import com.aegis.erp.modules.seguridad.role.repository.RoleDependencyRepository;
import com.aegis.erp.modules.seguridad.role.repository.RoleMaintenanceRepository;
import com.aegis.erp.modules.seguridad.usuario.entity.Role;

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
class RoleServiceTest {
    @Mock private RoleMaintenanceRepository roles;
    @Mock private RoleDependencyRepository dependencies;
    private RoleService service;
    private final Clock clock =
            Clock.fixed(Instant.parse("2026-08-14T18:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setup() {
        service = new RoleService(roles, dependencies, clock, new DocumentExportService());
    }

    @Test
    void listaObtieneYReporta404() {
        Role role = role("Administrador");
        when(roles.findAll()).thenReturn(List.of(role));
        when(roles.findById(1L)).thenReturn(Optional.of(role));
        assertThat(service.listar()).extracting("nombre").containsExactly("Administrador");
        assertThat(service.obtener(1L).nombre()).isEqualTo("Administrador");
        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void creaYModificaConAuditoria() {
        when(roles.saveAndFlush(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        service.crear(new RoleCreateRequest(" Temporal "), "Administrador");
        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roles).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Temporal");
        assertThat(captor.getValue().getUsuarioCreacion()).isEqualTo("Administrador");

        Role role = role("Anterior");
        when(roles.findById(1L)).thenReturn(Optional.of(role));
        service.modificar(1L, new RoleUpdateRequest(" Nuevo "), "Administrador");
        assertThat(role.getNombre()).isEqualTo("Nuevo");
        assertThat(role.getUsuarioModificacion()).isEqualTo("Administrador");
        assertThat(role.getFechaModificacion()).isEqualTo(LocalDateTime.of(2026, 8, 14, 18, 0));
    }

    @Test
    void rechazaDuplicadosAlCrearYModificar() {
        when(roles.existsByNombreIgnoreCase("Administrador")).thenReturn(true);
        assertThatThrownBy(
                        () ->
                                service.crear(
                                        new RoleCreateRequest("Administrador"), "Administrador"))
                .isInstanceOf(BusinessConflictException.class);

        Role role = role("Anterior");
        when(roles.findById(1L)).thenReturn(Optional.of(role));
        when(roles.existsByNombreIgnoreCaseAndIdNot("Sin Opciones", 1L)).thenReturn(true);
        assertThatThrownBy(
                        () ->
                                service.modificar(
                                        1L,
                                        new RoleUpdateRequest("Sin Opciones"),
                                        "Administrador"))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void eliminaRolLibre() {
        Role role = role("Temporal");
        when(roles.findById(1L)).thenReturn(Optional.of(role));
        service.eliminar(1L);
        verify(roles).delete(role);
        verify(roles).flush();
    }

    @Test
    void rechazaBajaConUsuarios() {
        when(roles.findById(1L)).thenReturn(Optional.of(role("Administrador")));
        when(dependencies.countUsuariosByRoleId(1L)).thenReturn(1L);
        assertThatThrownBy(() -> service.eliminar(1L))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("usuarios asociados");
        verify(roles, never()).delete(any());
    }

    @Test
    void rechazaBajaConOpciones() {
        when(roles.findById(1L)).thenReturn(Optional.of(role("Administrador")));
        when(dependencies.countOpcionesByRoleId(1L)).thenReturn(1L);
        assertThatThrownBy(() -> service.eliminar(1L))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("opciones/permisos");
        verify(roles, never()).delete(any());
    }

    @Test
    void imprimeYExportaCsvExcelPdfConFiltro() throws Exception {
        when(roles.findAll()).thenReturn(List.of(role("Administrador"), role("Sin Opciones")));
        assertThat(service.imprimir("admin"))
                .extracting("nombre")
                .containsExactly("Administrador");
        assertThat(new String(service.exportarCsv("admin"), StandardCharsets.UTF_8))
                .startsWith("\uFEFFID,Nombre\r\n")
                .contains("Administrador");
        try (XSSFWorkbook workbook =
                new XSSFWorkbook(new ByteArrayInputStream(service.exportarExcel("sin")))) {
            assertThat(workbook.getSheet("Roles").getLastRowNum()).isEqualTo(1);
        }
        try (var pdf = Loader.loadPDF(service.exportarPdf("admin"))) {
            assertThat(pdf.getNumberOfPages()).isEqualTo(1);
        }
    }

    private Role role(String nombre) {
        return Role.crear(nombre, "system", LocalDateTime.now(clock));
    }
}
