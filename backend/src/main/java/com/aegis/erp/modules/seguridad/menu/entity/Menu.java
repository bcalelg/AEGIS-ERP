package com.aegis.erp.modules.seguridad.menu.entity;
import jakarta.persistence.*;
@Entity @Table(name="MENU") public class Menu {
@Id @Column(name="ID_MENU",nullable=false) private Long id;
@ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="ID_MODULO",nullable=false) private Modulo modulo;
@Column(name="NOMBRE",nullable=false,length=50) private String nombre;
@Column(name="ORDEN_MENU",nullable=false) private Integer ordenMenu;
protected Menu(){} public Long getId(){return id;} public Modulo getModulo(){return modulo;} public String getNombre(){return nombre;} public Integer getOrdenMenu(){return ordenMenu;}}
