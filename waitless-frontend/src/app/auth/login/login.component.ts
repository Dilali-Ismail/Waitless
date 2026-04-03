import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
})
export class LoginComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    identifier: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  ngOnInit(): void {
    const code = this.route.snapshot.queryParamMap.get('code');
    if (!code) {
      return;
    }
    this.submitting.set(true);
    this.errorMessage.set(null);
    this.auth
      .exchangeAuthorizationCode(code)
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: () => {
          this.router.navigateByUrl(this.auth.getDashboardPath(), { replaceUrl: true });
        },
        error: (err: unknown) => {
          this.errorMessage.set(this.extractErrorMessage(err, 'Échec de la connexion après inscription.'));
        },
      });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.errorMessage.set(null);
    this.submitting.set(true);
    const { identifier, password } = this.form.getRawValue();

    this.auth
      .login(identifier, password)
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: () => {
          const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
          if (returnUrl?.startsWith('/') && !returnUrl.startsWith('//')) {
            this.router.navigateByUrl(returnUrl);
          } else {
            this.router.navigateByUrl(this.auth.getDashboardPath());
          }
        },
        error: (err: unknown) => {
          this.errorMessage.set(
            this.extractErrorMessage(err, 'Identifiant ou mot de passe incorrect.'),
          );
        },
      });
  }

  private extractErrorMessage(err: unknown, fallback: string): string {
    const e = err as { error?: { error_description?: string; error?: string }; message?: string; status?: number };
    const desc = e?.error?.error_description;
    if (typeof desc === 'string' && desc.trim()) {
      return desc;
    }
    if (e?.status === 0) {
      return 'Réseau bloqué (CORS) ou Keycloak injoignable. Vérifiez que Keycloak tourne sur le port 8180 et utilisez `ng serve` avec le proxy.';
    }
    if (typeof e?.error?.error === 'string' && e.error.error === 'invalid_client') {
      return 'Client OAuth invalide (client_id / secret). Vérifiez environment.ts et le realm Keycloak.';
    }
    if (typeof e?.message === 'string' && e.message) {
      return e.message;
    }
    return fallback;
  }
}
