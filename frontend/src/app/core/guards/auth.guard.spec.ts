import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { firstValueFrom, Observable, of } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { authGuard, changePasswordGuard } from './auth.guard';

describe('authentication navigation guards', () => {
  const auth = {
    restoreSession: vi.fn(),
  };
  const router = {
    createUrlTree: vi.fn((commands: string[]) => ({ commands }) as unknown as UrlTree),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('redirects an unauthenticated user to login', async () => {
    auth.restoreSession.mockReturnValue(of(null));

    const result = await execute(authGuard);

    expect(result).toEqual({ commands: ['/login'] });
  });

  it('redirects a user with mandatory change away from normal routes', async () => {
    auth.restoreSession.mockReturnValue(
      of({ idUsuario: 'admin', role: 'Administrador', requiereCambiarPassword: true }),
    );

    const result = await execute(authGuard);

    expect(result).toEqual({ commands: ['/change-password'] });
  });

  it('allows authenticated users into change-password in mandatory and voluntary modes', async () => {
    auth.restoreSession.mockReturnValue(
      of({ idUsuario: 'admin', role: 'Administrador', requiereCambiarPassword: true }),
    );
    expect(await execute(changePasswordGuard)).toBe(true);

    auth.restoreSession.mockReturnValue(
      of({ idUsuario: 'admin', role: 'Administrador', requiereCambiarPassword: false }),
    );
    expect(await execute(changePasswordGuard)).toBe(true);
  });

  function execute(guard: typeof authGuard): Promise<boolean | UrlTree> {
    const result = TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    ) as Observable<boolean | UrlTree>;
    return firstValueFrom(result);
  }
});
