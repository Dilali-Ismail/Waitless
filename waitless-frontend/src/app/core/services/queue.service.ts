import { Injectable } from '@angular/core';
import { Observable, catchError } from 'rxjs';
import { Queue, CreateQueueRequest } from '../../models';
import { BaseService } from './base.service';

@Injectable({ providedIn: 'root' })
export class QueueService extends BaseService {
  getQueueById(id: number): Observable<Queue> {
    this.loading.set(true);
    return this.http.get<Queue>(`${this.apiUrl}/queues/${id}`).pipe(
      this.handleTap<Queue>(),
      catchError(err => this.handleError(err))
    );
  }

  getQueuesByCompany(companyId: number): Observable<Queue[]> {
    this.loading.set(true);
    return this.http.get<Queue[]>(`${this.apiUrl}/queues/company/${companyId}`).pipe(
      this.handleTap<Queue[]>(),
      catchError(err => this.handleError(err))
    );
  }

  getActiveQueues(): Observable<Queue[]> {
    this.loading.set(true);
    return this.http.get<Queue[]>(`${this.apiUrl}/queues/active`).pipe(
      this.handleTap<Queue[]>(),
      catchError(err => this.handleError(err))
    );
  }

  openQueue(id: number): Observable<Queue> {
    this.loading.set(true);
    return this.http.put<Queue>(`${this.apiUrl}/queues/${id}/open`, {}).pipe(
      this.handleTap<Queue>(),
      catchError(err => this.handleError(err))
    );
  }

  closeQueue(id: number): Observable<Queue> {
    this.loading.set(true);
    return this.http.put<Queue>(`${this.apiUrl}/queues/${id}/close`, {}).pipe(
      this.handleTap<Queue>(),
      catchError(err => this.handleError(err))
    );
  }

  createQueue(body: CreateQueueRequest): Observable<Queue> {
    this.loading.set(true);
    this.error.set(null);
    return this.http.post<Queue>(`${this.apiUrl}/queues`, body).pipe(
      this.handleTap<Queue>(),
      catchError(err => this.handleError(err))
    );
  }

  updateQueue(id: number, body: Partial<Queue>): Observable<Queue> {
    this.loading.set(true);
    this.error.set(null);
    return this.http.put<Queue>(`${this.apiUrl}/queues/${id}`, body).pipe(
      this.handleTap<Queue>(),
      catchError(err => this.handleError(err))
    );
  }

  deleteQueue(id: number): Observable<void> {
    this.loading.set(true);
    this.error.set(null);
    return this.http.delete<void>(`${this.apiUrl}/queues/${id}`).pipe(
      this.handleTap<void>(),
      catchError(err => this.handleError(err))
    );
  }
}
