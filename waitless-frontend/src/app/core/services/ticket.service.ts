import { Injectable } from '@angular/core';
import { Observable, catchError } from 'rxjs';
import { Ticket, CreateTicketRequest } from '../models';
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
      this.handleTap<Ticket>(),
      catchError(err => this.handleError(err))
    );
  }

  callNextTicket(queueId: number, counterNumber: number): Observable<Ticket> {
    this.loading.set(true);
    return this.http.post<Ticket>(`${this.apiUrl}/tickets/call`, { queueId, counterNumber }).pipe(
      this.handleTap(),
      catchError(err => this.handleError(err))
    );
  }

  updateTicketStatus(id: number, status: string): Observable<Ticket> {
    this.loading.set(true);
    return this.http.put<Ticket>(`${this.apiUrl}/tickets/${id}/status`, { status }).pipe(
      this.handleTap(),
      catchError(err => this.handleError(err))
    );
  }
}
