package com.aegis.erp.modules.seguridad.role.service;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.seguridad.role.dto.RoleCreateRequest;
import com.aegis.erp.modules.seguridad.role.dto.RoleResponse;
import com.aegis.erp.modules.seguridad.role.dto.RoleUpdateRequest;
import com.aegis.erp.modules.seguridad.role.repository.RoleDependencyRepository;
import com.aegis.erp.modules.seguridad.role.repository.RoleMaintenanceRepository;
import com.aegis.erp.modules.seguridad.usuario.entity.Role;

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
public class RoleService {
    private final RoleMaintenanceRepository roles;
    private final RoleDependencyRepository dependencies;
    private final Clock clock;
    private final DocumentExportService documents;

    public RoleService(
            RoleMaintenanceRepository roles,
            RoleDependencyRepository dependencies,
            Clock clock,
            DocumentExportService documents) {
        this.roles = roles;
        this.dependencies = dependencies;
        this.clock = clock;
        this.documents = documents;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> listar() {
        return roles.findAll().stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse obtener(Long id) {
        return response(find(id));
    }

    @Transactional
    public RoleResponse crear(RoleCreateRequest request, String usuario) {
        String nombre = request.nombre().trim();
        validateDuplicate(nombre, null);
        return response(save(Role.crear(nombre, usuario, LocalDateTime.now(clock))));
    }

    @Transactional
    public RoleResponse modificar(Long id, RoleUpdateRequest request, String usuario) {
        Role role = find(id);
        String nombre = request.nombre().trim();
        validateDuplicate(nombre, id);
        role.modificar(nombre, usuario, LocalDateTime.now(clock));
        return response(save(role));
    }

    @Transactional
    public void eliminar(Long id) {
        Role role = find(id);
        if (dependencies.countUsuariosByRoleId(id) > 0) {
            throw new BusinessConflictException(
                    "No es posible eliminar el rol porque posee usuarios asociados.");
        }
        if (dependencies.countOpcionesByRoleId(id) > 0) {
            throw new BusinessConflictException(
                    "No es posible eliminar el rol porque posee opciones/permisos asignados.");
        }
        try {
            roles.delete(role);
            roles.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessConflictException(
                    "No es posible eliminar el rol porque posee registros asociados.");
        }
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> imprimir(String search) {
        return exportData(search).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportarCsv(String search) {
        StringBuilder csv = new StringBuilder("\uFEFFID,Nombre\r\n");
        for (Role role : exportData(search)) {
            csv.append(role.getId())
                    .append(',')
                    .append(csv(role.getNombre()))
                    .append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportarExcel(String search) {
        return documents.excel(
                "Roles",
                List.of("ID", "Nombre"),
                exportData(search).stream().map(this::exportRow).toList());
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdf(String search) {
        return documents.pdf(
                "Listado de roles",
                List.of("ID", "Nombre"),
                exportData(search).stream().map(this::exportRow).toList());
    }

    private List<Role> exportData(String search) {
        String normalized = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return roles.findAll().stream()
                .filter(
                        role ->
                                normalized.isEmpty()
                                        || role.getNombre()
                                                .toLowerCase(Locale.ROOT)
                                                .contains(normalized))
                .toList();
    }

    private Role save(Role role) {
        try {
            return roles.saveAndFlush(role);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessConflictException("Ya existe un rol con el nombre indicado.");
        }
    }

    private void validateDuplicate(String nombre, Long id) {
        boolean exists =
                id == null
                        ? roles.existsByNombreIgnoreCase(nombre)
                        : roles.existsByNombreIgnoreCaseAndIdNot(nombre, id);
        if (exists) {
            throw new BusinessConflictException("Ya existe un rol con el nombre indicado.");
        }
    }

    private Role find(Long id) {
        return roles.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado."));
    }

    private RoleResponse response(Role role) {
        return new RoleResponse(role.getId(), role.getNombre());
    }

    private List<?> exportRow(Role role) {
        return Arrays.asList(role.getId(), role.getNombre());
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
