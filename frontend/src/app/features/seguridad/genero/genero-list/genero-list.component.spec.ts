import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NEVER, of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { Permisos } from '../../../../core/models/menu.models';
import { PermissionService } from '../../../../core/services/permission.service';
import { GeneroService } from '../services/genero.service';
import { GeneroListComponent } from './genero-list.component';

describe('GeneroListComponent permissions', () => {
  let fixture: ComponentFixture<GeneroListComponent>;
  let permissions: Permisos;

  const service = {
    list: vi.fn(() => of([{ id: 1, nombre: 'Masculino' }])),
    create: vi.fn(() => NEVER),
    update: vi.fn(() => NEVER),
    delete: vi.fn(() => of(void 0)),
    print: vi.fn(() => of([{ id: 1, nombre: 'Masculino' }])),
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
      imports: [GeneroListComponent],
      providers: [
        { provide: GeneroService, useValue: service },
        { provide: PermissionService, useValue: { forPage: () => permissions } },
      ],
    }).compileComponents();
  });

  function render(): HTMLElement {
    fixture = TestBed.createComponent(GeneroListComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  function buttonByText(element: HTMLElement, text: string): HTMLButtonElement | undefined {
    return Array.from(element.querySelectorAll('button')).find((button) =>
      button.textContent?.includes(text),
    );
  }

  it('muestra Nuevo género únicamente con ALTA', () => {
    expect(render().textContent).not.toContain('Nuevo género');
    fixture.destroy();
    permissions.alta = true;
    expect(render().textContent).toContain('Nuevo género');
  });

  it('muestra Editar únicamente con CAMBIO', () => {
    expect(render().textContent).not.toContain('Editar');
    fixture.destroy();
    permissions.cambio = true;
    expect(render().textContent).toContain('Editar');
  });

  it('muestra Eliminar únicamente con BAJA', () => {
    expect(render().textContent).not.toContain('Eliminar');
    fixture.destroy();
    permissions.baja = true;
    expect(render().textContent).toContain('Eliminar');
  });

  it('muestra y ejecuta Imprimir únicamente con IMPRIMIR', () => {
    expect(buttonByText(render(), 'Imprimir')).toBeUndefined();
    fixture.destroy();
    permissions.imprimir = true;
    const button = buttonByText(render(), 'Imprimir');
    button?.click();
    expect(button).toBeDefined();
    expect(service.print).toHaveBeenCalledOnce();
  });

  it('oculta por completo Exportar sin EXPORTAR', () => {
    expect(buttonByText(render(), 'Exportar')).toBeUndefined();
  });

  it('muestra los tres formatos y ejecuta sus métodos con EXPORTAR', () => {
    permissions.exportar = true;
    const element = render();
    fixture.componentInstance.search.set('Fem');

    buttonByText(element, 'Exportar')?.click();
    fixture.detectChanges();
    expect(element.textContent).toContain('Excel (.xlsx)');
    expect(element.textContent).toContain('PDF (.pdf)');
    expect(element.textContent).toContain('CSV (.csv)');

    buttonByText(element, 'Excel (.xlsx)')?.click();
    buttonByText(element, 'PDF (.pdf)')?.click();
    buttonByText(element, 'CSV (.csv)')?.click();
    expect(service.exportExcel).toHaveBeenCalledWith('Fem');
    expect(service.exportPdf).toHaveBeenCalledWith('Fem');
    expect(service.exportCsv).toHaveBeenCalledWith('Fem');
  });
});
