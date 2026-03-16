# 🧪 Guide de Test Postman — Flux Kafka Ticket Cancelled

## Prérequis

Avant de commencer, vérifiez que les services suivants sont démarrés et accessibles :

| Service | Port | Health Check |
|---|---|---|
| user-service | 8083 | `GET http://localhost:8083/actuator/health` |
| ticket-service | 8082 | `GET http://localhost:8082/actuator/health` |
| Kafka | 9092 | — |
| PostgreSQL | 5432 | — |

---

## ✅ Étape 1 — Créer un Utilisateur (User Service)

**Method :** `POST`  
**URL :** `http://localhost:8083/api/users`

**Headers :**
```
Content-Type: application/json
```

**Body (JSON) :**
```json
{
  "userId": "test-user-123",
  "name": "Alice Dupont",
  "email": "alice.dupont@example.com"
}
```

**Réponse attendue : `201 Created`**
```json
{
  "userId": "test-user-123",
  "name": "Alice Dupont",
  "email": "alice.dupont@example.com",
  "status": "ACTIVE"
}
```

> ⚠️ Si vous obtenez `409 Conflict`, l'utilisateur existe déjà. Changez l'email ou le userId.

---

## ✅ Étape 2 — Vérifier que l'Utilisateur est Créé

**Method :** `GET`  
**URL :** `http://localhost:8083/api/users/test-user-123`

**Réponse attendue : `200 OK`**
```json
{
  "userId": "test-user-123",
  "name": "Alice Dupont",
  "email": "alice.dupont@example.com",
  "status": "ACTIVE"
}
```

---

## ✅ Étape 3 — Créer un Ticket (Ticket Service)

**Method :** `POST`  
**URL :** `http://localhost:8082/api/tickets`

**Headers :**
```
Content-Type: application/json
```

**Body (JSON) :**
```json
{
  "queueId": 1,
  "userId": "test-user-123",
  "clientName": "Alice Dupont"
}
```

**Réponse attendue : `201 Created`**
```json
{
  "id": 1,
  "queueId": 1,
  "userId": "test-user-123",
  "clientName": "Alice Dupont",
  "position": 1,
  "status": "WAITING"
}
```

> 📝 **Notez le `id` du ticket retourné** (ex: `1`). Vous en aurez besoin à l'étape suivante.

---

## ✅ Étape 4 — Annuler le Ticket

> Cette étape déclenche la publication de l'event Kafka `ticket.cancelled`.

**Method :** `DELETE`  
**URL :** `http://localhost:8082/api/tickets/{id}`

Remplacez `{id}` par le `id` du ticket obtenu à l'étape 3.

**Exemple :** `http://localhost:8082/api/tickets/1`

**Pas de body requis.**

**Réponse attendue : `200 OK`**
```json
{
  "id": 1,
  "status": "CANCELLED",
  ...
}
```

> 🚀 À ce moment, le **Ticket Service** publie automatiquement un event Kafka vers le topic `ticket-events` avec le type `TICKET_CANCELLED`.

---

## ✅ Étape 5 — Vérifier que le User Service a Consommé l'Event

Il y a deux façons de vérifier :

### Option A — Via les logs du User Service
Dans les logs IntelliJ / console du user-service, vous devriez voir :
```
INFO  Received ticket.cancelled event: ticketId=1, userId=test-user-123
INFO  Processing ticket cancellation: ticketId=1, userId=test-user-123
INFO  Successfully processed ticket.cancelled event: ticketId=1
```

### Option B — Via l'Actuator (vérifier les métriques Kafka)
**Method :** `GET`  
**URL :** `http://localhost:8083/actuator/health`

```json
{
  "status": "UP"
}
```

### Option C — Vérifier l'effet métier (restrictions utilisateur)
Le `TicketEventHandler` applique des restrictions selon le délai d'annulation.

**Method :** `GET`  
**URL :** `http://localhost:8083/api/users/test-user-123`

Vérifiez les champs de statut ou de restriction dans la réponse pour confirmer que le traitement a eu lieu.

---

## 📊 Résumé du Flux

```
Postman                  Ticket Service          Kafka              User Service
   |                          |                    |                      |
   |-- POST /api/tickets ---> |                    |                      |
   |<-- 201 ticketId=1 ---    |                    |                      |
   |                          |                    |                      |
   |-- DELETE /tickets/1 --> |                    |                      |
   |                          |-- TICKET_CANCELLED --> ticket-events      |
   |<-- 200 CANCELLED -----   |                    |                      |
   |                          |                    |-- consume event -->  |
   |                          |                    |               handle restriction
   |                          |                    |                      |
   |-- GET /api/users/... ------------------------------------------>  |
   |<-- 200 user data (with possible restriction applied) ------------- |
```

---

## ❗ Problèmes Courants

| Symptôme | Cause probable | Solution |
|---|---|---|
| `404` sur `/api/tickets` | Ticket Service non démarré | Vérifier port 8082 |
| `404` sur `/api/users` | User Service non démarré | Vérifier port 8083 |
| Logs Kafka absents | Kafka non démarré | `docker compose up kafka` |
| `500` sur DELETE | Queue introuvable (queueId=1 inexistant) | Créer d'abord une queue ou utiliser un queueId existant |
| Event reçu mais non traité | Désérialisation JSON échouée | Vérifier compatibilité DTO (champs de `TicketCancelledEvent`) |
