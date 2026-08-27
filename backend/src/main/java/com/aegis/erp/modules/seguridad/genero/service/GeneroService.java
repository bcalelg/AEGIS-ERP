package com.aegis.erp.modules.seguridad.genero.service;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.seguridad.genero.dto.GeneroCreateRequest;
import com.aegis.erp.modules.seguridad.genero.dto.GeneroResponse;
import com.aegis.erp.modules.seguridad.genero.dto.GeneroUpdateRequest;
import com.aegis.erp.modules.seguridad.genero.entity.Genero;
import com.aegis.erp.modules.seguridad.genero.repository.GeneroRepository;
import com.aegis.erp.modules.seguridad.genero.repository.UsuarioGeneroDependencyRepository;

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
public class GeneroService {
    private final GeneroRepository generos;
    private final UsuarioGeneroDependencyRepository usuarios;
    private final Clock clock;
    private final DocumentExportService documents;

    public GeneroService(
            GeneroRepository generos,
            UsuarioGeneroDependencyRepository usuarios,
            Clock clock,
            DocumentExportService documents) {
        this.generos = generos;
        this.usuarios = usuarios;
        this.clock = clock;
        this.documents = documents;
    }

    @Transactional(readOnly = true)
    public List<GeneroResponse> listarGeneros() {
        return generos.findAll().stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public GeneroResponse obtenerGeneroPorId(Long id) {
        return response(find(id));
    }

    @Transactional
    public GeneroResponse crearGenero(GeneroCreateRequest request, String usuario) {
        String nombre = request.nombre().trim();
        validateDuplicates(nombre, null);
        Genero genero = Genero.crear(nombre, usuario, LocalDateTime.now(clock));
        return response(save(genero));
    }

    @Transactional
    public GeneroResponse modificarGenero(Long id, GeneroUpdateRequest request, String usuario) {
        Genero genero = find(id);
        String nombre = request.nombre().trim();
        validateDuplicates(nombre, id);
        genero.modificar(nombre, usuario, LocalDateTime.now(clock));
        return response(save(genero));
    }

    @Transactional
    public void eliminarGenero(Long id) {
        Genero genero = find(id);
        if (usuarios.countUsuariosByGeneroId(id) > 0) {
            throw new BusinessConflictException(
                    "No es posible eliminar el género porque posee usuarios asociados.");
        }
        generos.delete(genero);
    }

    @Transactional(readOnly = true)
    public List<GeneroResponse> imprimir() {
        return listarGeneros();
    }

    @Transactional(readOnly = true)
    public byte[] exportarCsv(String search) {
        StringBuilder csv = new StringBuilder("\uFEFFID,Nombre\r\n");
        for (Genero genero : exportData(search)) {
            csv.append(genero.getId()).append(',').append(csv(genero.getNombre())).append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportarExcel(String search) {
        List<List<?>> rows =
                exportData(search).stream().map(this::exportRow).toList();
        return documents.excel("Géneros", List.of("ID", "Nombre"), rows);
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdf(String search) {
        List<List<?>> rows =
                exportData(search).stream().map(this::exportRow).toList();
        return documents.pdf("Listado de géneros", List.of("ID", "Nombre"), rows);
    }

    @Transactional(readOnly = true)
    public byte[] exportar() {
        return exportarCsv(null);
    }

    private List<Genero> exportData(String search) {
        String normalized = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return generos.findAll().stream()
                .filter(
                        genero ->
                                normalized.isEmpty()
                                        || genero.getNombre()
                                                .toLowerCase(Locale.ROOT)
                                                .contains(normalized))
                .toList();
    }

    private List<?> exportRow(Genero genero) {
        return Arrays.asList(genero.getId(), genero.getNombre());
    }

    private Genero save(Genero genero) {
        try {
            return generos.saveAndFlush(genero);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessConflictException("Ya existe un género con el nombre indicado.");
        }
    }

    private void validateDuplicates(String nombre, Long id) {
        boolean exists =
                id == null
                        ? generos.existsByNombreIgnoreCase(nombre)
                        : generos.existsByNombreIgnoreCaseAndIdNot(nombre, id);
        if (exists) {
            throw new BusinessConflictException("Ya existe un género con el nombre indicado.");
        }
    }

    private Genero find(Long id) {
        return generos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Género no encontrado."));
    }

    private GeneroResponse response(Genero genero) {
        return new GeneroResponse(genero.getId(), genero.getNombre());
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
