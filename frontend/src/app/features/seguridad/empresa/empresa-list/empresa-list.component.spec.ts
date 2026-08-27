import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NEVER, of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { Permisos } from '../../../../core/models/menu.models';
import { ConfirmationService } from '../../../../core/confirmation/confirmation.service';
import { NotificationService } from '../../../../core/notifications/notification.service';
import { PermissionService } from '../../../../core/services/permission.service';
import { Empresa } from '../models/empresa.models';
import { EmpresaService } from '../services/empresa.service';
import { EmpresaListComponent } from './empresa-list.component';

describe('EmpresaListComponent inline form and permissions', () => {
  let fixture: ComponentFixture<EmpresaListComponent>;
  let permissions: Permisos;
  let listCalls: number;

  const company: Empresa = {
    idEmpresa: 1,
    nombre: 'Software Inc.',
    direccion: 'Guatemala',
    nit: '12345678-9',
    passwordCantidadMayusculas: 1,
    passwordCantidadMinusculas: 1,
    passwordCantidadCaracteresEspeciales: 1,
    passwordCantidadCaducidadDias: 60,
    passwordLargo: 8,
    passwordIntentosAntesDeBloquear: 5,
    passwordCantidadNumeros: 2,
    passwordCantidadPreguntasValidar: 1,
    fechaCreacion: '2026-08-01T00:00:00',
    usuarioCreacion: 'system',
    fechaModificacion: undefined,
    usuarioModificacion: undefined,
  };

  const service = {
    list: () => {
      listCalls += 1;
      return of({ content: [company], page: 0, size: 10, totalElements: 1, totalPages: 1 });
    },
    create: () => of(company),
    update: () => of(company),
    delete: vi.fn(() => of(void 0)),
    print: vi.fn(() =>
      of({ content: [company], page: 0, size: 10, totalElements: 1, totalPages: 1 }),
    ),
    exportExcel: vi.fn(() => NEVER),
    exportPdf: vi.fn(() => NEVER),
    exportCsv: vi.fn(() => NEVER),
  };
  const confirmation = { confirm: vi.fn(() => Promise.resolve(true)), complete: vi.fn() };
  const notification = { success: vi.fn(), operationError: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();
    vi.spyOn(window, 'print').mockImplementation(() => undefined);
    listCalls = 0;
    permissions = {
      consultar: true,
      alta: false,
      baja: false,
      cambio: false,
      imprimir: false,
      exportar: false,
    };

    await TestBed.configureTestingModule({
      imports: [EmpresaListComponent],
      providers: [
        { provide: EmpresaService, useValue: service },
        { provide: PermissionService, useValue: { forPage: () => permissions } },
        { provide: ConfirmationService, useValue: confirmation },
        { provide: NotificationService, useValue: notification },
      ],
    }).compileComponents();
  });

  function render(): HTMLElement {
    fixture = TestBed.createComponent(EmpresaListComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  function buttonByText(element: HTMLElement, text: string): HTMLButtonElement | undefined {
    return Array.from(element.querySelectorAll('button')).find(
      (button) => button.textContent?.trim() === text,
    );
  }

  it('controla Nueva empresa con ALTA', () => {
    expect(render().textContent).not.toContain('Nueva empresa');
    fixture.destroy();
    permissions.alta = true;
    expect(render().textContent).toContain('Nueva empresa');
  });

  it('controla Editar con CAMBIO', () => {
    expect(render().textContent).not.toContain('Editar');
    fixture.destroy();
    permissions.cambio = true;
    expect(render().textContent).toContain('Editar');
  });

  it('controla Eliminar con BAJA', () => {
    expect(render().textContent).not.toContain('Eliminar');
    fixture.destroy();
    permissions.baja = true;
    expect(render().textContent).toContain('Eliminar');
  });

  it('abre confirmación al pulsar Eliminar', () => {
    permissions.baja = true;
    const element = render();
    buttonByText(element, 'Eliminar')?.click();
    expect(confirmation.confirm).toHaveBeenCalledWith(
      expect.objectContaining({
        title: 'Eliminar empresa',
        message: '¿Desea eliminar la empresa "Software Inc."?',
      }),
    );
  });

  it('Cancelar no ejecuta DELETE', async () => {
    confirmation.confirm.mockResolvedValueOnce(false);
    render();
    await fixture.componentInstance.confirmRemove(company);
    expect(service.delete).not.toHaveBeenCalled();
  });

  it('Confirmar ejecuta DELETE una vez, refresca y muestra toast', async () => {
    render();
    const callsBeforeDelete = listCalls;
    await fixture.componentInstance.confirmRemove(company);
    expect(service.delete).toHaveBeenCalledTimes(1);
    expect(listCalls).toBe(callsBeforeDelete + 1);
    expect(notification.success).toHaveBeenCalledWith('Empresa eliminada correctamente.');
  });

  it('un conflicto 409 conserva el listado y muestra el error global', async () => {
    const conflict = { status: 409, error: { detail: 'La empresa posee sucursales asociadas.' } };
    service.delete.mockReturnValueOnce(throwError(() => conflict));
    render();
    await fixture.componentInstance.confirmRemove(company);
    expect(fixture.componentInstance.items()).toContain(company);
    expect(notification.operationError).toHaveBeenCalledWith(
      conflict,
      'No fue posible eliminar la empresa.',
    );
  });

  it('openCreate abre el formulario en modo creación', () => {
    render();
    fixture.componentInstance.openCreate();
    fixture.detectChanges();
    expect(fixture.componentInstance.formOpen()).toBe(true);
    expect(fixture.componentInstance.editing()).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Nueva empresa');
  });

  it('openEdit abre el formulario con la empresa seleccionada', () => {
    render();
    fixture.componentInstance.openEdit(company);
    fixture.detectChanges();
    expect(fixture.componentInstance.formOpen()).toBe(true);
    expect(fixture.componentInstance.editing()).toBe(company);
    expect(fixture.nativeElement.textContent).toContain('Editar empresa');
  });

  it('cancelar cierra el formulario y limpia la selección', () => {
    render();
    fixture.componentInstance.openEdit(company);
    fixture.componentInstance.closeForm();
    expect(fixture.componentInstance.formOpen()).toBe(false);
    expect(fixture.componentInstance.editing()).toBeNull();
  });

  it('guardar exitosamente cierra el formulario y recarga la página actual', () => {
    render();
    fixture.componentInstance.openEdit(company);
    const callsBeforeSave = listCalls;
    fixture.componentInstance.saved();
    expect(fixture.componentInstance.formOpen()).toBe(false);
    expect(fixture.componentInstance.editing()).toBeNull();
    expect(listCalls).toBe(callsBeforeSave + 1);
  });

  it('controla Imprimir con IMPRIMIR', () => {
    expect(buttonByText(render(), 'Imprimir')).toBeUndefined();
    fixture.destroy();
    permissions.imprimir = true;
    buttonByText(render(), 'Imprimir')?.click();
    expect(service.print).toHaveBeenCalledOnce();
  });

  it('oculta por completo Exportar sin EXPORTAR', () => {
    expect(render().textContent).not.toContain('Exportar');
  });

  it('muestra los tres formatos y ejecuta sus métodos con EXPORTAR', () => {
    permissions.exportar = true;
    const element = render();

    buttonByText(element, 'Exportar')?.click();
    fixture.detectChanges();
    expect(element.textContent).toContain('Excel (.xlsx)');
    expect(element.textContent).toContain('PDF (.pdf)');
    expect(element.textContent).toContain('CSV (.csv)');

    buttonByText(element, 'Excel (.xlsx)')?.click();
    buttonByText(element, 'PDF (.pdf)')?.click();
    buttonByText(element, 'CSV (.csv)')?.click();
    expect(service.exportExcel).toHaveBeenCalledWith('');
    expect(service.exportPdf).toHaveBeenCalledWith('');
    expect(service.exportCsv).toHaveBeenCalledWith('');
  });
});
