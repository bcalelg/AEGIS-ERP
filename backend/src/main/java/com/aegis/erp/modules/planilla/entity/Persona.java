package com.aegis.erp.modules.planilla.entity;

import com.aegis.erp.modules.seguridad.genero.entity.Genero;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity @Table(name = "PERSONA")
public class Persona extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PERSONA", nullable = false) private Long id;
    @Column(name = "NOMBRE", nullable = false, length = 50) private String nombre;
    @Column(name = "APELLIDO", nullable = false, length = 50) private String apellido;
    @Column(name = "FECHA_NACIMIENTO", nullable = false) private LocalDate fechaNacimiento;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_GENERO", nullable = false) private Genero genero;
    @Column(name = "DIRECCION", nullable = false, length = 100) private String direccion;
    @Column(name = "TELEFONO", nullable = false, length = 50) private String telefono;
    @Column(name = "CORREO_ELECTRONICO", length = 50) private String correoElectronico;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ESTADO_CIVIL", nullable = false) private EstadoCivil estadoCivil;
    protected Persona() {}
    public static Persona crear(String nombre, String apellido, LocalDate fechaNacimiento,
            Genero genero, String direccion, String telefono, String correoElectronico,
            EstadoCivil estadoCivil, String usuario, java.time.LocalDateTime ahora) {
        Persona persona = new Persona();
        persona.modificarDatos(nombre, apellido, fechaNacimiento, genero, direccion, telefono,
                correoElectronico, estadoCivil);
        persona.usuarioCreacion = usuario; persona.fechaCreacion = ahora;
        return persona;
    }
    public void modificar(String nombre, String apellido, LocalDate fechaNacimiento,
            Genero genero, String direccion, String telefono, String correoElectronico,
            EstadoCivil estadoCivil, String usuario, java.time.LocalDateTime ahora) {
        modificarDatos(nombre, apellido, fechaNacimiento, genero, direccion, telefono,
                correoElectronico, estadoCivil);
        usuarioModificacion = usuario; fechaModificacion = ahora;
    }
    private void modificarDatos(String nombre, String apellido, LocalDate fechaNacimiento,
            Genero genero, String direccion, String telefono, String correoElectronico,
            EstadoCivil estadoCivil) {
        this.nombre = nombre; this.apellido = apellido; this.fechaNacimiento = fechaNacimiento;
        this.genero = genero; this.direccion = direccion; this.telefono = telefono;
        this.correoElectronico = correoElectronico; this.estadoCivil = estadoCivil;
    }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public Genero getGenero() { return genero; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public String getCorreoElectronico() { return correoElectronico; }
    public EstadoCivil getEstadoCivil() { return estadoCivil; }
}
