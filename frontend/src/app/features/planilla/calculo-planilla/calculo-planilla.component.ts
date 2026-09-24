import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { NotificationService } from '../../../core/notifications/notification.service';
import { CalculoPlanillaService, PeriodoCalculo } from './calculo-planilla.service';

@Component({selector:'app-calculo-planilla',imports:[FormsModule],templateUrl:'./calculo-planilla.component.html',styleUrl:'../planilla-maintenance.css',changeDetection:ChangeDetectionStrategy.OnPush})
export class CalculoPlanillaComponent implements OnInit {
  private readonly service=inject(CalculoPlanillaService);private readonly notices=inject(NotificationService);
  readonly periodos=signal<PeriodoCalculo[]>([]);readonly seleccionado=signal<PeriodoCalculo|null>(null);readonly clave=signal('');readonly loading=signal(false);
  ngOnInit(){this.loading.set(true);this.service.listar().pipe(finalize(()=>this.loading.set(false))).subscribe({next:x=>this.periodos.set(x),error:e=>this.notices.operationError(e,'No fue posible cargar los períodos de planilla.')});}
  seleccionar(value:string){this.clave.set(value);if(!value){this.seleccionado.set(null);return;}const [anio,mes]=value.split('-').map(Number);this.service.obtener(anio,mes).subscribe({next:x=>this.seleccionado.set(x),error:e=>{this.seleccionado.set(null);this.notices.operationError(e,'No fue posible consultar el período.');}});}
  nombreMes(mes:number){return new Intl.DateTimeFormat('es-GT',{month:'long'}).format(new Date(2026,mes-1,1));}
}
