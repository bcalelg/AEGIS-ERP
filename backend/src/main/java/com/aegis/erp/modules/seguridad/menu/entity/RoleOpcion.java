package com.aegis.erp.modules.seguridad.menu.entity;

import com.aegis.erp.modules.seguridad.usuario.entity.Role;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ROLE_OPCION")
public class RoleOpcion {
    @EmbeddedId private RoleOpcionId id;

    @MapsId("idRole")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ROLE", nullable = false)
    private Role role;

    @MapsId("idOpcion")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_OPCION", nullable = false)
    private Opcion opcion;

    @Column(name = "CONSULTAR", nullable = false)
    private Integer consultar;

    @Column(name = "ALTA", nullable = false)
    private Integer alta;

    @Column(name = "BAJA", nullable = false)
    private Integer baja;

    @Column(name = "CAMBIO", nullable = false)
    private Integer cambio;

    @Column(name = "IMPRIMIR", nullable = false)
    private Integer imprimir;

    @Column(name = "EXPORTAR", nullable = false)
    private Integer exportar;

    @Column(name = "USUARIO_CREACION", nullable = false, length = 100)
    private String usuarioCreacion;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "USUARIO_MODIFICACION", length = 100)
    private String usuarioModificacion;

    @Column(name = "FECHA_MODIFICACION")
    private LocalDateTime fechaModificacion;

    protected RoleOpcion() {}

    public static RoleOpcion crear(
            Role role,
            Opcion opcion,
            boolean consultar,
            boolean alta,
            boolean baja,
            boolean cambio,
            boolean imprimir,
            boolean exportar,
            String usuario,
            LocalDateTime fecha) {
        RoleOpcion value = new RoleOpcion();
        value.id = new RoleOpcionId(role.getId(), opcion.getId());
        value.role = role;
        value.opcion = opcion;
        value.aplicar(consultar, alta, baja, cambio, imprimir, exportar);
        value.usuarioCreacion = usuario;
        value.fechaCreacion = fecha;
        return value;
    }

    public void modificar(
            boolean consultar,
            boolean alta,
            boolean baja,
            boolean cambio,
            boolean imprimir,
            boolean exportar,
            String usuario,
            LocalDateTime fecha) {
        aplicar(consultar, alta, baja, cambio, imprimir, exportar);
        usuarioModificacion = usuario;
        fechaModificacion = fecha;
    }

    private void aplicar(
            boolean consultar,
            boolean alta,
            boolean baja,
            boolean cambio,
            boolean imprimir,
            boolean exportar) {
        this.consultar = bit(consultar);
        this.alta = bit(alta);
        this.baja = bit(baja);
        this.cambio = bit(cambio);
        this.imprimir = bit(imprimir);
        this.exportar = bit(exportar);
    }

    private static int bit(boolean value) {
        return value ? 1 : 0;
    }

    public RoleOpcionId getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public Opcion getOpcion() {
        return opcion;
    }

    public Integer getConsultar() {
        return consultar;
    }

    public Integer getAlta() {
        return alta;
    }

    public Integer getBaja() {
        return baja;
    }

    public Integer getCambio() {
        return cambio;
    }

    public Integer getImprimir() {
        return imprimir;
    }

    public Integer getExportar() {
        return exportar;
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
