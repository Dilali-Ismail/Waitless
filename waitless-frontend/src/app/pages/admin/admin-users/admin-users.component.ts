import { AsyncPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../../../models';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [AsyncPipe],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.css',
})
export class AdminUsersComponent {
  readonly userService = inject(UserService);
  users$: Observable<User[]> = this.userService.getUsers();
  readonly infoMessage = signal<string | null>(null);

  refresh(): void {
    this.users$ = this.userService.getUsers();
  }

  activate(user: User): void {
    if (user.status === 'ACTIVE') {
      return;
    }
    this.infoMessage.set(null);
    this.userService.activateUser(user.userId).subscribe({
      next: () => {
        this.infoMessage.set(`Utilisateur ${user.name} activé.`);
        this.refresh();
      },
    });
  }
}
