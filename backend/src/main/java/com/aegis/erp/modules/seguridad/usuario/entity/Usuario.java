package com.aegis.erp.modules.seguridad.usuario.entity;

import com.aegis.erp.modules.seguridad.genero.entity.Genero;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "USUARIO")
public class Usuario {
    @Id
    @Column(name = "ID_USUARIO", nullable = false, length = 50)
    private String idUsuario;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "APELLIDO", nullable = false, length = 100)
    private String apellido;

    @Column(name = "FECHA_NACIMIENTO", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "PASSWORD", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "ULTIMA_FECHA_INGRESO")
    private LocalDateTime ultimaFechaIngreso;

    @Column(name = "SESION_ACTUAL", length = 100)
    private String sesionActual;

    @Column(name = "ULTIMA_FECHA_CAMBIO_PASSWORD")
    private LocalDateTime ultimaFechaCambioPassword;

    @Column(name = "INTENTOS_ACCESO", nullable = false)
    private Integer intentosAcceso;

    @Column(name = "REQUIERE_CAMBIAR_PASSWORD", nullable = false)
    private Integer requiereCambiarPassword;

    @Column(name = "CORREO_ELECTRONICO", length = 100)
    private String correoElectronico;

    @Lob
    @Column(name = "FOTOGRAFIA")
    private byte[] fotografia;

    @Column(name = "TELEFONO_MOVIL", length = 30)
    private String telefonoMovil;

    @Column(name = "PREGUNTA", nullable = false, length = 200)
    private String pregunta;

    @Column(name = "RESPUESTA", nullable = false, length = 200)
    private String respuesta;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "USUARIO_CREACION", nullable = false, length = 100)
    private String usuarioCreacion;

    @Column(name = "FECHA_MODIFICACION")
    private LocalDateTime fechaModificacion;

    @Column(name = "USUARIO_MODIFICACION", length = 100)
    private String usuarioModificacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_GENERO", nullable = false)
    private Genero genero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_STATUS_USUARIO", nullable = false)
    private StatusUsuario status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ROLE", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SUCURSAL", nullable = false)
    private Sucursal sucursal;

    protected Usuario() {}

    public Usuario(
            String idUsuario,
            String nombre,
            String apellido,
            String passwordHash,
            Integer intentosAcceso,
            Integer requiereCambiarPassword,
            StatusUsuario status,
            Role role,
            Sucursal sucursal) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.passwordHash = passwordHash;
        this.intentosAcceso = intentosAcceso;
        this.requiereCambiarPassword = requiereCambiarPassword;
        this.status = status;
        this.role = role;
        this.sucursal = sucursal;
    }

    public static Usuario crear(
            String idUsuario,
            String nombre,
            String apellido,
            LocalDate fechaNacimiento,
            String passwordHash,
            String correoElectronico,
            String telefonoMovil,
            String pregunta,
            String respuesta,
            Genero genero,
            StatusUsuario status,
            Role role,
            Sucursal sucursal,
            String usuarioCreacion,
            LocalDateTime fechaCreacion) {
        Usuario usuario = new Usuario();
        usuario.idUsuario = idUsuario;
        usuario.nombre = nombre;
        usuario.apellido = apellido;
        usuario.fechaNacimiento = fechaNacimiento;
        usuario.passwordHash = passwordHash;
        usuario.correoElectronico = correoElectronico;
        usuario.telefonoMovil = telefonoMovil;
        usuario.pregunta = pregunta;
        usuario.respuesta = respuesta;
        usuario.genero = genero;
        usuario.status = status;
        usuario.role = role;
        usuario.sucursal = sucursal;
        usuario.intentosAcceso = 0;
        usuario.requiereCambiarPassword = 1;
        usuario.usuarioCreacion = usuarioCreacion;
        usuario.fechaCreacion = fechaCreacion;
        return usuario;
    }

    public void modificarDatosAdministrativos(
            String nombre,
            String apellido,
            LocalDate fechaNacimiento,
            String correoElectronico,
            String telefonoMovil,
            String pregunta,
            String nuevaRespuesta,
            Genero genero,
            StatusUsuario status,
            Role role,
            Sucursal sucursal,
            String usuarioModificacion,
            LocalDateTime fechaModificacion) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.correoElectronico = correoElectronico;
        this.telefonoMovil = telefonoMovil;
        this.pregunta = pregunta;
        if (nuevaRespuesta != null) this.respuesta = nuevaRespuesta;
        this.genero = genero;
        this.status = status;
        this.role = role;
        this.sucursal = sucursal;
        this.usuarioModificacion = usuarioModificacion;
        this.fechaModificacion = fechaModificacion;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Integer getIntentosAcceso() {
        return intentosAcceso;
    }

    public Integer getRequiereCambiarPassword() {
        return requiereCambiarPassword;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public String getTelefonoMovil() {
        return telefonoMovil;
    }

    public String getPregunta() {
        return pregunta;
    }

    public Genero getGenero() {
        return genero;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public String getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public String getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public StatusUsuario getStatus() {
        return status;
    }

    public Role getRole() {
        return role;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public LocalDateTime getUltimaFechaIngreso() {
        return ultimaFechaIngreso;
    }

    public String getSesionActual() {
        return sesionActual;
    }

    public LocalDateTime getUltimaFechaCambioPassword() {
        return ultimaFechaCambioPassword;
    }

    public void registrarIntentoFallido() {
        intentosAcceso = (intentosAcceso == null ? 0 : intentosAcceso) + 1;
    }

    public void bloquear(StatusUsuario bloqueado) {
        status = bloqueado;
    }

    public void registrarIngreso(LocalDateTime fecha, String identificadorSesion) {
        intentosAcceso = 0;
        ultimaFechaIngreso = fecha;
        sesionActual = identificadorSesion;
    }

    public void requerirCambioPassword() {
        requiereCambiarPassword = 1;
    }

    public void cambiarPassword(
            String nuevoHash,
            LocalDateTime fecha,
            String identificadorSesion) {
        passwordHash = nuevoHash;
        requiereCambiarPassword = 0;
        ultimaFechaCambioPassword = fecha;
        sesionActual = identificadorSesion;
    }

    public void restablecerPassword(
            String nuevoHash,
            LocalDateTime fecha,
            String usuarioModificacion) {
        passwordHash = nuevoHash;
        requiereCambiarPassword = 0;
        ultimaFechaCambioPassword = fecha;
        sesionActual = null;
        this.usuarioModificacion = usuarioModificacion;
        this.fechaModificacion = fecha;
    }

    public boolean cerrarSesion(String identificadorSesion) {
        if (sesionActual == null || !sesionActual.equals(identificadorSesion)) {
            return false;
        }
        sesionActual = null;
        return true;
    }
}
