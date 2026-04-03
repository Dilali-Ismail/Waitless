import { AsyncPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { Company } from '../../../models';
import { CompanyService } from '../../../core/services/company.service';

@Component({
  selector: 'app-admin-companies',
  standalone: true,
  imports: [AsyncPipe],
  templateUrl: './admin-companies.component.html',
  styleUrl: './admin-companies.component.css',
})
export class AdminCompaniesComponent {
  readonly companyService = inject(CompanyService);
  companies$: Observable<Company[]> = this.companyService.getCompanies();
  readonly actingId = signal<number | null>(null);

  refresh(): void {
    this.companies$ = this.companyService.getCompanies();
  }

  activate(id?: number): void {
    if (!id) return;
    this.actingId.set(id);
    this.companyService.activateCompany(id).subscribe({
      next: () => {
        this.actingId.set(null);
        this.refresh();
      },
      error: () => this.actingId.set(null),
    });
  }

  suspend(id?: number): void {
    if (!id) return;
    this.actingId.set(id);
    this.companyService.suspendCompany(id).subscribe({
      next: () => {
        this.actingId.set(null);
        this.refresh();
      },
      error: () => this.actingId.set(null),
    });
  }
}
