import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NEVER, of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { Permisos } from '../../../../core/models/menu.models';
import { ConfirmationService } from '../../../../core/confirmation/confirmation.service';
import { NotificationService } from '../../../../core/notifications/notification.service';
import { PermissionService } from '../../../../core/services/permission.service';
import { RoleService } from '../services/role.service';
import { RoleListComponent } from './role-list.component';

describe('RoleListComponent', () => {
  let fixture: ComponentFixture<RoleListComponent>;
  let permissions: Permisos;
  const service = {
    list: vi.fn(() => of([{ id: 1, nombre: 'Administrador' }])),
    create: vi.fn(() => NEVER),
    update: vi.fn(() => NEVER),
    delete: vi.fn(() => of(void 0)),
    print: vi.fn(() => of([{ id: 1, nombre: 'Administrador' }])),
    exportExcel: vi.fn(() => NEVER),
    exportPdf: vi.fn(() => NEVER),
    exportCsv: vi.fn(() => NEVER),
  };
  const notification = { success: vi.fn(), operationError: vi.fn() };
  const confirmation = { confirm: vi.fn(() => Promise.resolve(true)), complete: vi.fn() };

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
      imports: [RoleListComponent],
      providers: [
        { provide: RoleService, useValue: service },
        { provide: PermissionService, useValue: { forPage: () => permissions } },
        { provide: NotificationService, useValue: notification },
        { provide: ConfirmationService, useValue: confirmation },
      ],
    }).compileComponents();
  });

  function render(): HTMLElement {
    fixture = TestBed.createComponent(RoleListComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  function button(element: HTMLElement, text: string): HTMLButtonElement | undefined {
    return Array.from(element.querySelectorAll('button')).find((item) =>
      item.textContent?.includes(text),
    );
  }

  it('respeta ALTA, CAMBIO y BAJA y abre formularios inline', () => {
    expect(render().textContent).not.toContain('Nuevo rol');
    fixture.destroy();
    permissions.alta = true;
    permissions.cambio = true;
    permissions.baja = true;
    const element = render();
    button(element, 'Nuevo rol')?.click();
    fixture.detectChanges();
    expect(element.textContent).toContain('Nuevo rol');
    fixture.componentInstance.closeForm();
    fixture.componentInstance.openEdit({ id: 1, nombre: 'Administrador' });
    fixture.detectChanges();
    expect(element.textContent).toContain('Editar rol');
    button(element, 'Eliminar')?.click();
    expect(confirmation.confirm).toHaveBeenCalledWith(
      expect.objectContaining({ title: 'Eliminar rol' }),
    );
  });

  it('respeta IMPRIMIR y EXPORTAR', () => {
    expect(button(render(), 'Imprimir')).toBeUndefined();
    fixture.destroy();
    permissions.imprimir = true;
    permissions.exportar = true;
    const element = render();
    fixture.componentInstance.search.set('Admin');
    button(element, 'Imprimir')?.click();
    expect(service.print).toHaveBeenCalledWith('Admin');
    button(element, 'Exportar')?.click();
    fixture.detectChanges();
    button(element, 'Excel (.xlsx)')?.click();
    button(element, 'PDF (.pdf)')?.click();
    button(element, 'CSV (.csv)')?.click();
    expect(service.exportExcel).toHaveBeenCalledWith('Admin');
    expect(service.exportPdf).toHaveBeenCalledWith('Admin');
    expect(service.exportCsv).toHaveBeenCalledWith('Admin');
  });

  it('muestra el conflicto 409 de baja', () => {
    service.delete.mockReturnValueOnce(
      throwError(() => ({ error: { detail: 'El rol posee usuarios asociados.' } })),
    );
    render();
    fixture.componentInstance.remove({ id: 1, nombre: 'Administrador' });
    expect(notification.operationError).toHaveBeenCalledWith(
      expect.objectContaining({ error: { detail: 'El rol posee usuarios asociados.' } }),
      'No fue posible eliminar el rol.',
    );
  });
});
