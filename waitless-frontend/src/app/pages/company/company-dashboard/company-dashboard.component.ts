import { AsyncPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { CompanyService } from '../../../core/services/company.service';
import { QueueService } from '../../../core/services/queue.service';
import { TicketService } from '../../../core/services/ticket.service';
import { CompanyViewData, Queue } from '../../../models';

type CompanyDashboardVm = CompanyViewData & {
  waitingClients: number;
  servedToday: number;
};

@Component({
  selector: 'app-company-dashboard',
  standalone: true,
  imports: [RouterLink, AsyncPipe],
  templateUrl: './company-dashboard.component.html',
})
export class CompanyDashboardComponent {
  private readonly auth = inject(AuthService);
  private readonly companyService = inject(CompanyService);
  private readonly queueService = inject(QueueService);
  private readonly ticketService = inject(TicketService);

  readonly vm$: Observable<CompanyDashboardVm> = this.companyService.getCompanies().pipe(
    switchMap((companies) => {
      const email = this.auth.getUserEmail()?.toLowerCase() ?? '';
      const company =
        email.length > 0
          ? companies.find((c) => (c.email ?? '').toLowerCase().trim() === email) ?? null
          : null;

      if (!company?.id) {
        return of<CompanyDashboardVm>({
          company,
          queues: [],
          missingCompany: !company,
          loadFailed: false,
          waitingClients: 0,
          servedToday: 0,
        });
      }

      return this.queueService.getQueuesByCompany(company.id).pipe(
        switchMap((queues) => {
          if (!queues.length) {
            return of<CompanyDashboardVm>({
              company,
              queues,
              missingCompany: false,
              loadFailed: false,
              waitingClients: 0,
              servedToday: 0,
            });
          }

          const waitingCalls = queues.map((q) =>
            this.ticketService.getWaitingCount(q.id!).pipe(catchError(() => of(0))),
          );
          const servedTodayCalls = queues.map((q) =>
            this.ticketService.getServedTodayCount(q.id!).pipe(catchError(() => of(0))),
          );

          return forkJoin({
            waitingCounts: forkJoin(waitingCalls),
            servedTodayCounts: forkJoin(servedTodayCalls),
          }).pipe(
            map(({ waitingCounts, servedTodayCounts }): CompanyDashboardVm => ({
              company,
              queues,
              missingCompany: false,
              loadFailed: false,
              waitingClients: waitingCounts.reduce((sum, n) => sum + n, 0),
              servedToday: servedTodayCounts.reduce((sum, n) => sum + n, 0),
            })),
          );
        }),
      );
    }),
    catchError(() =>
      of<CompanyDashboardVm>({
        company: null,
        queues: [],
        missingCompany: false,
        loadFailed: true,
        waitingClients: 0,
        servedToday: 0,
      }),
    ),
  );

  openQueuesCount(queues: Queue[]): number {
    return queues.filter((q) => q.isActive === true).length;
  }

  formatAvgServiceMinutes(queues: Queue[]): string {
    if (!queues.length) return '—';
    const sum = queues.reduce((acc, q) => acc + (q.averageServiceTime ?? 0), 0);
    return `${Math.round(sum / queues.length)} min`;
  }
}
