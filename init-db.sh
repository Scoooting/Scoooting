#!/bin/bash
set -e

echo "Creating databases..."

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT 'CREATE DATABASE users_db'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'users_db')\gexec

    SELECT 'CREATE DATABASE transports_db'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'transports_db')\gexec

    SELECT 'CREATE DATABASE rentals_db'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'rentals_db')\gexec
EOSQL

echo "Databases created successfully!"
