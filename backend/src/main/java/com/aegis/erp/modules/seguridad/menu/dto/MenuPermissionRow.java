package com.aegis.erp.modules.seguridad.menu.dto;

public record MenuPermissionRow(
        Long idModulo,
        String moduloNombre,
        Integer moduloOrden,
        Long idMenu,
        String menuNombre,
        Integer menuOrden,
        Long idOpcion,
        String opcionNombre,
        String pagina,
        Integer opcionOrden,
        Integer consultar,
        Integer alta,
        Integer baja,
        Integer cambio,
        Integer imprimir,
        Integer exportar) {}
