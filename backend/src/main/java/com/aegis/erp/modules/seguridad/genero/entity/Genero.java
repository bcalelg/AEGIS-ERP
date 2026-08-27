package com.aegis.erp.modules.seguridad.genero.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "GENERO")
public class Genero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_GENERO")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "USUARIO_CREACION", nullable = false, length = 100)
    private String usuarioCreacion;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "USUARIO_MODIFICACION", length = 100)
    private String usuarioModificacion;

    @Column(name = "FECHA_MODIFICACION")
    private LocalDateTime fechaModificacion;

    protected Genero() {}

    public static Genero crear(String nombre, String usuarioCreacion, LocalDateTime fechaCreacion) {
        Genero g = new Genero();
        g.nombre = nombre;
        g.usuarioCreacion = usuarioCreacion;
        g.fechaCreacion = fechaCreacion;
        return g;
    }

    public void modificar(
            String nombre, String usuarioModificacion, LocalDateTime fechaModificacion) {
        this.nombre = nombre;
        this.usuarioModificacion = usuarioModificacion;
        this.fechaModificacion = fechaModificacion;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
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
