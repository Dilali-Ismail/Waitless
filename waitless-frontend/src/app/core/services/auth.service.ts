import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, tap, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly TOKEN_KEY = 'auth_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';

  private isLoggedInSubject = new BehaviorSubject<boolean>(this.hasToken());
  isLoggedIn$ = this.isLoggedInSubject.asObservable();

  private currentUserSubject = new BehaviorSubject<any>(this.decodeToken());
  currentUser$ = this.currentUserSubject.asObservable();

  /** redirect_uri OAuth (doit être identique entre inscription et échange de code) */
  getOAuthRedirectUri(): string {
    if (typeof window === 'undefined') {
      return 'http://localhost:4200/login';
    }
    return `${window.location.origin}/login`;
  }

  /** Lien Keycloak : page d’inscription (navigateur quitte l’app) */
  getKeycloakRegistrationUrl(): string {
    const redirect = encodeURIComponent(this.getOAuthRedirectUri());
    const { publicBaseUrl, realm, clientId } = environment.keycloak;
    return `${publicBaseUrl}/realms/${realm}/protocol/openid-connect/registrations?client_id=${encodeURIComponent(
      clientId,
    )}&response_type=code&scope=openid&redirect_uri=${redirect}`;
  }

  /** Après inscription (ou login OIDC), Keycloak redirige vers /login?code=… */
  exchangeAuthorizationCode(code: string): Observable<unknown> {
    const payload = new URLSearchParams();
    payload.set('grant_type', 'authorization_code');
    payload.set('client_id', environment.keycloak.clientId);
    payload.set('code', code);
    payload.set('redirect_uri', this.getOAuthRedirectUri());
    if (environment.keycloak.clientSecret) {
      payload.set('client_secret', environment.keycloak.clientSecret);
    }

    const headers = new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded');

    return this.http.post(environment.keycloak.tokenUrl, payload.toString(), { headers }).pipe(
      tap((response: any) => {
        this.saveTokens(response.access_token, response.refresh_token);
        this.isLoggedInSubject.next(true);
        this.currentUserSubject.next(this.decodeToken());
      }),
      catchError((err) => throwError(() => err)),
    );
  }

  /** Grant « password » (identifiant Keycloak + mot de passe) */
  login(username: string, password: string): Observable<unknown> {
    const payload = new URLSearchParams();
    payload.set('client_id', environment.keycloak.clientId);
    payload.set('grant_type', 'password');
    payload.set('username', username.trim());
    payload.set('password', password);
    payload.set('scope', 'openid profile email');
    if (environment.keycloak.clientSecret) {
      payload.set('client_secret', environment.keycloak.clientSecret);
    }

    const headers = new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded');

    return this.http.post(environment.keycloak.tokenUrl, payload.toString(), { headers }).pipe(
      tap((response: any) => {
        this.saveTokens(response.access_token, response.refresh_token);
        this.isLoggedInSubject.next(true);
        this.currentUserSubject.next(this.decodeToken());
      }),
      catchError((err) => throwError(() => err)),
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

  isCompanyAdminRole(): boolean {
    const r = this.getPrimaryRole();
    return r === 'CO_ADMIN' || r === 'COMPANY_ADMIN';
  }

  getPrimaryRole(): string | null {
    const user = this.currentUserSubject.value;
    const roles: string[] = user?.realm_access?.roles ?? [];
    const order = ['ADMIN', 'COMPANY_ADMIN', 'CO_ADMIN', 'AGENT', 'CLIENT'];
    for (const r of order) {
      if (roles.includes(r)) {
        return r;
      }
    }
    return roles.length ? roles[0] : null;
  }

  getDashboardPath(): string {
    const role = this.getPrimaryRole();
    switch (role) {
      case 'ADMIN':
        return '/admin/dashboard';
      case 'AGENT':
        return '/agent/dashboard';
      case 'CO_ADMIN':
      case 'COMPANY_ADMIN':
        return '/company/dashboard';
      case 'CLIENT':
      default:
        return '/home';
    }
  }

  getUserId(): string | null {
    const u = this.decodeToken();
    return u?.sub ?? null;
  }

  getUserName(): string {
    const u = this.decodeToken();
    return u?.name ?? u?.preferred_username ?? u?.email ?? 'Client';
  }

  getUserEmail(): string | null {
    const u = this.decodeToken();
    if (!u) {
      return null;
    }
    const raw = u['email'] ?? u['preferred_username'];
    return typeof raw === 'string' && raw.trim() ? raw.trim() : null;
  }


  getUserInitial(): string {
    const u = this.currentUserSubject.value as Record<string, string | undefined> | null;
    if (!u) {
      return '?';
    }
    const raw =
      (typeof u['name'] === 'string' && u['name']) ||
      (typeof u['preferred_username'] === 'string' && u['preferred_username']) ||
      (typeof u['email'] === 'string' && u['email']) ||
      '';
    const letter = raw.trim().charAt(0);
    return letter ? letter.toUpperCase() : '?';
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
