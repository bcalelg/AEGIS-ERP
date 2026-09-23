import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs';
import { ConfirmationService } from '../../../core/confirmation/confirmation.service';
import { NotificationService } from '../../../core/notifications/notification.service';
import { PermissionService } from '../../../core/services/permission.service';
import { downloadFile } from '../../../core/utils/download-file';
import { PlanillaCatalogItem, PlanillaCatalogService } from './planilla-catalog.service';

interface Config { catalog:string; page:string; title:string; description:string; relationCatalog?:string; relationLabel?:string; flow?:boolean; }
@Component({selector:'app-planilla-catalog',imports:[FormsModule,ReactiveFormsModule],templateUrl:'./planilla-catalog.component.html',styleUrl:'./planilla-catalog.component.css',changeDetection:ChangeDetectionStrategy.OnPush})
export class PlanillaCatalogComponent implements OnInit {
  private readonly service=inject(PlanillaCatalogService); private readonly route=inject(ActivatedRoute);
  private readonly permission=inject(PermissionService); private readonly notifications=inject(NotificationService);
  private readonly confirmation=inject(ConfirmationService); private readonly fb=inject(FormBuilder);
  readonly config=this.route.snapshot.data as Config; readonly permissions=signal(this.permission.forPage(this.config.page));
  readonly items=signal<PlanillaCatalogItem[]>([]); readonly relations=signal<PlanillaCatalogItem[]>([]); readonly statusOptions=signal<PlanillaCatalogItem[]>([]);
  readonly search=signal(''); readonly loading=signal(false); readonly formOpen=signal(false); readonly editing=signal<PlanillaCatalogItem|null>(null);
  readonly filtered=computed(()=>{const q=this.search().trim().toLowerCase();return q?this.items().filter(x=>`${x.nombre} ${x.relacionNombre??''} ${x.statusNuevoNombre??''}`.toLowerCase().includes(q)):this.items();});
  readonly form=this.fb.group({nombre:['',[Validators.required,Validators.maxLength(50)]],relacionId:[null as number|null],statusNuevoId:[null as number|null]});
  ngOnInit(){this.load();if(this.config.relationCatalog)this.service.options(this.config.relationCatalog).subscribe(x=>this.relations.set(x));if(this.config.flow)this.service.options('status-empleados').subscribe(x=>this.statusOptions.set(x));}
  load(){this.loading.set(true);this.service.list(this.config.catalog).pipe(finalize(()=>this.loading.set(false))).subscribe({next:x=>this.items.set(x),error:e=>this.notifications.operationError(e,'No fue posible cargar el catálogo.')});}
  open(item?:PlanillaCatalogItem){this.editing.set(item??null);this.form.reset({nombre:item?.nombre??'',relacionId:item?.relacionId??null,statusNuevoId:item?.statusNuevoId??null});this.formOpen.set(true);}
  close(){this.formOpen.set(false);this.editing.set(null);}
  save(){if(this.form.invalid){this.form.markAllAsTouched();return;}const value=this.form.getRawValue();if((this.config.relationCatalog||this.config.flow)&&!value.relacionId){this.notifications.warning('Debe seleccionar el registro relacionado.');return;}if(this.config.flow&&!value.statusNuevoId){this.notifications.warning('Debe seleccionar el status nuevo.');return;}const req={nombre:value.nombre!.trim(),relacionId:value.relacionId,statusNuevoId:value.statusNuevoId};const current=this.editing();const call=current?this.service.update(this.config.catalog,current.id,req):this.service.create(this.config.catalog,req);call.subscribe({next:()=>{this.notifications.success(current?'Registro actualizado correctamente.':'Registro creado correctamente.');this.close();this.load();},error:e=>this.notifications.operationError(e,'No fue posible guardar el registro.')});}
  async remove(item:PlanillaCatalogItem){if(!await this.confirmation.confirm({title:'Eliminar registro',message:`¿Desea eliminar "${item.nombre}"?`,warningText:'La operación será rechazada si existe información asociada.'}))return;this.service.delete(this.config.catalog,item.id).subscribe({next:()=>{this.confirmation.complete();this.notifications.success('Registro eliminado correctamente.');this.load();},error:e=>{this.confirmation.complete();this.notifications.operationError(e,'No se puede eliminar el registro porque tiene información asociada.');}});}
  print(){this.service.print(this.config.catalog).subscribe({next:x=>{this.items.set(x);setTimeout(()=>window.print());},error:e=>this.notifications.operationError(e,'No fue posible preparar la impresión.')});}
  export(format:'excel'|'pdf'){this.service.export(this.config.catalog,format,this.search()).subscribe({next:b=>downloadFile(b,`${this.config.catalog}.${format==='excel'?'xlsx':'pdf'}`),error:e=>this.notifications.operationError(e,'No fue posible exportar.')});}
}
