package com.aegis.erp.modules.seguridad.empresa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.seguridad.empresa.dto.EmpresaCreateRequest;
import com.aegis.erp.modules.seguridad.empresa.dto.EmpresaResponse;
import com.aegis.erp.modules.seguridad.empresa.entity.Empresa;
import com.aegis.erp.modules.seguridad.empresa.repository.EmpresaRepository;
import com.aegis.erp.modules.seguridad.empresa.repository.SucursalDependencyRepository;

import org.apache.pdfbox.Loader;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class EmpresaServiceTest {
    @Mock private EmpresaRepository empresas;
    @Mock private SucursalDependencyRepository sucursales;

    private EmpresaService service;
    private final EmpresaCreateRequest request =
            new EmpresaCreateRequest("Prueba", "Direccion", "NIT-1", 1, 1, 1, 60, 8, 5, 2, 1);

    @BeforeEach
    void setup() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-12T00:00:00Z"), ZoneOffset.UTC);
        service =
                new EmpresaService(
                        empresas, sucursales, clock, new DocumentExportService());
    }

    @Test
    void creaConAuditoria() {
        when(empresas.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EmpresaResponse response = service.crear(request, "Administrador");

        assertThat(response.usuarioCreacion()).isEqualTo("Administrador");
        assertThat(response.fechaCreacion()).isEqualTo(LocalDateTime.of(2026, 8, 12, 0, 0));
    }

    @Test
    void nitDuplicado() {
        when(empresas.existsByNit("NIT-1")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(request, "Administrador"))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("NIT");
    }

    @Test
    void noEliminaConSucursal() {
        Empresa empresa = empresa("A", "N");
        when(empresas.findById(1L)).thenReturn(Optional.of(empresa));
        when(sucursales.existsByEmpresaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.eliminar(1L))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("sucursales");
        verify(empresas, never()).delete(any());
    }

    @Test
    void eliminaSinDependencias() {
        Empresa empresa = empresa("A", "N");
        when(empresas.findById(1L)).thenReturn(Optional.of(empresa));

        service.eliminar(1L);

        verify(empresas).delete(empresa);
    }

    @Test
    void obtieneNoEncontrada() {
        assertThatThrownBy(() -> service.obtener(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void validaSumaPassword() {
        EmpresaCreateRequest invalid =
                new EmpresaCreateRequest("A", "D", "N", 2, 2, 2, 60, 5, 5, 2, 1);

        assertThat(invalid.isPasswordPolicyValid()).isFalse();
    }

    @Test
    void exportaCsvUtf8ConBomYFiltro() {
        when(empresas.export("área")).thenReturn(List.of(empresa("Área, Norte", "NIT-1")));

        String csv = new String(service.exportarCsv(" área "), StandardCharsets.UTF_8);

        assertThat(csv).startsWith("\uFEFFID,Nombre,NIT,Direccion");
        assertThat(csv).contains("\"Área, Norte\"");
        verify(empresas).export("área");
    }

    @Test
    void exportaExcelReal() throws IOException {
        when(empresas.export(null)).thenReturn(List.of(empresa("Empresa", "NIT-1")));

        byte[] excel = service.exportarExcel("");

        assertThat(excel).startsWith(0x50, 0x4B);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            assertThat(workbook.getSheet("Empresas").getRow(0).getCell(0).getStringCellValue())
                    .isEqualTo("ID");
            assertThat(workbook.getSheet("Empresas").getRow(1).getCell(1).getStringCellValue())
                    .isEqualTo("Empresa");
        }
    }

    @Test
    void exportaPdfReal() throws IOException {
        when(empresas.export(null)).thenReturn(List.of(empresa("Empresa", "NIT-1")));

        byte[] pdf = service.exportarPdf(null);

        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        try (var document = Loader.loadPDF(pdf)) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
        }
    }

    private Empresa empresa(String nombre, String nit) {
        return Empresa.crear(
                nombre,
                "Guatemala",
                nit,
                1,
                1,
                1,
                60,
                8,
                5,
                2,
                1,
                "system",
                LocalDateTime.of(2026, 8, 12, 0, 0));
    }
}
