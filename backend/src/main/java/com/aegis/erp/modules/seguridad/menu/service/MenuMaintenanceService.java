package com.aegis.erp.modules.seguridad.menu.service;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.seguridad.menu.dto.MenuCreateRequest;
import com.aegis.erp.modules.seguridad.menu.dto.MenuMaintenanceResponse;
import com.aegis.erp.modules.seguridad.menu.dto.MenuUpdateRequest;
import com.aegis.erp.modules.seguridad.menu.dto.ModuloOptionResponse;
import com.aegis.erp.modules.seguridad.menu.entity.Menu;
import com.aegis.erp.modules.seguridad.menu.entity.Modulo;
import com.aegis.erp.modules.seguridad.menu.repository.MenuMaintenanceRepository;
import com.aegis.erp.modules.seguridad.menu.repository.MenuModuloOptionRepository;
import com.aegis.erp.modules.seguridad.menu.repository.OpcionMenuDependencyRepository;

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
public class MenuMaintenanceService {
    private final MenuMaintenanceRepository menus;
    private final MenuModuloOptionRepository modulos;
    private final OpcionMenuDependencyRepository opciones;
    private final Clock clock;
    private final DocumentExportService documents;

    public MenuMaintenanceService(
            MenuMaintenanceRepository menus,
            MenuModuloOptionRepository modulos,
            OpcionMenuDependencyRepository opciones,
            Clock clock,
            DocumentExportService documents) {
        this.menus = menus;
        this.modulos = modulos;
        this.opciones = opciones;
        this.clock = clock;
        this.documents = documents;
    }

    @Transactional(readOnly = true)
    public List<MenuMaintenanceResponse> listar() {
        return ordered().stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public MenuMaintenanceResponse obtener(Long id) {
        return response(find(id));
    }

    @Transactional(readOnly = true)
    public List<ModuloOptionResponse> opcionesModulo() {
        return modulos.findAllByOrderByOrdenMenuAscIdAsc().stream()
                .map(modulo -> new ModuloOptionResponse(modulo.getId(), modulo.getNombre()))
                .toList();
    }

    @Transactional
    public MenuMaintenanceResponse crear(MenuCreateRequest request, String usuario) {
        Modulo modulo = findModulo(request.idModulo());
        String nombre = request.nombre().trim();
        validateDuplicate(modulo.getId(), nombre, request.orden(), null);
        return response(
                save(
                        Menu.crear(
                                modulo,
                                nombre,
                                request.orden(),
                                usuario,
                                LocalDateTime.now(clock))));
    }

    @Transactional
    public MenuMaintenanceResponse modificar(
            Long id,
            MenuUpdateRequest request,
            String usuario) {
        Menu menu = find(id);
        Modulo modulo = findModulo(request.idModulo());
        String nombre = request.nombre().trim();
        validateDuplicate(modulo.getId(), nombre, request.orden(), id);
        menu.modificar(modulo, nombre, request.orden(), usuario, LocalDateTime.now(clock));
        return response(save(menu));
    }

    @Transactional
    public void eliminar(Long id) {
        Menu menu = find(id);
        if (opciones.countOpcionesByMenuId(id) > 0) {
            throw dependencyConflict();
        }
        try {
            menus.delete(menu);
            menus.flush();
        } catch (DataIntegrityViolationException exception) {
            throw dependencyConflict();
        }
    }

    @Transactional(readOnly = true)
    public List<MenuMaintenanceResponse> imprimir(String search) {
        return exportData(search).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportarCsv(String search) {
        StringBuilder csv = new StringBuilder("\uFEFFID,Módulo,Menú,Orden\r\n");
        for (Menu menu : exportData(search)) {
            csv.append(menu.getId())
                    .append(',')
                    .append(csv(menu.getModulo().getNombre()))
                    .append(',')
                    .append(csv(menu.getNombre()))
                    .append(',')
                    .append(menu.getOrdenMenu())
                    .append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportarExcel(String search) {
        return documents.excel(
                "Menús",
                List.of("ID", "Módulo", "Menú", "Orden"),
                exportData(search).stream().map(this::exportRow).toList());
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdf(String search) {
        return documents.pdf(
                "Listado de menús",
                List.of("ID", "Módulo", "Menú", "Orden"),
                exportData(search).stream().map(this::exportRow).toList());
    }

    private List<Menu> ordered() {
        return menus.findAllWithModuloOrdered();
    }

    private List<Menu> exportData(String search) {
        String normalized = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return ordered().stream()
                .filter(
                        menu ->
                                normalized.isEmpty()
                                        || menu.getNombre()
                                                .toLowerCase(Locale.ROOT)
                                                .contains(normalized)
                                        || menu.getModulo()
                                                .getNombre()
                                                .toLowerCase(Locale.ROOT)
                                                .contains(normalized))
                .toList();
    }

    private Menu save(Menu menu) {
        try {
            return menus.saveAndFlush(menu);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessConflictException(
                    "Ya existe un menú con ese nombre u orden para el módulo seleccionado.");
        }
    }

    private void validateDuplicate(Long idModulo, String nombre, Integer orden, Long id) {
        boolean duplicateName =
                id == null
                        ? menus.existsByModuloIdAndNombreIgnoreCase(idModulo, nombre)
                        : menus.existsByModuloIdAndNombreIgnoreCaseAndIdNot(idModulo, nombre, id);
        boolean duplicateOrder =
                id == null
                        ? menus.existsByModuloIdAndOrdenMenu(idModulo, orden)
                        : menus.existsByModuloIdAndOrdenMenuAndIdNot(idModulo, orden, id);
        if (duplicateName || duplicateOrder) {
            throw new BusinessConflictException(
                    "Ya existe un menú con ese nombre u orden para el módulo seleccionado.");
        }
    }

    private Modulo findModulo(Long id) {
        return modulos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado."));
    }

    private Menu find(Long id) {
        return menus.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menú no encontrado."));
    }

    private MenuMaintenanceResponse response(Menu menu) {
        return new MenuMaintenanceResponse(
                menu.getId(),
                menu.getModulo().getId(),
                menu.getModulo().getNombre(),
                menu.getNombre(),
                menu.getOrdenMenu());
    }

    private List<?> exportRow(Menu menu) {
        return Arrays.asList(
                menu.getId(),
                menu.getModulo().getNombre(),
                menu.getNombre(),
                menu.getOrdenMenu());
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private BusinessConflictException dependencyConflict() {
        return new BusinessConflictException(
                "No es posible eliminar el menú porque posee opciones asociadas.");
    }
}
