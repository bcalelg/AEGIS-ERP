import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { NotificationService } from '../../../../core/notifications/notification.service';
import { UsuarioService } from '../services/usuario.service';
import { minimumAdultBirthDate, UsuarioFormComponent } from './usuario-form.component';

describe('UsuarioFormComponent', () => {
  let fixture: ComponentFixture<UsuarioFormComponent>;
  const service = {
    empresaOptions: vi.fn(() => of([{ id: 1, nombre: 'Empresa A' }, { id: 2, nombre: 'Empresa B' }])),
    sucursalOptions: vi.fn((id: number) => of(id === 1 ? [{ id: 10, nombre: 'Central' }] : [{ id: 20, nombre: 'Norte' }])),
    generoOptions: vi.fn(() => of([{ id: 3, nombre: 'Masculino' }])),
    statusOptions: vi.fn(() => of([{ id: 4, nombre: 'Activo' }])),
    roleOptions: vi.fn(() => of([{ id: 5, nombre: 'Operador' }])),
    create: vi.fn(() => of({})),
    update: vi.fn(() => of({})),
  };
  const notification = { success: vi.fn(), operationError: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [UsuarioFormComponent],
      providers: [
        { provide: UsuarioService, useValue: service },
        { provide: NotificationService, useValue: notification },
      ],
    }).compileComponents();
  });

  function create(selected: unknown = null) {
    fixture = TestBed.createComponent(UsuarioFormComponent);
    fixture.componentRef.setInput('selected', selected);
    fixture.detectChanges();
  }

  it('alta valida confirmación, carga catálogos y sucursal dependiente', () => {
    create();
    fixture.componentInstance.form.patchValue({ idEmpresa: 1 });
    fixture.componentInstance.companyChanged();
    expect(service.sucursalOptions).toHaveBeenCalledWith(1);
    expect(fixture.componentInstance.branches()).toEqual([{ id: 10, nombre: 'Central' }]);
    fixture.componentInstance.form.patchValue({
      idUsuario: 'TEST', nombre: 'Ana', apellido: 'López', fechaNacimiento: '1990-01-01',
      correoElectronico: 'ana@example.com', telefonoMovil: '', password: 'Temporal1!', passwordConfirmacion: 'Otra1!',
      pregunta: 'Pregunta', respuesta: 'Respuesta', idSucursal: 10, idGenero: 3, idStatusUsuario: 4, idRole: 5,
    });
    fixture.componentInstance.save();
    expect(service.create).not.toHaveBeenCalled();
    fixture.componentInstance.form.controls.passwordConfirmacion.setValue('Temporal1!');
    fixture.componentInstance.save();
    expect(service.create).toHaveBeenCalled();
    expect(notification.success).toHaveBeenCalledWith('Usuario creado correctamente.');
  });

  it('edición no precarga ni envía contraseña y conserva sucursal compatible', () => {
    create({
      idUsuario: 'TEST', nombre: 'Ana', apellido: 'López', fechaNacimiento: '1990-01-01',
      correoElectronico: 'ana@example.com', telefonoMovil: null, pregunta: 'Pregunta',
      idEmpresa: 1, nombreEmpresa: 'Empresa A', idSucursal: 10, nombreSucursal: 'Central',
      idGenero: 3, nombreGenero: 'Masculino', idStatusUsuario: 4, nombreStatusUsuario: 'Activo',
      idRole: 5, nombreRole: 'Operador', ultimaFechaIngreso: null, requiereCambiarPassword: true,
    });
    expect(fixture.componentInstance.form.controls.password.value).toBe('');
    expect(fixture.componentInstance.form.controls.idSucursal.value).toBe(10);
    fixture.componentInstance.save();
    expect(service.update).toHaveBeenCalledWith('TEST', expect.not.objectContaining({ password: expect.anything() }));
    expect(notification.success).toHaveBeenCalledWith('Usuario actualizado correctamente.');
  });

  it('cambiar empresa limpia sucursal incompatible', () => {
    create();
    fixture.componentInstance.form.patchValue({ idEmpresa: 1 });
    fixture.componentInstance.companyChanged();
    fixture.componentInstance.form.controls.idSucursal.setValue(10);
    fixture.componentInstance.form.controls.idEmpresa.setValue(2);
    fixture.componentInstance.companyChanged();
    expect(fixture.componentInstance.form.controls.idSucursal.value).toBe(0);
    expect(fixture.componentInstance.branches()).toEqual([{ id: 20, nombre: 'Norte' }]);
  });

  it('aplica máximo dinámico y valida correctamente la mayoría de edad', () => {
    create();
    const maximum = minimumAdultBirthDate();
    const input = fixture.nativeElement.querySelector('#usuario-nacimiento') as HTMLInputElement;
    expect(input.max).toBe(maximum);
    fixture.componentInstance.form.controls.fechaNacimiento.setValue(maximum);
    expect(fixture.componentInstance.form.controls.fechaNacimiento.valid).toBe(true);
    const nextDay = new Date(`${maximum}T12:00:00`);
    nextDay.setDate(nextDay.getDate() + 1);
    fixture.componentInstance.form.controls.fechaNacimiento.setValue(nextDay.toISOString().slice(0, 10));
    fixture.componentInstance.form.controls.fechaNacimiento.markAsTouched();
    fixture.detectChanges();
    expect(fixture.componentInstance.form.controls.fechaNacimiento.hasError('underage')).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('El usuario debe tener al menos 18 años.');
  });

  it('valida correo y formatos telefónicos sin impedir formatos internacionales', () => {
    create();
    const email = fixture.componentInstance.form.controls.correoElectronico;
    email.setValue('usuario');
    expect(email.invalid).toBe(true);
    email.setValue('nombre.apellido@empresa.com.gt');
    expect(email.valid).toBe(true);
    const phone = fixture.componentInstance.form.controls.telefonoMovil;
    for (const value of ['+502 5555-5555', '(001) 402-584754', '+1 (402) 584-7540', '50255555555']) {
      phone.setValue(value);
      expect(phone.valid).toBe(true);
    }
    phone.setValue('abc555');
    phone.markAsTouched();
    fixture.detectChanges();
    expect(phone.invalid).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('El teléfono solo puede contener');
  });

  it('no guarda cuando correo, teléfono o fecha son inválidos', () => {
    create();
    fixture.componentInstance.form.patchValue({
      idUsuario: 'TEST', nombre: 'Ana', apellido: 'López', fechaNacimiento: minimumAdultBirthDate(),
      correoElectronico: 'usuario', telefonoMovil: 'telefono123', password: 'Temporal12!',
      passwordConfirmacion: 'Temporal12!', pregunta: 'Pregunta', respuesta: 'Respuesta',
      idEmpresa: 1, idSucursal: 10, idGenero: 3, idStatusUsuario: 4, idRole: 5,
    });
    fixture.componentInstance.save();
    expect(service.create).not.toHaveBeenCalled();
  });
});
