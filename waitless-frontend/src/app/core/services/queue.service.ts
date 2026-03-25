import { Injectable } from '@angular/core';
import { Observable, catchError } from 'rxjs';
import { Queue } from '../models';
import { BaseService } from './base.service';

@Injectable({ providedIn: 'root' })
export class QueueService extends BaseService {
  getQueuesByCompany(companyId: number): Observable<Queue[]> {
    this.loading.set(true);
    return this.http.get<Queue[]>(`${this.apiUrl}/queues/company/${companyId}`).pipe(
      this.handleTap(),
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
      this.handleTap(),
      catchError(err => this.handleError(err))
    );
  }
}
