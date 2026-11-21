-- W12b (entrega 2026-11-20): quién dio de alta cada sensor. Hasta ahora un sensor recién creado y sin asociar no
-- era de nadie: no salía en «Mis sensores» (que solo miraba macetas) y cualquier usuario podía usarlo o borrarlo.
-- Con esta columna, un sensor libre es de quien lo creó. Las filas antiguas se quedan a NULL (libres de nadie: las
-- puede usar cualquiera, como hasta hoy) y al borrar la persona el sensor se conserva sin dueño.
ALTER TABLE sensor ADD COLUMN creado_por INTEGER REFERENCES persona (id) ON DELETE SET NULL;
CREATE INDEX idx_sensor_creado_por ON sensor (creado_por);
