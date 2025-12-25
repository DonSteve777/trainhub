-- 1️⃣ Eliminar la base de datos si existe (y terminar conexiones activas)
DROP DATABASE IF EXISTS trainhub_dev_db WITH (FORCE);

-- 2️⃣ Eliminar el usuario si existe (CASCADE elimina las dependencias)
DROP ROLE IF EXISTS dev_user;

-- 3️⃣ Crear la base de datos
CREATE DATABASE trainhub_dev_db;

-- 4️⃣ Crear usuario
CREATE USER dev_user WITH PASSWORD 'dev_pass';

-- 5️⃣ Dar permisos sobre la base de datos
GRANT ALL PRIVILEGES ON DATABASE trainhub_dev_db TO dev_user;

-- 6️⃣ Conectarse a la base de datos y dar permisos en el schema public
\c trainhub_dev_db

-- Otorgar permisos en el schema public
GRANT ALL ON SCHEMA public TO dev_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO dev_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO dev_user;