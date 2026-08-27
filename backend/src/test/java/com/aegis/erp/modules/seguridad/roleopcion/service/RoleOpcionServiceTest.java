package com.aegis.erp.modules.seguridad.roleopcion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.modules.seguridad.menu.entity.Menu;
import com.aegis.erp.modules.seguridad.menu.entity.Modulo;
import com.aegis.erp.modules.seguridad.menu.entity.Opcion;
import com.aegis.erp.modules.seguridad.menu.entity.RoleOpcion;
import com.aegis.erp.modules.seguridad.roleopcion.dto.RoleOpcionItemRequest;
import com.aegis.erp.modules.seguridad.roleopcion.dto.RoleOpcionSaveRequest;
import com.aegis.erp.modules.seguridad.roleopcion.repository.RoleOpcionAssignmentRepository;
import com.aegis.erp.modules.seguridad.roleopcion.repository.RoleOpcionMatrixRepository;
import com.aegis.erp.modules.seguridad.roleopcion.repository.RoleOpcionModuloRepository;
import com.aegis.erp.modules.seguridad.roleopcion.repository.RoleOpcionRoleRepository;
import com.aegis.erp.modules.seguridad.usuario.entity.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class RoleOpcionServiceTest {
    @Mock private RoleOpcionRoleRepository roles;
    @Mock private RoleOpcionModuloRepository modulos;
    @Mock private RoleOpcionMatrixRepository opciones;
    @Mock private RoleOpcionAssignmentRepository asignaciones;
    private RoleOpcionService service;
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-17T18:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setup() {
        service = new RoleOpcionService(roles, modulos, opciones, asignaciones, clock);
    }

    @Test
    void cargaCatalogosYMatrizIncluyeOpcionesSinAsignacion() {
        Role role = new Role(1L, "Supervisor");
        Modulo modulo = modulo(1L, "Seguridad");
        Opcion empresa = opcion(1L, modulo, "Parámetros", 1, "Empresas", 1);
        when(roles.findAllByOrderByNombreAscIdAsc()).thenReturn(List.of(role));
        when(modulos.findAllByOrderByOrdenMenuAscIdAsc()).thenReturn(List.of(modulo));
        prepare(role, modulo, List.of(empresa));

        assertThat(service.roles()).extracting("nombre").containsExactly("Supervisor");
        assertThat(service.modulos()).extracting("nombre").containsExactly("Seguridad");
        assertThat(service.matriz(1L, 1L).getFirst())
                .extracting("nombreOpcion", "consultar", "alta")
                .containsExactly("Empresas", false, false);
    }

    @Test
    void matrizRespetaAsignacionExistente() {
        Role role = new Role(1L, "Supervisor");
        Modulo modulo = modulo(1L, "Seguridad");
        Opcion empresa = opcion(1L, modulo, "Parámetros", 1, "Empresas", 1);
        RoleOpcion assignment = RoleOpcion.crear(
                role, empresa, true, false, false, true, false, true, "system", LocalDateTime.now(clock));
        prepare(role, modulo, List.of(empresa));
        when(asignaciones.findByRoleAndModulo(1L, 1L)).thenReturn(List.of(assignment));

        var item = service.matriz(1L, 1L).getFirst();
        assertThat(item.consultar()).isTrue();
        assertThat(item.cambio()).isTrue();
        assertThat(item.exportar()).isTrue();
    }

    @Test
    void guardaNuevaActualizaYEliminaFilaSinPermisos() {
        Role role = new Role(1L, "Supervisor");
        Modulo modulo = modulo(1L, "Seguridad");
        Opcion empresa = opcion(1L, modulo, "Parámetros", 1, "Empresas", 1);
        prepare(role, modulo, List.of(empresa));
        when(opciones.existsById(1L)).thenReturn(true);
        when(asignaciones.save(any(RoleOpcion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.guardar(request(true), "admin");
        verify(asignaciones).save(any(RoleOpcion.class));

        RoleOpcion existing = RoleOpcion.crear(
                role, empresa, true, true, false, false, false, false, "system", LocalDateTime.now(clock));
        when(asignaciones.findByRoleAndModulo(1L, 1L)).thenReturn(List.of(existing));
        service.guardar(request(false), "admin");
        verify(asignaciones).delete(existing);
    }

    @Test
    void validaRoleModuloOpcionYQuePertenezcaAlModulo() {
        assertThatThrownBy(() -> service.matriz(9L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rol");
        Role role = new Role(1L, "Supervisor");
        when(roles.findById(1L)).thenReturn(Optional.of(role));
        assertThatThrownBy(() -> service.matriz(1L, 9L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Módulo");

        Modulo modulo = modulo(1L, "Seguridad");
        when(modulos.findById(1L)).thenReturn(Optional.of(modulo));
        when(opciones.existsById(99L)).thenReturn(true);
        assertThatThrownBy(
                        () ->
                                service.guardar(
                                        new RoleOpcionSaveRequest(
                                                1L,
                                                1L,
                                                List.of(item(99L, true))),
                                        "admin"))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("no pertenece");
    }

    private void prepare(Role role, Modulo modulo, List<Opcion> values) {
        when(roles.findById(role.getId())).thenReturn(Optional.of(role));
        when(modulos.findById(modulo.getId())).thenReturn(Optional.of(modulo));
        when(opciones.findByModuloOrdered(modulo.getId())).thenReturn(values);
    }

    private RoleOpcionSaveRequest request(boolean enabled) {
        return new RoleOpcionSaveRequest(1L, 1L, List.of(item(1L, enabled)));
    }

    private RoleOpcionItemRequest item(long id, boolean enabled) {
        return new RoleOpcionItemRequest(id, enabled, false, false, false, false, false);
    }

    private Modulo modulo(long id, String nombre) {
        Modulo value = Modulo.crear(nombre, 1, "system", LocalDateTime.now(clock));
        ReflectionTestUtils.setField(value, "id", id);
        return value;
    }

    private Opcion opcion(
            long id,
            Modulo modulo,
            String menuNombre,
            int menuOrden,
            String nombre,
            int orden) {
        Menu menu = Menu.crear(modulo, menuNombre, menuOrden, "system", LocalDateTime.now(clock));
        ReflectionTestUtils.setField(menu, "id", 1L);
        Opcion value = Opcion.crear(menu, nombre, "pagina.php", orden, "system", LocalDateTime.now(clock));
        ReflectionTestUtils.setField(value, "id", id);
        return value;
    }
}
