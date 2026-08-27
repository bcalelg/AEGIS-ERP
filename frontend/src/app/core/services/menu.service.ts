import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ModuloMenu } from '../models/menu.models';
@Injectable({ providedIn: 'root' })
export class MenuService {
  private readonly http = inject(HttpClient);
  readonly modules = signal<ModuloMenu[]>([]);
  load() {
    return this.http
      .get<ModuloMenu[]>(`${environment.apiUrl}/security/menu`)
      .pipe(tap((items) => this.modules.set(this.sort(items))));
  }
  clear(): void {
    this.modules.set([]);
  }
  private sort(items: ModuloMenu[]): ModuloMenu[] {
    return [...items]
      .sort((a, b) => a.ordenMenu - b.ordenMenu)
      .map((module) => ({
        ...module,
        menus: [...module.menus]
          .sort((a, b) => a.ordenMenu - b.ordenMenu)
          .map((menu) => ({
            ...menu,
            opciones: [...menu.opciones].sort((a, b) => a.ordenMenu - b.ordenMenu),
          })),
      }));
  }
}
