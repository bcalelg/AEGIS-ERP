package com.aegis.erp.modules.seguridad.usuario.entity;

import com.aegis.erp.modules.seguridad.empresa.entity.Empresa;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SUCURSAL")
public class Sucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SUCURSAL", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_EMPRESA", nullable = false)
    private Empresa empresa;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "DIRECCION", nullable = false, length = 200)
    private String direccion;

    @Column(name = "USUARIO_CREACION", nullable = false, length = 100)
    private String usuarioCreacion;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "USUARIO_MODIFICACION", length = 100)
    private String usuarioModificacion;

    @Column(name = "FECHA_MODIFICACION")
    private LocalDateTime fechaModificacion;

    protected Sucursal() {}

    public Sucursal(Long id, Empresa empresa) {
        this.id = id;
        this.empresa = empresa;
    }

    public static Sucursal crear(
            Empresa empresa,
            String nombre,
            String direccion,
            String usuarioCreacion,
            LocalDateTime fechaCreacion) {
        Sucursal sucursal = new Sucursal();
        sucursal.empresa = empresa;
        sucursal.nombre = nombre;
        sucursal.direccion = direccion;
        sucursal.usuarioCreacion = usuarioCreacion;
        sucursal.fechaCreacion = fechaCreacion;
        return sucursal;
    }

    public void modificar(
            Empresa empresa,
            String nombre,
            String direccion,
            String usuarioModificacion,
            LocalDateTime fechaModificacion) {
        this.empresa = empresa;
        this.nombre = nombre;
        this.direccion = direccion;
        this.usuarioModificacion = usuarioModificacion;
        this.fechaModificacion = fechaModificacion;
    }

    public Long getId() {
        return id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
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
