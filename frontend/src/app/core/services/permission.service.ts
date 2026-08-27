import { inject, Injectable } from '@angular/core';
import { Permisos } from '../models/menu.models';
import { normalizePageIdentifier } from '../routing/option-route';
import { MenuService } from './menu.service';

@Injectable({ providedIn: 'root' })
export class PermissionService {
  private readonly menu = inject(MenuService);

  forPage(page: string): Permisos | null {
    const logicalPage = normalizePageIdentifier(page);
    for (const module of this.menu.modules()) {
      for (const group of module.menus) {
        const option = group.opciones.find(
          (item) => normalizePageIdentifier(item.pagina) === logicalPage,
        );
        if (option) return option.permisos;
      }
    }
    return null;
  }
}
