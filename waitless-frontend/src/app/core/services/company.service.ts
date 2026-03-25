import { Injectable } from '@angular/core';
import { Observable, catchError } from 'rxjs';
import { Company } from '../models';
import { BaseService } from './base.service';

@Injectable({ providedIn: 'root' })
export class CompanyService extends BaseService {
  getCompanies(): Observable<Company[]> {
    this.loading.set(true);
    return this.http.get<Company[]>(`${this.apiUrl}/companies`).pipe(
      this.handleTap<Company[]>(),
      catchError(err => this.handleError(err))
    );
  }

  getCompanyById(id: number): Observable<Company> {
    this.loading.set(true);
    return this.http.get<Company>(`${this.apiUrl}/companies/${id}`).pipe(
      this.handleTap<Company>(),
      catchError(err => this.handleError(err))
    );
  }

  getCompaniesByCategory(category: string): Observable<Company[]> {
    this.loading.set(true);
    return this.http.get<Company[]>(`${this.apiUrl}/companies/category/${category}`).pipe(
      this.handleTap<Company[]>(),
      catchError(err => this.handleError(err))
    );
  }
}
