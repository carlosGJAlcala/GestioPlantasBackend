-- A2/A3 (entrega 2026-11-13).
-- A3: el usuario elige si quiere las alertas de sus macetas por correo (por defecto sí, como hasta ahora).
ALTER TABLE persona ADD COLUMN avisos_correo BOOLEAN NOT NULL DEFAULT TRUE;
-- A3: «Entendido» en el panel. Una alerta silenciada sigue abierta (y visible) pero ya no se avisa por correo.
-- Se rearma sola: cuando el problema se resuelve la alerta se cierra, y si vuelve es una alerta nueva.
ALTER TABLE alerta ADD COLUMN silenciada TIMESTAMP;
-- A2: el nivel del depósito sale de la tabla lectura (sensor de ultrasonidos, maceta_id NULL); el índice
-- (sensor_id, ts) de V3 ya cubre esa consulta, así que no hace falta nada más.
