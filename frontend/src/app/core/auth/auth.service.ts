import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, finalize, map, Observable, of, shareReplay, switchMap, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AuthUser,
  ChangePasswordRequest,
  CurrentUserResponse,
  ForgotPasswordRequest,
  ForgotPasswordResponse,
  LoginRequest,
  LoginResponse,
  ResetPasswordRequest,
} from '../models/auth.models';
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly user = signal<AuthUser | null>(null);
  private restoreRequest?: Observable<AuthUser | null>;
  readonly currentUser = this.user.asReadonly();
  readonly isAuthenticated = computed(() => this.user() !== null);
  login(request: LoginRequest): Observable<LoginResponse> {
    return this.csrf().pipe(
      switchMap(() => this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, request)),
      tap((r) =>
        this.user.set({
          idUsuario: r.idUsuario,
          nombre: r.nombre,
          apellido: r.apellido,
          role: r.role,
          requiereCambiarPassword: r.requiereCambiarPassword,
        }),
      ),
    );
  }
  restoreSession(): Observable<AuthUser | null> {
    if (this.user()) return of(this.user());
    if (!this.restoreRequest)
      this.restoreRequest = this.http
        .get<CurrentUserResponse>(`${environment.apiUrl}/auth/me`)
        .pipe(
          map((r) => ({
            idUsuario: r.idUsuario,
            nombre: r.nombre,
            apellido: r.apellido,
            role: r.role,
            requiereCambiarPassword: r.requiereCambiarPassword,
          })),
          tap((u) => this.user.set(u)),
          catchError(() => {
            this.user.set(null);
            return of(null);
          }),
          finalize(() => (this.restoreRequest = undefined)),
          shareReplay(1),
        );
    return this.restoreRequest;
  }
  logout(): Observable<void> {
    return this.csrf().pipe(
      switchMap(() => this.http.post<void>(`${environment.apiUrl}/auth/logout`, {})),
      finalize(() => this.clearAuthentication()),
    );
  }
  changePassword(request: ChangePasswordRequest): Observable<LoginResponse> {
    return this.csrf().pipe(
      switchMap(() =>
        this.http.post<LoginResponse>(`${environment.apiUrl}/auth/change-password`, request),
      ),
      tap((response) =>
        this.user.set({
          idUsuario: response.idUsuario,
          nombre: response.nombre,
          apellido: response.apellido,
          role: response.role,
          requiereCambiarPassword: response.requiereCambiarPassword,
        }),
      ),
    );
  }
  forgotPassword(request: ForgotPasswordRequest): Observable<ForgotPasswordResponse> {
    return this.csrf().pipe(
      switchMap(() =>
        this.http.post<ForgotPasswordResponse>(
          `${environment.apiUrl}/auth/forgot-password`,
          request,
        ),
      ),
    );
  }
  resetPassword(request: ResetPasswordRequest): Observable<void> {
    return this.csrf().pipe(
      switchMap(() =>
        this.http.post<void>(`${environment.apiUrl}/auth/reset-password`, request),
      ),
      tap(() => this.clearAuthentication()),
    );
  }
  clearAuthentication(): void {
    this.user.set(null);
  }
  private csrf() {
    return this.http.get(`${environment.apiUrl}/auth/csrf`);
  }
}
