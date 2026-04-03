import { Component, inject, signal, computed } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { CompanyService } from '../../core/services/company.service';
import { QueueService } from '../../core/services/queue.service';
import { TicketService } from '../../core/services/ticket.service';
import { AuthService } from '../../core/services/auth.service';
import { Company, Queue, Ticket } from '../../models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './home.component.html',
})
export class HomeComponent {
  private readonly companyService = inject(CompanyService);
  private readonly queueService = inject(QueueService);
  readonly ticketService = inject(TicketService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly allCompanies = signal<Company[]>([]);
  readonly searchText = signal('');
  readonly selectedCategory = signal('');
  readonly currentPage = signal(1);
  readonly pageSize = 6;

  readonly categories = computed(() => {
    const cats = new Set(this.allCompanies().map((c) => c.category));
    return Array.from(cats).sort();
  });

  readonly filteredCompanies = computed(() => {
    let list = this.allCompanies().filter((c) => c.status === 'ACTIVE');
    const cat = this.selectedCategory();
    if (cat) {
      list = list.filter((c) => c.category === cat);
    }
    const search = this.searchText().toLowerCase().trim();
    if (search) {
      list = list.filter(
        (c) =>
          c.name.toLowerCase().includes(search) ||
          c.category.toLowerCase().includes(search) ||
          (c.address ?? '').toLowerCase().includes(search),
      );
    }
    return list;
  });

  readonly totalPages = computed(() => Math.max(1, Math.ceil(this.filteredCompanies().length / this.pageSize)));

  readonly pagedCompanies = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.filteredCompanies().slice(start, start + this.pageSize);
  });

  readonly selectedCompany = signal<Company | null>(null);
  readonly companyQueues = signal<Queue[]>([]);

  readonly createdTicket = signal<Ticket | null>(null);
  readonly errorMsg = signal<string | null>(null);

  constructor() {
    this.companyService.getCompanies().subscribe({
      next: (c) => this.allCompanies.set(c),
    });
  }

  onSearch(value: string): void {
    this.searchText.set(value);
    this.currentPage.set(1);
  }

  onCategoryChange(cat: string): void {
    this.selectedCategory.set(cat);
    this.currentPage.set(1);
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages()) {
      this.currentPage.set(page);
    }
  }

  viewCompany(company: Company): void {
    this.selectedCompany.set(company);
    this.createdTicket.set(null);
    this.errorMsg.set(null);
    if (company.id == null) return;
    this.queueService.getQueuesByCompany(company.id).subscribe({
      next: (q) => this.companyQueues.set(q.filter((queue) => queue.isActive)),
    });
  }

  backToList(): void {
    this.selectedCompany.set(null);
    this.companyQueues.set([]);
    this.createdTicket.set(null);
    this.errorMsg.set(null);
  }

  createTicket(queue: Queue): void {
    if (!this.auth.hasRole('CLIENT')) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/home' } });
      return;
    }

    const userId = this.auth.getUserId();
    const clientName = this.auth.getUserName();
    if (!userId || !queue.id) return;

    this.errorMsg.set(null);
    this.ticketService
      .createTicket({ queueId: queue.id, userId, clientName })
      .subscribe({
        next: (ticket) => this.createdTicket.set(ticket),
        error: () => this.errorMsg.set('Impossible de créer le ticket. Vous avez peut-être déjà un ticket actif dans cette file.'),
      });
  }

  pagesArray(): number[] {
    return Array.from({ length: this.totalPages() }, (_, i) => i + 1);
  }

  readonly brokenLogos = signal<Set<number>>(new Set());

  onLogoError(companyId: number | undefined): void {
    if (companyId == null) return;
    const next = new Set(this.brokenLogos());
    next.add(companyId);
    this.brokenLogos.set(next);
  }
}
