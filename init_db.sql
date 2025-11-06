-- Create databases if they don't exist
SELECT 'CREATE DATABASE users_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'users_db')\gexec

SELECT 'CREATE DATABASE transports_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'transports_db')\gexec

SELECT 'CREATE DATABASE rentals_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'rentals_db')\gexec
