import { Injectable } from '@angular/core';
import { Observable, catchError } from 'rxjs';
import { RegisterClientRequest, User } from '../../models';
import { BaseService } from './base.service';

@Injectable({ providedIn: 'root' })
export class ClientRegistrationService extends BaseService {
  registerClient(request: RegisterClientRequest): Observable<User> {
    this.loading.set(true);
    this.error.set(null);

    return this.http.post<User>(`${this.apiUrl}/users/register`, request).pipe(
      this.handleTap<User>(),
      catchError((err) => this.handleError(err)),
    );
  }
}

