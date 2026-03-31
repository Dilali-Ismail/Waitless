import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, EMPTY } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class BaseService {
  protected http = inject(HttpClient);
  protected apiUrl = environment.apiUrl;
  
  loading = signal<boolean>(false);
  error = signal<string | null>(null);

  protected handleTap<T>() {
    return tap<T>(() => this.loading.set(false));
  }

  protected handleError(err: any) {
    this.loading.set(false);
    this.error.set(err.message || 'Une erreur est survenue');
    return EMPTY;
  }
}
