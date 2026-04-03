import { Component, inject, signal } from '@angular/core';
import { QueueService } from '../../../core/services/queue.service';
import { CounterService } from '../../../core/services/counter.service';
import { TicketService } from '../../../core/services/ticket.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { Queue, Counter, Ticket } from '../../../models';

@Component({
  selector: 'app-agent-dashboard',
  standalone: true,
  imports: [],
  templateUrl: './agent-dashboard.component.html',
})
export class AgentDashboardComponent {
  private readonly queueService = inject(QueueService);
  private readonly counterService = inject(CounterService);
  readonly ticketService = inject(TicketService);
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);

  /** Étape 1 : l'agent choisit parmi les files actives. */
  readonly queues = signal<Queue[]>([]);
  readonly selectedQueue = signal<Queue | null>(null);

  /** Étape 2 : l'agent choisit son guichet dans la file sélectionnée. */
  readonly counters = signal<Counter[]>([]);
  readonly selectedCounter = signal<Counter | null>(null);

  /** Tickets en attente dans la file. */
  readonly waitingTickets = signal<Ticket[]>([]);

  /** Ticket actuellement appelé par cet agent (CALLED). */
  readonly currentTicket = signal<Ticket | null>(null);

  /** Compteur de tickets traités dans cette session. */
  readonly sessionCount = signal(0);

  readonly errorMsg = signal<string | null>(null);

  constructor() {
    this.loadActiveQueues();
  }

  private loadActiveQueues(): void {
    const userId = this.authService.getUserId();
    if (!userId) {
      this.errorMsg.set("Impossible de récupérer votre identifiant.");
      return;
    }

    this.userService.getUserById(userId).subscribe({
      next: (user) => {
        if (!user.companyId) {
          this.errorMsg.set("Votre compte agent n'est pas encore relié à une entreprise. Contactez votre administrateur.");
          return;
        }
        this.queueService.getQueuesByCompany(user.companyId).subscribe({
          next: (q) => this.queues.set(q.filter((queue) => queue.isActive === true)),
          error: () => this.errorMsg.set('Impossible de charger les files de votre entreprise.'),
        });
      },
      error: () => this.errorMsg.set('Impossible de récupérer votre profil.'),
    });
  }

  selectQueue(queue: Queue): void {
    this.selectedQueue.set(queue);
    this.selectedCounter.set(null);
    this.currentTicket.set(null);
    this.waitingTickets.set([]);
    this.errorMsg.set(null);

    if (queue.id == null) return;
    this.counterService.getCountersByQueue(queue.id).subscribe({
      next: (c) => this.counters.set(c),
    });
  }

  selectCounter(counter: Counter): void {
    this.selectedCounter.set(counter);
    this.currentTicket.set(null);
    this.errorMsg.set(null);
    this.refreshWaiting();
  }

  refreshWaiting(): void {
    const q = this.selectedQueue();
    if (!q?.id) return;
    this.ticketService.getWaitingTickets(q.id).subscribe({
      next: (tickets) => this.waitingTickets.set(tickets),
    });
  }

  callNext(): void {
    const q = this.selectedQueue();
    const c = this.selectedCounter();
    if (!q?.id || !c) return;

    this.errorMsg.set(null);
    this.ticketService.callNextTicket(q.id, c.counterNumber).subscribe({
      next: (ticket) => {
        this.currentTicket.set(ticket);
        this.refreshWaiting();
      },
      error: () => this.errorMsg.set('Aucun ticket en attente ou erreur.'),
    });
  }

  markStatus(status: 'COMPLETED' | 'ABSENT' | 'CANCELLED'): void {
    const ticket = this.currentTicket();
    if (!ticket?.id) return;

    this.errorMsg.set(null);
    this.ticketService.updateTicketStatus(ticket.id, status).subscribe({
      next: () => {
        if (status === 'COMPLETED' || status === 'ABSENT') {
          this.sessionCount.update((n) => n + 1);
        }
        this.currentTicket.set(null);
        this.refreshWaiting();
      },
      error: () => this.errorMsg.set('Erreur lors de la mise à jour du ticket.'),
    });
  }

  resetSession(): void {
    this.selectedQueue.set(null);
    this.selectedCounter.set(null);
    this.currentTicket.set(null);
    this.waitingTickets.set([]);
    this.sessionCount.set(0);
    this.errorMsg.set(null);
  }
}
