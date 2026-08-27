package com.aegis.erp.modules.seguridad.sucursal.service;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.seguridad.empresa.entity.Empresa;
import com.aegis.erp.modules.seguridad.sucursal.dto.EmpresaOptionResponse;
import com.aegis.erp.modules.seguridad.sucursal.dto.SucursalCreateRequest;
import com.aegis.erp.modules.seguridad.sucursal.dto.SucursalResponse;
import com.aegis.erp.modules.seguridad.sucursal.dto.SucursalUpdateRequest;
import com.aegis.erp.modules.seguridad.sucursal.repository.SucursalEmpresaOptionRepository;
import com.aegis.erp.modules.seguridad.sucursal.repository.SucursalRepository;
import com.aegis.erp.modules.seguridad.sucursal.repository.UsuarioSucursalDependencyRepository;
import com.aegis.erp.modules.seguridad.usuario.entity.Sucursal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class SucursalService {
    private final SucursalRepository sucursales;
    private final SucursalEmpresaOptionRepository empresas;
    private final UsuarioSucursalDependencyRepository usuarios;
    private final Clock clock;
    private final DocumentExportService documents;

    public SucursalService(
            SucursalRepository sucursales,
            SucursalEmpresaOptionRepository empresas,
            UsuarioSucursalDependencyRepository usuarios,
            Clock clock,
            DocumentExportService documents) {
        this.sucursales = sucursales;
        this.empresas = empresas;
        this.usuarios = usuarios;
        this.clock = clock;
        this.documents = documents;
    }

    @Transactional(readOnly = true)
    public List<SucursalResponse> listar() {
        return ordered().stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public SucursalResponse obtener(Long id) {
        return response(find(id));
    }

    @Transactional(readOnly = true)
    public List<EmpresaOptionResponse> opcionesEmpresa() {
        return empresas.findAllByOrderByNombreAscIdAsc().stream()
                .map(empresa -> new EmpresaOptionResponse(empresa.getId(), empresa.getNombre()))
                .toList();
    }

    @Transactional
    public SucursalResponse crear(SucursalCreateRequest request, String usuario) {
        Empresa empresa = findEmpresa(request.idEmpresa());
        String nombre = request.nombre().trim();
        validateDuplicate(request.idEmpresa(), nombre, null);
        return response(
                save(
                        Sucursal.crear(
                                empresa,
                                nombre,
                                request.direccion().trim(),
                                usuario,
                                LocalDateTime.now(clock))));
    }

    @Transactional
    public SucursalResponse modificar(Long id, SucursalUpdateRequest request, String usuario) {
        Sucursal sucursal = find(id);
        Empresa empresa = findEmpresa(request.idEmpresa());
        String nombre = request.nombre().trim();
        validateDuplicate(request.idEmpresa(), nombre, id);
        sucursal.modificar(
                empresa,
                nombre,
                request.direccion().trim(),
                usuario,
                LocalDateTime.now(clock));
        return response(save(sucursal));
    }

    @Transactional
    public void eliminar(Long id) {
        Sucursal sucursal = find(id);
        if (usuarios.countUsuariosBySucursalId(id) > 0) {
            throw new BusinessConflictException(
                    "No es posible eliminar la sucursal porque posee usuarios asociados.");
        }
        try {
            sucursales.delete(sucursal);
            sucursales.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessConflictException(
                    "No es posible eliminar la sucursal porque posee usuarios asociados.");
        }
    }

    @Transactional(readOnly = true)
    public List<SucursalResponse> imprimir(String search) {
        return exportData(search).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportarCsv(String search) {
        StringBuilder csv = new StringBuilder("\uFEFFID,Empresa,Sucursal,Dirección\r\n");
        for (Sucursal sucursal : exportData(search)) {
            csv.append(sucursal.getId())
                    .append(',')
                    .append(csv(sucursal.getEmpresa().getNombre()))
                    .append(',')
                    .append(csv(sucursal.getNombre()))
                    .append(',')
                    .append(csv(sucursal.getDireccion()))
                    .append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportarExcel(String search) {
        return documents.excel(
                "Sucursales",
                List.of("ID", "Empresa", "Sucursal", "Dirección"),
                exportData(search).stream().map(this::exportRow).toList());
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdf(String search) {
        return documents.pdf(
                "Listado de sucursales",
                List.of("ID", "Empresa", "Sucursal", "Dirección"),
                exportData(search).stream().map(this::exportRow).toList());
    }

    private List<Sucursal> ordered() {
        return sucursales.findAllWithEmpresaOrdered();
    }

    private List<Sucursal> exportData(String search) {
        String normalized = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return ordered().stream()
                .filter(
                        sucursal ->
                                normalized.isEmpty()
                                        || sucursal.getNombre()
                                                .toLowerCase(Locale.ROOT)
                                                .contains(normalized)
                                        || sucursal.getDireccion()
                                                .toLowerCase(Locale.ROOT)
                                                .contains(normalized)
                                        || sucursal.getEmpresa()
                                                .getNombre()
                                                .toLowerCase(Locale.ROOT)
                                                .contains(normalized))
                .toList();
    }

    private Sucursal save(Sucursal sucursal) {
        try {
            return sucursales.saveAndFlush(sucursal);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessConflictException(
                    "Ya existe una sucursal con ese nombre para la empresa seleccionada.");
        }
    }

    private void validateDuplicate(Long idEmpresa, String nombre, Long id) {
        boolean exists =
                id == null
                        ? sucursales.existsByEmpresaIdAndNombreIgnoreCase(idEmpresa, nombre)
                        : sucursales.existsByEmpresaIdAndNombreIgnoreCaseAndIdNot(
                                idEmpresa,
                                nombre,
                                id);
        if (exists) {
            throw new BusinessConflictException(
                    "Ya existe una sucursal con ese nombre para la empresa seleccionada.");
        }
    }

    private Empresa findEmpresa(Long id) {
        return empresas.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada."));
    }

    private Sucursal find(Long id) {
        return sucursales.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada."));
    }

    private SucursalResponse response(Sucursal sucursal) {
        return new SucursalResponse(
                sucursal.getId(),
                sucursal.getEmpresa().getId(),
                sucursal.getEmpresa().getNombre(),
                sucursal.getNombre(),
                sucursal.getDireccion());
    }

    private List<?> exportRow(Sucursal sucursal) {
        return Arrays.asList(
                sucursal.getId(),
                sucursal.getEmpresa().getNombre(),
                sucursal.getNombre(),
                sucursal.getDireccion());
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
