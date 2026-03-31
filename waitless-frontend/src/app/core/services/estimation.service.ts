import { Injectable } from '@angular/core';
import { Observable, catchError } from 'rxjs';
import { Estimation } from '../../models';
import { BaseService } from './base.service';

@Injectable({ providedIn: 'root' })
export class EstimationService extends BaseService {
  calculate(queueId: number, position: number): Observable<Estimation> {
    this.loading.set(true);
    return this.http.get<Estimation>(`${this.apiUrl}/estimations/calculate?queueId=${queueId}&position=${position}`).pipe(
      this.handleTap<Estimation>(),
      catchError(err => this.handleError(err))
    );
  }
}
