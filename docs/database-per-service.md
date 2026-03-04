# 🐘 Database-per-Service : Bonnes pratiques PostgreSQL + Docker Compose

## Pourquoi séparer les bases de données ?

Dans une architecture **microservices**, chaque service doit posséder **sa propre base de données**. C'est l'un des principes fondamentaux (Database per Service pattern).

| ✅ Avec séparation | ❌ Sans séparation |
|---|---|
| Isolation complète des données | Les services partagent les mêmes tables |
| Déploiement indépendant | Une migration casse tous les services |
| Choix de schéma libre par service | Couplage fort entre équipes |
| Scaling indépendant | Goulot d'étranglement |

---

## Architecture retenue pour Waitless

```
PostgreSQL (instance unique)
├── queue_db      → queue-service
├── ticket_db     → ticket-service
└── keycloak_db   → keycloak
```

> Un seul container PostgreSQL avec **trois bases isolées**. C'est acceptable en développement.  
> En production, envisager un serveur PostgreSQL par service.

---

## Comment ça fonctionne

### 1. Le script `init-multiple-databases.sh`

PostgreSQL exécute automatiquement tous les fichiers `.sh` et `.sql` placés dans `/docker-entrypoint-initdb.d/` **uniquement lors du premier démarrage** (volume vide).

```bash
# Variable lue par le script
POSTGRES_MULTIPLE_DATABASES="queue_db,ticket_db,keycloak_db"
```

Le script :
1. Lit la variable `POSTGRES_MULTIPLE_DATABASES`
2. La découpe par virgule
3. Crée chaque base et accorde les droits à l'utilisateur principal

### 2. Le montage dans `docker-compose.yml`

```yaml
postgres:
  volumes:
    - postgres_data:/var/lib/postgresql/data          # Persistance des données
    - ./init-multiple-databases.sh:/docker-entrypoint-initdb.d/init-multiple-databases.sh:ro
```

Le `:ro` signifie **read-only** : le container ne peut pas modifier le script.

### 3. Chaque service pointe vers sa propre base

```yaml
queue-service:
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/queue_db

ticket-service:
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/ticket_db
```

---

## Erreurs corrigées dans le `docker-compose.yml`

### ❌ Faute 1 — `POSTGRES_DB` manquant / bases non créées

**Avant :** Aucune variable ne déclenchait la création des bases. PostgreSQL créait uniquement la base par défaut `postgres`.

**Après :**
```yaml
environment:
  POSTGRES_MULTIPLE_DATABASES: queue_db,ticket_db,keycloak_db
```
+ montage du script d'init.

---

### ❌ Faute 2 — Kafka `ADVERTISED_LISTENERS` pointait vers `localhost`

**Avant :**
```yaml
KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
```
Les autres containers (microservices) ne peuvent pas joindre `localhost` dans le réseau Docker — ils doivent utiliser le **nom du service**.

**Après :**
```yaml
KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
```

---

### ❌ Faute 3 — Keycloak sans base de données configurée

**Avant :** Keycloak était lancé sans base de données PostgreSQL configurée (il utilisait H2 en mémoire → données perdues au redémarrage).

**Après :**
```yaml
keycloak:
  environment:
    KC_DB: postgres
    KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak_db
    KC_DB_USERNAME: waitless
    KC_DB_PASSWORD: waitless123
```

---

### ❌ Faute 4 — URL Keycloak hardcodée dans `application.yml`

`application.yml` référençait `localhost:8180` → ne fonctionne pas en Docker.

La valeur correcte est injectée via variable d'environnement dans `docker-compose.yml` :
```yaml
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://keycloak:8080/realms/waitless-realm
```
Spring Boot prend automatiquement les variables d'environnement en priorité sur `application.yml`.

---

## Commandes utiles

```bash
# 1. Supprimer les volumes pour repartir de zéro (IMPORTANT si scripts modifiés)
docker compose down -v

# 2. Rebuilder et démarrer
docker compose up --build -d

# 3. Vérifier que les bases existent
docker exec -it waitless-postgres psql -U waitless -c "\l"

# 4. Se connecter à une base spécifique
docker exec -it waitless-postgres psql -U waitless -d queue_db

# 5. Voir les logs du script d'init
docker logs waitless-postgres | grep "\[init\]"
```

> [!IMPORTANT]
> Le script `init-multiple-databases.sh` n'est exécuté qu'**une seule fois**, lors du premier démarrage.  
> Si tu modifies le script, tu dois **supprimer le volume** : `docker compose down -v`

---

## Bonnes pratiques supplémentaires

### En développement (situation actuelle ✅)
- Un seul PostgreSQL, plusieurs bases → simple et suffisant
- `ddl-auto: update` dans Spring → Hibernate crée les tables automatiquement

### En production (à faire évoluer)
- Un PostgreSQL **par service** (isolation maximale)
- `ddl-auto: none` + **Flyway** ou **Liquibase** pour les migrations versionnées
- Secrets via **Docker Secrets** ou **Vault** (pas de mots de passe en clair)
- Health checks sur `pg_isready` comme déjà fait ✅

### Flyway (migration recommandée en prod)
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```
```yaml
# application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```
