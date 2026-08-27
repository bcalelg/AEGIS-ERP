import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly form = this.fb.nonNullable.group({
    idUsuario: ['', Validators.required],
    password: ['', Validators.required],
  });
  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.errorMessage.set('');
    this.auth
      .login(this.form.getRawValue())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) =>
          void this.router.navigate([
            response.requiereCambiarPassword ? '/change-password' : '/dashboard',
          ]),
        error: (error: HttpErrorResponse) =>
          this.errorMessage.set(
            error.status === 401
              ? 'Usuario o contraseña incorrectos.'
              : 'No fue posible conectar con el servidor.',
          ),
      });
  }
}
