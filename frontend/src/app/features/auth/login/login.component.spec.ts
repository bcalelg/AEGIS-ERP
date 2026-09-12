import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { LoginResponse } from '../../../core/models/auth.models';
import { LoginComponent } from './login.component';

describe('LoginComponent mandatory password change', () => {
  const auth = {
    login: vi.fn(),
  };
  const router = {
    navigate: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: {} },
      ],
    }).compileComponents();
  });

  it('redirects the authenticated user to mandatory change', () => {
    auth.login.mockReturnValue(of(response(true)));
    const component = TestBed.createComponent(LoginComponent).componentInstance;
    component.form.setValue({ idUsuario: 'Administrador', password: 'ITAdmin' });

    component.submit();

    expect(router.navigate).toHaveBeenCalledWith(['/change-password']);
  });

  it('redirects a user with a current password to the dashboard', () => {
    auth.login.mockReturnValue(of(response(false)));
    const component = TestBed.createComponent(LoginComponent).componentInstance;
    component.form.setValue({ idUsuario: 'Administrador', password: 'ITAdmin' });

    component.submit();

    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('orienta al usuario sin revelar la causa de una autenticación rechazada', () => {
    auth.login.mockReturnValue(throwError(() => ({ status: 401 })));
    const component = TestBed.createComponent(LoginComponent).componentInstance;
    component.form.setValue({ idUsuario: 'usuario', password: 'incorrecta' });

    component.submit();

    expect(component.errorMessage()).toBe(
      'Usuario o contraseña incorrectos. Si el problema persiste, contacte al administrador del sistema.',
    );
  });

  function response(requiresChange: boolean): LoginResponse {
    return {
      authenticated: true,
      expiresIn: 3600,
      idUsuario: 'Administrador',
      nombre: 'Administrador',
      apellido: 'IT',
      role: 'Administrador',
      requiereCambiarPassword: requiresChange,
    };
  }
});
