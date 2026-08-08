package com.aegis.erp.modules.seguridad.usuario.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USUARIO")
public class Usuario {
    @Id
    @Column(name = "ID_USUARIO", nullable = false, length = 50)
    private String idUsuario;
    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;
    @Column(name = "APELLIDO", nullable = false, length = 100)
    private String apellido;
    @Column(name = "PASSWORD", nullable = false, length = 100)
    private String passwordHash;
    @Column(name = "ULTIMA_FECHA_INGRESO")
    private LocalDateTime ultimaFechaIngreso;
    @Column(name = "INTENTOS_ACCESO", nullable = false)
    private Integer intentosAcceso;
    @Column(name = "REQUIERE_CAMBIAR_PASSWORD", nullable = false)
    private Integer requiereCambiarPassword;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_STATUS_USUARIO", nullable = false)
    private StatusUsuario status;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ROLE", nullable = false)
    private Role role;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SUCURSAL", nullable = false)
    private Sucursal sucursal;
    protected Usuario() {}
    public Usuario(String idUsuario, String nombre, String apellido, String passwordHash, Integer intentosAcceso,
                   Integer requiereCambiarPassword, StatusUsuario status, Role role, Sucursal sucursal) {
        this.idUsuario=idUsuario; this.nombre=nombre; this.apellido=apellido; this.passwordHash=passwordHash;
        this.intentosAcceso=intentosAcceso; this.requiereCambiarPassword=requiereCambiarPassword;
        this.status=status; this.role=role; this.sucursal=sucursal;
    }
    public String getIdUsuario(){return idUsuario;} public String getNombre(){return nombre;}
    public String getApellido(){return apellido;} public String getPasswordHash(){return passwordHash;}
    public Integer getIntentosAcceso(){return intentosAcceso;} public Integer getRequiereCambiarPassword(){return requiereCambiarPassword;}
    public StatusUsuario getStatus(){return status;} public Role getRole(){return role;} public Sucursal getSucursal(){return sucursal;}
    public LocalDateTime getUltimaFechaIngreso(){return ultimaFechaIngreso;}
    public void registrarIntentoFallido(){intentosAcceso=(intentosAcceso==null?0:intentosAcceso)+1;}
    public void bloquear(StatusUsuario bloqueado){status=bloqueado;}
    public void registrarIngreso(LocalDateTime fecha){intentosAcceso=0;ultimaFechaIngreso=fecha;}
}