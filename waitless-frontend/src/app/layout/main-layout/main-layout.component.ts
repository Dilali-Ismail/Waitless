import { Component, inject, signal } from '@angular/core';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { HeaderComponent } from '../header/header.component';
import { FooterComponent } from '../footer/footer.component';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, FooterComponent],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css',
})
export class MainLayoutComponent {
  private readonly router = inject(Router);

  /** Footer masqué sur les zones dashboard (admin / agent / company). */
  readonly footerVisible = signal(this.shouldShowFooter(this.router.url));

  constructor() {
    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe(() => this.footerVisible.set(this.shouldShowFooter(this.router.url)));
  }

  private shouldShowFooter(url: string): boolean {
    const path = url.split('?')[0] ?? '';
    // Pas de footer sur les espaces back-office
    return !/^\/(admin|agent|company)(\/|$)/.test(path);
  }
}
