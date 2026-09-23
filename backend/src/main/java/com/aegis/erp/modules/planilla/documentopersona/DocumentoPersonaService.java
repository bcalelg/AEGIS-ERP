package com.aegis.erp.modules.planilla.documentopersona;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.planilla.entity.*;
import com.aegis.erp.modules.planilla.persona.PersonaOptionResponse;
import com.aegis.erp.modules.planilla.repository.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentoPersonaService {
    private final DocumentoPersonaRepository documentos; private final PersonaRepository personas;
    private final TipoDocumentoRepository tipos; private final DocumentExportService exports; private final Clock clock;
    public DocumentoPersonaService(DocumentoPersonaRepository documentos,PersonaRepository personas,
            TipoDocumentoRepository tipos,DocumentExportService exports,Clock clock){this.documentos=documentos;this.personas=personas;this.tipos=tipos;this.exports=exports;this.clock=clock;}
    @Transactional(readOnly=true) public List<DocumentoPersonaResponse> listar(String search){String q=search==null?"":search.trim().toLowerCase(Locale.ROOT);return documentos.findAll().stream().map(this::response).filter(x->q.isEmpty()||searchable(x).contains(q)).toList();}
    @Transactional public DocumentoPersonaResponse crear(DocumentoPersonaRequest request,String user){var id=id(request);if(documentos.existsById(id))throw new BusinessConflictException("La persona ya tiene registrado ese tipo de documento.");var entity=DocumentoPersona.crear(tipo(request.tipoDocumentoId()),persona(request.personaId()),optional(request.numeroDocumento()),user,LocalDateTime.now(clock));try{return response(documentos.saveAndFlush(entity));}catch(DataIntegrityViolationException ex){throw new BusinessConflictException("La combinación de persona y tipo de documento ya existe o no es válida.");}}
    @Transactional public DocumentoPersonaResponse modificar(Long tipoId,Long personaId,DocumentoPersonaRequest request,String user){var id=new DocumentoPersonaId(tipoId,personaId);var entity=find(id);if(!Objects.equals(tipoId,request.tipoDocumentoId())||!Objects.equals(personaId,request.personaId()))throw new BusinessConflictException("Persona y tipo de documento forman la clave y no pueden cambiarse; elimine el registro y cree uno nuevo.");entity.modificarNumero(optional(request.numeroDocumento()),user,LocalDateTime.now(clock));try{return response(documentos.saveAndFlush(entity));}catch(DataIntegrityViolationException ex){throw new BusinessConflictException("No fue posible actualizar el documento por una restricción de integridad.");}}
    @Transactional public void eliminar(Long tipoId,Long personaId){var entity=find(new DocumentoPersonaId(tipoId,personaId));try{documentos.delete(entity);documentos.flush();}catch(DataIntegrityViolationException ex){throw new BusinessConflictException("No se puede eliminar el documento porque tiene información asociada.");}}
    @Transactional(readOnly=true) public List<PersonaOptionResponse> personas(String search){String q=search==null?"":search.trim().toLowerCase(Locale.ROOT);return personas.findAll().stream().filter(x->q.isEmpty()||(x.getId()+" "+x.getNombre()+" "+x.getApellido()).toLowerCase(Locale.ROOT).contains(q)).limit(50).map(x->new PersonaOptionResponse(x.getId(),x.getNombre()+" "+x.getApellido())).toList();}
    @Transactional(readOnly=true) public List<PersonaOptionResponse> tipos(){return tipos.findAll().stream().map(x->new PersonaOptionResponse(x.getId(),x.getNombre())).toList();}
    @Transactional(readOnly=true) public byte[] excel(String search){return exports.excel("Documentos persona",headers(),rows(search));}
    @Transactional(readOnly=true) public byte[] pdf(String search){return exports.pdf("Documentos de persona",headers(),rows(search));}
    private List<String> headers(){return List.of("Persona","Tipo de documento","Número de documento");}
    private List<List<?>> rows(String search){return listar(search).stream().<List<?>>map(x->List.of(x.personaNombre(),x.tipoDocumentoNombre(),x.numeroDocumento()==null?"":x.numeroDocumento())).toList();}
    private DocumentoPersonaResponse response(DocumentoPersona x){return new DocumentoPersonaResponse(x.getId().tipoDocumentoId()+"-"+x.getId().personaId(),x.getTipoDocumento().getId(),x.getTipoDocumento().getNombre(),x.getPersona().getId(),x.getPersona().getNombre()+" "+x.getPersona().getApellido(),x.getNumeroDocumento());}
    private DocumentoPersona find(DocumentoPersonaId id){return documentos.findById(id).orElseThrow(()->new ResourceNotFoundException("Documento de persona no encontrado."));}
    private Persona persona(Long id){return personas.findById(id).orElseThrow(()->new ResourceNotFoundException("Persona no encontrada."));}
    private TipoDocumento tipo(Long id){return tipos.findById(id).orElseThrow(()->new ResourceNotFoundException("Tipo de documento no encontrado."));}
    private DocumentoPersonaId id(DocumentoPersonaRequest r){return new DocumentoPersonaId(r.tipoDocumentoId(),r.personaId());}
    private String optional(String value){return value==null||value.isBlank()?null:value.trim();}
    private String searchable(DocumentoPersonaResponse x){return (x.personaNombre()+" "+x.tipoDocumentoNombre()+" "+(x.numeroDocumento()==null?"":x.numeroDocumento())).toLowerCase(Locale.ROOT);}
}
