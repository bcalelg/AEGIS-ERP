import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../auth/auth.service';
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService),
    router = inject(Router);
  return auth.restoreSession().pipe(
    map((user) => {
      if (!user) return router.createUrlTree(['/login']);
      return user.requiereCambiarPassword ? router.createUrlTree(['/change-password']) : true;
    }),
  );
};

export const changePasswordGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.restoreSession().pipe(
    map((user) => {
      if (!user) return router.createUrlTree(['/login']);
      return true;
    }),
  );
};
