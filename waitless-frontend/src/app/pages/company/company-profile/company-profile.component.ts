import { AsyncPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { CompanyService } from '../../../core/services/company.service';
import { Company } from '../../../models';

@Component({
  selector: 'app-company-profile',
  standalone: true,
  imports: [RouterLink, AsyncPipe, ReactiveFormsModule],
  templateUrl: './company-profile.component.html',
})
export class CompanyProfileComponent {
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  readonly companyService = inject(CompanyService);

  readonly saving = signal(false);
  readonly successMessage = signal<string | null>(null);

  readonly profileForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    category: ['', [Validators.required]],
    address: [''],
    phoneNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
    email: [{ value: '', disabled: true }],
  });

  private companyId: number | null = null;

  readonly company$: Observable<Company | null> = this.companyService.getCompanies().pipe(
    map((companies) => {
      const email = this.auth.getUserEmail()?.toLowerCase() ?? '';
      return email.length > 0
        ? companies.find((c) => (c.email ?? '').toLowerCase().trim() === email) ?? null
        : null;
    }),
    map((company) => {
      if (company) {
        this.companyId = company.id ?? null;
        this.profileForm.patchValue({
          name: company.name,
          category: company.category,
          address: company.address ?? '',
          phoneNumber: company.phoneNumber ?? '',
          email: company.email,
        });
      }
      return company;
    }),
    catchError(() => of(null)),
  );

  submit(): void {
    if (this.profileForm.invalid || this.companyId == null) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.successMessage.set(null);
    this.companyService.error.set(null);
    this.saving.set(true);

    const raw = this.profileForm.getRawValue();

    this.companyService
      .updateCompany(this.companyId, {
        name: raw.name,
        category: raw.category,
        address: raw.address || undefined,
        phoneNumber: raw.phoneNumber,
        email: raw.email,
      })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.successMessage.set('Profil mis à jour avec succès.');
        },
        error: () => {
          this.saving.set(false);
        },
      });
  }
}
