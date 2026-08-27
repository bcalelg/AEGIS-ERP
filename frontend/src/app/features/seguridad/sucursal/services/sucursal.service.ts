import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../../../environments/environment';
import { EmpresaOption, Sucursal, SucursalRequest } from '../models/sucursal.models';

@Injectable({ providedIn: 'root' })
export class SucursalService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/security/sucursales`;

  list() {
    return this.http.get<Sucursal[]>(this.url);
  }

  get(id: number) {
    return this.http.get<Sucursal>(`${this.url}/${id}`);
  }

  empresaOptions() {
    return this.http.get<EmpresaOption[]>(`${this.url}/options/empresas`);
  }

  create(request: SucursalRequest) {
    return this.http.post<Sucursal>(this.url, request);
  }

  update(id: number, request: SucursalRequest) {
    return this.http.put<Sucursal>(`${this.url}/${id}`, request);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  print(search: string) {
    return this.http.get<Sucursal[]>(`${this.url}/print`, { params: this.searchParams(search) });
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
    const normalized = search.trim();
    return normalized ? new HttpParams().set('search', normalized) : new HttpParams();
  }
}
