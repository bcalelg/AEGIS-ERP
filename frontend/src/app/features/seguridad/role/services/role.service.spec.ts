import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { RoleService } from './role.service';

describe('RoleService', () => {
  let service: RoleService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(RoleService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('usa las URLs CRUD correctas', () => {
    service.list().subscribe();
    let request = http.expectOne('/api/security/roles');
    expect(request.request.method).toBe('GET');
    request.flush([]);
    service.get(2).subscribe();
    request = http.expectOne('/api/security/roles/2');
    expect(request.request.method).toBe('GET');
    request.flush({ id: 2, nombre: 'Temporal' });
    service.create({ nombre: 'Temporal' }).subscribe();
    request = http.expectOne('/api/security/roles');
    expect(request.request.method).toBe('POST');
    request.flush({ id: 2, nombre: 'Temporal' });
    service.update(2, { nombre: 'Nuevo' }).subscribe();
    request = http.expectOne('/api/security/roles/2');
    expect(request.request.method).toBe('PUT');
    request.flush({ id: 2, nombre: 'Nuevo' });
    service.delete(2).subscribe();
    request = http.expectOne('/api/security/roles/2');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('envía el filtro en impresión y exportaciones', () => {
    service.print(' Admin ').subscribe();
    http.expectOne('/api/security/roles/print?search=Admin').flush([]);
    for (const format of ['csv', 'excel', 'pdf'] as const) {
      service[`export${format === 'csv' ? 'Csv' : format === 'excel' ? 'Excel' : 'Pdf'}`](
        ' Admin ',
      ).subscribe();
      const request = http.expectOne(`/api/security/roles/export/${format}?search=Admin`);
      expect(request.request.responseType).toBe('blob');
      request.flush(new Blob());
    }
  });
});
