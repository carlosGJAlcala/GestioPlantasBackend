-- Crea el usuario y la base de datos del huerto en el PostgreSQL de la Raspberry (192.168.1.33).
-- Ejecutar UNA vez como superusuario (lo hace montar-bd-raspberry.sh):
--   psql -h 192.168.1.33 -U postgres -v clave=LA_CLAVE -f 01-crear-bd.sql
-- Las tablas NO se crean aquí: las crea el backend al arrancar (Flyway, src/main/resources/db/migration/postgresql).
SELECT format('CREATE ROLE huerto LOGIN PASSWORD %L', :'clave')
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'huerto')\gexec

SELECT 'CREATE DATABASE huertodb OWNER huerto ENCODING ''UTF8'' TEMPLATE template0'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'huertodb')\gexec

\connect huertodb
-- PostgreSQL 15+ ya no deja crear en public a cualquiera: el dueño del esquema será huerto.
ALTER SCHEMA public OWNER TO huerto;
ALTER DATABASE huertodb SET timezone TO 'Europe/Madrid';
