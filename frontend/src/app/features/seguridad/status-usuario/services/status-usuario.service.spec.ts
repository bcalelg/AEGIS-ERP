import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { StatusUsuarioService } from './status-usuario.service';

describe('StatusUsuarioService', () => {
  let service: StatusUsuarioService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(StatusUsuarioService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista, obtiene y elimina en sus endpoints', () => {
    service.list().subscribe();
    let request = http.expectOne('/api/security/status-usuarios');
    expect(request.request.method).toBe('GET');
    request.flush([]);

    service.get(4).subscribe();
    request = http.expectOne('/api/security/status-usuarios/4');
    expect(request.request.method).toBe('GET');
    request.flush({ id: 4, nombre: 'Activo' });

    service.delete(4).subscribe();
    request = http.expectOne('/api/security/status-usuarios/4');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('crea y actualiza estatus', () => {
    service.create({ nombre: 'Temporal' }).subscribe();
    let request = http.expectOne('/api/security/status-usuarios');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ nombre: 'Temporal' });
    request.flush({ id: 1, nombre: 'Temporal' });

    service.update(1, { nombre: 'Actualizado' }).subscribe();
    request = http.expectOne('/api/security/status-usuarios/1');
    expect(request.request.method).toBe('PUT');
    request.flush({ id: 1, nombre: 'Actualizado' });
  });

  it('envía el filtro al imprimir', () => {
    service.print(' Act ').subscribe();
    const request = http.expectOne('/api/security/status-usuarios/print?search=Act');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it.each([
    ['exportExcel', 'excel'],
    ['exportPdf', 'pdf'],
    ['exportCsv', 'csv'],
  ] as const)('exporta %s como blob respetando el filtro', (method, format) => {
    service[method](' Act ').subscribe();
    const request = http.expectOne(`/api/security/status-usuarios/export/${format}?search=Act`);
    expect(request.request.responseType).toBe('blob');
    request.flush(new Blob());
  });
});
