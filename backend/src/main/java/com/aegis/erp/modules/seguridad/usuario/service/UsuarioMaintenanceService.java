package com.aegis.erp.modules.seguridad.usuario.service;

import com.aegis.erp.common.exception.*;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.seguridad.auth.service.PasswordPolicyValidator;
import com.aegis.erp.modules.seguridad.empresa.entity.Empresa;
import com.aegis.erp.modules.seguridad.empresa.repository.EmpresaRepository;
import com.aegis.erp.modules.seguridad.genero.entity.Genero;
import com.aegis.erp.modules.seguridad.genero.repository.GeneroRepository;
import com.aegis.erp.modules.seguridad.role.repository.RoleMaintenanceRepository;
import com.aegis.erp.modules.seguridad.statususuario.repository.StatusUsuarioMaintenanceRepository;
import com.aegis.erp.modules.seguridad.sucursal.repository.SucursalRepository;
import com.aegis.erp.modules.seguridad.usuario.dto.*;
import com.aegis.erp.modules.seguridad.usuario.entity.*;
import com.aegis.erp.modules.seguridad.usuario.repository.UsuarioRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

@Service
public class UsuarioMaintenanceService {
    private final UsuarioRepository usuarios;
    private final EmpresaRepository empresas;
    private final SucursalRepository sucursales;
    private final GeneroRepository generos;
    private final StatusUsuarioMaintenanceRepository statuses;
    private final RoleMaintenanceRepository roles;
    private final PasswordEncoder encoder;
    private final DocumentExportService documents;
    private final Clock clock;

    public UsuarioMaintenanceService(
            UsuarioRepository usuarios,
            EmpresaRepository empresas,
            SucursalRepository sucursales,
            GeneroRepository generos,
            StatusUsuarioMaintenanceRepository statuses,
            RoleMaintenanceRepository roles,
            PasswordEncoder encoder,
            DocumentExportService documents,
            Clock clock) {
        this.usuarios = usuarios;
        this.empresas = empresas;
        this.sucursales = sucursales;
        this.generos = generos;
        this.statuses = statuses;
        this.roles = roles;
        this.encoder = encoder;
        this.documents = documents;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<UsuarioListResponse> listar() {
        return usuarios.findAllForMaintenance().stream().map(this::listResponse).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(String id) {
        return response(find(id));
    }

    @Transactional(readOnly = true)
    public List<UsuarioOptionResponse> opcionesEmpresa() {
        return empresas.findAll().stream()
                .sorted(Comparator.comparing(Empresa::getNombre).thenComparing(Empresa::getId))
                .map(value -> new UsuarioOptionResponse(value.getId(), value.getNombre()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioOptionResponse> opcionesSucursal(Long idEmpresa) {
        findEmpresa(idEmpresa);
        return sucursales.findAllByEmpresaIdOrderByNombreAscIdAsc(idEmpresa).stream()
                .map(value -> new UsuarioOptionResponse(value.getId(), value.getNombre()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioOptionResponse> opcionesGenero() {
        return generos.findAll().stream()
                .sorted(Comparator.comparing(Genero::getNombre).thenComparing(Genero::getId))
                .map(value -> new UsuarioOptionResponse(value.getId(), value.getNombre()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioOptionResponse> opcionesStatus() {
        return statuses.findAll().stream()
                .sorted(Comparator.comparing(StatusUsuario::getNombre).thenComparing(StatusUsuario::getId))
                .map(value -> new UsuarioOptionResponse(value.getId(), value.getNombre()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioOptionResponse> opcionesRole() {
        return roles.findAll().stream()
                .sorted(Comparator.comparing(Role::getNombre).thenComparing(Role::getId))
                .map(value -> new UsuarioOptionResponse(value.getId(), value.getNombre()))
                .toList();
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest request, String actor) {
        String id = request.idUsuario().trim();
        if (usuarios.existsById(id)) {
            throw new BusinessConflictException("Ya existe un usuario con el identificador indicado.");
        }
        if (!request.password().equals(request.passwordConfirmacion())) {
            throw new InvalidPasswordChangeException("La contraseña temporal y su confirmación no coinciden.");
        }
        Relations relations = relations(
                request.idEmpresa(), request.idSucursal(), request.idGenero(),
                request.idStatusUsuario(), request.idRole());
        PasswordPolicyValidator.validate(relations.sucursal().getEmpresa(), request.password());
        String email = nullable(request.correoElectronico());
        String phone = nullable(request.telefonoMovil());
        validateUnique(email, phone, null);
        Usuario usuario = Usuario.crear(
                id,
                request.nombre().trim(),
                request.apellido().trim(),
                request.fechaNacimiento(),
                encoder.encode(request.password()),
                email,
                phone,
                request.pregunta().trim(),
                request.respuesta().trim(),
                relations.genero(),
                relations.status(),
                relations.role(),
                relations.sucursal(),
                actor,
                LocalDateTime.now(clock));
        return response(save(usuario));
    }

    @Transactional
    public UsuarioResponse modificar(String id, UsuarioUpdateRequest request, String actor) {
        Usuario usuario = find(id);
        Relations relations = relations(
                request.idEmpresa(), request.idSucursal(), request.idGenero(),
                request.idStatusUsuario(), request.idRole());
        String email = nullable(request.correoElectronico());
        String phone = nullable(request.telefonoMovil());
        validateUnique(email, phone, id);
        usuario.modificarDatosAdministrativos(
                request.nombre().trim(),
                request.apellido().trim(),
                request.fechaNacimiento(),
                email,
                phone,
                request.pregunta().trim(),
                nullable(request.respuesta()),
                relations.genero(),
                relations.status(),
                relations.role(),
                relations.sucursal(),
                actor,
                LocalDateTime.now(clock));
        return response(save(usuario));
    }

    @Transactional
    public void eliminar(String id) {
        Usuario usuario = find(id);
        try {
            usuarios.delete(usuario);
            usuarios.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessConflictException(
                    "No es posible eliminar el usuario porque posee registros asociados.");
        }
    }

    @Transactional(readOnly = true)
    public List<UsuarioListResponse> imprimir(String search) {
        return exportData(search).stream().map(this::listResponse).toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportarCsv(String search) {
        StringBuilder csv = new StringBuilder(
                "\uFEFFUsuario,Nombre,Apellidos,Género,Empresa,Sucursal,Rol,Estatus,Último ingreso\r\n");
        for (Usuario usuario : exportData(search)) {
            csv.append(csv(usuario.getIdUsuario())).append(',')
                    .append(csv(usuario.getNombre())).append(',')
                    .append(csv(usuario.getApellido())).append(',')
                    .append(csv(usuario.getGenero().getNombre())).append(',')
                    .append(csv(usuario.getSucursal().getEmpresa().getNombre())).append(',')
                    .append(csv(usuario.getSucursal().getNombre())).append(',')
                    .append(csv(usuario.getRole().getNombre())).append(',')
                    .append(csv(usuario.getStatus().getNombre())).append(',')
                    .append(csv(format(usuario.getUltimaFechaIngreso()))).append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportarExcel(String search) {
        return documents.excel("Usuarios", exportHeaders(), exportData(search).stream().map(this::exportRow).toList());
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdf(String search) {
        return documents.pdf("Listado de usuarios", exportHeaders(), exportData(search).stream().map(this::exportRow).toList());
    }

    private Relations relations(Long empresaId, Long sucursalId, Long generoId, Long statusId, Long roleId) {
        Empresa empresa = findEmpresa(empresaId);
        Sucursal sucursal = sucursales.findById(sucursalId)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada."));
        if (!empresa.getId().equals(sucursal.getEmpresa().getId())) {
            throw new BusinessConflictException("La sucursal no pertenece a la empresa seleccionada.");
        }
        Genero genero = generos.findById(generoId)
                .orElseThrow(() -> new ResourceNotFoundException("Género no encontrado."));
        StatusUsuario status = statuses.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Estatus de usuario no encontrado."));
        Role role = roles.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado."));
        return new Relations(sucursal, genero, status, role);
    }

    private Empresa findEmpresa(Long id) {
        return empresas.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada."));
    }

    private Usuario find(String id) {
        return usuarios.findForMaintenance(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    }

    private Usuario save(Usuario usuario) {
        try {
            return usuarios.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessConflictException(
                    "Ya existe un usuario con el mismo identificador, correo o teléfono.");
        }
    }

    private void validateUnique(String email, String phone, String id) {
        boolean emailExists = email != null && (id == null
                ? usuarios.existsByCorreoElectronico(email)
                : usuarios.existsByCorreoElectronicoAndIdUsuarioNot(email, id));
        if (emailExists) throw new BusinessConflictException("Ya existe un usuario con ese correo electrónico.");
        boolean phoneExists = phone != null && (id == null
                ? usuarios.existsByTelefonoMovil(phone)
                : usuarios.existsByTelefonoMovilAndIdUsuarioNot(phone, id));
        if (phoneExists) throw new BusinessConflictException("Ya existe un usuario con ese teléfono móvil.");
    }

    private List<Usuario> exportData(String search) {
        String value = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return usuarios.findAllForMaintenance().stream()
                .filter(usuario -> value.isEmpty()
                        || contains(usuario.getIdUsuario(), value)
                        || contains(usuario.getNombre(), value)
                        || contains(usuario.getApellido(), value)
                        || contains(usuario.getRole().getNombre(), value)
                        || contains(usuario.getSucursal().getEmpresa().getNombre(), value)
                        || contains(usuario.getSucursal().getNombre(), value)
                        || contains(usuario.getStatus().getNombre(), value))
                .toList();
    }

    private boolean contains(String text, String search) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(search);
    }

    private UsuarioResponse response(Usuario value) {
        return new UsuarioResponse(
                value.getIdUsuario(), value.getNombre(), value.getApellido(), value.getFechaNacimiento(),
                value.getCorreoElectronico(), value.getTelefonoMovil(), value.getPregunta(),
                value.getSucursal().getEmpresa().getId(), value.getSucursal().getEmpresa().getNombre(),
                value.getSucursal().getId(), value.getSucursal().getNombre(),
                value.getGenero().getId(), value.getGenero().getNombre(),
                value.getStatus().getId(), value.getStatus().getNombre(),
                value.getRole().getId(), value.getRole().getNombre(),
                value.getUltimaFechaIngreso(), Integer.valueOf(1).equals(value.getRequiereCambiarPassword()));
    }

    private UsuarioListResponse listResponse(Usuario value) {
        return new UsuarioListResponse(
                value.getIdUsuario(), value.getNombre(), value.getApellido(), value.getFechaNacimiento(),
                value.getCorreoElectronico(), value.getTelefonoMovil(),
                value.getSucursal().getEmpresa().getId(), value.getSucursal().getEmpresa().getNombre(),
                value.getSucursal().getId(), value.getSucursal().getNombre(),
                value.getGenero().getId(), value.getGenero().getNombre(),
                value.getStatus().getId(), value.getStatus().getNombre(),
                value.getRole().getId(), value.getRole().getNombre(),
                value.getUltimaFechaIngreso(), Integer.valueOf(1).equals(value.getRequiereCambiarPassword()));
    }

    private List<String> exportHeaders() {
        return List.of("Usuario", "Nombre", "Apellidos", "Género", "Empresa", "Sucursal", "Rol", "Estatus", "Último ingreso");
    }

    private List<?> exportRow(Usuario value) {
        return Arrays.asList(value.getIdUsuario(), value.getNombre(), value.getApellido(),
                value.getGenero().getNombre(), value.getSucursal().getEmpresa().getNombre(),
                value.getSucursal().getNombre(), value.getRole().getNombre(), value.getStatus().getNombre(),
                format(value.getUltimaFechaIngreso()));
    }

    private String format(LocalDateTime value) {
        return value == null ? "" : value.toString();
    }

    private String nullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private record Relations(Sucursal sucursal, Genero genero, StatusUsuario status, Role role) {}
}
