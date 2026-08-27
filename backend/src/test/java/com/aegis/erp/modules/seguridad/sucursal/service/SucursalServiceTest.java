package com.aegis.erp.modules.seguridad.sucursal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.seguridad.empresa.entity.Empresa;
import com.aegis.erp.modules.seguridad.sucursal.dto.SucursalCreateRequest;
import com.aegis.erp.modules.seguridad.sucursal.dto.SucursalUpdateRequest;
import com.aegis.erp.modules.seguridad.sucursal.repository.SucursalEmpresaOptionRepository;
import com.aegis.erp.modules.seguridad.sucursal.repository.SucursalRepository;
import com.aegis.erp.modules.seguridad.sucursal.repository.UsuarioSucursalDependencyRepository;
import com.aegis.erp.modules.seguridad.usuario.entity.Sucursal;

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
class SucursalServiceTest {
    @Mock private SucursalRepository sucursales;
    @Mock private SucursalEmpresaOptionRepository empresas;
    @Mock private UsuarioSucursalDependencyRepository usuarios;
    private SucursalService service;
    private final Clock clock =
            Clock.fixed(Instant.parse("2026-08-14T18:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setup() {
        service =
                new SucursalService(
                        sucursales,
                        empresas,
                        usuarios,
                        clock,
                        new DocumentExportService());
    }

    @Test
    void listaObtieneYReporta404() {
        Sucursal sucursal = sucursal(empresa("Software Inc."), "Central", "Guatemala");
        when(sucursales.findAllWithEmpresaOrdered()).thenReturn(List.of(sucursal));
        when(sucursales.findById(1L)).thenReturn(Optional.of(sucursal));
        assertThat(service.listar()).extracting("nombreEmpresa").containsExactly("Software Inc.");
        assertThat(service.obtener(1L).nombre()).isEqualTo("Central");
        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void creaConEmpresaValidaYAuditoria() {
        Empresa empresa = empresa("Software Inc.");
        when(empresas.findById(1L)).thenReturn(Optional.of(empresa));
        when(sucursales.saveAndFlush(any(Sucursal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        service.crear(
                new SucursalCreateRequest(1L, " Central ", " Guatemala "),
                "Administrador");
        ArgumentCaptor<Sucursal> captor = ArgumentCaptor.forClass(Sucursal.class);
        verify(sucursales).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getEmpresa()).isSameAs(empresa);
        assertThat(captor.getValue().getNombre()).isEqualTo("Central");
        assertThat(captor.getValue().getUsuarioCreacion()).isEqualTo("Administrador");
    }

    @Test
    void rechazaEmpresaInexistenteYDuplicadoPorEmpresa() {
        assertThatThrownBy(
                        () ->
                                service.crear(
                                        new SucursalCreateRequest(99L, "Central", "Dirección"),
                                        "Administrador"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Empresa no encontrada.");
        Empresa empresa = empresa("Software Inc.");
        when(empresas.findById(1L)).thenReturn(Optional.of(empresa));
        when(sucursales.existsByEmpresaIdAndNombreIgnoreCase(1L, "Central"))
                .thenReturn(true);
        assertThatThrownBy(
                        () ->
                                service.crear(
                                        new SucursalCreateRequest(1L, "Central", "Dirección"),
                                        "Administrador"))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void modificaEmpresaDatosYAuditoria() {
        Empresa anterior = empresa("Anterior");
        Empresa nueva = empresa("Nueva");
        Sucursal sucursal = sucursal(anterior, "Central", "Anterior");
        when(sucursales.findById(1L)).thenReturn(Optional.of(sucursal));
        when(empresas.findById(2L)).thenReturn(Optional.of(nueva));
        when(sucursales.saveAndFlush(sucursal)).thenReturn(sucursal);
        service.modificar(
                1L,
                new SucursalUpdateRequest(2L, "Norte", "Nueva dirección"),
                "Administrador");
        assertThat(sucursal.getEmpresa()).isSameAs(nueva);
        assertThat(sucursal.getNombre()).isEqualTo("Norte");
        assertThat(sucursal.getUsuarioModificacion()).isEqualTo("Administrador");
    }

    @Test
    void eliminaLibreYRechazaUsuariosAsociados() {
        Sucursal sucursal = sucursal(empresa("Empresa"), "Central", "Dirección");
        when(sucursales.findById(1L)).thenReturn(Optional.of(sucursal));
        service.eliminar(1L);
        verify(sucursales).delete(sucursal);
        when(usuarios.countUsuariosBySucursalId(1L)).thenReturn(1L);
        assertThatThrownBy(() -> service.eliminar(1L))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("usuarios asociados");
        verify(sucursales, never()).deleteAll();
    }

    @Test
    void listaOpcionesYExportaFormatosConFiltro() throws Exception {
        Empresa empresa = empresa("Software Inc.");
        when(empresas.findAllByOrderByNombreAscIdAsc()).thenReturn(List.of(empresa));
        assertThat(service.opcionesEmpresa()).extracting("nombre").containsExactly("Software Inc.");
        when(sucursales.findAllWithEmpresaOrdered())
                .thenReturn(List.of(sucursal(empresa, "Central", "Guatemala")));
        assertThat(service.imprimir("software")).hasSize(1);
        assertThat(new String(service.exportarCsv("central"), StandardCharsets.UTF_8))
                .startsWith("\uFEFFID,Empresa,Sucursal,Dirección\r\n");
        try (XSSFWorkbook workbook =
                new XSSFWorkbook(new ByteArrayInputStream(service.exportarExcel("central")))) {
            assertThat(workbook.getSheet("Sucursales").getLastRowNum()).isEqualTo(1);
        }
        try (var pdf = Loader.loadPDF(service.exportarPdf("central"))) {
            assertThat(pdf.getNumberOfPages()).isEqualTo(1);
        }
    }

    private Empresa empresa(String nombre) {
        return Empresa.crear(
                nombre,
                "Dirección",
                "NIT",
                1,
                1,
                1,
                60,
                8,
                5,
                1,
                1,
                "system",
                LocalDateTime.now(clock));
    }

    private Sucursal sucursal(Empresa empresa, String nombre, String direccion) {
        return Sucursal.crear(empresa, nombre, direccion, "system", LocalDateTime.now(clock));
    }
}
