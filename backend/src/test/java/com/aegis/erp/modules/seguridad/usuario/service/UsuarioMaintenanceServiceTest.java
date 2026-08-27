package com.aegis.erp.modules.seguridad.usuario.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.aegis.erp.common.exception.*;
import com.aegis.erp.common.export.DocumentExportService;
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

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class UsuarioMaintenanceServiceTest {
    @Mock private UsuarioRepository usuarios;
    @Mock private EmpresaRepository empresas;
    @Mock private SucursalRepository sucursales;
    @Mock private GeneroRepository generos;
    @Mock private StatusUsuarioMaintenanceRepository statuses;
    @Mock private RoleMaintenanceRepository roles;
    @Mock private Empresa empresa;
    @Mock private Sucursal sucursal;
    @Mock private Genero genero;
    @Mock private StatusUsuario status;
    @Mock private Role role;
    private UsuarioMaintenanceService service;
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-18T16:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setup() {
        service = new UsuarioMaintenanceService(
                usuarios, empresas, sucursales, generos, statuses, roles,
                new BCryptPasswordEncoder(), new DocumentExportService(), clock);
        lenient().when(empresa.getId()).thenReturn(1L);
        lenient().when(empresa.getNombre()).thenReturn("Software Inc.");
        lenient().when(empresa.getPasswordLargo()).thenReturn(8);
        lenient().when(empresa.getPasswordCantidadMayusculas()).thenReturn(1);
        lenient().when(empresa.getPasswordCantidadMinusculas()).thenReturn(1);
        lenient().when(empresa.getPasswordCantidadNumeros()).thenReturn(2);
        lenient().when(empresa.getPasswordCantidadCaracteresEspeciales()).thenReturn(1);
        lenient().when(sucursal.getId()).thenReturn(2L);
        lenient().when(sucursal.getNombre()).thenReturn("Central");
        lenient().when(sucursal.getEmpresa()).thenReturn(empresa);
        lenient().when(genero.getId()).thenReturn(3L);
        lenient().when(genero.getNombre()).thenReturn("Masculino");
        lenient().when(status.getId()).thenReturn(4L);
        lenient().when(status.getNombre()).thenReturn("Activo");
        lenient().when(role.getId()).thenReturn(5L);
        lenient().when(role.getNombre()).thenReturn("Operador");
    }

    @Test
    void altaUsaBCryptMarcaCambioObligatorioYAuditoriaSinExponerPassword() {
        relations();
        when(usuarios.saveAndFlush(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UsuarioResponse response = service.crear(create(), "Administrador");
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).saveAndFlush(captor.capture());
        Usuario saved = captor.getValue();
        assertThat(saved.getPasswordHash()).startsWith("$2");
        assertThat(new BCryptPasswordEncoder().matches("Temporal12!", saved.getPasswordHash())).isTrue();
        assertThat(saved.getRequiereCambiarPassword()).isEqualTo(1);
        assertThat(saved.getUltimaFechaCambioPassword()).isNull();
        assertThat(saved.getIntentosAcceso()).isZero();
        assertThat(saved.getUsuarioCreacion()).isEqualTo("Administrador");
        assertThat(response.toString()).doesNotContain("Temporal1!", "$2", "Respuesta privada");
    }

    @Test
    void rechazaDuplicadoConfirmacionYPoliticaInvalida() {
        when(usuarios.existsById("NUEVO")).thenReturn(true);
        assertThatThrownBy(() -> service.crear(create(), "admin"))
                .isInstanceOf(BusinessConflictException.class);
        when(usuarios.existsById("NUEVO")).thenReturn(false);
        UsuarioCreateRequest mismatch = new UsuarioCreateRequest(
                "NUEVO", "Ana", "López", LocalDate.of(1990, 1, 1), "ana@example.com", null,
                "Temporal12!", "Distinta12!", "Pregunta", "Respuesta", 1L, 2L, 3L, 4L, 5L);
        assertThatThrownBy(() -> service.crear(mismatch, "admin"))
                .isInstanceOf(InvalidPasswordChangeException.class);
        relations();
        UsuarioCreateRequest weak = new UsuarioCreateRequest(
                "NUEVO", "Ana", "López", LocalDate.of(1990, 1, 1), null, null,
                "débil", "débil", "Pregunta", "Respuesta", 1L, 2L, 3L, 4L, 5L);
        assertThatThrownBy(() -> service.crear(weak, "admin"))
                .isInstanceOf(InvalidPasswordChangeException.class);
    }

    @Test
    void validaSucursalEmpresaYCatalogos() {
        when(empresas.findById(1L)).thenReturn(Optional.of(empresa));
        Empresa otra = mock(Empresa.class);
        when(otra.getId()).thenReturn(99L);
        when(sucursal.getEmpresa()).thenReturn(otra);
        when(sucursales.findById(2L)).thenReturn(Optional.of(sucursal));
        assertThatThrownBy(() -> service.crear(create(), "admin"))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("no pertenece");
    }

    @Test
    void normalizaDuplicadosDeCorreoYTelefonoComoConflicto() {
        relations();
        when(usuarios.existsByCorreoElectronico("ana@example.com")).thenReturn(true);
        assertThatThrownBy(() -> service.crear(create(), "admin"))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessage("Ya existe un usuario con ese correo electrónico.");

        when(usuarios.existsByCorreoElectronico("ana@example.com")).thenReturn(false);
        when(usuarios.existsByTelefonoMovil("555-1000")).thenReturn(true);
        assertThatThrownBy(() -> service.crear(create(), "admin"))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessage("Ya existe un usuario con ese teléfono móvil.");
    }

    @Test
    void modificaSoloDatosAdministrativosYPreservaPassword() {
        Usuario existing = existing();
        when(usuarios.findForMaintenance("NUEVO")).thenReturn(Optional.of(existing));
        relations();
        when(usuarios.saveAndFlush(existing)).thenReturn(existing);
        String hash = existing.getPasswordHash();
        service.modificar("NUEVO", new UsuarioUpdateRequest(
                "Ana María", "López", LocalDate.of(1991, 2, 2), "nueva@example.com", "555",
                "Nueva pregunta", "", 1L, 2L, 3L, 4L, 5L), "Administrador");
        assertThat(existing.getNombre()).isEqualTo("Ana María");
        assertThat(existing.getPasswordHash()).isEqualTo(hash);
        assertThat(existing.getUsuarioModificacion()).isEqualTo("Administrador");
    }

    @Test
    void eliminaUsuarioYNormalizaConflictoReferencial() {
        Usuario existing = existing();
        when(usuarios.findForMaintenance("NUEVO")).thenReturn(Optional.of(existing));
        service.eliminar("NUEVO");
        verify(usuarios).delete(existing);
    }

    @Test
    void exportacionEsSeguraYFiltrable() {
        Usuario existing = existing();
        when(usuarios.findAllForMaintenance()).thenReturn(List.of(existing));
        String csv = new String(service.exportarCsv("software"), StandardCharsets.UTF_8);
        assertThat(csv).contains("NUEVO", "Software Inc.", "Central")
                .doesNotContain("Temporal1!", existing.getPasswordHash(), "Respuesta privada", "Pregunta");
    }

    private void relations() {
        when(empresas.findById(1L)).thenReturn(Optional.of(empresa));
        when(sucursales.findById(2L)).thenReturn(Optional.of(sucursal));
        when(generos.findById(3L)).thenReturn(Optional.of(genero));
        when(statuses.findById(4L)).thenReturn(Optional.of(status));
        when(roles.findById(5L)).thenReturn(Optional.of(role));
    }

    private UsuarioCreateRequest create() {
        return new UsuarioCreateRequest(
                " NUEVO ", " Ana ", " López ", LocalDate.of(1990, 1, 1),
                "ana@example.com", "555-1000", "Temporal12!", "Temporal12!",
                " Pregunta ", "Respuesta privada", 1L, 2L, 3L, 4L, 5L);
    }

    private Usuario existing() {
        return Usuario.crear(
                "NUEVO", "Ana", "López", LocalDate.of(1990, 1, 1),
                new BCryptPasswordEncoder().encode("Temporal12!"), "ana@example.com", "555-1000",
                "Pregunta", "Respuesta privada", genero, status, role, sucursal,
                "system", LocalDateTime.now(clock));
    }
}
