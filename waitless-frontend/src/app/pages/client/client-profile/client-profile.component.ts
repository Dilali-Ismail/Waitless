import { Component, inject, signal, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { User } from '../../../models';

@Component({
  selector: 'app-client-profile',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './client-profile.component.html',
})
export class ClientProfileComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly auth = inject(AuthService);

  readonly user = signal<User | null>(null);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly successMsg = signal('');
  readonly errorMsg = signal('');

  readonly form = this.fb.group({
    name: ['', Validators.required],
    phoneNumber: ['', Validators.required],
  });

  ngOnInit(): void {
    const userId = this.auth.getUserId();
    if (!userId) return;
    this.userService.getUserById(userId).subscribe({
      next: (u) => {
        this.user.set(u);
        this.form.patchValue({ name: u.name, phoneNumber: u.phoneNumber ?? '' });
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  save(): void {
    const u = this.user();
    if (!u || this.form.invalid) return;

    this.saving.set(true);
    this.successMsg.set('');
    this.errorMsg.set('');

    this.userService.updateUser(u.userId, this.form.value as Partial<User>).subscribe({
      next: (updated) => {
        this.user.set(updated);
        this.saving.set(false);
        this.successMsg.set('Profil mis à jour.');
      },
      error: () => {
        this.saving.set(false);
        this.errorMsg.set('Erreur lors de la mise à jour.');
      },
    });
  }

  scoreLevel(): string {
    const s = this.user()?.score ?? 0;
    if (s >= 80) return 'Excellent';
    if (s >= 60) return 'Bon';
    if (s >= 40) return 'Moyen';
    return 'Faible';
  }

  scoreColor(): string {
    const s = this.user()?.score ?? 0;
    if (s >= 80) return 'text-success';
    if (s >= 60) return 'text-primary';
    if (s >= 40) return 'text-warning';
    return 'text-danger';
  }

  statusLabel(): string {
    const labels: Record<string, string> = {
      ACTIVE: 'Actif',
      SUSPENDED: 'Suspendu',
      BANNED: 'Banni',
    };
    return labels[this.user()?.status ?? ''] ?? this.user()?.status ?? '';
  }

  statusColor(): string {
    const s = this.user()?.status;
    if (s === 'ACTIVE') return 'text-success';
    if (s === 'SUSPENDED') return 'text-warning';
    return 'text-danger';
  }
}
