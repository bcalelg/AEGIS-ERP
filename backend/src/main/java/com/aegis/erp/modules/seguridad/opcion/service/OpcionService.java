package com.aegis.erp.modules.seguridad.opcion.service;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.seguridad.menu.entity.Menu;
import com.aegis.erp.modules.seguridad.menu.entity.Opcion;
import com.aegis.erp.modules.seguridad.opcion.dto.MenuOptionResponse;
import com.aegis.erp.modules.seguridad.opcion.dto.OpcionCreateRequest;
import com.aegis.erp.modules.seguridad.opcion.dto.OpcionMaintenanceResponse;
import com.aegis.erp.modules.seguridad.opcion.dto.OpcionUpdateRequest;
import com.aegis.erp.modules.seguridad.opcion.repository.OpcionMaintenanceRepository;
import com.aegis.erp.modules.seguridad.opcion.repository.OpcionMenuOptionRepository;
import com.aegis.erp.modules.seguridad.opcion.repository.RoleOpcionDependencyRepository;

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
public class OpcionService {
    private static final String DUPLICATE_MESSAGE =
            "Ya existe una opción con ese nombre u orden en el menú, o con la página indicada.";
    private final OpcionMaintenanceRepository opciones;
    private final OpcionMenuOptionRepository menus;
    private final RoleOpcionDependencyRepository roleOpciones;
    private final Clock clock;
    private final DocumentExportService documents;

    public OpcionService(
            OpcionMaintenanceRepository opciones,
            OpcionMenuOptionRepository menus,
            RoleOpcionDependencyRepository roleOpciones,
            Clock clock,
            DocumentExportService documents) {
        this.opciones = opciones;
        this.menus = menus;
        this.roleOpciones = roleOpciones;
        this.clock = clock;
        this.documents = documents;
    }

    @Transactional(readOnly = true)
    public List<OpcionMaintenanceResponse> listar() {
        return ordered().stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public OpcionMaintenanceResponse obtener(Long id) {
        return response(find(id));
    }

    @Transactional(readOnly = true)
    public List<MenuOptionResponse> opcionesMenu() {
        return menus.findAllWithModuloOrdered().stream()
                .map(
                        menu ->
                                new MenuOptionResponse(
                                        menu.getId(),
                                        menu.getNombre(),
                                        menu.getModulo().getNombre()))
                .toList();
    }

    @Transactional
    public OpcionMaintenanceResponse crear(OpcionCreateRequest request, String usuario) {
        Menu menu = findMenu(request.idMenu());
        String nombre = request.nombre().trim();
        String pagina = request.pagina().trim();
        validateDuplicate(menu.getId(), nombre, pagina, request.orden(), null);
        return response(
                save(
                        Opcion.crear(
                                menu,
                                nombre,
                                pagina,
                                request.orden(),
                                usuario,
                                LocalDateTime.now(clock))));
    }

    @Transactional
    public OpcionMaintenanceResponse modificar(
            Long id,
            OpcionUpdateRequest request,
            String usuario) {
        Opcion opcion = find(id);
        Menu menu = findMenu(request.idMenu());
        String nombre = request.nombre().trim();
        validateDuplicateForUpdate(menu.getId(), nombre, request.orden(), id);
        opcion.modificar(
                menu,
                nombre,
                request.orden(),
                usuario,
                LocalDateTime.now(clock));
        return response(save(opcion));
    }

    @Transactional
    public void eliminar(Long id) {
        Opcion opcion = find(id);
        if (roleOpciones.existsByOpcionId(id)) {
            throw dependencyConflict();
        }
        try {
            opciones.delete(opcion);
            opciones.flush();
        } catch (DataIntegrityViolationException exception) {
            throw dependencyConflict();
        }
    }

    @Transactional(readOnly = true)
    public List<OpcionMaintenanceResponse> imprimir(String search) {
        return exportData(search).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportarCsv(String search) {
        StringBuilder csv = new StringBuilder("\uFEFFID,Módulo,Menú,Opción,Página,Orden\r\n");
        for (Opcion opcion : exportData(search)) {
            csv.append(opcion.getId())
                    .append(',')
                    .append(csv(opcion.getMenu().getModulo().getNombre()))
                    .append(',')
                    .append(csv(opcion.getMenu().getNombre()))
                    .append(',')
                    .append(csv(opcion.getNombre()))
                    .append(',')
                    .append(csv(opcion.getPagina()))
                    .append(',')
                    .append(opcion.getOrdenMenu())
                    .append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportarExcel(String search) {
        return documents.excel(
                "Opciones",
                headers(),
                exportData(search).stream().map(this::exportRow).toList());
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdf(String search) {
        return documents.pdf(
                "Listado de opciones",
                headers(),
                exportData(search).stream().map(this::exportRow).toList());
    }

    private List<Opcion> ordered() {
        return opciones.findAllWithHierarchyOrdered();
    }

    private List<Opcion> exportData(String search) {
        String normalized = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return ordered().stream()
                .filter(
                        opcion ->
                                normalized.isEmpty()
                                        || contains(opcion.getMenu().getModulo().getNombre(), normalized)
                                        || contains(opcion.getMenu().getNombre(), normalized)
                                        || contains(opcion.getNombre(), normalized)
                                        || contains(opcion.getPagina(), normalized))
                .toList();
    }

    private boolean contains(String value, String search) {
        return value.toLowerCase(Locale.ROOT).contains(search);
    }

    private Opcion save(Opcion opcion) {
        try {
            return opciones.saveAndFlush(opcion);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessConflictException(DUPLICATE_MESSAGE);
        }
    }

    private void validateDuplicate(
            Long idMenu,
            String nombre,
            String pagina,
            Integer orden,
            Long id) {
        boolean duplicateName =
                id == null
                        ? opciones.existsByMenuIdAndNombreIgnoreCase(idMenu, nombre)
                        : opciones.existsByMenuIdAndNombreIgnoreCaseAndIdNot(idMenu, nombre, id);
        boolean duplicateOrder =
                id == null
                        ? opciones.existsByMenuIdAndOrdenMenu(idMenu, orden)
                        : opciones.existsByMenuIdAndOrdenMenuAndIdNot(idMenu, orden, id);
        boolean duplicatePage =
                id == null
                        ? opciones.existsByPaginaIgnoreCase(pagina)
                        : opciones.existsByPaginaIgnoreCaseAndIdNot(pagina, id);
        if (duplicateName || duplicateOrder || duplicatePage) {
            throw new BusinessConflictException(DUPLICATE_MESSAGE);
        }
    }

    private void validateDuplicateForUpdate(
            Long idMenu,
            String nombre,
            Integer orden,
            Long id) {
        boolean duplicateName =
                opciones.existsByMenuIdAndNombreIgnoreCaseAndIdNot(idMenu, nombre, id);
        boolean duplicateOrder =
                opciones.existsByMenuIdAndOrdenMenuAndIdNot(idMenu, orden, id);
        if (duplicateName || duplicateOrder) {
            throw new BusinessConflictException(DUPLICATE_MESSAGE);
        }
    }

    private Menu findMenu(Long id) {
        return menus.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menú no encontrado."));
    }

    private Opcion find(Long id) {
        return opciones.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opción no encontrada."));
    }

    private OpcionMaintenanceResponse response(Opcion opcion) {
        return new OpcionMaintenanceResponse(
                opcion.getId(),
                opcion.getMenu().getId(),
                opcion.getMenu().getNombre(),
                opcion.getMenu().getModulo().getNombre(),
                opcion.getNombre(),
                opcion.getPagina(),
                opcion.getOrdenMenu());
    }

    private List<?> exportRow(Opcion opcion) {
        return Arrays.asList(
                opcion.getId(),
                opcion.getMenu().getModulo().getNombre(),
                opcion.getMenu().getNombre(),
                opcion.getNombre(),
                opcion.getPagina(),
                opcion.getOrdenMenu());
    }

    private List<String> headers() {
        return List.of("ID", "Módulo", "Menú", "Opción", "Página", "Orden");
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private BusinessConflictException dependencyConflict() {
        return new BusinessConflictException(
                "No es posible eliminar la opción porque posee permisos asignados a roles.");
    }
}
