import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { CompanyService } from '../../../core/services/company.service';
import { UserService } from '../../../core/services/user.service';
import { RegisterAgentRequest } from '../../../models';

@Component({
  selector: 'app-company-agents',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './company-agents.component.html',
})
export class CompanyAgentsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly companyService = inject(CompanyService);
  readonly userService = inject(UserService);

  readonly submitting = signal(false);
  readonly successMessage = signal<string | null>(null);

  readonly agentForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  submit(): void {
    if (this.agentForm.invalid) {
      this.agentForm.markAllAsTouched();
      return;
    }

    this.successMessage.set(null);
    this.userService.error.set(null);
    this.submitting.set(true);

    const formValue = this.agentForm.getRawValue();
    const email = this.auth.getUserEmail()?.toLowerCase() ?? '';

    this.companyService.getCompanies().pipe(
      switchMap((companies) => {
        const company = companies.find((c) => (c.email ?? '').toLowerCase() === email) ?? null;
        const payload: RegisterAgentRequest = { ...formValue, companyId: company?.id };
        return this.userService.registerAgent(payload);
      }),
      finalize(() => this.submitting.set(false)),
    ).subscribe({
      next: (user) => {
        this.successMessage.set(
          `Agent « ${user.name} » créé avec succès (${user.email}). Il peut se connecter avec son email et mot de passe.`,
        );
        this.agentForm.reset({ name: '', email: '', password: '' });
      },
    });
  }
}
