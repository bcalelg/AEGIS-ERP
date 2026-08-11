package com.aegis.erp.modules.seguridad.auth.service;

import com.aegis.erp.modules.seguridad.auth.dto.LoginClientContext;
import com.aegis.erp.modules.seguridad.auth.entity.*;
import com.aegis.erp.modules.seguridad.auth.repository.*;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class AccessAuditService {
    private final TipoAccesoRepository tipos;
    private final BitacoraAccesoRepository bitacora;
    private final Clock clock;

    public AccessAuditService(TipoAccesoRepository tipos, BitacoraAccesoRepository bitacora, Clock clock) {
        this.tipos = tipos;
        this.bitacora = bitacora;
        this.clock = clock;
    }

    public void registrar(String idUsuario, String tipoNombre, LoginClientContext c) {
        TipoAcceso tipo = tipos.findByNombre(tipoNombre)
                .orElseThrow(() -> new IllegalStateException("Catálogo TIPO_ACCESO incompleto."));
        bitacora.save(new BitacoraAcceso(limit(idUsuario, 50), tipo, LocalDateTime.now(clock),
                limit(c.userAgent(), 200), limit(c.direccionIp(), 50), tipoNombre, limit(c.sesion(), 100)));
    }

    private String limit(String value, int max) {
        return value == null ? null : value.substring(0, Math.min(value.length(), max));
    }
}