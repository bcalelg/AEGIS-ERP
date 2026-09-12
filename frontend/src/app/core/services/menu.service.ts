import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Subject, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ModuloMenu } from '../models/menu.models';
@Injectable({ providedIn: 'root' })
export class MenuService {
  private readonly http = inject(HttpClient);
  private readonly refreshRequests = new Subject<void>();
  readonly modules = signal<ModuloMenu[]>([]);
  readonly refreshRequested$ = this.refreshRequests.asObservable();
  load() {
    return this.http
      .get<ModuloMenu[]>(`${environment.apiUrl}/security/menu`)
      .pipe(tap((items) => this.modules.set(this.sort(items))));
  }
  clear(): void {
    this.modules.set([]);
  }
  refresh(): void {
    this.refreshRequests.next();
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
