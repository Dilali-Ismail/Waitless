# WaitLess - Backend API & Frontend Guide

Ce document sert de guide de référence pour l'intégration entre le backend Spring Boot et le frontend Angular 19.

## 1. Analyse des Endpoints Backend

### A. User Service (`/api/users`)
| Méthode | Endpoint | Rôle | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/users` | `ADMIN` | Créer un nouvel utilisateur |
| GET | `/api/users/{userId}` | `ANY` | Récupérer les infos d'un utilisateur par son ID |
| PUT | `/api/users/{userId}` | `ADMIN, CLIENT` | Mettre à jour son profil |
| DELETE | `/api/users/{userId}` | `ADMIN` | Supprimer un utilisateur |
| GET | `/api/users` | `ADMIN` | Lister tous les utilisateurs |
| GET | `/api/users/email/{email}` | `ADMIN, AGENT, CO_ADMIN` | Trouver un utilisateur par email |

**DTO Clé : [UserDTO](file:///c:/Users/Youcode/Desktop/Waitless/services/user-service/src/main/java/com/waitless/user/dto/UserDTO.java#7-219)**
- [userId](file:///c:/Users/Youcode/Desktop/Waitless/services/user-service/src/main/java/com/waitless/user/dto/UserDTO.java#154-158), [name](file:///c:/Users/Youcode/Desktop/Waitless/services/user-service/src/main/java/com/waitless/user/dto/UserDTO.java#159-163), [email](file:///c:/Users/Youcode/Desktop/Waitless/services/user-service/src/main/java/com/waitless/user/dto/UserDTO.java#164-168), [phoneNumber](file:///c:/Users/Youcode/Desktop/Waitless/services/user-service/src/main/java/com/waitless/user/dto/UserDTO.java#169-173), [score](file:///c:/Users/Youcode/Desktop/Waitless/services/user-service/src/main/java/com/waitless/user/dto/UserDTO.java#174-178), [status](file:///c:/Users/Youcode/Desktop/Waitless/services/user-service/src/main/java/com/waitless/user/dto/UserDTO.java#179-183), [suspensionEndDate](file:///c:/Users/Youcode/Desktop/Waitless/services/user-service/src/main/java/com/waitless/user/dto/UserDTO.java#184-188), [ticketsCreated](file:///c:/Users/Youcode/Desktop/Waitless/services/user-service/src/main/java/com/waitless/user/dto/UserDTO.java#189-193), [ticketsServed](file:///c:/Users/Youcode/Desktop/Waitless/services/user-service/src/main/java/com/waitless/user/dto/UserDTO.java#194-198), [ticketsCancelled](file:///c:/Users/Youcode/Desktop/Waitless/services/user-service/src/main/java/com/waitless/user/dto/UserDTO.java#199-203).

---

### B. Company Service (`/api/companies`)
| Méthode | Endpoint | Rôle | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/companies` | `ADMIN` | Créer une entreprise |
| GET | `/api/companies` | `ANY` | Lister toutes les entreprises |
| GET | `/api/companies/{id}` | `ANY` | Détails d'une entreprise |
| GET | `/api/companies/category/{cat}` | `ANY` | Filtrer par catégorie |
| PUT | `/api/companies/{id}` | `ADMIN, CO_ADMIN` | Modifier une entreprise |
| PUT | `/api/companies/{id}/activate` | `ADMIN` | Activer une entreprise |
| PUT | `/api/companies/{id}/suspend` | `ADMIN` | Suspendre une entreprise |

---

### C. Queue Service (`/api/queues`)
| Méthode | Endpoint | Rôle | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/queues` | `ADMIN, CO_ADMIN` | Créer une file d'attente |
| GET | `/api/queues/company/{id}` | `ANY` | Lister les files d'une entreprise |
| PUT | `/api/queues/{id}/open` | `ADMIN, AGENT, CO_ADMIN` | Ouvrir la file |
| PUT | `/api/queues/{id}/close` | `ADMIN, AGENT, CO_ADMIN` | Fermer la file |

---

### D. Counter Service (`/api/counters`)
| Méthode | Endpoint | Rôle | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/counters` | `ADMIN, CO_ADMIN` | Créer un guichet |
| GET | `/api/counters/queue/{id}` | `ANY` | Lister les guichets d'une file |
| PATCH | `/api/counters/{id}/open` | `ADMIN, AGENT, CO_ADMIN` | Ouvrir un guichet |

---

### E. Ticket Service (`/api/tickets`)
| Méthode | Endpoint | Rôle | Description |
| :--- | :--- | :--- | :--- |
| POST | `/api/tickets` | `ADMIN, CLIENT` | Réserver un ticket |
| POST | `/api/tickets/call` | `ADMIN, AGENT` | Appeler le prochain ticket |
| PUT | `/api/tickets/{id}/status` | `ADMIN, AGENT` | Statut : COMPLETED, ABSENT, CANCELLED |
| GET | `/api/tickets/me` | `ADMIN, CLIENT` | Lister ses propres tickets (`?userId=...`) |
| GET | `/api/tickets/{id}` | `ANY` | Détails d'un ticket |

---

### F. Estimation Service (`/api/estimations`)
| Méthode | Endpoint | Rôle | Description |
| :--- | :--- | :--- | :--- |
| GET | `/api/estimations/calculate` | `ANY` | Calculer le temps d'attente (`?queueId=...&position=...`) |

---

## 2. Contenu Exact du Frontend (Guide de Réalisation)

Pour que le projet soit complet et fonctionnel, voici les éléments **obligatoires** que nous devons implémenter :

### A. Core (Infrastructure)
- [ ] **AuthInterceptor** : Ajoute automatiquement le token JWT de Keycloak dans le header `Authorization`.
- [ ] **AuthService** : Gère la connexion, déconnexion et l'extraction des rôles du token.
- [ ] **AuthGuard** : Protège les routes selon les rôles (ex: `/admin` réservé au rôle `ADMIN`).
- [ ] **Models (Interfaces)** : Interfaces TypeScript strictes pour chaque DTO backend cité ci-dessus.

### B. Pages & Fonctionnalités par Rôle

#### 1. Public / Client
- [ ] **Login/Register** : Formulaires liés au `user-service` et Keycloak.
- [ ] **Home (Search)** : Recherche avec filtres par catégorie (`/api/companies/category/`).
- [ ] **Company Detail** : Affiche les infos de l'entreprise et ses files d'attente.
- [ ] **Booking Flow** : Création de ticket (`POST /api/tickets`) + Calcul d'attente (`/api/estimations`).
- [ ] **My Tickets** : Liste et suivi en temps réel des tickets de l'utilisateur.

#### 2. Admin (Global)
- [ ] **User Management** : Liste, activation/suspension via `user-service`.
- [ ] **Company Management** : Validation et activation des nouvelles entreprises.
- [ ] **Global Dashboard** : Statistiques agrégées.

#### 3. Company Admin
- [ ] **Queue Management** : Création et configuration des files d'attente.
- [ ] **Counter Management** : Assignation des guichets aux files.
- [ ] **Stats Company** : Temps d'attente moyen de sa propre entreprise.

#### 4. Agent (Guichet)
- [ ] **Agent Dashboard** : Sélection du guichet actif.
- [ ] **Queue Control** : Bouton "Appeler Suivant" (`POST /api/tickets/call`).
- [ ] **Ticket Action** : Marquer comme Terminé ou Absent.

---

## 3. Guide de Priorité (Checklist)

1. [ ] **Backend Mapping** : Valider que toutes les interfaces TS matchent les DTO Java.
2. [ ] **JWT Interceptor** : Assurer que chaque appel API est sécurisé.
3. [ ] **Auth Flow** : Redirection automatique vers le bon dashboard après login.
4. [ ] **Feature Client** : Cycle complet "Recherche -> Ticket -> Suivi".
5. [ ] **Dashboards Roles** : Implémenter les vues spécifiques ADMIN/CO_ADMIN/AGENT.
