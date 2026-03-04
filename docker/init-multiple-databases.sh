#!/bin/bash
# =============================================================================
# init-multiple-databases.sh
# -----------------------------------------------------------------------------
# Ce script est exécuté automatiquement par PostgreSQL au premier démarrage
# du container (via /docker-entrypoint-initdb.d/).
#
# Il crée une base de données séparée pour chaque microservice à partir de:
#   POSTGRES_MULTIPLE_DATABASES="queue_db,ticket_db,keycloak_db"
# =============================================================================

set -e
set -u

# Fonction : créer une base et accorder les droits à l'utilisateur principal
create_user_and_database() {
    local database=$1
    echo "  [init] Création de la base '$database' pour l'utilisateur '$POSTGRES_USER'"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
        CREATE DATABASE $database;
        GRANT ALL PRIVILEGES ON DATABASE $database TO $POSTGRES_USER;
EOSQL
}

# Lecture de la variable POSTGRES_MULTIPLE_DATABASES
if [ -n "${POSTGRES_MULTIPLE_DATABASES:-}" ]; then
    echo "======================================================="
    echo " Création de plusieurs bases de données"
    echo "======================================================="

    # Sépare par virgule et crée chaque base
    for db in $(echo "$POSTGRES_MULTIPLE_DATABASES" | tr ',' ' '); do
        create_user_and_database "$db"
    done

    echo "======================================================="
    echo " Toutes les bases ont été créées avec succès !"
    echo "======================================================="
fi
