package com.aegis.erp.modules.seguridad.menu.dto;
import java.util.List;
public record MenuResponse(Long idMenu,String nombre,Integer ordenMenu,List<OpcionResponse> opciones){}
