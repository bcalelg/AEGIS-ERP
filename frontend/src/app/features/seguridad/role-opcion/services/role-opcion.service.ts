import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../../../environments/environment';
import {
  CatalogOption,
  RoleOpcionMatrixItem,
  RoleOpcionSaveRequest,
} from '../models/role-opcion.models';

@Injectable({ providedIn: 'root' })
export class RoleOpcionService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/security/role-opciones`;

  roles() {
    return this.http.get<CatalogOption[]>(`${this.url}/options/roles`);
  }

  modulos() {
    return this.http.get<CatalogOption[]>(`${this.url}/options/modulos`);
  }

  matrix(roleId: number, moduloId: number) {
    const params = new HttpParams().set('roleId', roleId).set('moduloId', moduloId);
    return this.http.get<RoleOpcionMatrixItem[]>(this.url, { params });
  }

  save(request: RoleOpcionSaveRequest) {
    return this.http.put<RoleOpcionMatrixItem[]>(this.url, request);
  }
}
