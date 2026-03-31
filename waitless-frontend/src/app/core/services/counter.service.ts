import { Injectable } from '@angular/core';
import { Observable, catchError } from 'rxjs';
import { Counter, CreateCounterRequest } from '../../models';
import { BaseService } from './base.service';

@Injectable({ providedIn: 'root' })
export class CounterService extends BaseService {

  getCountersByQueue(queueId: number): Observable<Counter[]> {
    this.loading.set(true);
    return this.http.get<Counter[]>(`${this.apiUrl}/counters/queue/${queueId}`).pipe(
      this.handleTap<Counter[]>(),
      catchError(err => this.handleError(err))
    );
  }

  createCounter(body: CreateCounterRequest): Observable<Counter> {
    this.loading.set(true);
    this.error.set(null);
    return this.http.post<Counter>(`${this.apiUrl}/counters`, body).pipe(
      this.handleTap<Counter>(),
      catchError(err => this.handleError(err))
    );
  }

  openCounter(id: number): Observable<Counter> {
    this.loading.set(true);
    return this.http.patch<Counter>(`${this.apiUrl}/counters/${id}/open`, {}).pipe(
      this.handleTap<Counter>(),
      catchError(err => this.handleError(err))
    );
  }

  closeCounter(id: number): Observable<Counter> {
    this.loading.set(true);
    return this.http.patch<Counter>(`${this.apiUrl}/counters/${id}/close`, {}).pipe(
      this.handleTap<Counter>(),
      catchError(err => this.handleError(err))
    );
  }

  deleteCounter(id: number): Observable<void> {
    this.loading.set(true);
    this.error.set(null);
    return this.http.delete<void>(`${this.apiUrl}/counters/${id}`).pipe(
      this.handleTap<void>(),
      catchError(err => this.handleError(err))
    );
  }
}
