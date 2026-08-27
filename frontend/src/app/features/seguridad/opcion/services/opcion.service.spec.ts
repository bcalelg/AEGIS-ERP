import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { OpcionService } from './opcion.service';

describe('OpcionService', () => {
  let service: OpcionService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(OpcionService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('usa CRUD y selector ligero sin llamar al endpoint dinámico', () => {
    service.list().subscribe();
    http.expectOne('/api/security/opciones').flush([]);
    service.menuOptions().subscribe();
    http.expectOne('/api/security/opciones/options/menus').flush([]);
    const body = { idMenu: 1, nombre: 'Empresas', pagina: 'empresa.php', orden: 1 };
    service.create(body).subscribe();
    let request = http.expectOne('/api/security/opciones');
    expect(request.request.method).toBe('POST');
    request.flush({});
    service.update(2, { idMenu: 1, nombre: 'Administración de Empresas', orden: 2 }).subscribe();
    request = http.expectOne('/api/security/opciones/2');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({
      idMenu: 1,
      nombre: 'Administración de Empresas',
      orden: 2,
    });
    expect(request.request.body).not.toHaveProperty('pagina');
    request.flush({});
    service.delete(2).subscribe();
    request = http.expectOne('/api/security/opciones/2');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('envía filtro a impresión y exportaciones', () => {
    service.print(' Empresa ').subscribe();
    http.expectOne('/api/security/opciones/print?search=Empresa').flush([]);
    service.exportExcel(' Empresa ').subscribe();
    http.expectOne('/api/security/opciones/export/excel?search=Empresa').flush(new Blob());
    service.exportPdf(' Empresa ').subscribe();
    http.expectOne('/api/security/opciones/export/pdf?search=Empresa').flush(new Blob());
    service.exportCsv(' Empresa ').subscribe();
    http.expectOne('/api/security/opciones/export/csv?search=Empresa').flush(new Blob());
  });
});
