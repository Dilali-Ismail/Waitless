import {
  Component,
  ElementRef,
  HostListener,
  inject,
  signal,
} from '@angular/core';
import { AsyncPipe, NgClass } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, AsyncPipe, NgClass],
  templateUrl: './header.component.html',
})
export class HeaderComponent {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly host = inject(ElementRef<HTMLElement>);

  /** Effet verre au scroll */
  readonly scrolled = signal(false);

  /** Menu utilisateur (desktop) */
  readonly userMenuOpen = signal(false);

  /** Drawer mobile */
  readonly mobileOpen = signal(false);

  constructor() {
    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe(() => {
        this.mobileOpen.set(false);
        this.userMenuOpen.set(false);
      });
  }

  @HostListener('window:scroll')
  onWindowScroll(): void {
    this.scrolled.set(window.scrollY > 6);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.host.nativeElement.contains(event.target as Node)) {
      this.userMenuOpen.set(false);
    }
  }

  toggleUserMenu(): void {
    this.userMenuOpen.update((v) => !v);
  }

  closeUserMenu(): void {
    this.userMenuOpen.set(false);
  }

  toggleMobile(): void {
    this.mobileOpen.update((v) => !v);
  }

  closeMobile(): void {
    this.mobileOpen.set(false);
  }

  logout(): void {
    this.userMenuOpen.set(false);
    this.auth.logout();
  }

  /** Lien du logo selon le rôle (accueil public ou dashboard). */
  logoLink(): string {
    const role = this.auth.getPrimaryRole();
    if (!role) {
      return '/';
    }
    switch (role) {
      case 'CLIENT':
        return '/home';
      case 'ADMIN':
        return '/admin/dashboard';
      case 'AGENT':
        return '/agent/dashboard';
      case 'CO_ADMIN':
      case 'COMPANY_ADMIN':
        return '/company/dashboard';
      default:
        return '/';
    }
  }
}
