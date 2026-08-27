import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { UsuarioService } from './usuario.service';

describe('UsuarioService', () => {
  it('usa endpoints seguros y filtra sucursales por empresa', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const service = TestBed.inject(UsuarioService);
    const http = TestBed.inject(HttpTestingController);
    service.sucursalOptions(7).subscribe();
    expect(http.expectOne((request) => request.url.endsWith('/options/sucursales') && request.params.get('idEmpresa') === '7').request.method).toBe('GET');
    service.delete('USUARIO PRUEBA').subscribe();
    expect(http.expectOne((request) => request.url.endsWith('/USUARIO%20PRUEBA')).request.method).toBe('DELETE');
    http.verify();
  });
});
