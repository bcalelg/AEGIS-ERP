import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { NotificationService } from '../../../core/notifications/notification.service';
import { PermissionService } from '../../../core/services/permission.service';
import { RoleOpcionComponent } from './role-opcion.component';
import { RoleOpcionService } from './services/role-opcion.service';

describe('RoleOpcionComponent', () => {
  let fixture: ComponentFixture<RoleOpcionComponent>;
  const item = {
    idOpcion: 1,
    nombreOpcion: 'Empresas',
    nombreMenu: 'Parámetros Generales',
    ordenMenu: 1,
    ordenOpcion: 1,
    consultar: false,
    alta: false,
    baja: false,
    cambio: false,
    imprimir: false,
    exportar: false,
  };
  const service = {
    roles: vi.fn(() => of([{ id: 2, nombre: 'Supervisor' }])),
    modulos: vi.fn(() => of([{ id: 1, nombre: 'Seguridad' }])),
    matrix: vi.fn(() => of([item])),
    save: vi.fn(() => of([{ ...item, consultar: true }])),
  };
  const notification = {
    success: vi.fn(),
    operationError: vi.fn(),
  };

  async function create() {
    await TestBed.configureTestingModule({
      imports: [RoleOpcionComponent],
      providers: [
        { provide: RoleOpcionService, useValue: service },
        { provide: NotificationService, useValue: notification },
        {
          provide: PermissionService,
          useValue: {
            forPage: vi.fn(() => ({
              consultar: true,
              alta: false,
              baja: false,
              cambio: true,
              imprimir: false,
              exportar: false,
            })),
          },
        },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(RoleOpcionComponent);
    fixture.detectChanges();
  }

  it('carga roles y módulos, luego consulta la matriz', async () => {
    await create();
    expect(fixture.componentInstance.roles()).toHaveLength(1);
    expect(fixture.componentInstance.modulos()).toHaveLength(1);
    fixture.componentInstance.changeSelection(2, 1);
    expect(service.matrix).toHaveBeenCalledWith(2, 1);
    expect(fixture.componentInstance.matrix()).toHaveLength(1);
  });

  it('mantiene cambios locales y guarda la matriz completa', async () => {
    await create();
    fixture.componentInstance.changeSelection(2, 1);
    fixture.componentInstance.toggle(item, 'consultar', true);
    expect(service.save).not.toHaveBeenCalled();
    expect(fixture.componentInstance.dirty()).toBe(true);
    fixture.componentInstance.save();
    expect(service.save).toHaveBeenCalledWith(expect.objectContaining({ idRole: 2, idModulo: 1 }));
    expect(fixture.componentInstance.dirty()).toBe(false);
    expect(notification.success).toHaveBeenCalledWith('Permisos guardados correctamente.');
  });

  it('protege los cambios sin guardar al cambiar selección', async () => {
    await create();
    fixture.componentInstance.changeSelection(2, 1);
    fixture.componentInstance.toggle(item, 'alta', true);
    fixture.componentInstance.changeSelection(3, 2);
    expect(fixture.componentInstance.pendingSelection()).toEqual({ role: 3, modulo: 2 });
    fixture.componentInstance.keepEditing();
    expect(fixture.componentInstance.selectedRole()).toBe(2);
  });
});
