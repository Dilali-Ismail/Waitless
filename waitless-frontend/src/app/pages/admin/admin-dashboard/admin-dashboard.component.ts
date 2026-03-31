import { AsyncPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { map } from 'rxjs/operators';
import { CompanyService } from '../../../core/services/company.service';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [RouterLink, AsyncPipe],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css',
})
export class AdminDashboardComponent {
  private readonly userService = inject(UserService);
  private readonly companyService = inject(CompanyService);

  readonly usersCount$ = this.userService.getUsers().pipe(map((users) => users.length));
  readonly companiesCount$ = this.companyService.getCompanies().pipe(map((companies) => companies.length));
}
