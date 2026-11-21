#!/usr/bin/env bash
# Copia los DATOS de la BD MySQL antigua (huertodbdev) al PostgreSQL de la Raspberry.
# Requisitos: pgloader (dnf/apt install pgloader), MySQL accesible, y haber arrancado el backend una vez
# contra Postgres (Flyway ya creó las tablas). Uso:
#   MYSQL_URL='mysql://root:clave@localhost/huertodbdev' PG_ADMIN_URL='postgresql://postgres:clave@192.168.1.33/huertodb' \
#     ./migrar-desde-mysql.sh
# Se usa el superusuario de Postgres porque pgloader desactiva los triggers (claves foráneas) durante la carga.
# ¡Vacía las tablas de destino antes de copiar (truncate)! Pensado para hacerlo una sola vez, al migrar.
set -euo pipefail
cd "$(dirname "$0")"
: "${MYSQL_URL:?Define MYSQL_URL}"
: "${PG_ADMIN_URL:?Define PG_ADMIN_URL}"
command -v pgloader >/dev/null || { echo "Instala pgloader"; exit 1; }

CARGA=$(mktemp --suffix=.load)
trap 'rm -f "$CARGA"' EXIT
cat > "$CARGA" <<CARGA
LOAD DATABASE
     FROM $MYSQL_URL
     INTO $PG_ADMIN_URL
WITH data only, truncate, disable triggers, downcase identifiers, workers = 2, concurrency = 1
-- Tablas que no existen en MySQL o que se rellenan después:
EXCLUDING TABLE NAMES MATCHING 'lectura', 'flyway_schema_history'
ALTER SCHEMA 'huertodbdev' RENAME TO 'public';
CARGA

pgloader "$CARGA"
psql "$PG_ADMIN_URL" -v ON_ERROR_STOP=1 -f 03-despues-de-migrar.sql
echo "Migración terminada. Arranca el backend: Hibernate validará el esquema al iniciar."
