import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { CompanyRegisterComponent } from './auth/company-register/company-register.component';
import { guestGuard } from './core/guards/guest.guard';
import { authGuard } from './core/guards/auth.guard';
import { AdminDashboardComponent } from './pages/admin/admin-dashboard/admin-dashboard.component';
import { AdminCompaniesComponent } from './pages/admin/admin-companies/admin-companies.component';
import { AdminUsersComponent } from './pages/admin/admin-users/admin-users.component';
import { CompanyDashboardComponent } from './pages/company/company-dashboard/company-dashboard.component';
import { CompanyQueuesComponent } from './pages/company/company-queues/company-queues.component';
import { CompanyCountersComponent } from './pages/company/company-counters/company-counters.component';
import { CompanyAgentsComponent } from './pages/company/company-agents/company-agents.component';
import { CompanyProfileComponent } from './pages/company/company-profile/company-profile.component';
import { AgentDashboardComponent } from './pages/agent/agent-dashboard/agent-dashboard.component';
import { HomeComponent } from './pages/home/home.component';
import { ClientTicketsComponent } from './pages/client/client-tickets/client-tickets.component';
import { ClientProfileComponent } from './pages/client/client-profile/client-profile.component';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'home' },
      { path: 'explore', redirectTo: 'home' },
      {
        path: 'login',
        component: LoginComponent,
        canActivate: [guestGuard],
        data: { title: 'Connexion' },
      },
      {
        path: 'register',
        component: RegisterComponent,
        canActivate: [guestGuard],
        data: { title: 'Inscription' },
      },
      {
        path: 'company/register',
        component: CompanyRegisterComponent,
        canActivate: [guestGuard],
        data: { title: "Inscription Entreprise" },
      },
      {
        path: 'home',
        component: HomeComponent,
        data: { title: 'Accueil' },
      },
      {
        path: 'tickets',
        component: ClientTicketsComponent,
        canActivate: [authGuard],
        data: { title: 'Mes tickets', roles: ['CLIENT'] },
      },
      {
        path: 'profile',
        component: ClientProfileComponent,
        canActivate: [authGuard],
        data: { title: 'Profil', roles: ['CLIENT'] },
      },
      {
        path: 'admin',
        canActivate: [authGuard],
        data: { roles: ['ADMIN'] },
        children: [
          { path: 'dashboard', component: AdminDashboardComponent, data: { title: 'Admin — Dashboard' } },
          { path: 'users', component: AdminUsersComponent, data: { title: 'Admin — Utilisateurs' } },
          { path: 'companies', component: AdminCompaniesComponent, data: { title: 'Admin — Entreprises' } },
          { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
        ],
      },
      {
        path: 'agent',
        canActivate: [authGuard],
        data: { roles: ['AGENT'] },
        children: [
          { path: 'dashboard', component: AgentDashboardComponent, data: { title: 'Agent — Dashboard' } },
          { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
        ],
      },
      {
        path: 'company',
        canActivate: [authGuard],
        data: { roles: ['COMPANY_ADMIN', 'CO_ADMIN'] },
        children: [
          { path: 'dashboard', component: CompanyDashboardComponent, data: { title: 'Entreprise — Dashboard' } },
          { path: 'queues', component: CompanyQueuesComponent, data: { title: "Files d'attente" } },
          { path: 'counters', component: CompanyCountersComponent, data: { title: 'Guichets' } },
          { path: 'agents', component: CompanyAgentsComponent, data: { title: 'Créer un agent' } },
          { path: 'profile', component: CompanyProfileComponent, data: { title: 'Profil entreprise' } },
          { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
        ],
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
