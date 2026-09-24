import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CalculoPlanillaService } from './calculo-planilla.service';

describe('CalculoPlanillaService',()=>{
 let service:CalculoPlanillaService;let http:HttpTestingController;
 beforeEach(()=>{TestBed.configureTestingModule({providers:[provideHttpClient(),provideHttpClientTesting()]});service=TestBed.inject(CalculoPlanillaService);http=TestBed.inject(HttpTestingController);});
 afterEach(()=>http.verify());
 it('consulta períodos sin realizar DML',()=>{service.listar().subscribe();const r=http.expectOne('/api/planilla/calculo/periodos');expect(r.request.method).toBe('GET');r.flush([]);});
 it('consulta la PK compuesta seleccionada',()=>{service.obtener(2026,9).subscribe(x=>expect(x.planillaRegistrada).toBeFalsy());const r=http.expectOne('/api/planilla/calculo/periodos/2026/9');expect(r.request.method).toBe('GET');r.flush({anio:2026,mes:9,fechaInicio:'2026-09-01',fechaFin:'2026-09-30',planillaRegistrada:false});});
});
