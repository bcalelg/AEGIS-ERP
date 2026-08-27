import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { RoleOpcionService } from './role-opcion.service';

describe('RoleOpcionService', () => {
  let service: RoleOpcionService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(RoleOpcionService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('usa catálogos ligeros y consulta la matriz por rol y módulo', () => {
    service.roles().subscribe();
    http.expectOne('/api/security/role-opciones/options/roles').flush([]);
    service.modulos().subscribe();
    http.expectOne('/api/security/role-opciones/options/modulos').flush([]);
    service.matrix(2, 1).subscribe();
    http.expectOne('/api/security/role-opciones?roleId=2&moduloId=1').flush([]);
  });

  it('guarda toda la matriz en una sola petición', () => {
    const request = { idRole: 2, idModulo: 1, opciones: [] };
    service.save(request).subscribe();
    const call = http.expectOne('/api/security/role-opciones');
    expect(call.request.method).toBe('PUT');
    expect(call.request.body).toEqual(request);
    call.flush([]);
  });
});
