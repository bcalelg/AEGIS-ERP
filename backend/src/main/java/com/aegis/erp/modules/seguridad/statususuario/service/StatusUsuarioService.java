package com.aegis.erp.modules.seguridad.statususuario.service;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.seguridad.statususuario.dto.StatusUsuarioCreateRequest;
import com.aegis.erp.modules.seguridad.statususuario.dto.StatusUsuarioResponse;
import com.aegis.erp.modules.seguridad.statususuario.dto.StatusUsuarioUpdateRequest;
import com.aegis.erp.modules.seguridad.statususuario.repository.StatusUsuarioMaintenanceRepository;
import com.aegis.erp.modules.seguridad.statususuario.repository.UsuarioStatusDependencyRepository;
import com.aegis.erp.modules.seguridad.usuario.entity.StatusUsuario;

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
public class StatusUsuarioService {
    private final StatusUsuarioMaintenanceRepository statuses;
    private final UsuarioStatusDependencyRepository usuarios;
    private final Clock clock;
    private final DocumentExportService documents;

    public StatusUsuarioService(
            StatusUsuarioMaintenanceRepository statuses,
            UsuarioStatusDependencyRepository usuarios,
            Clock clock,
            DocumentExportService documents) {
        this.statuses = statuses;
        this.usuarios = usuarios;
        this.clock = clock;
        this.documents = documents;
    }

    @Transactional(readOnly = true)
    public List<StatusUsuarioResponse> listar() {
        return statuses.findAll().stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public StatusUsuarioResponse obtener(Long id) {
        return response(find(id));
    }

    @Transactional
    public StatusUsuarioResponse crear(StatusUsuarioCreateRequest request, String usuario) {
        String nombre = request.nombre().trim();
        validateDuplicates(nombre, null);
        StatusUsuario status = StatusUsuario.crear(nombre, usuario, LocalDateTime.now(clock));
        return response(save(status));
    }

    @Transactional
    public StatusUsuarioResponse modificar(
            Long id,
            StatusUsuarioUpdateRequest request,
            String usuario) {
        StatusUsuario status = find(id);
        String nombre = request.nombre().trim();
        validateDuplicates(nombre, id);
        status.modificar(nombre, usuario, LocalDateTime.now(clock));
        return response(save(status));
    }

    @Transactional
    public void eliminar(Long id) {
        StatusUsuario status = find(id);
        if (usuarios.countUsuariosByStatusId(id) > 0) {
            throw new BusinessConflictException(
                    "No es posible eliminar el estatus porque posee usuarios asociados.");
        }
        try {
            statuses.delete(status);
            statuses.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessConflictException(
                    "No es posible eliminar el estatus porque posee usuarios asociados.");
        }
    }

    @Transactional(readOnly = true)
    public List<StatusUsuarioResponse> imprimir(String search) {
        return exportData(search).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportarCsv(String search) {
        StringBuilder csv = new StringBuilder("\uFEFFID,Nombre\r\n");
        for (StatusUsuario status : exportData(search)) {
            csv.append(status.getId())
                    .append(',')
                    .append(csv(status.getNombre()))
                    .append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportarExcel(String search) {
        List<List<?>> rows = exportData(search).stream().map(this::exportRow).toList();
        return documents.excel("Estatus de usuario", List.of("ID", "Nombre"), rows);
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdf(String search) {
        List<List<?>> rows = exportData(search).stream().map(this::exportRow).toList();
        return documents.pdf("Listado de estatus de usuario", List.of("ID", "Nombre"), rows);
    }

    private List<StatusUsuario> exportData(String search) {
        String normalized = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return statuses.findAll().stream()
                .filter(
                        status ->
                                normalized.isEmpty()
                                        || status.getNombre()
                                                .toLowerCase(Locale.ROOT)
                                                .contains(normalized))
                .toList();
    }

    private List<?> exportRow(StatusUsuario status) {
        return Arrays.asList(status.getId(), status.getNombre());
    }

    private StatusUsuario save(StatusUsuario status) {
        try {
            return statuses.saveAndFlush(status);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessConflictException(
                    "Ya existe un estatus de usuario con el nombre indicado.");
        }
    }

    private void validateDuplicates(String nombre, Long id) {
        boolean exists =
                id == null
                        ? statuses.existsByNombreIgnoreCase(nombre)
                        : statuses.existsByNombreIgnoreCaseAndIdNot(nombre, id);
        if (exists) {
            throw new BusinessConflictException(
                    "Ya existe un estatus de usuario con el nombre indicado.");
        }
    }

    private StatusUsuario find(Long id) {
        return statuses.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Estatus de usuario no encontrado."));
    }

    private StatusUsuarioResponse response(StatusUsuario status) {
        return new StatusUsuarioResponse(status.getId(), status.getNombre());
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
