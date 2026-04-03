import { AsyncPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable, Subject, merge, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { CompanyService } from '../../../core/services/company.service';
import { QueueService } from '../../../core/services/queue.service';
import { CounterService } from '../../../core/services/counter.service';
import { Company, Queue, Counter } from '../../../models';

interface CountersViewData {
  company: Company | null;
  queues: Queue[];
  countersMap: Record<number, Counter[]>;
  missingCompany: boolean;
  loadFailed: boolean;
}

@Component({
  selector: 'app-company-counters',
  standalone: true,
  imports: [RouterLink, AsyncPipe, ReactiveFormsModule],
  templateUrl: './company-counters.component.html',
})
export class CompanyCountersComponent {
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  readonly companyService = inject(CompanyService);
  readonly queueService = inject(QueueService);
  readonly counterService = inject(CounterService);

  private readonly reload$ = new Subject<void>();

  readonly vm$: Observable<CountersViewData> = merge(of(undefined), this.reload$).pipe(
    switchMap(() => this.loadVm()),
  );

  readonly selectedQueueId = signal<number | null>(null);

  readonly counterForm = this.fb.nonNullable.group({
    counterNumber: [1, [Validators.required, Validators.min(1)]],
    queueId: [0, [Validators.required, Validators.min(1)]],
  });

  private loadVm(): Observable<CountersViewData> {
    return this.companyService.getCompanies().pipe(
      switchMap((companies) => {
        const email = this.auth.getUserEmail()?.toLowerCase() ?? '';
        const company =
          email.length > 0
            ? companies.find((c) => (c.email ?? '').toLowerCase().trim() === email) ?? null
            : null;

        if (!company?.id) {
          return of<CountersViewData>({
            company, queues: [], countersMap: {}, missingCompany: !company, loadFailed: false,
          });
        }

        return this.queueService.getQueuesByCompany(company.id).pipe(
          switchMap((queues) => {
            if (!queues.length) {
              return of<CountersViewData>({
                company, queues, countersMap: {}, missingCompany: false, loadFailed: false,
              });
            }

            return this.loadCountersForQueues(queues).pipe(
              map((countersMap): CountersViewData => ({
                company, queues, countersMap, missingCompany: false, loadFailed: false,
              })),
            );
          }),
        );
      }),
      catchError(() =>
        of<CountersViewData>({
          company: null, queues: [], countersMap: {}, missingCompany: false, loadFailed: true,
        }),
      ),
    );
  }

  private loadCountersForQueues(queues: Queue[]): Observable<Record<number, Counter[]>> {
    const ids = queues.map((q) => q.id!).filter((id) => id != null);
    if (!ids.length) {
      return of({});
    }

    return new Observable<Record<number, Counter[]>>((subscriber) => {
      const result: Record<number, Counter[]> = {};
      let completed = 0;

      ids.forEach((queueId) => {
        this.counterService.getCountersByQueue(queueId).subscribe({
          next: (counters) => {
            result[queueId] = counters;
            completed++;
            if (completed === ids.length) {
              subscriber.next(result);
              subscriber.complete();
            }
          },
          error: () => {
            result[queueId] = [];
            completed++;
            if (completed === ids.length) {
              subscriber.next(result);
              subscriber.complete();
            }
          },
        });
      });
    });
  }

  private refreshList(): void {
    this.reload$.next();
  }

  selectQueue(queueId: number): void {
    this.selectedQueueId.set(queueId);
    this.counterForm.patchValue({ queueId });
  }

  submitCounter(): void {
    if (this.counterForm.invalid) {
      this.counterForm.markAllAsTouched();
      return;
    }

    this.counterService.error.set(null);
    const raw = this.counterForm.getRawValue();

    this.counterService
      .createCounter({ counterNumber: raw.counterNumber, queueId: raw.queueId })
      .subscribe({
        next: () => {
          this.counterForm.patchValue({ counterNumber: raw.counterNumber + 1 });
          this.refreshList();
        },
      });
  }

  openCounter(id: number): void {
    this.counterService.error.set(null);
    this.counterService.openCounter(id).subscribe({ next: () => this.refreshList() });
  }

  closeCounter(id: number): void {
    this.counterService.error.set(null);
    this.counterService.closeCounter(id).subscribe({ next: () => this.refreshList() });
  }

  deleteCounter(c: Counter): void {
    if (c.id == null) return;
    if (!confirm(`Supprimer le guichet n°${c.counterNumber} ?`)) return;
    this.counterService.error.set(null);
    this.counterService.deleteCounter(c.id).subscribe({ next: () => this.refreshList() });
  }

  getCounters(countersMap: Record<number, Counter[]>, queueId: number): Counter[] {
    return countersMap[queueId] ?? [];
  }
}
