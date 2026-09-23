import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { ConfirmationService } from '../../../core/confirmation/confirmation.service';
import { NotificationService } from '../../../core/notifications/notification.service';
import { PermissionService } from '../../../core/services/permission.service';
import { downloadFile } from '../../../core/utils/download-file';
import { CatalogOption, Persona, PersonaRequest, PersonaService } from './persona.service';

@Component({selector:'app-persona',imports:[FormsModule,ReactiveFormsModule],templateUrl:'./persona.component.html',styleUrl:'../planilla-maintenance.css',changeDetection:ChangeDetectionStrategy.OnPush})
export class PersonaComponent implements OnInit {
 private readonly service=inject(PersonaService);private readonly permission=inject(PermissionService);private readonly fb=inject(FormBuilder);private readonly notices=inject(NotificationService);private readonly confirmation=inject(ConfirmationService);
 readonly permissions=signal(this.permission.forPage('personas'));readonly items=signal<Persona[]>([]);readonly generos=signal<CatalogOption[]>([]);readonly estados=signal<CatalogOption[]>([]);readonly search=signal('');readonly open=signal(false);readonly editing=signal<Persona|null>(null);readonly loading=signal(false);
 readonly form=this.fb.group({nombre:['',[Validators.required,Validators.maxLength(50)]],apellido:['',[Validators.required,Validators.maxLength(50)]],fechaNacimiento:['',Validators.required],generoId:[null as number|null,Validators.required],direccion:['',[Validators.required,Validators.maxLength(100)]],telefono:['',[Validators.required,Validators.maxLength(50)]],correoElectronico:['',[Validators.email,Validators.maxLength(50)]],estadoCivilId:[null as number|null,Validators.required]});
 ngOnInit(){forkJoin({generos:this.service.options('generos'),estados:this.service.options('estados-civiles')}).subscribe({next:x=>{this.generos.set(x.generos);this.estados.set(x.estados);},error:e=>this.notices.operationError(e,'No fue posible cargar los catálogos.')});this.load();}
 load(){this.loading.set(true);this.service.list(this.search()).pipe(finalize(()=>this.loading.set(false))).subscribe({next:x=>this.items.set(x),error:e=>this.notices.operationError(e,'No fue posible cargar las personas.')});}
 edit(item?:Persona){this.editing.set(item??null);this.form.reset(item?{nombre:item.nombre,apellido:item.apellido,fechaNacimiento:item.fechaNacimiento,generoId:item.generoId,direccion:item.direccion,telefono:item.telefono,correoElectronico:item.correoElectronico??'',estadoCivilId:item.estadoCivilId}:{nombre:'',apellido:'',fechaNacimiento:'',generoId:null,direccion:'',telefono:'',correoElectronico:'',estadoCivilId:null});this.open.set(true);}
 close(){this.open.set(false);this.editing.set(null);}
 save(){if(this.form.invalid){this.form.markAllAsTouched();return;}const v=this.form.getRawValue();const body:PersonaRequest={nombre:v.nombre!.trim(),apellido:v.apellido!.trim(),fechaNacimiento:v.fechaNacimiento!,generoId:v.generoId!,direccion:v.direccion!.trim(),telefono:v.telefono!.trim(),correoElectronico:v.correoElectronico?.trim()||null,estadoCivilId:v.estadoCivilId!};const current=this.editing();(current?this.service.update(current.id,body):this.service.create(body)).subscribe({next:()=>{this.notices.success(current?'Persona actualizada correctamente.':'Persona creada correctamente.');this.close();this.load();},error:e=>this.notices.operationError(e,'No fue posible guardar la persona.')});}
 async remove(x:Persona){if(!await this.confirmation.confirm({title:'Eliminar persona',message:`¿Desea eliminar a ${x.nombre} ${x.apellido}?`,warningText:'La operación será rechazada si tiene documentos, empleo u otra información asociada.'}))return;this.service.delete(x.id).subscribe({next:()=>{this.confirmation.complete();this.notices.success('Persona eliminada correctamente.');this.load();},error:e=>{this.confirmation.complete();this.notices.operationError(e,'No se puede eliminar la persona porque tiene información asociada.');}});}
 print(){this.service.print(this.search()).subscribe({next:x=>{this.items.set(x);setTimeout(()=>window.print());},error:e=>this.notices.operationError(e,'No fue posible preparar la impresión.')});}
 export(format:'excel'|'pdf'){this.service.export(format,this.search()).subscribe({next:b=>downloadFile(b,`personas.${format==='excel'?'xlsx':'pdf'}`),error:e=>this.notices.operationError(e,'No fue posible exportar.')});}
}
