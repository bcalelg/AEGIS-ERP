package com.aegis.erp.modules.planilla.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "INASISTENCIA")
public class Inasistencia extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_INASISTENCIA", nullable = false) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_EMPLEADO", nullable = false) private Empleado empleado;
    @Column(name = "FECHA_INICIAL", nullable = false) private LocalDateTime fechaInicial;
    @Column(name = "FECHA_FINAL", nullable = false) private LocalDateTime fechaFinal;
    @Column(name = "MOTIVO_INASISTENCIA", length = 300) private String motivo;
    @Column(name = "FECHA_PROCESADO") private LocalDateTime fechaProcesado;
    protected Inasistencia() {}
    public Long getId() { return id; }
    public Empleado getEmpleado() { return empleado; }
}
