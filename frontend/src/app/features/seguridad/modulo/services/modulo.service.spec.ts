import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ModuloService } from './modulo.service';

describe('ModuloService', () => {
  let service: ModuloService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ModuloService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('usa las URLs CRUD y conserva Nombre y Orden', () => {
    service.list().subscribe();
    let request = http.expectOne('/api/security/modulos');
    expect(request.request.method).toBe('GET');
    request.flush([]);
    service.get(2).subscribe();
    request = http.expectOne('/api/security/modulos/2');
    expect(request.request.method).toBe('GET');
    request.flush({ id: 2, nombre: 'Inventario', orden: 2 });
    service.create({ nombre: 'Inventario', orden: 2 }).subscribe();
    request = http.expectOne('/api/security/modulos');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ nombre: 'Inventario', orden: 2 });
    request.flush({ id: 2, nombre: 'Inventario', orden: 2 });
    service.update(2, { nombre: 'Ventas', orden: 3 }).subscribe();
    request = http.expectOne('/api/security/modulos/2');
    expect(request.request.method).toBe('PUT');
    request.flush({ id: 2, nombre: 'Ventas', orden: 3 });
    service.delete(2).subscribe();
    request = http.expectOne('/api/security/modulos/2');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('envía el filtro en impresión, CSV, Excel y PDF', () => {
    service.print(' Seg ').subscribe();
    http.expectOne('/api/security/modulos/print?search=Seg').flush([]);
    service.exportCsv(' Seg ').subscribe();
    http.expectOne('/api/security/modulos/export/csv?search=Seg').flush(new Blob());
    service.exportExcel(' Seg ').subscribe();
    http.expectOne('/api/security/modulos/export/excel?search=Seg').flush(new Blob());
    service.exportPdf(' Seg ').subscribe();
    http.expectOne('/api/security/modulos/export/pdf?search=Seg').flush(new Blob());
  });
});
