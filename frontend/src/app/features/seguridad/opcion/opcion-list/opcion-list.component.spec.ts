import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NEVER, of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { Permisos } from '../../../../core/models/menu.models';
import { NotificationService } from '../../../../core/notifications/notification.service';
import { PermissionService } from '../../../../core/services/permission.service';
import { OpcionService } from '../services/opcion.service';
import { OpcionListComponent } from './opcion-list.component';

describe('OpcionListComponent', () => {
  let fixture: ComponentFixture<OpcionListComponent>;
  let permissions: Permisos;
  const service = {
    list: vi.fn(() =>
      of([
        {
          id: 1,
          idMenu: 1,
          nombreModulo: 'Seguridad',
          nombreMenu: 'Catálogos',
          nombre: 'Empresas',
          pagina: 'empresa',
          orden: 1,
        },
      ]),
    ),
    delete: vi.fn(() => of(void 0)),
    print: vi.fn(() => of([])),
    exportExcel: vi.fn(() => NEVER),
    exportPdf: vi.fn(() => NEVER),
    exportCsv: vi.fn(() => NEVER),
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    permissions = {
      consultar: true,
      alta: false,
      baja: true,
      cambio: false,
      imprimir: false,
      exportar: false,
    };
    await TestBed.configureTestingModule({
      imports: [OpcionListComponent],
      providers: [
        { provide: OpcionService, useValue: service },
        { provide: PermissionService, useValue: { forPage: () => permissions } },
        { provide: NotificationService, useValue: { operationError: vi.fn() } },
      ],
    }).compileComponents();
  });

  function render(): HTMLElement {
    fixture = TestBed.createComponent(OpcionListComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('no expone Eliminar aun con BAJA y muestra Editar únicamente con CAMBIO', () => {
    let element = render();
    expect(element.textContent).not.toContain('Eliminar');
    expect(element.textContent).not.toContain('Editar');
    fixture.destroy();

    permissions.cambio = true;
    element = render();
    expect(element.textContent).toContain('Editar');
    expect(element.textContent).not.toContain('Eliminar');
  });

  it('respeta ALTA, IMPRIMIR y EXPORTAR', () => {
    let element = render();
    expect(element.textContent).not.toContain('Nueva opción');
    expect(element.textContent).not.toContain('Imprimir');
    expect(element.textContent).not.toContain('Exportar');
    fixture.destroy();

    permissions.alta = true;
    permissions.imprimir = true;
    permissions.exportar = true;
    element = render();
    expect(element.textContent).toContain('Nueva opción');
    expect(element.textContent).toContain('Imprimir');
    expect(element.textContent).toContain('Exportar');
  });
});
