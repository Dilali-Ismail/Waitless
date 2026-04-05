# WaitLess

**Une plateforme pour gérer les listes d’attente à distance et suivre son tour facilement.**

WaitLess digitalise la prise de ticket, le suivi de position et les notifications, tout en offrant aux entreprises des outils pour organiser files, guichets et agents — sur une base **modulaire en microservices**, prête à évoluer.

---

## Contexte

La gestion des files d’attente constitue une source importante de **perte de temps** et d’**insatisfaction** pour les usagers et les organisations (hôpitaux, administrations, banques, restaurants, commerces de proximité, etc.). L’affluence physique augmente les délais perçus, complique la planification des équipes et dégrade l’expérience client.

La **digitalisation des files** permet de :

- réduire la présence physique aux guichets au bon moment ;
- mieux **répartir la charge** entre les points de service ;
- offrir une **visibilité** (position, temps estimé) à l’usager ;
- collecter des **indicateurs** utiles au pilotage.

WaitLess vise à remplacer ou compléter les files physiques par une **solution numérique simple**, réutilisable et adaptable à différents secteurs, sans imposer un modèle unique : chaque entreprise configure ses files et ses guichets selon son activité.

---

## Objectifs

- Permettre aux utilisateurs de **rejoindre une liste d’attente à distance** (prise de ticket numérique).
- Afficher en **temps réel** (ou quasi temps réel) la **position** dans la file et une **estimation du temps d’attente**.
- Envoyer des **notifications fiables** lorsque le tour approche (canal SMS via intégration configurable).
- Offrir aux **entreprises** des outils pour gérer leurs **files**, **guichets**, **agents** et suivre l’activité.
- Concevoir une solution **modulaire**, **scalable** et **extensible** grâce à une architecture **microservices** (services métier indépendants, messagerie événementielle, API Gateway, découverte de services).

---

## Aperçu du projet

WaitLess s’articule autour de **plusieurs profils** :

| Rôle | Rôle dans la plateforme |
|------|-------------------------|
| **Client** | Rejoindre une file, consulter ses tickets et son profil. |
| **Agent** | Traiter la file au guichet (appel de ticket, file de travail). |
| **Entreprise** | Configurer files d’attente, guichets, agents, profil et visibilité (ex. logos). |
| **Administrateur** | Superviser entreprises et utilisateurs à l’échelle plateforme. |

Le **backend** expose une **API unifiée** via une **passerelle** (API Gateway) : le client (navigateur ou future app mobile) ne parle qu’à un point d’entrée ; en interne, les requêtes sont routées vers le bon **microservice**. Les services communiquent aussi par **événements** (file d’attente, cycle de vie des tickets) pour découpler les traitements — par exemple pour mettre à jour des statistiques ou déclencher des **notifications**.

L’**authentification et les rôles** sont gérés par **Keycloak** (OpenID Connect / JWT) ; les microservices valident les jetons et appliquent les habilitations côté API.

---

## Architecture (microservices)

Vue synthétique :

```
[ Navigateur — Angular ]
           │
           ▼
[ API Gateway — Spring Cloud Gateway ]  ← JWT (Keycloak)
           │
           ├── Eureka (annuaire des services)
           │
           ├── queue-service      → files, entreprises, guichets, logos (S3)
           ├── ticket-service     → tickets, orchestration, événements Kafka
           ├── user-service       → utilisateurs, scoring / profils, consommation Kafka
           ├── estimation-service → estimation des temps d’attente
           └── notification-service → SMS (Twilio), consommation Kafka, appels Feign

[ Kafka ] — bus d’événements (ex. événements ticket)

[ PostgreSQL ] — bases dédiées par domaine (queue_db, ticket_db, user_db, keycloak_db)
```

- **Eureka** : enregistrement et résolution des instances de services (`lb://...` côté Gateway).
- **Spring Cloud OpenFeign** : appels HTTP déclaratifs entre services (ex. ticket → user, estimation).
- **Kafka** : publication / consommation d’événements pour un traitement asynchrone et extensible.

Cette découpe permet d’**évoluer service par service** (montée en charge, déploiement, technologie ciblée) sans monolithiser tout le produit.

---

## Stack technique

### Frontend

- **Angular 19**, **TypeScript**, **RxJS**
- Routage avec **garde de routes** et contrôle par **rôles**
- **Interceptors HTTP** pour les appels API sécurisés
- Intégration **Keycloak** (token / realm configurés via environnement)

### Backend & infrastructure

- **Java 17**, **Spring Boot 3.4**, **Spring Cloud 2024**
- **Spring Cloud Gateway**, **Netflix Eureka**
- **Spring Security** + **OAuth2 Resource Server** (JWT Keycloak)
- **Spring Data JPA**, **PostgreSQL 15**
- **Spring Kafka**, **Apache Kafka** (image Confluent en Docker)
- **OpenFeign** pour la communication inter-services
- **AWS SDK** — stockage **S3** pour les logos entreprise (variables d’environnement)
- **Twilio** — notifications SMS (optionnel, via variables d’environnement)
- **Spring Actuator** (santé des services)
- **Docker** & **Docker Compose**
- **CI GitHub Actions** — tests Maven sur les microservices

Outils optionnels dans Compose : **pgAdmin**, **Kafka UI**.

---

## Prérequis

- **Docker** & **Docker Compose**
- Compte / clés si vous activez les options : **AWS S3** (logos), **Twilio** (SMS)

---

## Démarrage rapide

1. Cloner le dépôt.

2. Depuis le dossier `docker/` :

   ```bash
   docker compose up -d --build
   ```

3. Vérifier que les services sont **healthy** (Gateway, Eureka, Keycloak, Postgres, Kafka, microservices).

4. **Frontend** (développement local) :

   ```bash
   cd waitless-frontend
   npm install
   ng serve
   ```

   Adapter `src/environments/environment.ts` si besoin (URL API, Keycloak). Un proxy peut être utilisé pour éviter les problèmes CORS en dev (voir configuration du projet).

**Keycloak** : realm et clients sont importés via `docker/keycloak-realm.json` (exemples d’utilisateurs de test documentés dans ce fichier / guides du projet).

**Variables d’environnement** : pour S3 et Twilio, un fichier `docker/.env` peut être utilisé (ne pas commiter de secrets) — voir `docker-compose.yml` et la documentation des services.

---

## Structure du dépôt

```
Waitless/
├── docker/                 # Docker Compose, Keycloak realm, scripts DB
├── infrastructure/
│   ├── eureka-server/      # Annuaire de services
│   └── gateway-service/    # API Gateway
├── services/
│   ├── queue-service/
│   ├── ticket-service/
│   ├── user-service/
│   ├── estimation-service/
│   └── notification-service/
├── waitless-frontend/      # Application Angular
├── .github/workflows/    # CI (tests Maven)
└── SECURITY_GUIDE.md     # Sécurisation JWT / Keycloak (microservices)
```

---

## Sécurité

Le fichier **`SECURITY_GUIDE.md`** décrit comment les microservices valident les **JWT Keycloak** et utilisent **`@PreAuthorize`** pour les rôles. En production, renforcez les secrets, HTTPS et la configuration realm.

---

## Licence

Précisez ici la licence de votre choix (ex. MIT, Apache-2.0) ou « Tous droits réservés » selon votre situation.

---

*Projet WaitLess — files d’attente numériques, architecture microservices.*
