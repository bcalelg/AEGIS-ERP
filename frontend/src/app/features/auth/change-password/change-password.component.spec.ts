import { HttpErrorResponse } from '@angular/common/http';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { AuthUser, LoginResponse } from '../../../core/models/auth.models';
import { NotificationService } from '../../../core/notifications/notification.service';
import { ChangePasswordComponent } from './change-password.component';

describe('ChangePasswordComponent', () => {
  const currentUser = signal<AuthUser | null>(user(false));
  const auth = {
    currentUser,
    changePassword: vi.fn(),
    logout: vi.fn(() => of(undefined)),
  };
  const router = {
    navigate: vi.fn(),
  };
  const notifications = {
    success: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    currentUser.set(user(false));
    await TestBed.configureTestingModule({
      imports: [ChangePasswordComponent],
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
        { provide: NotificationService, useValue: notifications },
      ],
    }).compileComponents();
  });

  it('does not submit required or mismatched fields', () => {
    const component = TestBed.createComponent(ChangePasswordComponent).componentInstance;
    component.form.setValue({
      passwordActual: 'Actual1!',
      passwordNueva: 'Nueva2@Segura',
      passwordConfirmacion: 'Distinta2@',
    });

    component.submit();

    expect(component.form.hasError('passwordMismatch')).toBe(true);
    expect(auth.changePassword).not.toHaveBeenCalled();
  });

  it('allows the application after a successful change', () => {
    auth.changePassword.mockReturnValue(of(response()));
    const component = TestBed.createComponent(ChangePasswordComponent).componentInstance;
    component.form.setValue({
      passwordActual: 'Actual1!',
      passwordNueva: 'Nueva2@Segura',
      passwordConfirmacion: 'Nueva2@Segura',
    });

    component.submit();

    expect(auth.changePassword).toHaveBeenCalledOnce();
    expect(notifications.success).toHaveBeenCalledWith('Contraseña actualizada correctamente.');
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('allows cancelling a voluntary change', () => {
    const component = TestBed.createComponent(ChangePasswordComponent).componentInstance;

    component.cancel();

    expect(component.mandatory()).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('does not render or execute cancel during a mandatory change', () => {
    currentUser.set(user(true));
    const fixture = TestBed.createComponent(ChangePasswordComponent);
    fixture.detectChanges();

    fixture.componentInstance.cancel();

    expect(fixture.componentInstance.mandatory()).toBe(true);
    expect(router.navigate).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).not.toContain('Volver al Dashboard');
  });

  it('shows the backend validation error', () => {
    auth.changePassword.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: { detail: 'La nueva contraseña no cumple la política.' },
          }),
      ),
    );
    const component = TestBed.createComponent(ChangePasswordComponent).componentInstance;
    component.form.setValue({
      passwordActual: 'Actual1!',
      passwordNueva: 'Nueva2@Segura',
      passwordConfirmacion: 'Nueva2@Segura',
    });

    component.submit();

    expect(component.errorMessage()).toContain('política');
  });

  function response(): LoginResponse {
    return {
      authenticated: true,
      expiresIn: 3600,
      idUsuario: 'Administrador',
      nombre: 'Administrador',
      apellido: 'IT',
      role: 'Administrador',
      requiereCambiarPassword: false,
    };
  }

  function user(requiresChange: boolean): AuthUser {
    return {
      idUsuario: 'Administrador',
      nombre: 'Administrador',
      apellido: 'IT',
      role: 'Administrador',
      requiereCambiarPassword: requiresChange,
    };
  }
});
