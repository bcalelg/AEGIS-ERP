import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MenuMaintenanceService } from './menu-maintenance.service';

describe('MenuMaintenanceService', () => {
  let service: MenuMaintenanceService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(MenuMaintenanceService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('usa el CRUD plural sin interferir con /api/security/menu', () => {
    service.list().subscribe();
    http.expectOne('/api/security/menus').flush([]);
    service.moduloOptions().subscribe();
    http.expectOne('/api/security/menus/options/modulos').flush([]);
    service.get(1).subscribe();
    http.expectOne('/api/security/menus/1').flush({});
    const body = { idModulo: 2, nombre: 'Parámetros', orden: 1 };
    service.create(body).subscribe();
    let request = http.expectOne('/api/security/menus');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(body);
    request.flush({});
    service.update(1, body).subscribe();
    request = http.expectOne('/api/security/menus/1');
    expect(request.request.method).toBe('PUT');
    request.flush({});
    service.delete(1).subscribe();
    request = http.expectOne('/api/security/menus/1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('envía el filtro a impresión y Excel, PDF y CSV', () => {
    service.print(' Seguridad ').subscribe();
    http.expectOne('/api/security/menus/print?search=Seguridad').flush([]);
    service.exportExcel(' Seguridad ').subscribe();
    http.expectOne('/api/security/menus/export/excel?search=Seguridad').flush(new Blob());
    service.exportPdf(' Seguridad ').subscribe();
    http.expectOne('/api/security/menus/export/pdf?search=Seguridad').flush(new Blob());
    service.exportCsv(' Seguridad ').subscribe();
    http.expectOne('/api/security/menus/export/csv?search=Seguridad').flush(new Blob());
  });
});
