import { Injectable } from '@angular/core';
import { Observable, catchError } from 'rxjs';
import { Company } from '../../models';
import { BaseService } from './base.service';

@Injectable({ providedIn: 'root' })
export class CompanyService extends BaseService {


  
  registerCompany(request: Company, logo?: File): Observable<Company> {
    this.loading.set(true);
    this.error.set(null);
    const url = `${this.apiUrl}/companies`;
    const formData = new FormData();
    formData.append('company', new Blob([JSON.stringify(request)], { type: 'application/json' }));
    if (logo) {
      formData.append('logo', logo, logo.name);
    }
    return this.http.post<Company>(url, formData).pipe(
      this.handleTap<Company>(),
      catchError((err) => this.handleError(err)),
    );
  }

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

  updateCompany(id: number, body: Partial<Company>): Observable<Company> {
    this.loading.set(true);
    this.error.set(null);
    return this.http.put<Company>(`${this.apiUrl}/companies/${id}`, body).pipe(
      this.handleTap<Company>(),
      catchError(err => this.handleError(err))
    );
  }

  activateCompany(id: number): Observable<Company> {
    this.loading.set(true);
    return this.http.put<Company>(`${this.apiUrl}/companies/${id}/activate`, {}).pipe(
      this.handleTap<Company>(),
      catchError(err => this.handleError(err))
    );
  }

  suspendCompany(id: number): Observable<Company> {
    this.loading.set(true);
    return this.http.put<Company>(`${this.apiUrl}/companies/${id}/suspend`, {}).pipe(
      this.handleTap<Company>(),
      catchError(err => this.handleError(err))
    );
  }
}
