package com.aegis.erp.modules.seguridad.empresa.service;

import com.aegis.erp.common.exception.*;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.seguridad.empresa.dto.*;
import com.aegis.erp.modules.seguridad.empresa.entity.Empresa;
import com.aegis.erp.modules.seguridad.empresa.repository.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Arrays;
import java.util.List;

@Service
public class EmpresaService {
    private final EmpresaRepository empresas;
    private final SucursalDependencyRepository sucursales;
    private final Clock clock;
    private final DocumentExportService documents;

    public EmpresaService(
            EmpresaRepository empresas,
            SucursalDependencyRepository sucursales,
            Clock clock,
            DocumentExportService documents) {
        this.empresas = empresas;
        this.sucursales = sucursales;
        this.clock = clock;
        this.documents = documents;
    }

    @Transactional(readOnly = true)
    public PageResponse<EmpresaResponse> listar(String search, Pageable pageable) {
        return PageResponse.from(
                empresas.search(normalizeSearch(search), pageable).map(this::response));
    }

    @Transactional(readOnly = true)
    public EmpresaResponse obtener(Long id) {
        return response(find(id));
    }

    @Transactional
    public EmpresaResponse crear(EmpresaCreateRequest r, String user) {
        validateDuplicates(r.nit().trim(), r.nombre().trim(), null);
        Empresa e =
                Empresa.crear(
                        r.nombre().trim(),
                        r.direccion().trim(),
                        r.nit().trim(),
                        r.passwordCantidadMayusculas(),
                        r.passwordCantidadMinusculas(),
                        r.passwordCantidadCaracteresEspeciales(),
                        r.passwordCantidadCaducidadDias(),
                        r.passwordLargo(),
                        r.passwordIntentosAntesDeBloquear(),
                        r.passwordCantidadNumeros(),
                        r.passwordCantidadPreguntasValidar(),
                        user,
                        LocalDateTime.now(clock));
        return response(save(e));
    }

    @Transactional
    public EmpresaResponse modificar(Long id, EmpresaUpdateRequest r, String user) {
        Empresa e = find(id);
        validateDuplicates(r.nit().trim(), r.nombre().trim(), id);
        e.modificar(
                r.nombre().trim(),
                r.direccion().trim(),
                r.nit().trim(),
                r.passwordCantidadMayusculas(),
                r.passwordCantidadMinusculas(),
                r.passwordCantidadCaracteresEspeciales(),
                r.passwordCantidadCaducidadDias(),
                r.passwordLargo(),
                r.passwordIntentosAntesDeBloquear(),
                r.passwordCantidadNumeros(),
                r.passwordCantidadPreguntasValidar(),
                user,
                LocalDateTime.now(clock));
        return response(save(e));
    }

    @Transactional
    public void eliminar(Long id) {
        Empresa e = find(id);
        if (sucursales.existsByEmpresaId(id))
            throw new BusinessConflictException(
                    "No es posible eliminar la empresa porque posee sucursales asociadas.");
        empresas.delete(e);
    }

    @Transactional(readOnly = true)
    public PageResponse<EmpresaResponse> imprimir(String search, Pageable pageable) {
        return listar(search, pageable);
    }

    @Transactional(readOnly = true)
    public byte[] exportarCsv(String search) {
        StringBuilder csv =
                new StringBuilder(
                        "\uFEFFID,Nombre,NIT,Direccion,Mayusculas,Minusculas,Numeros,Especiales,"
                                + "Longitud,Intentos,Preguntas,CaducidadDias\r\n");
        for (Empresa e : exportData(search))
            csv.append(e.getId())
                    .append(',')
                    .append(csv(e.getNombre()))
                    .append(',')
                    .append(csv(e.getNit()))
                    .append(',')
                    .append(csv(e.getDireccion()))
                    .append(',')
                    .append(e.getPasswordCantidadMayusculas())
                    .append(',')
                    .append(e.getPasswordCantidadMinusculas())
                    .append(',')
                    .append(e.getPasswordCantidadNumeros())
                    .append(',')
                    .append(e.getPasswordCantidadCaracteresEspeciales())
                    .append(',')
                    .append(e.getPasswordLargo())
                    .append(',')
                    .append(e.getPasswordIntentosAntesDeBloquear())
                    .append(',')
                    .append(e.getPasswordCantidadPreguntasValidar())
                    .append(',')
                    .append(e.getPasswordCantidadCaducidadDias())
                    .append("\r\n");
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportarExcel(String search) {
        List<String> headers =
                List.of(
                        "ID",
                        "Nombre",
                        "NIT",
                        "Dirección",
                        "Mayúsculas",
                        "Minúsculas",
                        "Números",
                        "Especiales",
                        "Longitud",
                        "Intentos",
                        "Preguntas",
                        "Caducidad (días)");
        List<List<?>> rows = exportData(search).stream().map(this::excelRow).toList();
        return documents.excel("Empresas", headers, rows);
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdf(String search) {
        List<List<?>> rows =
                exportData(search).stream().map(this::pdfRow).toList();
        return documents.pdf(
                "Listado de empresas", List.of("ID", "Nombre", "NIT", "Dirección"), rows);
    }

    @Transactional(readOnly = true)
    public byte[] exportar(String search) {
        return exportarCsv(search);
    }

    private List<Empresa> exportData(String search) {
        return empresas.export(normalizeSearch(search));
    }

    private List<?> excelRow(Empresa empresa) {
        return Arrays.asList(
                empresa.getId(),
                empresa.getNombre(),
                empresa.getNit(),
                empresa.getDireccion(),
                empresa.getPasswordCantidadMayusculas(),
                empresa.getPasswordCantidadMinusculas(),
                empresa.getPasswordCantidadNumeros(),
                empresa.getPasswordCantidadCaracteresEspeciales(),
                empresa.getPasswordLargo(),
                empresa.getPasswordIntentosAntesDeBloquear(),
                empresa.getPasswordCantidadPreguntasValidar(),
                empresa.getPasswordCantidadCaducidadDias());
    }

    private List<?> pdfRow(Empresa empresa) {
        return Arrays.asList(
                empresa.getId(), empresa.getNombre(), empresa.getNit(), empresa.getDireccion());
    }

    private Empresa save(Empresa e) {
        try {
            return empresas.saveAndFlush(e);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessConflictException("Ya existe una empresa con el mismo NIT o nombre.");
        }
    }

    private void validateDuplicates(String nit, String nombre, Long id) {
        boolean nitExists =
                id == null ? empresas.existsByNit(nit) : empresas.existsByNitAndIdNot(nit, id);
        if (nitExists)
            throw new BusinessConflictException("Ya existe una empresa con el NIT indicado.");
        boolean nameExists =
                id == null
                        ? empresas.existsByNombre(nombre)
                        : empresas.existsByNombreAndIdNot(nombre, id);
        if (nameExists)
            throw new BusinessConflictException("Ya existe una empresa con el nombre indicado.");
    }

    private Empresa find(Long id) {
        return empresas.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada."));
    }

    private String normalizeSearch(String search) {
        return search == null || search.isBlank() ? null : search.trim();
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private EmpresaResponse response(Empresa e) {
        return new EmpresaResponse(
                e.getId(),
                e.getNombre(),
                e.getDireccion(),
                e.getNit(),
                e.getPasswordCantidadMayusculas(),
                e.getPasswordCantidadMinusculas(),
                e.getPasswordCantidadCaracteresEspeciales(),
                e.getPasswordCantidadCaducidadDias(),
                e.getPasswordLargo(),
                e.getPasswordIntentosAntesDeBloquear(),
                e.getPasswordCantidadNumeros(),
                e.getPasswordCantidadPreguntasValidar(),
                e.getFechaCreacion(),
                e.getUsuarioCreacion(),
                e.getFechaModificacion(),
                e.getUsuarioModificacion());
    }
}
