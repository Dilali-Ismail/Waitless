#!/bin/bash
# =============================================================================
# init-multiple-databases.sh
# =============================================================================
# This script is automatically executed by PostgreSQL on first container start
# (via /docker-entrypoint-initdb.d/).
#
# It creates a separate database for each microservice based on:
#   POSTGRES_MULTIPLE_DATABASES="queue_db,ticket_db,keycloak_db"
# =============================================================================

set -e
set -u

# Function: create a database and grant privileges to the main user
create_user_and_database() {
    local database=$1
    echo "  [init] Creating database '$database' for user '$POSTGRES_USER'"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
        CREATE DATABASE $database;
        GRANT ALL PRIVILEGES ON DATABASE $database TO $POSTGRES_USER;
EOSQL
}

# Read the POSTGRES_MULTIPLE_DATABASES variable
if [ -n "${POSTGRES_MULTIPLE_DATABASES:-}" ]; then
    echo "======================================================="
    echo " Creating multiple databases"
    echo "======================================================="

    # Split by comma and create each database
    for db in $(echo "$POSTGRES_MULTIPLE_DATABASES" | tr ',' ' '); do
        create_user_and_database "$db"
    done

    echo "======================================================="
    echo " All databases created successfully!"
    echo "======================================================="
fi
