import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NEVER, of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { Permisos } from '../../../../core/models/menu.models';
import { NotificationService } from '../../../../core/notifications/notification.service';
import { PermissionService } from '../../../../core/services/permission.service';
import { ModuloService } from '../services/modulo.service';
import { ModuloListComponent } from './modulo-list.component';

describe('ModuloListComponent', () => {
  let fixture: ComponentFixture<ModuloListComponent>;
  let permissions: Permisos;
  const service = {
    list: vi.fn(() => of([{ id: 1, nombre: 'Seguridad', orden: 1 }])),
    create: vi.fn(() => NEVER),
    update: vi.fn(() => NEVER),
    delete: vi.fn(() => of(void 0)),
    print: vi.fn(() => of([{ id: 1, nombre: 'Seguridad', orden: 1 }])),
    exportExcel: vi.fn(() => NEVER),
    exportPdf: vi.fn(() => NEVER),
    exportCsv: vi.fn(() => NEVER),
  };
  const notification = { success: vi.fn(), operationError: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();
    vi.spyOn(window, 'print').mockImplementation(() => undefined);
    permissions = {
      consultar: true,
      alta: false,
      baja: false,
      cambio: false,
      imprimir: false,
      exportar: false,
    };
    await TestBed.configureTestingModule({
      imports: [ModuloListComponent],
      providers: [
        { provide: ModuloService, useValue: service },
        { provide: PermissionService, useValue: { forPage: () => permissions } },
        { provide: NotificationService, useValue: notification },
      ],
    }).compileComponents();
  });

  function render(): HTMLElement {
    fixture = TestBed.createComponent(ModuloListComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  function button(element: HTMLElement, text: string): HTMLButtonElement | undefined {
    return Array.from(element.querySelectorAll('button')).find((item) =>
      item.textContent?.includes(text),
    );
  }

  it('muestra Nuevo y Editar según permisos, pero nunca expone Eliminar', () => {
    expect(render().textContent).not.toContain('Nuevo módulo');
    fixture.destroy();
    permissions.alta = true;
    permissions.cambio = true;
    permissions.baja = true;
    const element = render();
    expect(element.textContent).not.toContain('Eliminar');
    button(element, 'Nuevo módulo')?.click();
    fixture.detectChanges();
    expect(element.textContent).toContain('Nuevo módulo');
    fixture.componentInstance.openEdit({ id: 1, nombre: 'Seguridad', orden: 1 });
    fixture.detectChanges();
    expect(element.textContent).toContain('Editar módulo');
    expect(fixture.componentInstance.editing()?.orden).toBe(1);
    fixture.componentInstance.closeForm();
    fixture.detectChanges();
    expect(button(element, 'Editar')).toBeDefined();
    expect(button(element, 'Eliminar')).toBeUndefined();
  });

  it('filtra por nombre y respeta IMPRIMIR y EXPORTAR', () => {
    permissions.imprimir = true;
    permissions.exportar = true;
    const element = render();
    fixture.componentInstance.search.set('inventario');
    expect(fixture.componentInstance.filteredItems()).toHaveLength(0);
    button(element, 'Imprimir')?.click();
    expect(service.print).toHaveBeenCalledWith('inventario');
    button(element, 'Exportar')?.click();
    fixture.detectChanges();
    button(element, 'Excel (.xlsx)')?.click();
    button(element, 'PDF (.pdf)')?.click();
    button(element, 'CSV (.csv)')?.click();
    expect(service.exportExcel).toHaveBeenCalledWith('inventario');
    expect(service.exportPdf).toHaveBeenCalledWith('inventario');
    expect(service.exportCsv).toHaveBeenCalledWith('inventario');
  });
});
