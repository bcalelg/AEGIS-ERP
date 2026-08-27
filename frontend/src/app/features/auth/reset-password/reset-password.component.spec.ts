import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/notifications/notification.service';
import { ResetPasswordComponent } from './reset-password.component';

describe('ResetPasswordComponent', () => {
  const auth = { resetPassword: vi.fn() };
  const router = { navigate: vi.fn() };
  const notifications = { success: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [ResetPasswordComponent],
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
        { provide: NotificationService, useValue: notifications },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: { get: () => 'signed-token' } } },
        },
      ],
    }).compileComponents();
  });

  it('does not submit mismatched passwords', () => {
    const component = TestBed.createComponent(ResetPasswordComponent).componentInstance;
    component.form.setValue({
      passwordNueva: 'Nueva2@Segura',
      passwordConfirmacion: 'Distinta2@',
    });

    component.submit();

    expect(component.form.hasError('passwordMismatch')).toBe(true);
    expect(auth.resetPassword).not.toHaveBeenCalled();
  });

  it('returns to login after a successful reset', () => {
    auth.resetPassword.mockReturnValue(of(undefined));
    const component = TestBed.createComponent(ResetPasswordComponent).componentInstance;
    component.form.setValue({
      passwordNueva: 'Nueva2@Segura',
      passwordConfirmacion: 'Nueva2@Segura',
    });

    component.submit();

    expect(auth.resetPassword).toHaveBeenCalledWith({
      token: 'signed-token',
      passwordNueva: 'Nueva2@Segura',
      passwordConfirmacion: 'Nueva2@Segura',
    });
    expect(notifications.success).toHaveBeenCalledOnce();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
