package com.aegis.erp.modules.seguridad.menu.entity;
import com.aegis.erp.modules.seguridad.usuario.entity.Role; import jakarta.persistence.*;
@Entity @Table(name="ROLE_OPCION") public class RoleOpcion {
@EmbeddedId private RoleOpcionId id;
@MapsId("idRole") @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="ID_ROLE",nullable=false) private Role role;
@MapsId("idOpcion") @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="ID_OPCION",nullable=false) private Opcion opcion;
@Column(name="CONSULTAR",nullable=false) private Integer consultar; @Column(name="ALTA",nullable=false) private Integer alta;
@Column(name="BAJA",nullable=false) private Integer baja; @Column(name="CAMBIO",nullable=false) private Integer cambio;
@Column(name="IMPRIMIR",nullable=false) private Integer imprimir; @Column(name="EXPORTAR",nullable=false) private Integer exportar;
protected RoleOpcion(){} public RoleOpcionId getId(){return id;} public Role getRole(){return role;} public Opcion getOpcion(){return opcion;}
public Integer getConsultar(){return consultar;} public Integer getAlta(){return alta;} public Integer getBaja(){return baja;} public Integer getCambio(){return cambio;} public Integer getImprimir(){return imprimir;} public Integer getExportar(){return exportar;}}
