package com.aegis.erp.modules.seguridad.usuario.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "STATUS_USUARIO")
public class StatusUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_STATUS_USUARIO", nullable = false)
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

    protected StatusUsuario() {}

    public StatusUsuario(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public static StatusUsuario crear(
            String nombre,
            String usuarioCreacion,
            LocalDateTime fechaCreacion) {
        StatusUsuario status = new StatusUsuario();
        status.nombre = nombre;
        status.usuarioCreacion = usuarioCreacion;
        status.fechaCreacion = fechaCreacion;
        return status;
    }

    public void modificar(
            String nombre,
            String usuarioModificacion,
            LocalDateTime fechaModificacion) {
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
