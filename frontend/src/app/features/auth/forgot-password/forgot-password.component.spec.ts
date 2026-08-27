import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { Subject, of } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/notifications/notification.service';
import { ForgotPasswordComponent } from './forgot-password.component';

describe('ForgotPasswordComponent', () => {
  const auth = { forgotPassword: vi.fn() };
  const notifications = { info: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [ForgotPasswordComponent],
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: NotificationService, useValue: notifications },
        { provide: ActivatedRoute, useValue: {} },
      ],
    }).compileComponents();
  });

  it('does not send an empty identifier', () => {
    const component = TestBed.createComponent(ForgotPasswordComponent).componentInstance;

    component.submit();

    expect(auth.forgotPassword).not.toHaveBeenCalled();
  });

  it('uses the global notification and preserves the identifier', () => {
    const message =
      'Si la cuenta existe y posee un correo registrado, recibirás instrucciones para restablecer la contraseña.';
    auth.forgotPassword.mockReturnValue(of({ message }));
    const component = TestBed.createComponent(ForgotPasswordComponent).componentInstance;
    component.form.setValue({ identifier: 'usuario@ejemplo.com' });

    component.submit();

    expect(auth.forgotPassword).toHaveBeenCalledWith({ identifier: 'usuario@ejemplo.com' });
    expect(notifications.info).toHaveBeenCalledWith(message);
    expect(component.form.getRawValue().identifier).toBe('usuario@ejemplo.com');
    expect(component.submitted()).toBe(true);
  });

  it('does not render a duplicate success alert', () => {
    const fixture = TestBed.createComponent(ForgotPasswordComponent);

    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.alert-success')).toBeNull();
  });

  it('keeps loading until the request completes', () => {
    const pending = new Subject<{ message: string }>();
    auth.forgotPassword.mockReturnValue(pending.asObservable());
    const component = TestBed.createComponent(ForgotPasswordComponent).componentInstance;
    component.form.setValue({ identifier: 'TEST_LOGIN' });

    component.submit();
    expect(component.loading()).toBe(true);

    pending.next({ message: 'Respuesta genérica' });
    pending.complete();
    expect(component.loading()).toBe(false);
  });

  it('allows editing after a processed request without navigation or reload', () => {
    auth.forgotPassword.mockReturnValue(of({ message: 'Respuesta genérica' }));
    const component = TestBed.createComponent(ForgotPasswordComponent).componentInstance;
    component.form.setValue({ identifier: 'TEST_LOGIN' });
    component.submit();

    component.editIdentifier();

    expect(component.submitted()).toBe(false);
  });
});
