# Plan d'Exécution - WaitLess Frontend Angular 19

Ce plan suit strictement les règles techniques d'Angular 19 (Standalone, Signals, New Control Flow) et utilise NgRx uniquement pour la file d'attente en temps réel.

## Règles Techniques Strictes
- **Components** : Standalone, `inject()`, Signals pour l'état local.
- **Templates** : Syntax `@if`, `@for`, `@switch`, `@empty`.
- **Logic** : Observables pour HTTP, `| async` dans les templates.
- **State** : BehaviorSubject (Auth), NgRx (Queue en temps réel).
- **Forms** : ReactiveFormsModule obligatoire.

---

## État d'avancement

### Phase 1 : Infrastructure & Auth
- [✅] Analyse des endpoints backend & Guide API
- [✅] Analyse de l'existant (Layout, Tailwind CDN)
- [✅] **Étape 1 : Modèles & Services de base** (Interfaces TS, ApiService, UserService)
- [✅] **Étape 1b : Layout Header + Footer** (nav par rôle, sticky glass, footer public/client, routes placeholder)
- [✅] **Étape 2 : Authentification — partie 1** (`environment`, Keycloak token + client dev, Login/Register réactifs, `guestGuard`, redirection post-login, intercepteur 401 sans casser le login)
- [✅] **Étape 2a bis : Register côté backend** (`POST /api/users/register` -> Keycloak provisioning + insertion DB)
- [✅] **Étape 2b : Authentification — partie 2** (routes protégées `authGuard` + `data.roles`, redirection si rôle insuffisant)

### Phase 2 : Features Publiques & Client
- [⬜] **Étape 4 : Home & Recherche** (Filtres par catégorie)
- [⬜] **Étape 5 : Détails Company & Files**
- [⬜] **Étape 6 : Booking & Suivi Ticket** (NgRx pour le temps réel)

### Phase 3 : Dashboards Spécifiques
- [⬜] **Étape 7 : Dashboard Client (Profil & Tickets)**
- [⬜] **Étape 8 : Dashboard Agent (Appel ticket)**
- [⬜] **Étape 9 : Dashboard Co-Admin (Gestion Queues/Counters)**
- [✅] **Étape 10 : Dashboard Admin Global (Users/Companies)** (Dashboard ✅, Companies ✅, Users ✅)

---

## 🚦 Prochaine étape (après validation)
**Étape 2b** — Protéger les routes (`authGuard` + `data.roles`), rafraîchissement du token, ajuster proxy/CORS Keycloak si le navigateur bloque l’appel token.
