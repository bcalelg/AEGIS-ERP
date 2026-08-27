package com.aegis.erp.modules.seguridad.usuario.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ROLE")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ROLE", nullable = false)
    private Long id;

    @Column(name = "NOMBRE", nullable = false, length = 50)
    private String nombre;

    @Column(name = "USUARIO_CREACION", nullable = false, length = 100)
    private String usuarioCreacion;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "USUARIO_MODIFICACION", length = 100)
    private String usuarioModificacion;

    @Column(name = "FECHA_MODIFICACION")
    private LocalDateTime fechaModificacion;

    protected Role() {}

    public Role(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public static Role crear(String nombre, String usuarioCreacion, LocalDateTime fechaCreacion) {
        Role role = new Role();
        role.nombre = nombre;
        role.usuarioCreacion = usuarioCreacion;
        role.fechaCreacion = fechaCreacion;
        return role;
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
