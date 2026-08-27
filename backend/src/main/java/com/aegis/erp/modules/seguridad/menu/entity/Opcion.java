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
@Table(name = "OPCION")
public class Opcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_OPCION", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_MENU", nullable = false)
    private Menu menu;

    @Column(name = "NOMBRE", nullable = false, length = 50)
    private String nombre;

    @Column(name = "ORDEN_MENU", nullable = false)
    private Integer ordenMenu;

    @Column(name = "PAGINA", nullable = false, length = 100)
    private String pagina;

    @Column(name = "USUARIO_CREACION", nullable = false, length = 100)
    private String usuarioCreacion;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "USUARIO_MODIFICACION", length = 100)
    private String usuarioModificacion;

    @Column(name = "FECHA_MODIFICACION")
    private LocalDateTime fechaModificacion;

    protected Opcion() {}

    public static Opcion crear(
            Menu menu,
            String nombre,
            String pagina,
            Integer ordenMenu,
            String usuarioCreacion,
            LocalDateTime fechaCreacion) {
        Opcion opcion = new Opcion();
        opcion.menu = menu;
        opcion.nombre = nombre;
        opcion.pagina = pagina;
        opcion.ordenMenu = ordenMenu;
        opcion.usuarioCreacion = usuarioCreacion;
        opcion.fechaCreacion = fechaCreacion;
        return opcion;
    }

    public void modificar(
            Menu menu,
            String nombre,
            Integer ordenMenu,
            String usuarioModificacion,
            LocalDateTime fechaModificacion) {
        this.menu = menu;
        this.nombre = nombre;
        this.ordenMenu = ordenMenu;
        this.usuarioModificacion = usuarioModificacion;
        this.fechaModificacion = fechaModificacion;
    }

    public Long getId() {
        return id;
    }

    public Menu getMenu() {
        return menu;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getOrdenMenu() {
        return ordenMenu;
    }

    public String getPagina() {
        return pagina;
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
