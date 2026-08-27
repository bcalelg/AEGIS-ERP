package com.aegis.erp.modules.seguridad.menu.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "MODULO")
public class Modulo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MODULO", nullable = false)
    private Long id;

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

    protected Modulo() {}

    public static Modulo crear(
            String nombre,
            Integer ordenMenu,
            String usuarioCreacion,
            LocalDateTime fechaCreacion) {
        Modulo modulo = new Modulo();
        modulo.nombre = nombre;
        modulo.ordenMenu = ordenMenu;
        modulo.usuarioCreacion = usuarioCreacion;
        modulo.fechaCreacion = fechaCreacion;
        return modulo;
    }

    public void modificar(
            String nombre,
            Integer ordenMenu,
            String usuarioModificacion,
            LocalDateTime fechaModificacion) {
        this.nombre = nombre;
        this.ordenMenu = ordenMenu;
        this.usuarioModificacion = usuarioModificacion;
        this.fechaModificacion = fechaModificacion;
    }

    public Long getId() {
        return id;
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
