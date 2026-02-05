-- Créer la base de données pour Keycloak
CREATE DATABASE keycloak_db;

-- Donner les permissions à l'utilisateur waitless
GRANT ALL PRIVILEGES ON DATABASE keycloak_db TO waitless;