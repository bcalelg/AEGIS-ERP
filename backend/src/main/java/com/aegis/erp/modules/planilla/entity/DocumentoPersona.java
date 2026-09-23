package com.aegis.erp.modules.planilla.entity;

import jakarta.persistence.*;

@Entity @Table(name = "DOCUMENTO_PERSONA")
public class DocumentoPersona extends AuditableEntity {
    @EmbeddedId private DocumentoPersonaId id;
    @MapsId("tipoDocumentoId") @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_TIPO_DOCUMENTO", nullable = false) private TipoDocumento tipoDocumento;
    @MapsId("personaId") @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PERSONA", nullable = false) private Persona persona;
    @Column(name = "NO_DOCUMENTO", length = 50) private String numeroDocumento;
    protected DocumentoPersona() {}
    public static DocumentoPersona crear(TipoDocumento tipoDocumento, Persona persona,
            String numeroDocumento, String usuario, java.time.LocalDateTime ahora) {
        DocumentoPersona documento = new DocumentoPersona();
        documento.id = new DocumentoPersonaId(tipoDocumento.getId(), persona.getId());
        documento.tipoDocumento = tipoDocumento; documento.persona = persona;
        documento.numeroDocumento = numeroDocumento;
        documento.usuarioCreacion = usuario; documento.fechaCreacion = ahora;
        return documento;
    }
    public void modificarNumero(String numeroDocumento, String usuario,
            java.time.LocalDateTime ahora) {
        this.numeroDocumento = numeroDocumento;
        usuarioModificacion = usuario; fechaModificacion = ahora;
    }
    public DocumentoPersonaId getId() { return id; }
    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public Persona getPersona() { return persona; }
    public String getNumeroDocumento() { return numeroDocumento; }
}
