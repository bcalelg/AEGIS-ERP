import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/notifications/notification.service';

function matchingPasswords(control: AbstractControl): ValidationErrors | null {
  const password = control.get('passwordNueva')?.value;
  const confirmation = control.get('passwordConfirmacion')?.value;
  return password && confirmation && password !== confirmation ? { passwordMismatch: true } : null;
}

@Component({
  selector: 'app-reset-password',
  imports: [ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.css',
})
export class ResetPasswordComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);

  readonly token = this.route.snapshot.queryParamMap.get('token') ?? '';
  readonly loading = signal(false);
  readonly errorMessage = signal(this.token ? '' : 'El enlace de recuperación está incompleto.');
  readonly form = this.fb.nonNullable.group(
    {
      passwordNueva: ['', Validators.required],
      passwordConfirmacion: ['', Validators.required],
    },
    { validators: matchingPasswords },
  );

  submit(): void {
    if (!this.token || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.errorMessage.set('');
    this.auth
      .resetPassword({ token: this.token, ...this.form.getRawValue() })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => {
          this.notifications.success('Contraseña restablecida correctamente. Ya puedes iniciar sesión.');
          void this.router.navigate(['/login']);
        },
        error: (error: HttpErrorResponse) =>
          this.errorMessage.set(
            error.error?.detail ?? 'No fue posible restablecer la contraseña.',
          ),
      });
  }
}
