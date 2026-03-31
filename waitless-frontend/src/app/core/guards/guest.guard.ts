import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { map, take } from 'rxjs';
import { AuthService } from '../services/auth.service';

/** Empêche un utilisateur déjà connecté d’accéder à login / register (redirige vers son dashboard). */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.isLoggedIn$.pipe(
    take(1),
    map((isLoggedIn) => {
      if (isLoggedIn) {
        router.navigateByUrl(auth.getDashboardPath());
        return false;
      }
      return true;
    }),
  );
};
