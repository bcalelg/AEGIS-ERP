package com.aegis.erp.modules.seguridad.menu.entity;
import jakarta.persistence.*;
@Entity @Table(name="OPCION") public class Opcion {
@Id @Column(name="ID_OPCION",nullable=false) private Long id;
@ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="ID_MENU",nullable=false) private Menu menu;
@Column(name="NOMBRE",nullable=false,length=50) private String nombre;
@Column(name="ORDEN_MENU",nullable=false) private Integer ordenMenu;
@Column(name="PAGINA",nullable=false,length=100) private String pagina;
protected Opcion(){} public Long getId(){return id;} public Menu getMenu(){return menu;} public String getNombre(){return nombre;} public Integer getOrdenMenu(){return ordenMenu;} public String getPagina(){return pagina;}}
