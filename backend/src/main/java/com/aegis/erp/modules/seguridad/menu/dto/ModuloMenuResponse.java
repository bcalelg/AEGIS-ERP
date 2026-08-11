package com.aegis.erp.modules.seguridad.menu.dto;
import java.util.List;
public record ModuloMenuResponse(Long idModulo,String nombre,Integer ordenMenu,List<MenuResponse> menus){}
