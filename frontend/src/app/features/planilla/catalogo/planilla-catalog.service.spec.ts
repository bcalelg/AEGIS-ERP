import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PlanillaCatalogService } from './planilla-catalog.service';

describe('PlanillaCatalogService',()=>{
  let service:PlanillaCatalogService;let http:HttpTestingController;
  beforeEach(()=>{TestBed.configureTestingModule({providers:[provideHttpClient(),provideHttpClientTesting()]});service=TestBed.inject(PlanillaCatalogService);http=TestBed.inject(HttpTestingController);});
  afterEach(()=>http.verify());
  it('uses the selected catalog for CRUD',()=>{service.list('bancos').subscribe();const a=http.expectOne('/api/planilla/catalogos/bancos');expect(a.request.method).toBe('GET');a.flush([]);service.create('bancos',{nombre:'Banco',relacionId:null,statusNuevoId:null}).subscribe();const b=http.expectOne('/api/planilla/catalogos/bancos');expect(b.request.method).toBe('POST');b.flush({});});
  it('sends a composite flow identifier on update and delete',()=>{const body={nombre:'Evento',relacionId:1,statusNuevoId:2};service.update('flujos-status-empleado','1-2',body).subscribe();const a=http.expectOne('/api/planilla/catalogos/flujos-status-empleado/1-2');expect(a.request.method).toBe('PUT');a.flush({});service.delete('flujos-status-empleado','1-2').subscribe();const b=http.expectOne('/api/planilla/catalogos/flujos-status-empleado/1-2');expect(b.request.method).toBe('DELETE');b.flush(null);});
});
