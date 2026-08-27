package com.aegis.erp.modules.seguridad.auth.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "BITACORA_ACCESO")
public class BitacoraAcceso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_BITACORA_ACCESO")
    private Long id;

    @Column(name = "ID_USUARIO", nullable = false, length = 50)
    private String idUsuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_TIPO_ACCESO", nullable = false)
    private TipoAcceso tipoAcceso;

    @Column(name = "FECHA_ACCESO", nullable = false)
    private LocalDateTime fechaAcceso;

    @Column(name = "HTTP_USER_AGENT", length = 200)
    private String userAgent;

    @Column(name = "DIRECCION_IP", length = 50)
    private String direccionIp;

    @Column(name = "ACCESO", length = 100)
    private String acceso;

    @Column(name = "SESION", length = 100)
    private String sesion;

    protected BitacoraAcceso() {}

    public BitacoraAcceso(
            String idUsuario,
            TipoAcceso tipoAcceso,
            LocalDateTime fechaAcceso,
            String userAgent,
            String direccionIp,
            String acceso,
            String sesion) {
        this.idUsuario = idUsuario;
        this.tipoAcceso = tipoAcceso;
        this.fechaAcceso = fechaAcceso;
        this.userAgent = userAgent;
        this.direccionIp = direccionIp;
        this.acceso = acceso;
        this.sesion = sesion;
    }

    public Long getId() {
        return id;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public TipoAcceso getTipoAcceso() {
        return tipoAcceso;
    }

    public String getAcceso() {
        return acceso;
    }
}
