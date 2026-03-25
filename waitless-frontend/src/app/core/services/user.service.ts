import { Injectable } from '@angular/core';
import { Observable, catchError } from 'rxjs';
import { User } from '../models';
import { BaseService } from './base.service';

@Injectable({ providedIn: 'root' })
export class UserService extends BaseService {
  getUsers(): Observable<User[]> {
    this.loading.set(true);
    return this.http.get<User[]>(`${this.apiUrl}/users`).pipe(
      this.handleTap<User[]>(),
      catchError(err => this.handleError(err))
    );
  }

  getUserById(id: string): Observable<User> {
    this.loading.set(true);
    return this.http.get<User>(`${this.apiUrl}/users/${id}`).pipe(
      this.handleTap<User>(),
      catchError(err => this.handleError(err))
    );
  }

  updateUser(id: string, user: Partial<User>): Observable<User> {
    this.loading.set(true);
    return this.http.put<User>(`${this.apiUrl}/users/${id}`, user).pipe(
      this.handleTap<User>(),
      catchError(err => this.handleError(err))
    );
  }
}
