-- Primer administrador (sin él nadie puede dar de alta usuarios desde la web).
-- La contraseña se mete como SHA-256 en hex (lo que manda el front al entrar); el backend la convierte a BCrypt
-- en el primer login o en el siguiente arranque (S2, MigracionContrasenas).
-- Ejecutar DESPUÉS del primer arranque del backend (cuando Flyway ya ha creado las tablas):
--   psql -h 192.168.1.33 -U huerto -d huertodb -v usuario=admin -v nombre=Carlos \
--        -v hash="$(printf '%s' 'CONTRASEÑA' | sha256sum | cut -d' ' -f1)" -f 02-crear-admin.sql
BEGIN;
WITH p AS (
    INSERT INTO persona (nombre, usuario, passwordsha256, role)
    VALUES (:'nombre', :'usuario', :'hash', 'ADMIN')
    RETURNING id
)
INSERT INTO administrador (persona_id) SELECT id FROM p;
COMMIT;
