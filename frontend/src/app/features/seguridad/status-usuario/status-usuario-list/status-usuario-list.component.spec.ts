import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NEVER, of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { Permisos } from '../../../../core/models/menu.models';
import { PermissionService } from '../../../../core/services/permission.service';
import { StatusUsuarioService } from '../services/status-usuario.service';
import { StatusUsuarioListComponent } from './status-usuario-list.component';

describe('StatusUsuarioListComponent permissions', () => {
  let fixture: ComponentFixture<StatusUsuarioListComponent>;
  let permissions: Permisos;
  const service = {
    list: vi.fn(() => of([{ id: 1, nombre: 'Activo' }])),
    create: vi.fn(() => NEVER),
    update: vi.fn(() => NEVER),
    delete: vi.fn(() => of(void 0)),
    print: vi.fn(() => of([{ id: 1, nombre: 'Activo' }])),
    exportExcel: vi.fn(() => NEVER),
    exportPdf: vi.fn(() => NEVER),
    exportCsv: vi.fn(() => NEVER),
  };

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
      imports: [StatusUsuarioListComponent],
      providers: [
        { provide: StatusUsuarioService, useValue: service },
        { provide: PermissionService, useValue: { forPage: () => permissions } },
      ],
    }).compileComponents();
  });

  function render(): HTMLElement {
    fixture = TestBed.createComponent(StatusUsuarioListComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  function button(element: HTMLElement, text: string): HTMLButtonElement | undefined {
    return Array.from(element.querySelectorAll('button')).find((item) =>
      item.textContent?.includes(text),
    );
  }

  it('respeta ALTA, CAMBIO y BAJA', () => {
    expect(render().textContent).not.toContain('Nuevo estatus');
    expect(fixture.nativeElement.textContent).not.toContain('Editar');
    expect(fixture.nativeElement.textContent).not.toContain('Eliminar');
    fixture.destroy();
    permissions.alta = true;
    permissions.cambio = true;
    permissions.baja = true;
    const content = render().textContent;
    expect(content).toContain('Nuevo estatus');
    expect(content).toContain('Editar');
    expect(content).toContain('Eliminar');
  });

  it('respeta IMPRIMIR y envía la búsqueda', () => {
    expect(button(render(), 'Imprimir')).toBeUndefined();
    fixture.destroy();
    permissions.imprimir = true;
    const element = render();
    fixture.componentInstance.search.set('Act');
    button(element, 'Imprimir')?.click();
    expect(service.print).toHaveBeenCalledWith('Act');
  });

  it('respeta EXPORTAR y ofrece los tres formatos', () => {
    expect(button(render(), 'Exportar')).toBeUndefined();
    fixture.destroy();
    permissions.exportar = true;
    const element = render();
    fixture.componentInstance.search.set('Act');
    button(element, 'Exportar')?.click();
    fixture.detectChanges();
    button(element, 'Excel (.xlsx)')?.click();
    button(element, 'PDF (.pdf)')?.click();
    button(element, 'CSV (.csv)')?.click();
    expect(service.exportExcel).toHaveBeenCalledWith('Act');
    expect(service.exportPdf).toHaveBeenCalledWith('Act');
    expect(service.exportCsv).toHaveBeenCalledWith('Act');
  });
});
