import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NEVER, of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { Permisos } from '../../../../core/models/menu.models';
import { ConfirmationService } from '../../../../core/confirmation/confirmation.service';
import { NotificationService } from '../../../../core/notifications/notification.service';
import { PermissionService } from '../../../../core/services/permission.service';
import { SucursalService } from '../services/sucursal.service';
import { SucursalListComponent } from './sucursal-list.component';

describe('SucursalListComponent', () => {
  let fixture: ComponentFixture<SucursalListComponent>;
  let permissions: Permisos;
  const item = {
    id: 1,
    idEmpresa: 2,
    nombreEmpresa: 'Software Inc.',
    nombre: 'Central',
    direccion: 'Guatemala',
  };
  const service = {
    list: vi.fn(() => of([item])),
    empresaOptions: vi.fn(() => of([{ id: 2, nombre: 'Software Inc.' }])),
    create: vi.fn(() => NEVER),
    update: vi.fn(() => NEVER),
    delete: vi.fn(() => of(void 0)),
    print: vi.fn(() => of([item])),
    exportExcel: vi.fn(() => NEVER),
    exportPdf: vi.fn(() => NEVER),
    exportCsv: vi.fn(() => NEVER),
  };
  const notification = {
    success: vi.fn(),
    operationError: vi.fn(),
  };
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
      imports: [SucursalListComponent],
      providers: [
        { provide: SucursalService, useValue: service },
        { provide: PermissionService, useValue: { forPage: () => permissions } },
        { provide: NotificationService, useValue: notification },
        { provide: ConfirmationService, useValue: confirmation },
      ],
    }).compileComponents();
  });

  function render(): HTMLElement {
    fixture = TestBed.createComponent(SucursalListComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  function button(element: HTMLElement, text: string): HTMLButtonElement | undefined {
    return Array.from(element.querySelectorAll('button')).find((candidate) =>
      candidate.textContent?.includes(text),
    );
  }

  it('muestra Empresa y controla alta, cambio y baja', () => {
    expect(render().textContent).toContain('Software Inc.');
    expect(fixture.nativeElement.textContent).not.toContain('Nueva sucursal');
    fixture.destroy();
    permissions.alta = true;
    permissions.cambio = true;
    permissions.baja = true;
    const element = render();
    button(element, 'Nueva sucursal')?.click();
    fixture.detectChanges();
    expect(element.textContent).toContain('Nueva sucursal');
    fixture.componentInstance.closeForm();
    fixture.componentInstance.openEdit(item);
    fixture.detectChanges();
    expect(element.textContent).toContain('Editar sucursal');
    fixture.componentInstance.closeForm();
    fixture.detectChanges();
    button(element, 'Eliminar')?.click();
    expect(confirmation.confirm).toHaveBeenCalledWith(
      expect.objectContaining({
        title: 'Eliminar sucursal',
        message: '¿Desea eliminar la sucursal "Central"?',
      }),
    );
  });

  it('busca e invoca impresión y los tres formatos', () => {
    permissions.imprimir = true;
    permissions.exportar = true;
    const element = render();
    fixture.componentInstance.search.set('software');
    expect(fixture.componentInstance.filteredItems()).toHaveLength(1);
    button(element, 'Imprimir')?.click();
    button(element, 'Exportar')?.click();
    fixture.detectChanges();
    button(element, 'Excel (.xlsx)')?.click();
    button(element, 'PDF (.pdf)')?.click();
    button(element, 'CSV (.csv)')?.click();
    expect(service.print).toHaveBeenCalledWith('software');
    expect(service.exportExcel).toHaveBeenCalledWith('software');
    expect(service.exportPdf).toHaveBeenCalledWith('software');
    expect(service.exportCsv).toHaveBeenCalledWith('software');
  });

  it('muestra conflicto de baja', () => {
    service.delete.mockReturnValueOnce(
      throwError(() => ({ error: { detail: 'La sucursal posee usuarios asociados.' } })),
    );
    render();
    fixture.componentInstance.remove(item);
    expect(notification.operationError).toHaveBeenCalledWith(
      expect.objectContaining({ error: { detail: 'La sucursal posee usuarios asociados.' } }),
      'No fue posible eliminar la sucursal.',
    );
  });

  it('confirma la eliminación exitosa', () => {
    render();
    fixture.componentInstance.remove(item);
    expect(notification.success).toHaveBeenCalledWith('Sucursal eliminada correctamente.');
  });

  it('reutiliza la confirmación global antes del DELETE', async () => {
    render();
    await fixture.componentInstance.confirmRemove(item);
    expect(confirmation.confirm).toHaveBeenCalledOnce();
    expect(service.delete).toHaveBeenCalledWith(1);
  });
});
