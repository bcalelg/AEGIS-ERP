import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SucursalService } from './sucursal.service';

describe('SucursalService', () => {
  let service: SucursalService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SucursalService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('usa endpoints CRUD y el catálogo ligero de empresas', () => {
    service.list().subscribe();
    http.expectOne('/api/security/sucursales').flush([]);
    service.empresaOptions().subscribe();
    http.expectOne('/api/security/sucursales/options/empresas').flush([]);
    service.get(1).subscribe();
    http.expectOne('/api/security/sucursales/1').flush({});
    const body = { idEmpresa: 2, nombre: 'Central', direccion: 'Guatemala' };
    service.create(body).subscribe();
    let request = http.expectOne('/api/security/sucursales');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(body);
    request.flush({});
    service.update(1, body).subscribe();
    request = http.expectOne('/api/security/sucursales/1');
    expect(request.request.method).toBe('PUT');
    request.flush({});
    service.delete(1).subscribe();
    request = http.expectOne('/api/security/sucursales/1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('envía búsqueda a impresión y exportaciones', () => {
    service.print(' Central ').subscribe();
    http.expectOne('/api/security/sucursales/print?search=Central').flush([]);
    service.exportCsv(' Central ').subscribe();
    http.expectOne('/api/security/sucursales/export/csv?search=Central').flush(new Blob());
    service.exportExcel(' Central ').subscribe();
    http.expectOne('/api/security/sucursales/export/excel?search=Central').flush(new Blob());
    service.exportPdf(' Central ').subscribe();
    http.expectOne('/api/security/sucursales/export/pdf?search=Central').flush(new Blob());
  });
});
