import { AsyncPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable, Subject, merge, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { CompanyService } from '../../../core/services/company.service';
import { QueueService } from '../../../core/services/queue.service';
import { CompanyViewData, Queue } from '../../../models';

@Component({
  selector: 'app-company-queues',
  standalone: true,
  imports: [RouterLink, AsyncPipe, ReactiveFormsModule],
  templateUrl: './company-queues.component.html',
})
export class CompanyQueuesComponent {
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  readonly companyService = inject(CompanyService);
  readonly queueService = inject(QueueService);

  private readonly reload$ = new Subject<void>();

  readonly vm$: Observable<CompanyViewData> = merge(of(undefined), this.reload$).pipe(
    switchMap(() => this.loadVm()),
  );

  readonly editingQueueId = signal<number | null>(null);

  readonly queueForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    description: [''],
    capacity: [50, [Validators.required, Validators.min(10), Validators.max(500)]],
    averageServiceTime: [5, [Validators.required, Validators.min(1), Validators.max(60)]],
  });

  private loadVm(): Observable<CompanyViewData> {
    return this.companyService.getCompanies().pipe(
      switchMap((companies) => {
        const email = this.auth.getUserEmail()?.toLowerCase() ?? '';
        const company =
          email.length > 0
            ? companies.find((c) => (c.email ?? '').toLowerCase().trim() === email) ?? null
            : null;

        if (!company?.id) {
          return of<CompanyViewData>({ company, queues: [], missingCompany: !company, loadFailed: false });
        }

        return this.queueService.getQueuesByCompany(company.id).pipe(
          map((queues): CompanyViewData => ({ company, queues, missingCompany: false, loadFailed: false })),
        );
      }),
      catchError(() =>
        of<CompanyViewData>({ company: null, queues: [], missingCompany: false, loadFailed: true }),
      ),
    );
  }

  private refreshList(): void {
    this.reload$.next();
  }

  submitQueue(companyId: number): void {
    if (this.queueForm.invalid) {
      this.queueForm.markAllAsTouched();
      return;
    }

    this.queueService.error.set(null);
    const raw = this.queueForm.getRawValue();
    const editId = this.editingQueueId();

    if (editId != null) {
      this.queueService.updateQueue(editId, raw).subscribe({
        next: () => { this.cancelEdit(); this.refreshList(); },
      });
      return;
    }

    this.queueService
      .createQueue({
        name: raw.name,
        description: raw.description || undefined,
        capacity: raw.capacity,
        averageServiceTime: raw.averageServiceTime,
        companyId,
      })
      .subscribe({
        next: () => {
          this.queueForm.reset({ name: '', description: '', capacity: 50, averageServiceTime: 5 });
          this.refreshList();
        },
      });
  }

  startEdit(q: Queue): void {
    if (q.id == null) return;
    this.editingQueueId.set(q.id);
    this.queueForm.patchValue({
      name: q.name,
      description: q.description ?? '',
      capacity: q.capacity,
      averageServiceTime: q.averageServiceTime,
    });
  }

  cancelEdit(): void {
    this.editingQueueId.set(null);
    this.queueForm.reset({ name: '', description: '', capacity: 50, averageServiceTime: 5 });
  }

  openQueue(id: number): void {
    this.queueService.error.set(null);
    this.queueService.openQueue(id).subscribe({ next: () => this.refreshList() });
  }

  closeQueue(id: number): void {
    this.queueService.error.set(null);
    this.queueService.closeQueue(id).subscribe({ next: () => this.refreshList() });
  }

  deleteQueue(q: Queue): void {
    if (q.id == null) return;
    if (!confirm(`Supprimer la file « ${q.name} » ?`)) return;
    this.queueService.error.set(null);
    this.queueService.deleteQueue(q.id).subscribe({ next: () => this.refreshList() });
  }
}
