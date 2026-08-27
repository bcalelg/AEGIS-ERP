package com.aegis.erp.modules.seguridad.menu.service;

import com.aegis.erp.modules.seguridad.menu.dto.*;
import com.aegis.erp.modules.seguridad.menu.repository.RoleOpcionRepository;
import com.aegis.erp.modules.seguridad.usuario.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MenuService {
    private final UsuarioRepository usuarios;
    private final RoleOpcionRepository rolesOpciones;

    public MenuService(UsuarioRepository usuarios, RoleOpcionRepository rolesOpciones) {
        this.usuarios = usuarios;
        this.rolesOpciones = rolesOpciones;
    }

    @Transactional(readOnly = true)
    public List<ModuloMenuResponse> findMenuForUser(String idUsuario) {
        return usuarios.findRoleIdByIdUsuario(idUsuario)
                .map(rolesOpciones::findMenuRowsByRoleId)
                .map(this::assemble)
                .orElseGet(List::of);
    }

    private List<ModuloMenuResponse> assemble(List<MenuPermissionRow> rows) {
        Map<Long, ModuloBuilder> modulos = new LinkedHashMap<>();
        for (MenuPermissionRow row : rows) {
            ModuloBuilder modulo =
                    modulos.computeIfAbsent(
                            row.idModulo(),
                            ignored ->
                                    new ModuloBuilder(
                                            row.idModulo(), row.moduloNombre(), row.moduloOrden()));
            MenuBuilder menu =
                    modulo.menus.computeIfAbsent(
                            row.idMenu(),
                            ignored ->
                                    new MenuBuilder(
                                            row.idMenu(), row.menuNombre(), row.menuOrden()));
            menu.opciones.add(
                    new OpcionResponse(
                            row.idOpcion(),
                            row.opcionNombre(),
                            row.pagina(),
                            row.opcionOrden(),
                            permisos(row)));
        }
        return modulos.values().stream().map(ModuloBuilder::toResponse).toList();
    }

    private PermisosResponse permisos(MenuPermissionRow row) {
        return new PermisosResponse(
                enabled(row.consultar()),
                enabled(row.alta()),
                enabled(row.baja()),
                enabled(row.cambio()),
                enabled(row.imprimir()),
                enabled(row.exportar()));
    }

    private boolean enabled(Integer value) {
        return Integer.valueOf(1).equals(value);
    }

    private static final class ModuloBuilder {
        private final Long id;
        private final String nombre;
        private final Integer orden;
        private final Map<Long, MenuBuilder> menus = new LinkedHashMap<>();

        private ModuloBuilder(Long id, String nombre, Integer orden) {
            this.id = id;
            this.nombre = nombre;
            this.orden = orden;
        }

        private ModuloMenuResponse toResponse() {
            return new ModuloMenuResponse(
                    id,
                    nombre,
                    orden,
                    menus.values().stream().map(MenuBuilder::toResponse).toList());
        }
    }

    private static final class MenuBuilder {
        private final Long id;
        private final String nombre;
        private final Integer orden;
        private final List<OpcionResponse> opciones = new ArrayList<>();

        private MenuBuilder(Long id, String nombre, Integer orden) {
            this.id = id;
            this.nombre = nombre;
            this.orden = orden;
        }

        private MenuResponse toResponse() {
            return new MenuResponse(id, nombre, orden, List.copyOf(opciones));
        }
    }
}
