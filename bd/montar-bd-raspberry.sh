#!/usr/bin/env bash
# Monta la BD del huerto en el PostgreSQL de la Raspberry. Uso:
#   PG_ADMIN=postgres ./montar-bd-raspberry.sh            (pide las contraseñas)
# Variables opcionales: PG_HOST (192.168.1.33), PG_PORT (5432), PG_ADMIN (postgres), HUERTO_CLAVE.
set -euo pipefail
cd "$(dirname "$0")"
PG_HOST=${PG_HOST:-192.168.1.33}
PG_PORT=${PG_PORT:-5432}
PG_ADMIN=${PG_ADMIN:-postgres}

pg_isready -h "$PG_HOST" -p "$PG_PORT" >/dev/null || { echo "PostgreSQL no responde en $PG_HOST:$PG_PORT"; exit 1; }

if [ -z "${HUERTO_CLAVE:-}" ]; then
  HUERTO_CLAVE=$(openssl rand -base64 24 | tr -d '/+=')
  echo "Clave generada para el usuario 'huerto': $HUERTO_CLAVE  (guárdala en DB_PASSWORD del .env)"
fi

echo "== Creando rol y base de datos (te pedirá la contraseña de $PG_ADMIN)"
psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_ADMIN" -d postgres -v ON_ERROR_STOP=1 \
     -v clave="$HUERTO_CLAVE" -f 01-crear-bd.sql

echo "== Comprobando acceso como huerto"
PGPASSWORD="$HUERTO_CLAVE" psql -h "$PG_HOST" -p "$PG_PORT" -U huerto -d huertodb -tAc "select current_user, version()"

cat <<FIN

Listo. Siguientes pasos:
 1. En GestioPlantasBackend/.env:
      DB_URL=jdbc:postgresql://$PG_HOST:$PG_PORT/huertodb
      DB_USER=huerto
      DB_PASSWORD=$HUERTO_CLAVE
 2. Arrancar el backend: set -a; . ./.env; set +a; ./mvnw spring-boot:run
    (Flyway crea las tablas y el catálogo de tipos de planta en el primer arranque).
 3. Crear el primer administrador con bd/02-crear-admin.sql (ver cabecera del fichero).
 4. Opcional: traer los datos de MySQL con bd/migrar-desde-mysql.sh.
 Si la Raspberry rechaza la conexión desde otro equipo, revisa en ella pg_hba.conf
 (host huertodb huerto 192.168.1.0/24 scram-sha-256) y listen_addresses='*' en postgresql.conf.
FIN
