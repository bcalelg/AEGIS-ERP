package com.aegis.erp.modules.seguridad.auth.dto;
public record LoginResponse(boolean authenticated,String tokenType,String accessToken,long expiresIn,String idUsuario,String nombre,String apellido,String role,boolean requiereCambiarPassword){}
