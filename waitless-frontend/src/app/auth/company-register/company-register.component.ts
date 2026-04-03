import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { CompanyService } from '../../core/services/company.service';
import { Company } from '../../models';

@Component({
  selector: 'app-company-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './company-register.component.html',
})
export class CompanyRegisterComponent {
  private readonly fb = inject(FormBuilder);
  readonly companyService = inject(CompanyService);
  private readonly router = inject(Router);

  readonly submitting = signal(false);
  readonly successMessage = signal<string | null>(null);
  readonly selectedFile = signal<File | null>(null);
  readonly previewUrl = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    category: ['', [Validators.required]],
    address: [''],
    phoneNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.selectedFile.set(file);

    if (file) {
      const reader = new FileReader();
      reader.onload = () => this.previewUrl.set(reader.result as string);
      reader.readAsDataURL(file);
    } else {
      this.previewUrl.set(null);
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.successMessage.set(null);
    this.submitting.set(true);

    const payload: Company = this.form.getRawValue();
    this.companyService
      .registerCompany(payload, this.selectedFile() ?? undefined)
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: () => {
          this.successMessage.set(
            "Entreprise enregistrée avec succès. Votre demande est en attente de validation.",
          );
          setTimeout(() => this.router.navigate(['/login']), 900);
        },
      });
  }
}
