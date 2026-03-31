import { Injectable } from '@angular/core';
import { Observable, catchError, map } from 'rxjs';
import { Ticket, CreateTicketRequest } from '../../models';
import { BaseService } from './base.service';

@Injectable({ providedIn: 'root' })
export class TicketService extends BaseService {
  createTicket(request: CreateTicketRequest): Observable<Ticket> {
    this.loading.set(true);
    return this.http.post<Ticket>(`${this.apiUrl}/tickets`, request).pipe(
      this.handleTap<Ticket>(),
      catchError(err => this.handleError(err))
    );
  }

  getMyTickets(userId: string): Observable<Ticket[]> {
    this.loading.set(true);
    return this.http.get<Ticket[]>(`${this.apiUrl}/tickets/me?userId=${userId}`).pipe(
      this.handleTap<Ticket[]>(),
      catchError(err => this.handleError(err))
    );
  }

  callNextTicket(queueId: number, counterNumber: number): Observable<Ticket> {
    this.loading.set(true);
    return this.http.post<Ticket>(`${this.apiUrl}/tickets/call`, { queueId, counterNumber }).pipe(
      this.handleTap<Ticket>(),
      catchError(err => this.handleError(err))
    );
  }

  updateTicketStatus(id: number, status: string): Observable<Ticket> {
    this.loading.set(true);
    return this.http.put<Ticket>(`${this.apiUrl}/tickets/${id}/status`, { status }).pipe(
      this.handleTap<Ticket>(),
      catchError(err => this.handleError(err))
    );
  }

  cancelTicket(id: number): Observable<Ticket> {
    this.loading.set(true);
    return this.http.delete<Ticket>(`${this.apiUrl}/tickets/${id}`).pipe(
      this.handleTap<Ticket>(),
      catchError(err => this.handleError(err))
    );
  }

  getWaitingTickets(queueId: number): Observable<Ticket[]> {
    this.loading.set(true);
    return this.http.get<Ticket[]>(`${this.apiUrl}/tickets/queue/${queueId}/waiting`).pipe(
      this.handleTap<Ticket[]>(),
      catchError(err => this.handleError(err))
    );
  }

  getWaitingCount(queueId: number): Observable<number> {
    return this.getWaitingTickets(queueId).pipe(map((tickets) => tickets.length));
  }

  getServedTodayCount(queueId: number): Observable<number> {
    this.loading.set(true);
    return this.http.get<number>(`${this.apiUrl}/tickets/queue/${queueId}/served-today/count`).pipe(
      this.handleTap<number>(),
      catchError(err => this.handleError(err))
    );
  }
}
