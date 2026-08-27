import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/notifications/notification.service';

function matchingPasswords(control: AbstractControl): ValidationErrors | null {
  const password = control.get('passwordNueva')?.value;
  const confirmation = control.get('passwordConfirmacion')?.value;
  return password && confirmation && password !== confirmation ? { passwordMismatch: true } : null;
}

@Component({
  selector: 'app-change-password',
  imports: [ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.css',
})
export class ChangePasswordComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly mandatory = computed(() => this.auth.currentUser()?.requiereCambiarPassword ?? false);
  readonly form = this.fb.nonNullable.group(
    {
      passwordActual: ['', Validators.required],
      passwordNueva: ['', Validators.required],
      passwordConfirmacion: ['', Validators.required],
    },
    { validators: matchingPasswords },
  );

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.errorMessage.set('');
    this.auth
      .changePassword(this.form.getRawValue())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => {
          this.notifications.success('Contraseña actualizada correctamente.');
          void this.router.navigate(['/dashboard']);
        },
        error: (error: HttpErrorResponse) =>
          this.errorMessage.set(error.error?.detail ?? 'No fue posible actualizar la contraseña.'),
      });
  }

  cancel(): void {
    if (!this.mandatory()) void this.router.navigate(['/dashboard']);
  }

  logout(): void {
    this.loading.set(true);
    this.auth
      .logout()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => void this.router.navigate(['/login']),
        error: () => void this.router.navigate(['/login']),
      });
  }
}
