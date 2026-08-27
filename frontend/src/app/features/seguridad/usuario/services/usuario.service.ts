import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../../../environments/environment';
import {
  Usuario,
  UsuarioCreateRequest,
  UsuarioOption,
  UsuarioSummary,
  UsuarioUpdateRequest,
} from '../models/usuario.models';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/security/usuarios`;

  list() {
    return this.http.get<UsuarioSummary[]>(this.url);
  }

  get(id: string) {
    return this.http.get<Usuario>(`${this.url}/${encodeURIComponent(id)}`);
  }

  create(request: UsuarioCreateRequest) {
    return this.http.post<Usuario>(this.url, request);
  }

  update(id: string, request: UsuarioUpdateRequest) {
    return this.http.put<Usuario>(`${this.url}/${encodeURIComponent(id)}`, request);
  }

  delete(id: string) {
    return this.http.delete<void>(`${this.url}/${encodeURIComponent(id)}`);
  }

  empresaOptions() {
    return this.http.get<UsuarioOption[]>(`${this.url}/options/empresas`);
  }

  sucursalOptions(idEmpresa: number) {
    return this.http.get<UsuarioOption[]>(`${this.url}/options/sucursales`, {
      params: new HttpParams().set('idEmpresa', idEmpresa),
    });
  }

  generoOptions() {
    return this.http.get<UsuarioOption[]>(`${this.url}/options/generos`);
  }

  statusOptions() {
    return this.http.get<UsuarioOption[]>(`${this.url}/options/status`);
  }

  roleOptions() {
    return this.http.get<UsuarioOption[]>(`${this.url}/options/roles`);
  }

  print(search: string) {
    return this.http.get<UsuarioSummary[]>(`${this.url}/print`, { params: this.searchParams(search) });
  }

  exportCsv(search: string) {
    return this.exportFile('csv', search);
  }

  exportExcel(search: string) {
    return this.exportFile('excel', search);
  }

  exportPdf(search: string) {
    return this.exportFile('pdf', search);
  }

  private exportFile(format: 'csv' | 'excel' | 'pdf', search: string) {
    return this.http.get(`${this.url}/export/${format}`, {
      params: this.searchParams(search),
      responseType: 'blob',
    });
  }

  private searchParams(search: string): HttpParams {
    const value = search.trim();
    return value ? new HttpParams().set('search', value) : new HttpParams();
  }
}
