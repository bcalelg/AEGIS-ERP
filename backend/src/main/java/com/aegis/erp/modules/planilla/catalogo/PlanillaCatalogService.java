package com.aegis.erp.modules.planilla.catalogo;

import com.aegis.erp.common.exception.*;
import com.aegis.erp.common.export.DocumentExportService;
import com.aegis.erp.modules.planilla.entity.*;
import com.aegis.erp.modules.planilla.repository.*;
import com.aegis.erp.modules.seguridad.empresa.repository.EmpresaRepository;
import java.time.*;
import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanillaCatalogService {
    private final EstadoCivilRepository estados; private final StatusEmpleadoRepository status;
    private final TipoDocumentoRepository tipos; private final BancoRepository bancos;
    private final DepartamentoRepository departamentos; private final FlujoStatusEmpleadoRepository flujos;
    private final PuestoRepository puestos; private final EmpresaRepository empresas;
    private final Clock clock; private final DocumentExportService documents;
    public PlanillaCatalogService(EstadoCivilRepository a,StatusEmpleadoRepository b,TipoDocumentoRepository c,
            BancoRepository d,DepartamentoRepository e,FlujoStatusEmpleadoRepository f,PuestoRepository g,
            EmpresaRepository h,Clock clock,DocumentExportService documents){estados=a;status=b;tipos=c;bancos=d;departamentos=e;flujos=f;puestos=g;empresas=h;this.clock=clock;this.documents=documents;}

    @Transactional(readOnly=true) public List<PlanillaCatalogResponse> list(String c){return switch(c){
        case "estados-civiles"->estados.findAll().stream().map(x->simple(x.getId(),x.getNombre())).toList();
        case "status-empleados"->status.findAll().stream().map(x->simple(x.getId(),x.getNombre())).toList();
        case "tipos-documento"->tipos.findAll().stream().map(x->simple(x.getId(),x.getNombre())).toList();
        case "bancos"->bancos.findAll().stream().map(x->simple(x.getId(),x.getNombre())).toList();
        case "departamentos"->departamentos.findAll().stream().map(x->related(x.getId(),x.getNombre(),x.getEmpresa()==null?null:x.getEmpresa().getId(),x.getEmpresa()==null?null:x.getEmpresa().getNombre())).toList();
        case "puestos"->puestos.findAll().stream().map(x->related(x.getId(),x.getNombre(),x.getDepartamento().getId(),x.getDepartamento().getNombre())).toList();
        case "flujos-status-empleado"->flujos.findAll().stream().map(this::flow).toList();
        default->throw new ResourceNotFoundException("Catálogo de Planilla no encontrado.");};}
    @Transactional public PlanillaCatalogResponse create(String c,PlanillaCatalogRequest r,String user){String n=clean(r.nombre());LocalDateTime now=LocalDateTime.now(clock);rejectDuplicate(c,n,null);try{return switch(c){
        case "estados-civiles"->simple(estados.saveAndFlush(EstadoCivil.crear(n,user,now)).getId(),n);
        case "status-empleados"->simple(status.saveAndFlush(StatusEmpleado.crear(n,user,now)).getId(),n);
        case "tipos-documento"->simple(tipos.saveAndFlush(TipoDocumento.crear(n,user,now)).getId(),n);
        case "bancos"->simple(bancos.saveAndFlush(Banco.crear(n,user,now)).getId(),n);
        case "departamentos"->{var rel=empresas.findById(required(r.relacionId())).orElseThrow(()->new ResourceNotFoundException("Empresa no encontrada."));yield response(departamentos.saveAndFlush(Departamento.crear(n,rel,user,now)));}
        case "puestos"->{var rel=departamentos.findById(required(r.relacionId())).orElseThrow(()->new ResourceNotFoundException("Departamento no encontrado."));yield response(puestos.saveAndFlush(Puesto.crear(n,rel,user,now)));}
        case "flujos-status-empleado"->{var a=status.findById(required(r.relacionId())).orElseThrow(()->new ResourceNotFoundException("Status actual no encontrado."));var b=status.findById(required(r.statusNuevoId())).orElseThrow(()->new ResourceNotFoundException("Status nuevo no encontrado."));if(flujos.existsById(new FlujoStatusEmpleadoId(a.getId(),b.getId())))throw new BusinessConflictException("La transición indicada ya existe.");yield flow(flujos.saveAndFlush(FlujoStatusEmpleado.crear(a,b,n,user,now)));}
        default->throw new ResourceNotFoundException("Catálogo de Planilla no encontrado.");};}catch(DataIntegrityViolationException ex){throw new BusinessConflictException("Ya existe el registro o sus relaciones no son válidas.");}}
    @Transactional public PlanillaCatalogResponse update(String c,String id,PlanillaCatalogRequest r,String user){String n=clean(r.nombre());LocalDateTime now=LocalDateTime.now(clock);rejectDuplicate(c,n,id);try{return switch(c){
        case "estados-civiles"->{var x=estados.findById(longId(id)).orElseThrow(()->notFound());x.modificar(n,user,now);yield simple(estados.saveAndFlush(x).getId(),n);}
        case "status-empleados"->{var x=status.findById(longId(id)).orElseThrow(()->notFound());x.modificar(n,user,now);yield simple(status.saveAndFlush(x).getId(),n);}
        case "tipos-documento"->{var x=tipos.findById(longId(id)).orElseThrow(()->notFound());x.modificar(n,user,now);yield simple(tipos.saveAndFlush(x).getId(),n);}
        case "bancos"->{var x=bancos.findById(longId(id)).orElseThrow(()->notFound());x.modificar(n,user,now);yield simple(bancos.saveAndFlush(x).getId(),n);}
        case "departamentos"->{var x=departamentos.findById(longId(id)).orElseThrow(()->notFound());var rel=empresas.findById(required(r.relacionId())).orElseThrow(()->new ResourceNotFoundException("Empresa no encontrada."));x.modificar(n,rel,user,now);yield response(departamentos.saveAndFlush(x));}
        case "puestos"->{var x=puestos.findById(longId(id)).orElseThrow(()->notFound());var rel=departamentos.findById(required(r.relacionId())).orElseThrow(()->new ResourceNotFoundException("Departamento no encontrado."));x.modificar(n,rel,user,now);yield response(puestos.saveAndFlush(x));}
        case "flujos-status-empleado"->{var key=flowId(id);var x=flujos.findById(key).orElseThrow(()->notFound());if(!Objects.equals(key.statusActualId(),r.relacionId())||!Objects.equals(key.statusNuevoId(),r.statusNuevoId()))throw new BusinessConflictException("Los status de una transición existente no pueden cambiarse; elimínela y cree otra.");x.modificar(n,user,now);yield flow(flujos.saveAndFlush(x));}
        default->throw notFound();};}catch(DataIntegrityViolationException ex){throw new BusinessConflictException("No fue posible guardar por una restricción de integridad.");}}
    @Transactional public void delete(String c,String id){try{switch(c){case "estados-civiles"->estados.deleteById(longId(id));case "status-empleados"->status.deleteById(longId(id));case "tipos-documento"->tipos.deleteById(longId(id));case "bancos"->bancos.deleteById(longId(id));case "departamentos"->departamentos.deleteById(longId(id));case "puestos"->puestos.deleteById(longId(id));case "flujos-status-empleado"->flujos.deleteById(flowId(id));default->throw notFound();}flush(c);}catch(DataIntegrityViolationException ex){throw new BusinessConflictException("No se puede eliminar el registro porque tiene información asociada.");}}
    @Transactional(readOnly=true) public byte[] excel(String c,String search){return documents.excel(title(c),headers(c),rows(c,search));}
    @Transactional(readOnly=true) public byte[] pdf(String c,String search){return documents.pdf("Listado de "+title(c),headers(c),rows(c,search));}
    @Transactional(readOnly=true) public List<PlanillaCatalogResponse> options(String c){if(c.equals("empresas"))return empresas.findAll().stream().map(x->simple(x.getId(),x.getNombre())).toList();return list(c);}
    private List<List<?>> rows(String c,String search){String q=search==null?"":search.trim().toLowerCase(Locale.ROOT);return list(c).stream().filter(x->q.isEmpty()||x.nombre().toLowerCase(Locale.ROOT).contains(q)).<List<?>>map(x->List.of(x.id(),x.nombre(),x.relacionNombre()==null?"":x.relacionNombre(),x.statusNuevoNombre()==null?"":x.statusNuevoNombre())).toList();}
    private List<String> headers(String c){return List.of("ID","Nombre","Relacionado","Status nuevo");} private String title(String c){return c.replace('-',' ');}
    private void rejectDuplicate(String c,String n,String id){boolean found=list(c).stream().anyMatch(x->x.nombre().equalsIgnoreCase(n)&&!x.id().equals(id));if(found)throw new BusinessConflictException("Ya existe un registro con el nombre indicado.");}
    private void flush(String c){switch(c){case "estados-civiles"->estados.flush();case "status-empleados"->status.flush();case "tipos-documento"->tipos.flush();case "bancos"->bancos.flush();case "departamentos"->departamentos.flush();case "puestos"->puestos.flush();case "flujos-status-empleado"->flujos.flush();default->{}}}
    private PlanillaCatalogResponse response(Departamento x){return related(x.getId(),x.getNombre(),x.getEmpresa()==null?null:x.getEmpresa().getId(),x.getEmpresa()==null?null:x.getEmpresa().getNombre());} private PlanillaCatalogResponse response(Puesto x){return related(x.getId(),x.getNombre(),x.getDepartamento().getId(),x.getDepartamento().getNombre());}
    private PlanillaCatalogResponse flow(FlujoStatusEmpleado x){return new PlanillaCatalogResponse(x.getId().statusActualId()+"-"+x.getId().statusNuevoId(),x.getNombreEvento(),x.getStatusActual().getId(),x.getStatusActual().getNombre(),x.getStatusNuevo().getId(),x.getStatusNuevo().getNombre());}
    private PlanillaCatalogResponse simple(Long id,String n){return new PlanillaCatalogResponse(id.toString(),n,null,null,null,null);} private PlanillaCatalogResponse related(Long id,String n,Long r,String rn){return new PlanillaCatalogResponse(id.toString(),n,r,rn,null,null);}
    private String clean(String n){return n.trim();} private Long required(Long n){if(n==null)throw new BusinessConflictException("Debe seleccionar el registro relacionado.");return n;} private long longId(String id){try{return Long.parseLong(id);}catch(Exception e){throw notFound();}} private FlujoStatusEmpleadoId flowId(String id){try{var p=id.split("-");return new FlujoStatusEmpleadoId(Long.valueOf(p[0]),Long.valueOf(p[1]));}catch(Exception e){throw notFound();}} private ResourceNotFoundException notFound(){return new ResourceNotFoundException("Registro no encontrado.");}
}
