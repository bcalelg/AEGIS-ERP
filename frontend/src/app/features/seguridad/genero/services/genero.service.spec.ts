import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { GeneroService } from './genero.service';

describe('GeneroService', () => {
  let service: GeneroService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(GeneroService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista géneros', () => {
    service.list().subscribe();
    const request = http.expectOne('/api/security/generos');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('crea un género', () => {
    service.create({ nombre: 'Temporal' }).subscribe();
    const request = http.expectOne('/api/security/generos');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ nombre: 'Temporal' });
    request.flush({ id: 1, nombre: 'Temporal' });
  });

  it('actualiza un género', () => {
    service.update(4, { nombre: 'Actualizado' }).subscribe();
    const request = http.expectOne('/api/security/generos/4');
    expect(request.request.method).toBe('PUT');
    request.flush({ id: 4, nombre: 'Actualizado' });
  });

  it('elimina un género', () => {
    service.delete(4).subscribe();
    const request = http.expectOne('/api/security/generos/4');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('obtiene el listado para impresión', () => {
    service.print().subscribe();
    const request = http.expectOne('/api/security/generos/print');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it.each([
    ['exportExcel', 'excel'],
    ['exportPdf', 'pdf'],
    ['exportCsv', 'csv'],
  ] as const)('usa el endpoint correcto para %s y respeta el filtro', (method, format) => {
    service[method](' Fem ').subscribe();
    const request = http.expectOne(`/api/security/generos/export/${format}?search=Fem`);
    expect(request.request.method).toBe('GET');
    expect(request.request.responseType).toBe('blob');
    request.flush(new Blob());
  });
});
