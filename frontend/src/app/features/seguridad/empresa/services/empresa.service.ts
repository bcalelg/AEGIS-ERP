import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { Empresa, EmpresaRequest, PageResponse } from '../models/empresa.models';
@Injectable({ providedIn: 'root' })
export class EmpresaService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/security/empresas`;
  list(search: string, page: number, size = 10) {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'nombre,asc');
    if (search.trim()) params = params.set('search', search.trim());
    return this.http.get<PageResponse<Empresa>>(this.url, { params });
  }
  get(id: number) {
    return this.http.get<Empresa>(`${this.url}/${id}`);
  }
  create(value: EmpresaRequest) {
    return this.http.post<Empresa>(this.url, value);
  }
  update(id: number, value: EmpresaRequest) {
    return this.http.put<Empresa>(`${this.url}/${id}`, value);
  }
  delete(id: number) {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
  print(search: string) {
    let params = new HttpParams().set('page', 0).set('size', 100).set('sort', 'nombre,asc');
    if (search.trim()) params = params.set('search', search.trim());
    return this.http.get<PageResponse<Empresa>>(`${this.url}/print`, { params });
  }
  exportCsv(search: string) {
    let params = new HttpParams();
    if (search.trim()) params = params.set('search', search.trim());
    return this.http.get(`${this.url}/export/csv`, { params, responseType: 'blob' });
  }
  exportExcel(search: string) {
    let params = new HttpParams();
    if (search.trim()) params = params.set('search', search.trim());
    return this.http.get(`${this.url}/export/excel`, { params, responseType: 'blob' });
  }
  exportPdf(search: string) {
    let params = new HttpParams();
    if (search.trim()) params = params.set('search', search.trim());
    return this.http.get(`${this.url}/export/pdf`, { params, responseType: 'blob' });
  }
}
