import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { ConfirmationService } from '../../../core/confirmation/confirmation.service';
import { NotificationService } from '../../../core/notifications/notification.service';
import { PermissionService } from '../../../core/services/permission.service';
import { downloadFile } from '../../../core/utils/download-file';
import { CatalogOption } from '../persona/persona.service';
import { DocumentoPersona, DocumentoPersonaRequest, DocumentoPersonaService } from './documento-persona.service';
@Component({selector:'app-documento-persona',imports:[FormsModule,ReactiveFormsModule],templateUrl:'./documento-persona.component.html',styleUrl:'../planilla-maintenance.css',changeDetection:ChangeDetectionStrategy.OnPush})
export class DocumentoPersonaComponent implements OnInit {
 private readonly service=inject(DocumentoPersonaService);private readonly permission=inject(PermissionService);private readonly fb=inject(FormBuilder);private readonly notices=inject(NotificationService);private readonly confirmation=inject(ConfirmationService);
 readonly permissions=signal(this.permission.forPage('documento_persona'));readonly items=signal<DocumentoPersona[]>([]);readonly personas=signal<CatalogOption[]>([]);readonly tipos=signal<CatalogOption[]>([]);readonly search=signal('');readonly personSearch=signal('');readonly open=signal(false);readonly editing=signal<DocumentoPersona|null>(null);readonly loading=signal(false);
 readonly form=this.fb.group({personaId:[null as number|null,Validators.required],tipoDocumentoId:[null as number|null,Validators.required],numeroDocumento:['',Validators.maxLength(50)]});
 ngOnInit(){this.service.tipos().subscribe({next:x=>this.tipos.set(x),error:e=>this.notices.operationError(e,'No fue posible cargar los tipos de documento.')});this.findPeople();this.load();}
 load(){this.loading.set(true);this.service.list(this.search()).pipe(finalize(()=>this.loading.set(false))).subscribe({next:x=>this.items.set(x),error:e=>this.notices.operationError(e,'No fue posible cargar los documentos.')});}
 findPeople(){this.service.personas(this.personSearch()).subscribe({next:x=>this.personas.set(x),error:e=>this.notices.operationError(e,'No fue posible buscar personas.')});}
 edit(x?:DocumentoPersona){this.editing.set(x??null);if(x&&!this.personas().some(p=>p.id===x.personaId))this.personas.update(p=>[{id:x.personaId,nombre:x.personaNombre},...p]);this.form.reset({personaId:x?.personaId??null,tipoDocumentoId:x?.tipoDocumentoId??null,numeroDocumento:x?.numeroDocumento??''});if(x){this.form.controls.personaId.disable();this.form.controls.tipoDocumentoId.disable();}else{this.form.controls.personaId.enable();this.form.controls.tipoDocumentoId.enable();}this.open.set(true);}
 close(){this.open.set(false);this.editing.set(null);}
 save(){if(this.form.invalid){this.form.markAllAsTouched();return;}const v=this.form.getRawValue();const body:DocumentoPersonaRequest={personaId:v.personaId!,tipoDocumentoId:v.tipoDocumentoId!,numeroDocumento:v.numeroDocumento?.trim()||null};const current=this.editing();(current?this.service.update(current.tipoDocumentoId,current.personaId,body):this.service.create(body)).subscribe({next:()=>{this.notices.success(current?'Documento actualizado correctamente.':'Documento creado correctamente.');this.close();this.load();},error:e=>this.notices.operationError(e,'No fue posible guardar el documento.')});}
 async remove(x:DocumentoPersona){if(!await this.confirmation.confirm({title:'Eliminar documento',message:`¿Desea eliminar ${x.tipoDocumentoNombre} de ${x.personaNombre}?`,warningText:'Esta acción no puede deshacerse.'}))return;this.service.delete(x.tipoDocumentoId,x.personaId).subscribe({next:()=>{this.confirmation.complete();this.notices.success('Documento eliminado correctamente.');this.load();},error:e=>{this.confirmation.complete();this.notices.operationError(e,'No fue posible eliminar el documento.');}});}
 print(){this.service.print(this.search()).subscribe({next:x=>{this.items.set(x);setTimeout(()=>window.print());},error:e=>this.notices.operationError(e,'No fue posible preparar la impresión.')});}
 export(format:'excel'|'pdf'){this.service.export(format,this.search()).subscribe({next:b=>downloadFile(b,`documentos-persona.${format==='excel'?'xlsx':'pdf'}`),error:e=>this.notices.operationError(e,'No fue posible exportar.')});}
}
