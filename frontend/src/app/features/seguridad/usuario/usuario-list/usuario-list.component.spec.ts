import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NEVER, of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ConfirmationService } from '../../../../core/confirmation/confirmation.service';
import { NotificationService } from '../../../../core/notifications/notification.service';
import { PermissionService } from '../../../../core/services/permission.service';
import { UsuarioService } from '../services/usuario.service';
import { UsuarioListComponent } from './usuario-list.component';

describe('UsuarioListComponent', () => {
  let fixture: ComponentFixture<UsuarioListComponent>;
  const item = {
    idUsuario: 'TEST', nombre: 'Ana', apellido: 'López', fechaNacimiento: '1990-01-01',
    correoElectronico: 'ana@example.com', telefonoMovil: null, pregunta: 'Pregunta',
    idEmpresa: 1, nombreEmpresa: 'Empresa A', idSucursal: 10, nombreSucursal: 'Central',
    idGenero: 3, nombreGenero: 'Femenino', idStatusUsuario: 4, nombreStatusUsuario: 'Activo',
    idRole: 5, nombreRole: 'Operador', ultimaFechaIngreso: null, requiereCambiarPassword: true,
  };
  const service = {
    list: vi.fn(() => of([item])), get: vi.fn(() => of(item)), delete: vi.fn(() => of(void 0)), print: vi.fn(() => of([item])),
    exportCsv: vi.fn(() => NEVER), exportExcel: vi.fn(() => NEVER), exportPdf: vi.fn(() => NEVER),
    empresaOptions: vi.fn(() => of([])), sucursalOptions: vi.fn(() => of([])), generoOptions: vi.fn(() => of([])),
    statusOptions: vi.fn(() => of([])), roleOptions: vi.fn(() => of([])), create: vi.fn(() => NEVER), update: vi.fn(() => NEVER),
  };
  const notification = { success: vi.fn(), operationError: vi.fn() };
  const confirmation = { confirm: vi.fn(() => Promise.resolve(true)), complete: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [UsuarioListComponent],
      providers: [
        { provide: UsuarioService, useValue: service },
        { provide: PermissionService, useValue: { forPage: () => ({ consultar: true, alta: true, baja: true, cambio: true, imprimir: true, exportar: true }) } },
        { provide: NotificationService, useValue: notification },
        { provide: ConfirmationService, useValue: confirmation },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(UsuarioListComponent);
    fixture.detectChanges();
  });

  it('lista información segura, busca y respeta acciones', () => {
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('TEST');
    expect(text).toContain('Empresa A');
    expect(text).toContain('Cambio de contraseña pendiente');
    expect(text).not.toContain('Pregunta');
    fixture.componentInstance.search.set('operador');
    expect(fixture.componentInstance.filteredItems()).toHaveLength(1);
  });

  it('reutiliza confirmación global y notificación al eliminar', async () => {
    await fixture.componentInstance.confirmRemove(item);
    expect(confirmation.confirm).toHaveBeenCalledWith(expect.objectContaining({ title: 'Eliminar usuario', message: '¿Desea eliminar el usuario "TEST"?' }));
    expect(service.delete).toHaveBeenCalledWith('TEST');
    expect(notification.success).toHaveBeenCalledWith('Usuario eliminado correctamente.');
  });

  it('normaliza el conflicto de baja mediante notificación global', () => {
    service.delete.mockReturnValueOnce(throwError(() => ({ error: { detail: 'Posee registros asociados.' } })));
    fixture.componentInstance.remove(item);
    expect(notification.operationError).toHaveBeenCalledWith(expect.anything(), 'No fue posible eliminar el usuario.');
  });
});
