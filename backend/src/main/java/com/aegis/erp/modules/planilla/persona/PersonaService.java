package com.aegis.erp.modules.planilla.persona;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.planilla.entity.Persona;
import com.aegis.erp.modules.planilla.repository.EstadoCivilRepository;
import com.aegis.erp.modules.planilla.repository.PersonaRepository;
import com.aegis.erp.modules.seguridad.genero.repository.GeneroRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonaService {
    private final PersonaRepository personas;
    private final GeneroRepository generos;
    private final EstadoCivilRepository estados;
    private final DocumentExportService documents;
    private final Clock clock;

    public PersonaService(PersonaRepository personas, GeneroRepository generos,
            EstadoCivilRepository estados, DocumentExportService documents, Clock clock) {
        this.personas = personas; this.generos = generos; this.estados = estados;
        this.documents = documents; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PersonaResponse> listar(String search) {
        String query = normalizeSearch(search);
        return personas.findAll().stream().map(this::response)
                .filter(x -> query.isEmpty() || searchable(x).contains(query)).toList();
    }

    @Transactional(readOnly = true)
    public PersonaResponse obtener(Long id) { return response(find(id)); }

    @Transactional
    public PersonaResponse crear(PersonaRequest request, String user) {
        Persona persona = Persona.crear(clean(request.nombre()), clean(request.apellido()),
                request.fechaNacimiento(), genero(request.generoId()), clean(request.direccion()),
                clean(request.telefono()), optional(request.correoElectronico()),
                estado(request.estadoCivilId()), user, LocalDateTime.now(clock));
        try { return response(personas.saveAndFlush(persona)); }
        catch (DataIntegrityViolationException ex) {
            throw new BusinessConflictException("No fue posible guardar la persona por una restricción de integridad.");
        }
    }

    @Transactional
    public PersonaResponse modificar(Long id, PersonaRequest request, String user) {
        Persona persona = find(id);
        persona.modificar(clean(request.nombre()), clean(request.apellido()), request.fechaNacimiento(),
                genero(request.generoId()), clean(request.direccion()), clean(request.telefono()),
                optional(request.correoElectronico()), estado(request.estadoCivilId()), user,
                LocalDateTime.now(clock));
        try { return response(personas.saveAndFlush(persona)); }
        catch (DataIntegrityViolationException ex) {
            throw new BusinessConflictException("No fue posible actualizar la persona por una restricción de integridad.");
        }
    }

    @Transactional
    public void eliminar(Long id) {
        Persona persona = find(id);
        try { personas.delete(persona); personas.flush(); }
        catch (DataIntegrityViolationException ex) {
            throw new BusinessConflictException("No se puede eliminar la persona porque tiene información asociada.");
        }
    }

    @Transactional(readOnly = true)
    public List<PersonaOptionResponse> generos() {
        return generos.findAll().stream().map(x -> new PersonaOptionResponse(x.getId(), x.getNombre())).toList();
    }
    @Transactional(readOnly = true)
    public List<PersonaOptionResponse> estadosCiviles() {
        return estados.findAll().stream().map(x -> new PersonaOptionResponse(x.getId(), x.getNombre())).toList();
    }
    @Transactional(readOnly = true)
    public List<PersonaOptionResponse> opciones(String search) {
        String query = normalizeSearch(search);
        return personas.findAll().stream()
                .filter(x -> query.isEmpty() || (x.getNombre()+" "+x.getApellido()+" "+x.getId()).toLowerCase(Locale.ROOT).contains(query))
                .limit(50).map(x -> new PersonaOptionResponse(x.getId(), x.getNombre()+" "+x.getApellido())).toList();
    }
    @Transactional(readOnly = true) public byte[] excel(String search) {
        return documents.excel("Personas", headers(), rows(search));
    }
    @Transactional(readOnly = true) public byte[] pdf(String search) {
        return documents.pdf("Listado de personas", headers(), rows(search));
    }

    private List<String> headers() { return List.of("ID","Nombre","Apellido","Nacimiento","Género","Dirección","Teléfono","Correo","Estado civil"); }
    private List<List<?>> rows(String search) { return listar(search).stream().<List<?>>map(x -> List.of(x.id(),x.nombre(),x.apellido(),x.fechaNacimiento(),x.generoNombre(),x.direccion(),x.telefono(),x.correoElectronico()==null?"":x.correoElectronico(),x.estadoCivilNombre())).toList(); }
    private PersonaResponse response(Persona x) { return new PersonaResponse(x.getId(),x.getNombre(),x.getApellido(),x.getFechaNacimiento(),x.getGenero().getId(),x.getGenero().getNombre(),x.getDireccion(),x.getTelefono(),x.getCorreoElectronico(),x.getEstadoCivil().getId(),x.getEstadoCivil().getNombre()); }
    private Persona find(Long id) { return personas.findById(id).orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada.")); }
    private com.aegis.erp.modules.seguridad.genero.entity.Genero genero(Long id) { return generos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Género no encontrado.")); }
    private com.aegis.erp.modules.planilla.entity.EstadoCivil estado(Long id) { return estados.findById(id).orElseThrow(() -> new ResourceNotFoundException("Estado civil no encontrado.")); }
    private String clean(String value) { return value.trim(); }
    private String optional(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String normalizeSearch(String value) { return value == null ? "" : value.trim().toLowerCase(Locale.ROOT); }
    private String searchable(PersonaResponse x) { return (x.id()+" "+x.nombre()+" "+x.apellido()+" "+x.generoNombre()+" "+x.direccion()+" "+x.telefono()+" "+(x.correoElectronico()==null?"":x.correoElectronico())+" "+x.estadoCivilNombre()).toLowerCase(Locale.ROOT); }
}
