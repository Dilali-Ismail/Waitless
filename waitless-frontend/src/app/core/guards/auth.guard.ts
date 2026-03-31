import { inject } from '@angular/core';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map, take } from 'rxjs';

export const authGuard: CanActivateFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.isLoggedIn$.pipe(
    take(1),
    map(isLoggedIn => {
      if (!isLoggedIn) {
        router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
        return false;
      }

      // Vérification des rôles si définis dans la route
      const requiredRoles = route.data['roles'] as Array<string> | undefined;
      if (requiredRoles && requiredRoles.length > 0) {
        const hasRequiredRole = requiredRoles.some(role => authService.hasRole(role));
        if (!hasRequiredRole) {
          // Rediriger vers la page du rôle courant
          router.navigateByUrl(authService.getDashboardPath());
          return false;
        }
      }

      return true;
    })
  );
};
