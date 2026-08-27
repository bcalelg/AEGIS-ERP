package com.aegis.erp.modules.seguridad.roleopcion.service;

import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.modules.seguridad.menu.entity.Opcion;
import com.aegis.erp.modules.seguridad.menu.entity.RoleOpcion;
import com.aegis.erp.modules.seguridad.menu.entity.RoleOpcionId;
import com.aegis.erp.modules.seguridad.roleopcion.dto.CatalogOptionResponse;
import com.aegis.erp.modules.seguridad.roleopcion.dto.RoleOpcionItemRequest;
import com.aegis.erp.modules.seguridad.roleopcion.dto.RoleOpcionMatrixResponse;
import com.aegis.erp.modules.seguridad.roleopcion.dto.RoleOpcionSaveRequest;
import com.aegis.erp.modules.seguridad.roleopcion.repository.RoleOpcionAssignmentRepository;
import com.aegis.erp.modules.seguridad.roleopcion.repository.RoleOpcionMatrixRepository;
import com.aegis.erp.modules.seguridad.roleopcion.repository.RoleOpcionModuloRepository;
import com.aegis.erp.modules.seguridad.roleopcion.repository.RoleOpcionRoleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RoleOpcionService {
    private final RoleOpcionRoleRepository roles;
    private final RoleOpcionModuloRepository modulos;
    private final RoleOpcionMatrixRepository opciones;
    private final RoleOpcionAssignmentRepository asignaciones;
    private final Clock clock;

    public RoleOpcionService(
            RoleOpcionRoleRepository roles,
            RoleOpcionModuloRepository modulos,
            RoleOpcionMatrixRepository opciones,
            RoleOpcionAssignmentRepository asignaciones,
            Clock clock) {
        this.roles = roles;
        this.modulos = modulos;
        this.opciones = opciones;
        this.asignaciones = asignaciones;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<CatalogOptionResponse> roles() {
        return roles.findAllByOrderByNombreAscIdAsc().stream()
                .map(role -> new CatalogOptionResponse(role.getId(), role.getNombre()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogOptionResponse> modulos() {
        return modulos.findAllByOrderByOrdenMenuAscIdAsc().stream()
                .map(modulo -> new CatalogOptionResponse(modulo.getId(), modulo.getNombre()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleOpcionMatrixResponse> matriz(Long idRole, Long idModulo) {
        findRole(idRole);
        findModulo(idModulo);
        Map<Long, RoleOpcion> current = current(idRole, idModulo);
        return opciones.findByModuloOrdered(idModulo).stream()
                .map(opcion -> response(opcion, current.get(opcion.getId())))
                .toList();
    }

    @Transactional
    public List<RoleOpcionMatrixResponse> guardar(
            RoleOpcionSaveRequest request,
            String usuario) {
        var role = findRole(request.idRole());
        findModulo(request.idModulo());
        List<Opcion> moduleOptions = opciones.findByModuloOrdered(request.idModulo());
        Map<Long, Opcion> allowed = new HashMap<>();
        moduleOptions.forEach(opcion -> allowed.put(opcion.getId(), opcion));
        validateRequestOptions(request.opciones(), allowed.keySet());
        Map<Long, RoleOpcion> current = current(request.idRole(), request.idModulo());
        LocalDateTime now = LocalDateTime.now(clock);

        for (RoleOpcionItemRequest item : request.opciones()) {
            RoleOpcion existing = current.get(item.idOpcion());
            if (!item.anyEnabled()) {
                if (existing != null) asignaciones.delete(existing);
            } else if (existing == null) {
                asignaciones.save(
                        RoleOpcion.crear(
                                role,
                                allowed.get(item.idOpcion()),
                                item.consultar(),
                                item.alta(),
                                item.baja(),
                                item.cambio(),
                                item.imprimir(),
                                item.exportar(),
                                usuario,
                                now));
            } else {
                existing.modificar(
                        item.consultar(),
                        item.alta(),
                        item.baja(),
                        item.cambio(),
                        item.imprimir(),
                        item.exportar(),
                        usuario,
                        now);
            }
        }
        asignaciones.flush();
        return matriz(request.idRole(), request.idModulo());
    }

    private void validateRequestOptions(List<RoleOpcionItemRequest> items, Set<Long> allowed) {
        Set<Long> received = new HashSet<>();
        for (RoleOpcionItemRequest item : items) {
            if (!received.add(item.idOpcion())) {
                throw new BusinessConflictException("La opción está repetida en la matriz.");
            }
            if (!opciones.existsById(item.idOpcion())) {
                throw new ResourceNotFoundException("Opción no encontrada.");
            }
            if (!allowed.contains(item.idOpcion())) {
                throw new BusinessConflictException(
                        "La opción no pertenece al módulo seleccionado.");
            }
        }
        if (!received.equals(allowed)) {
            throw new BusinessConflictException(
                    "La matriz debe contener todas las opciones del módulo seleccionado.");
        }
    }

    private Map<Long, RoleOpcion> current(Long idRole, Long idModulo) {
        Map<Long, RoleOpcion> result = new HashMap<>();
        asignaciones.findByRoleAndModulo(idRole, idModulo)
                .forEach(value -> result.put(value.getOpcion().getId(), value));
        return result;
    }

    private com.aegis.erp.modules.seguridad.usuario.entity.Role findRole(Long id) {
        return roles.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado."));
    }

    private com.aegis.erp.modules.seguridad.menu.entity.Modulo findModulo(Long id) {
        return modulos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado."));
    }

    private RoleOpcionMatrixResponse response(Opcion opcion, RoleOpcion value) {
        return new RoleOpcionMatrixResponse(
                opcion.getId(),
                opcion.getNombre(),
                opcion.getMenu().getNombre(),
                opcion.getMenu().getOrdenMenu(),
                opcion.getOrdenMenu(),
                enabled(value, RoleOpcion::getConsultar),
                enabled(value, RoleOpcion::getAlta),
                enabled(value, RoleOpcion::getBaja),
                enabled(value, RoleOpcion::getCambio),
                enabled(value, RoleOpcion::getImprimir),
                enabled(value, RoleOpcion::getExportar));
    }

    private boolean enabled(
            RoleOpcion value,
            java.util.function.Function<RoleOpcion, Integer> getter) {
        return value != null && Integer.valueOf(1).equals(getter.apply(value));
    }
}
