package com.aegis.erp.modules.seguridad.modulo.service;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.seguridad.menu.entity.Modulo;
import com.aegis.erp.modules.seguridad.modulo.dto.ModuloCreateRequest;
import com.aegis.erp.modules.seguridad.modulo.dto.ModuloResponse;
import com.aegis.erp.modules.seguridad.modulo.dto.ModuloUpdateRequest;
import com.aegis.erp.modules.seguridad.modulo.repository.MenuModuloDependencyRepository;
import com.aegis.erp.modules.seguridad.modulo.repository.ModuloMaintenanceRepository;

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
public class ModuloService {
    private final ModuloMaintenanceRepository modulos;
    private final MenuModuloDependencyRepository menus;
    private final Clock clock;
    private final DocumentExportService documents;

    public ModuloService(
            ModuloMaintenanceRepository modulos,
            MenuModuloDependencyRepository menus,
            Clock clock,
            DocumentExportService documents) {
        this.modulos = modulos;
        this.menus = menus;
        this.clock = clock;
        this.documents = documents;
    }

    @Transactional(readOnly = true)
    public List<ModuloResponse> listar() {
        return ordered().stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public ModuloResponse obtener(Long id) {
        return response(find(id));
    }

    @Transactional
    public ModuloResponse crear(ModuloCreateRequest request, String usuario) {
        String nombre = request.nombre().trim();
        validateDuplicate(nombre, null);
        return response(
                save(
                        Modulo.crear(
                                nombre,
                                request.orden(),
                                usuario,
                                LocalDateTime.now(clock))));
    }

    @Transactional
    public ModuloResponse modificar(Long id, ModuloUpdateRequest request, String usuario) {
        Modulo modulo = find(id);
        String nombre = request.nombre().trim();
        validateDuplicate(nombre, id);
        modulo.modificar(nombre, request.orden(), usuario, LocalDateTime.now(clock));
        return response(save(modulo));
    }

    @Transactional
    public void eliminar(Long id) {
        Modulo modulo = find(id);
        if (menus.countMenusByModuloId(id) > 0) {
            throw new BusinessConflictException(
                    "No es posible eliminar el módulo porque posee menús asociados.");
        }
        try {
            modulos.delete(modulo);
            modulos.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessConflictException(
                    "No es posible eliminar el módulo porque posee menús asociados.");
        }
    }

    @Transactional(readOnly = true)
    public List<ModuloResponse> imprimir(String search) {
        return exportData(search).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportarCsv(String search) {
        StringBuilder csv = new StringBuilder("\uFEFFID,Nombre,Orden\r\n");
        for (Modulo modulo : exportData(search)) {
            csv.append(modulo.getId())
                    .append(',')
                    .append(csv(modulo.getNombre()))
                    .append(',')
                    .append(modulo.getOrdenMenu())
                    .append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportarExcel(String search) {
        return documents.excel(
                "Módulos",
                List.of("ID", "Nombre", "Orden"),
                exportData(search).stream().map(this::exportRow).toList());
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdf(String search) {
        return documents.pdf(
                "Listado de módulos",
                List.of("ID", "Nombre", "Orden"),
                exportData(search).stream().map(this::exportRow).toList());
    }

    private List<Modulo> ordered() {
        return modulos.findAllByOrderByOrdenMenuAscIdAsc();
    }

    private List<Modulo> exportData(String search) {
        String normalized = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return ordered().stream()
                .filter(
                        modulo ->
                                normalized.isEmpty()
                                        || modulo.getNombre()
                                                .toLowerCase(Locale.ROOT)
                                                .contains(normalized))
                .toList();
    }

    private Modulo save(Modulo modulo) {
        try {
            return modulos.saveAndFlush(modulo);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessConflictException("Ya existe un módulo con el nombre indicado.");
        }
    }

    private void validateDuplicate(String nombre, Long id) {
        boolean exists =
                id == null
                        ? modulos.existsByNombreIgnoreCase(nombre)
                        : modulos.existsByNombreIgnoreCaseAndIdNot(nombre, id);
        if (exists) {
            throw new BusinessConflictException("Ya existe un módulo con el nombre indicado.");
        }
    }

    private Modulo find(Long id) {
        return modulos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado."));
    }

    private ModuloResponse response(Modulo modulo) {
        return new ModuloResponse(modulo.getId(), modulo.getNombre(), modulo.getOrdenMenu());
    }

    private List<?> exportRow(Modulo modulo) {
        return Arrays.asList(modulo.getId(), modulo.getNombre(), modulo.getOrdenMenu());
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
