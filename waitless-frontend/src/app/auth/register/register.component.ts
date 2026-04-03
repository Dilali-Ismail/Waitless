import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ClientRegistrationService } from '../../core/services/client-registration.service';
import { Company, RegisterClientRequest } from '../../models';
import { finalize } from 'rxjs/operators';
import { CompanyService } from '../../core/services/company.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  readonly registration = inject(ClientRegistrationService);
  readonly companyService = inject(CompanyService);
  private readonly router = inject(Router);

  readonly activeTab = signal<'client' | 'company'>('client');
  readonly submittingClient = signal(false);
  readonly submittingCompany = signal(false);
  readonly successMessage = signal<string | null>(null);
  readonly companyLogoFile = signal<File | null>(null);
  readonly companyLogoPreview = signal<string | null>(null);

  // Formulaire client (avec password)
  protected readonly clientForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    phoneNumber: ['', [Validators.required, Validators.minLength(6)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  // Formulaire entreprise: même infos + password pour créer le compte Keycloak.
  protected readonly companyForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    category: ['', [Validators.required]],
    address: [''],
    phoneNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  selectTab(tab: 'client' | 'company'): void {
    this.activeTab.set(tab);
    this.successMessage.set(null);
    this.registration.error.set(null);
    this.companyService.error.set(null);
    this.companyLogoFile.set(null);
    this.companyLogoPreview.set(null);
  }

  onCompanyLogoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.companyLogoFile.set(file);
    if (file) {
      const reader = new FileReader();
      reader.onload = () => this.companyLogoPreview.set(reader.result as string);
      reader.readAsDataURL(file);
    } else {
      this.companyLogoPreview.set(null);
    }
  }

  submitClient(): void {
    if (this.clientForm.invalid) {
      this.clientForm.markAllAsTouched();
      return;
    }

    this.successMessage.set(null);
    this.submittingClient.set(true);

    const payload: RegisterClientRequest = this.clientForm.getRawValue();

    this.registration
      .registerClient(payload)
      .pipe(finalize(() => this.submittingClient.set(false)))
      .subscribe({
        next: () => {
          this.router.navigate(['/login']);
        },
      });
  }

  submitCompany(): void {
    if (this.companyForm.invalid) {
      this.companyForm.markAllAsTouched();
      return;
    }

    this.successMessage.set(null);
    this.submittingCompany.set(true);

    const payload: Company = this.companyForm.getRawValue();
    this.companyService
      .registerCompany(payload, this.companyLogoFile() ?? undefined)
      .pipe(finalize(() => this.submittingCompany.set(false)))
      .subscribe({
        next: () => {
          this.successMessage.set(
            "Entreprise enregistrée avec succès. Votre demande est en attente de validation admin.",
          );
          setTimeout(() => this.router.navigate(['/login']), 900);
        },
      });
  }
}
