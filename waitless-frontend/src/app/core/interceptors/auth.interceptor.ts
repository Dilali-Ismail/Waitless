import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // On n'ajoute pas le token pour l'appel au token endpoint de Keycloak
  if (token && !req.url.includes('/protocol/openid-connect/token')) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Ne pas déconnecter sur un échec de login (401 sur /token)
      if (error.status === 401 && !req.url.includes('/protocol/openid-connect/token')) {
        authService.logout();
      }
      return throwError(() => error);
    }),
  );
};
