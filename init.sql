-- docker exec -it trainhub-postgres-1 psql -U postgres

CREATE DATABASE trainhub_dev_db OWNER postgres;

CREATE ROLE dev_user WITH LOGIN PASSWORD 'dev_pass';

-- docker exec -i trainhub-postgres-1 psql -U postgres -d postgres < ./init.sql


-- Conectarse a la base antes de dar permisos de esquema
\connect trainhub_dev_db

-- Permisos sobre el esquema público
GRANT USAGE, CREATE ON SCHEMA public TO dev_user;

-- Permisos sobre las tablas y secuencias existentes
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO dev_user;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO dev_user;

-- Para que las tablas que se creen después también hereden permisos automáticamente (opcional pero 🔥)
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO dev_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO dev_user;

-- Ajustar el search_path (para que use 'public' por defecto)
ALTER ROLE dev_user SET search_path TO public;