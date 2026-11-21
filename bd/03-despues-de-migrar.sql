-- Tras copiar los datos desde MySQL (migrar-desde-mysql.sh). Idempotente.
-- 1) Las columnas IDENTITY no saben que se han insertado ids a mano: se ajustan al máximo actual.
DO $$
DECLARE r record;
BEGIN
    FOR r IN SELECT c.table_name, c.column_name
             FROM information_schema.columns c
             WHERE c.table_schema = 'public' AND c.is_identity = 'YES'
    LOOP
        EXECUTE format('SELECT setval(pg_get_serial_sequence(%L, %L), COALESCE((SELECT max(%I) FROM %I), 0) + 1, false)',
                       r.table_name, r.column_name, r.column_name, r.table_name);
    END LOOP;
END $$;

-- 2) P-DB1: pasa el histórico antiguo (fechahora + fechahora_has_sensor) a la tabla lectura.
--    Solo filas con el formato corregido el 2026-11-03 (B1: año completo, mes 1-12, día del mes); las anteriores
--    tenían año-1900 y día de la semana y no se pueden fechar. Valores no numéricos se descartan.
INSERT INTO lectura (sensor_id, maceta_id, magnitud, valor, ts)
SELECT fs.sensor_idsensor,
       mhs.maceta_idmaceta,
       CASE upper(s.magnitudamedir) WHEN 'LUMINOSIDAD' THEN 'LUZ' ELSE left(upper(coalesce(s.magnitudamedir, 'DESCONOCIDA')), 20) END,
       replace(fs.cantidadregistrada, ',', '.')::real,
       make_timestamp(f.anyio::int, f.mes::int, f.dia::int, f.hora::int, f.minuto::int, 0)
FROM fechahora_has_sensor fs
JOIN fechahora f ON f.idfecha = fs.fechahora_idfecha
JOIN sensor s ON s.idsensor = fs.sensor_idsensor
LEFT JOIN maceta_has_sensor mhs ON mhs.sensor_idsensor = fs.sensor_idsensor
WHERE f.anyio ~ '^\d{4}$' AND f.anyio::int >= 2000
  AND f.mes ~ '^\d{1,2}$' AND f.mes::int BETWEEN 1 AND 12
  AND f.dia ~ '^\d{1,2}$' AND f.hora ~ '^\d{1,2}$' AND f.minuto ~ '^\d{1,2}$'
  AND f.dia::int BETWEEN 1 AND 31 AND f.hora::int < 24 AND f.minuto::int < 60
  AND fs.cantidadregistrada ~ '^\s*-?\d+([.,]\d+)?\s*$'
  -- día 31 de un mes de 30, etc.: make_timestamp fallaría
  AND f.dia::int <= extract(day from (make_date(f.anyio::int, f.mes::int, 1) + interval '1 month - 1 day'))
  AND NOT EXISTS (SELECT 1 FROM lectura l WHERE l.sensor_id = fs.sensor_idsensor
                    AND l.ts = make_timestamp(f.anyio::int, f.mes::int, f.dia::int, f.hora::int, f.minuto::int, 0));

ANALYZE lectura; ANALYZE riego; ANALYZE planta;
SELECT 'lecturas' AS tabla, count(*) FROM lectura
UNION ALL SELECT 'personas', count(*) FROM persona
UNION ALL SELECT 'macetas', count(*) FROM maceta;
