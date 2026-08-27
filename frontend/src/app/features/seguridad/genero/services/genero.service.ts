import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { Genero, GeneroRequest } from '../models/genero.models';

@Injectable({ providedIn: 'root' })
export class GeneroService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/security/generos`;

  list() {
    return this.http.get<Genero[]>(this.url);
  }

  get(id: number) {
    return this.http.get<Genero>(`${this.url}/${id}`);
  }

  create(request: GeneroRequest) {
    return this.http.post<Genero>(this.url, request);
  }

  update(id: number, request: GeneroRequest) {
    return this.http.put<Genero>(`${this.url}/${id}`, request);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  print() {
    return this.http.get<Genero[]>(`${this.url}/print`);
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
    let params = new HttpParams();
    if (search.trim()) params = params.set('search', search.trim());
    return this.http.get(`${this.url}/export/${format}`, { params, responseType: 'blob' });
  }
}
