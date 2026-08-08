package com.aegis.erp.modules.seguridad.auth.entity;
import jakarta.persistence.*;
@Entity @Table(name="TIPO_ACCESO")
public class TipoAcceso {
@Id @Column(name="ID_TIPO_ACCESO",nullable=false) private Long id;
@Column(name="NOMBRE",nullable=false,length=100) private String nombre;
protected TipoAcceso(){} public TipoAcceso(Long id,String nombre){this.id=id;this.nombre=nombre;}
public Long getId(){return id;} public String getNombre(){return nombre;}}