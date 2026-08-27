package com.aegis.erp.modules.seguridad.menu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "MENU")
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MENU", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_MODULO", nullable = false)
    private Modulo modulo;

    @Column(name = "NOMBRE", nullable = false, length = 50)
    private String nombre;

    @Column(name = "ORDEN_MENU", nullable = false)
    private Integer ordenMenu;

    @Column(name = "USUARIO_CREACION", nullable = false, length = 100)
    private String usuarioCreacion;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "USUARIO_MODIFICACION", length = 100)
    private String usuarioModificacion;

    @Column(name = "FECHA_MODIFICACION")
    private LocalDateTime fechaModificacion;

    protected Menu() {}

    public static Menu crear(
            Modulo modulo,
            String nombre,
            Integer ordenMenu,
            String usuarioCreacion,
            LocalDateTime fechaCreacion) {
        Menu menu = new Menu();
        menu.modulo = modulo;
        menu.nombre = nombre;
        menu.ordenMenu = ordenMenu;
        menu.usuarioCreacion = usuarioCreacion;
        menu.fechaCreacion = fechaCreacion;
        return menu;
    }

    public void modificar(
            Modulo modulo,
            String nombre,
            Integer ordenMenu,
            String usuarioModificacion,
            LocalDateTime fechaModificacion) {
        this.modulo = modulo;
        this.nombre = nombre;
        this.ordenMenu = ordenMenu;
        this.usuarioModificacion = usuarioModificacion;
        this.fechaModificacion = fechaModificacion;
    }

    public Long getId() {
        return id;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getOrdenMenu() {
        return ordenMenu;
    }

    public String getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public String getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }
}
