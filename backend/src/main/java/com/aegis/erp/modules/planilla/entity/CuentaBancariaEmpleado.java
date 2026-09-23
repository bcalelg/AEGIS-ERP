package com.aegis.erp.modules.planilla.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity @Table(name = "CUENTA_BANCARIA_EMPLEADO")
public class CuentaBancariaEmpleado extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CUENTA_BANCARIA", nullable = false) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_EMPLEADO", nullable = false) private Empleado empleado;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_BANCO", nullable = false) private Banco banco;
    @Column(name = "NUMERO_DE_CUENTA", nullable = false, length = 50) private String numeroCuenta;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "ACTIVA", nullable = false, length = 1) private String activa;
    protected CuentaBancariaEmpleado() {}
    public Long getId() { return id; }
    public Empleado getEmpleado() { return empleado; }
    public Banco getBanco() { return banco; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public String getActiva() { return activa; }
}
