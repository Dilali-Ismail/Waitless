import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { DatePipe } from '@angular/common';
import { TicketService } from '../../../core/services/ticket.service';
import { QueueService } from '../../../core/services/queue.service';
import { CompanyService } from '../../../core/services/company.service';
import { AuthService } from '../../../core/services/auth.service';
import { Ticket, Queue, Company } from '../../../models';

interface EnrichedTicket extends Ticket {
  queueName?: string;
  companyName?: string;
  companyLogoUrl?: string;
}

@Component({
  selector: 'app-client-tickets',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './client-tickets.component.html',
})
export class ClientTicketsComponent implements OnInit, OnDestroy {
  private readonly ticketService = inject(TicketService);
  private readonly queueService = inject(QueueService);
  private readonly companyService = inject(CompanyService);
  private readonly auth = inject(AuthService);

  readonly activeTicket = signal<EnrichedTicket | null>(null);
  readonly historyTickets = signal<EnrichedTicket[]>([]);
  readonly loadingState = signal(true);
  readonly countdown = signal('');

  private queuesCache = new Map<number, Queue>();
  private companiesCache = new Map<number, Company>();
  private timerInterval: ReturnType<typeof setInterval> | null = null;

  ngOnInit(): void {
    this.loadTickets();
  }

  ngOnDestroy(): void {
    this.clearTimer();
  }

  loadTickets(): void {
    const userId = this.auth.getUserId();
    if (!userId) return;

    this.loadingState.set(true);
    this.ticketService.getMyTickets(userId).subscribe({
      next: (tickets) => this.processTickets(tickets),
      error: () => this.loadingState.set(false),
    });
  }

  private processTickets(tickets: Ticket[]): void {
    const active = tickets.find((t) => t.status === 'WAITING' || t.status === 'CALLED') ?? null;
    const history = tickets.filter((t) => t.status !== 'WAITING' && t.status !== 'CALLED');

    const queueIds = [...new Set(tickets.map((t) => t.queueId))];
    let loaded = 0;

    if (queueIds.length === 0) {
      this.activeTicket.set(active);
      this.historyTickets.set(history);
      this.loadingState.set(false);
      return;
    }

    queueIds.forEach((qId) => {
      this.queueService.getQueueById(qId).subscribe({
        next: (queue) => {
          this.queuesCache.set(qId, queue);
          if (queue.companyId && !this.companiesCache.has(queue.companyId)) {
            this.companyService.getCompanyById(queue.companyId).subscribe({
              next: (company) => {
                this.companiesCache.set(queue.companyId, company);
                loaded++;
                if (loaded >= queueIds.length) this.enrichAndSet(active, history);
              },
              error: () => {
                loaded++;
                if (loaded >= queueIds.length) this.enrichAndSet(active, history);
              },
            });
          } else {
            loaded++;
            if (loaded >= queueIds.length) this.enrichAndSet(active, history);
          }
        },
        error: () => {
          loaded++;
          if (loaded >= queueIds.length) this.enrichAndSet(active, history);
        },
      });
    });
  }

  private enrichAndSet(active: Ticket | null, history: Ticket[]): void {
    const enrich = (t: Ticket): EnrichedTicket => {
      const queue = this.queuesCache.get(t.queueId);
      const company = queue ? this.companiesCache.get(queue.companyId) : undefined;
      return { ...t, queueName: queue?.name, companyName: company?.name, companyLogoUrl: company?.logoUrl };
    };

    this.activeTicket.set(active ? enrich(active) : null);
    this.historyTickets.set(history.map(enrich).sort((a, b) => {
      const da = a.createdAt ? new Date(a.createdAt).getTime() : 0;
      const db = b.createdAt ? new Date(b.createdAt).getTime() : 0;
      return db - da;
    }));
    this.loadingState.set(false);
    this.startCountdown();
  }

  cancelActiveTicket(): void {
    const ticket = this.activeTicket();
    if (!ticket?.id) return;
    this.ticketService.cancelTicket(ticket.id).subscribe({
      next: () => {
        this.clearTimer();
        this.loadTickets();
      },
    });
  }

  private startCountdown(): void {
    this.clearTimer();
    const ticket = this.activeTicket();
    if (!ticket?.estimatedWaitTime || !ticket.createdAt) return;

    // Normaliser la date en UTC : si le backend envoie sans 'Z', on force UTC
    const rawDate = ticket.createdAt.endsWith('Z') ? ticket.createdAt : ticket.createdAt + 'Z';
    const endTime = new Date(rawDate).getTime() + ticket.estimatedWaitTime * 60 * 1000;

    const tick = () => {
      const diff = endTime - Date.now();
      if (diff <= 0) {
        this.countdown.set('Imminent');
        this.clearTimer();
        return;
      }
      const mins = Math.floor(diff / 60000);
      const secs = Math.floor((diff % 60000) / 1000);
      this.countdown.set(`${mins}m ${secs < 10 ? '0' : ''}${secs}s`);
    };

    tick();
    this.timerInterval = setInterval(tick, 1000);
  }

  private clearTimer(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
  }

  statusLabel(status: string): string {
    const labels: Record<string, string> = {
      WAITING: 'En attente',
      CALLED: 'Appelé',
      COMPLETED: 'Terminé',
      ABSENT: 'Absent',
      CANCELLED: 'Annulé',
    };
    return labels[status] ?? status;
  }

  statusColor(status: string): string {
    const colors: Record<string, string> = {
      COMPLETED: 'text-success',
      ABSENT: 'text-warning',
      CANCELLED: 'text-danger',
    };
    return colors[status] ?? 'text-text-secondary';
  }

  statusBg(status: string): string {
    const bg: Record<string, string> = {
      WAITING:   'bg-primary',
      CALLED:    'bg-primary',
      COMPLETED: 'bg-success',
      ABSENT:    'bg-warning',
      CANCELLED: 'bg-danger',
    };
    return bg[status] ?? 'bg-text-secondary';
  }
}
