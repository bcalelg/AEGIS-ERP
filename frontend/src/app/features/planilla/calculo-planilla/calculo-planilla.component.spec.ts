import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { NotificationService } from '../../../core/notifications/notification.service';
import { CalculoPlanillaComponent } from './calculo-planilla.component';
import { CalculoPlanillaService, PeriodoCalculo } from './calculo-planilla.service';

describe('CalculoPlanillaComponent',()=>{
 const periodo:PeriodoCalculo={anio:2026,mes:9,fechaInicio:'2026-09-01',fechaFin:'2026-09-30',fechaCreacion:'2026-01-01T00:00:00',usuarioCreacion:'system',fechaModificacion:null,usuarioModificacion:null,planillaRegistrada:false};
 const service={listar:vi.fn(),obtener:vi.fn()};const notices={operationError:vi.fn()};
 beforeEach(async()=>{vi.clearAllMocks();service.listar.mockReturnValue(of([periodo]));service.obtener.mockReturnValue(of(periodo));await TestBed.configureTestingModule({imports:[CalculoPlanillaComponent],providers:[{provide:CalculoPlanillaService,useValue:service},{provide:NotificationService,useValue:notices}]}).compileComponents();});
 it('carga el selector con períodos existentes',()=>{const f=TestBed.createComponent(CalculoPlanillaComponent);f.detectChanges();expect(f.nativeElement.querySelectorAll('select option').length).toBe(2);expect(service.listar).toHaveBeenCalledOnce();});
 it('cambia período y muestra fechas e indicador no procesado',()=>{const f=TestBed.createComponent(CalculoPlanillaComponent);f.detectChanges();f.componentInstance.seleccionar('2026-9');f.detectChanges();expect(service.obtener).toHaveBeenCalledWith(2026,9);expect(f.nativeElement.textContent).toContain('2026-09-01');expect(f.nativeElement.textContent).toContain('todavía no tiene una planilla registrada');});
 it('mantiene deshabilitado el botón de cálculo',()=>{const f=TestBed.createComponent(CalculoPlanillaComponent);f.detectChanges();f.componentInstance.seleccionar('2026-9');f.detectChanges();expect((f.nativeElement.querySelector('button') as HTMLButtonElement).disabled).toBe(true);});
});
