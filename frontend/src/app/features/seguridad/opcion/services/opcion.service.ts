import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../../../environments/environment';
import {
  MenuOption,
  OpcionCreateRequest,
  OpcionMaintenance,
  OpcionUpdateRequest,
} from '../models/opcion.models';

@Injectable({ providedIn: 'root' })
export class OpcionService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/security/opciones`;

  list() {
    return this.http.get<OpcionMaintenance[]>(this.url);
  }

  get(id: number) {
    return this.http.get<OpcionMaintenance>(`${this.url}/${id}`);
  }

  menuOptions() {
    return this.http.get<MenuOption[]>(`${this.url}/options/menus`);
  }

  create(request: OpcionCreateRequest) {
    return this.http.post<OpcionMaintenance>(this.url, request);
  }

  update(id: number, request: OpcionUpdateRequest) {
    return this.http.put<OpcionMaintenance>(`${this.url}/${id}`, request);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  print(search: string) {
    return this.http.get<OpcionMaintenance[]>(`${this.url}/print`, {
      params: this.searchParams(search),
    });
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
