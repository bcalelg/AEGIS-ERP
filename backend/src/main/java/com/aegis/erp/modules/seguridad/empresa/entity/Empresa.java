package com.aegis.erp.modules.seguridad.empresa.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "EMPRESA")
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_EMPRESA", nullable = false)
    private Long id;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "DIRECCION", nullable = false, length = 200)
    private String direccion;

    @Column(name = "NIT", nullable = false, length = 20)
    private String nit;

    @Column(name = "PASSWORD_CANTIDAD_MAYUSCULAS")
    private Integer passwordCantidadMayusculas;

    @Column(name = "PASSWORD_CANTIDAD_MINUSCULAS")
    private Integer passwordCantidadMinusculas;

    @Column(name = "PASSWORD_CANTIDAD_CARACTERES_ESPECIALES")
    private Integer passwordCantidadCaracteresEspeciales;

    @Column(name = "PASSWORD_CANTIDAD_CADUCIDAD_DIAS")
    private Integer passwordCantidadCaducidadDias;

    @Column(name = "PASSWORD_LARGO")
    private Integer passwordLargo;

    @Column(name = "PASSWORD_INTENTOS_ANTES_DE_BLOQUEAR")
    private Integer passwordIntentosAntesDeBloquear;

    @Column(name = "PASSWORD_CANTIDAD_NUMEROS")
    private Integer passwordCantidadNumeros;

    @Column(name = "PASSWORD_CANTIDAD_PREGUNTAS_VALIDAR")
    private Integer passwordCantidadPreguntasValidar;

    @Column(name = "USUARIO_CREACION", nullable = false, length = 100)
    private String usuarioCreacion;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "USUARIO_MODIFICACION", length = 100)
    private String usuarioModificacion;

    @Column(name = "FECHA_MODIFICACION")
    private LocalDateTime fechaModificacion;

    protected Empresa() {}

    public Empresa(Long id, Integer intentos) {
        this.id = id;
        this.passwordIntentosAntesDeBloquear = intentos;
    }

    public static Empresa crear(
            String nombre,
            String direccion,
            String nit,
            Integer mayusculas,
            Integer minusculas,
            Integer especiales,
            Integer caducidad,
            Integer largo,
            Integer intentos,
            Integer numeros,
            Integer preguntas,
            String usuario,
            LocalDateTime fecha) {

        Empresa e = new Empresa();
        e.nombre = nombre;
        e.direccion = direccion;
        e.nit = nit;
        e.aplicarPoliticas(
                mayusculas, minusculas, especiales, caducidad, largo, intentos, numeros, preguntas);
        e.usuarioCreacion = usuario;
        e.fechaCreacion = fecha;
        return e;
    }

    public void modificar(
            String nombre,
            String direccion,
            String nit,
            Integer mayusculas,
            Integer minusculas,
            Integer especiales,
            Integer caducidad,
            Integer largo,
            Integer intentos,
            Integer numeros,
            Integer preguntas,
            String usuario,
            LocalDateTime fecha) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.nit = nit;
        aplicarPoliticas(
                mayusculas, minusculas, especiales, caducidad, largo, intentos, numeros, preguntas);
        this.usuarioModificacion = usuario;
        this.fechaModificacion = fecha;
    }

    private void aplicarPoliticas(
            Integer mayusculas,
            Integer minusculas,
            Integer especiales,
            Integer caducidad,
            Integer largo,
            Integer intentos,
            Integer numeros,
            Integer preguntas) {
        this.passwordCantidadMayusculas = mayusculas;
        this.passwordCantidadMinusculas = minusculas;
        this.passwordCantidadCaracteresEspeciales = especiales;
        this.passwordCantidadCaducidadDias = caducidad;
        this.passwordLargo = largo;
        this.passwordIntentosAntesDeBloquear = intentos;
        this.passwordCantidadNumeros = numeros;
        this.passwordCantidadPreguntasValidar = preguntas;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getNit() {
        return nit;
    }

    public Integer getPasswordCantidadMayusculas() {
        return passwordCantidadMayusculas;
    }

    public Integer getPasswordCantidadMinusculas() {
        return passwordCantidadMinusculas;
    }

    public Integer getPasswordCantidadCaracteresEspeciales() {
        return passwordCantidadCaracteresEspeciales;
    }

    public Integer getPasswordCantidadCaducidadDias() {
        return passwordCantidadCaducidadDias;
    }

    public Integer getPasswordLargo() {
        return passwordLargo;
    }

    public Integer getPasswordIntentosAntesDeBloquear() {
        return passwordIntentosAntesDeBloquear;
    }

    public Integer getIntentosAntesDeBloquear() {
        return passwordIntentosAntesDeBloquear;
    }

    public Integer getPasswordCantidadNumeros() {
        return passwordCantidadNumeros;
    }

    public Integer getPasswordCantidadPreguntasValidar() {
        return passwordCantidadPreguntasValidar;
    }

    public String getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public String getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }
}
