import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { EmpresaService } from './empresa.service';
describe('EmpresaService', () => {
  let service: EmpresaService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(EmpresaService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('consume el listado paginado correcto', () => {
    service.list('Acme', 0).subscribe();
    const request = http.expectOne((r) => r.url === '/api/security/empresas');
    expect(request.request.params.get('search')).toBe('Acme');
    expect(request.request.params.get('page')).toBe('0');
    request.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
  });
  it('usa la API correcta para crear', () => {
    const value = {
      nombre: 'Acme',
      direccion: 'Guatemala',
      nit: 'NIT',
      passwordCantidadMayusculas: 1,
      passwordCantidadMinusculas: 1,
      passwordCantidadCaracteresEspeciales: 1,
      passwordCantidadCaducidadDias: 60,
      passwordLargo: 8,
      passwordIntentosAntesDeBloquear: 5,
      passwordCantidadNumeros: 1,
      passwordCantidadPreguntasValidar: 1,
    };
    service.create(value).subscribe();
    const request = http.expectOne('/api/security/empresas');
    expect(request.request.method).toBe('POST');
    request.flush({});
  });

  it.each([
    ['exportExcel', 'excel'],
    ['exportPdf', 'pdf'],
    ['exportCsv', 'csv'],
  ] as const)('usa el endpoint correcto para %s', (method, format) => {
    service[method](' Acme ').subscribe();
    const request = http.expectOne(`/api/security/empresas/export/${format}?search=Acme`);
    expect(request.request.method).toBe('GET');
    expect(request.request.responseType).toBe('blob');
    request.flush(new Blob());
  });
});
