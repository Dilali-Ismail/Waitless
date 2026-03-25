import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, map, tap, throwError } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  
  private readonly TOKEN_KEY = 'auth_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  
  // Note: Keycloak configuration - ideally these would be in environment files
  private keycloakUrl = 'http://keycloak:8080/realms/waitless-realm/protocol/openid-connect/token';
  private clientId = 'waitless-frontend';

  private isLoggedInSubject = new BehaviorSubject<boolean>(this.hasToken());
  isLoggedIn$ = this.isLoggedInSubject.asObservable();

  private currentUserSubject = new BehaviorSubject<any>(this.decodeToken());
  currentUser$ = this.currentUserSubject.asObservable();

  login(email: string, password: string): Observable<any> {
    const payload = new URLSearchParams();
    payload.set('client_id', this.clientId);
    payload.set('grant_type', 'password');
    payload.set('username', email);
    payload.set('password', password);
    payload.set('scope', 'openid profile email');

    const headers = new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded');

    return this.http.post(this.keycloakUrl, payload.toString(), { headers }).pipe(
      tap((response: any) => {
        this.saveTokens(response.access_token, response.refresh_token);
        this.isLoggedInSubject.next(true);
        this.currentUserSubject.next(this.decodeToken());
      }),
      catchError(err => throwError(() => err))
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    this.isLoggedInSubject.next(false);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  hasRole(role: string): boolean {
    const user = this.currentUserSubject.value;
    if (!user || !user.realm_access || !user.realm_access.roles) {
      return false;
    }
    return user.realm_access.roles.includes(role);
  }

  private hasToken(): boolean {
    return !!localStorage.getItem(this.TOKEN_KEY);
  }

  private saveTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(this.TOKEN_KEY, accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
  }

  private decodeToken(): any {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = token.split('.')[1];
      return JSON.parse(atob(payload));
    } catch (e) {
      return null;
    }
  }
}
