-- docker exec -it trainhub-postgres-1 psql -U postgres

CREATE DATABASE trainhub_dev_db OWNER postgres;

CREATE ROLE dev_user WITH LOGIN PASSWORD 'dev_pass';

-- Permisos mínimos
GRANT CONNECT ON DATABASE trainhub_dev_db TO dev_user;
GRANT USAGE ON SCHEMA public TO dev_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO dev_user;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO dev_user;